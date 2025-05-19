/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.external;

import static org.odk.collect.android.database.DatabaseObjectMapper.getInstanceFromValues;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns._ID;
import static org.odk.collect.android.external.FormsProvider.deferDaggerInit;
import static org.odk.collect.android.external.InstancesContract.CONTENT_ITEM_TYPE;
import static org.odk.collect.android.external.InstancesContract.CONTENT_TYPE;
import static org.odk.collect.android.external.InstancesContract.getUri;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.analytics.AnalyticsUtils;
import org.odk.collect.android.dao.CursorLoaderFactory;
import org.odk.collect.android.database.instances.DatabaseInstanceColumns;
import org.odk.collect.android.database.instances.DatabaseInstancesRepository;
import org.odk.collect.android.instancemanagement.InstanceDeleter;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.ContentUriHelper;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.ZipUtils;
import org.odk.collect.androidshared.utils.PathUtils;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.projects.Project;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.settings.SettingsProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class InstanceProvider extends ContentProvider {

    private static final int INSTANCES = 1;
    private static final int INSTANCE_ID = 2;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    @Inject
    InstancesRepositoryProvider instancesRepositoryProvider;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    StoragePathProvider storagePathProvider;

    @Inject
    ProjectsRepository projectsRepository;

    @Inject
    SettingsProvider settingsProvider;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        deferDaggerInit(this);

        String projectId = getProjectId(uri);

        // We only want to log external calls to the content provider
        if (uri.getQueryParameter(CursorLoaderFactory.INTERNAL_QUERY_PARAM) == null) {
            logServerEvent(projectId, AnalyticsEvents.INSTANCE_PROVIDER_QUERY);
        }

        Cursor c;
        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                c = dbQuery(projectId, projection, selection, selectionArgs, sortOrder);
                break;

            case INSTANCE_ID:
                String id = String.valueOf(ContentUriHelper.getIdFromUri(uri));
                c = dbQuery(projectId, projection, _ID + "=?", new String[]{id}, null);
                final var gtsStep = uri.getQueryParameter("gtsStep");
                if ("ASK_GRANT".equals(gtsStep)) {
                    if (c.moveToNext()) {
                        c = queryGtsHookGrantPermissions(c, projectId);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    private Cursor dbQuery(String projectId, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return ((DatabaseInstancesRepository) instancesRepositoryProvider.create(projectId)).rawQuery(projection, selection, selectionArgs, sortOrder, null);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                return CONTENT_TYPE;

            case INSTANCE_ID:
                return CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        Log.i("ODK_MIGRATION", "> InstanceProvider > insert... (" + uri + ")");
        deferDaggerInit(this);

        String projectId = getProjectId(uri);
        logServerEvent(projectId, AnalyticsEvents.INSTANCE_PROVIDER_INSERT);

        // Validate the requested uri
        if (URI_MATCHER.match(uri) != INSTANCES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (initialValues.containsKey(DatabaseInstanceColumns.SUBMISSION_URI)) {
            throw new SecurityException();
        }

        Instance instance = getInstanceFromValues(initialValues);
        final String instanceFolderNameZip = Uri.parse(instance.getInstanceFilePath()).getLastPathSegment();
        final var gtsStep = uri.getQueryParameter("gtsStep");
        if ("MIGRATION_ASK_GRANT".equals(gtsStep)) {
            return insertGtsHookStep1(projectId, instanceFolderNameZip);
        } else if ("MIGRATION_NOTIFY_ZIP_COPIED".equals(gtsStep)) {
            instance = insertGtsHookStep2(projectId, instance);
        }
        Log.i("ODK_MIGRATION", "Inserting... : " + instance.getDisplayName() + " | " + instance.getInstanceFilePath());
        Instance newInstance = instancesRepositoryProvider.create(projectId).save(instance);
        Log.i("ODK_MIGRATION", "Inserted : " + instance.getDisplayName() + "(" + instance.getDbId() + ")");
        return getUri(projectId, newInstance.getDbId());
    }

    /**
     * This method removes the entry from the content provider, and also removes any associated
     * files.
     * files:  form.xml, [formmd5].formdef, formname-media {directory}
     */
    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        deferDaggerInit(this);

        String projectId = getProjectId(uri);
        logServerEvent(projectId, AnalyticsEvents.INSTANCE_PROVIDER_DELETE);

        int count;

        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                try (Cursor cursor = dbQuery(projectId, new String[]{_ID}, where, whereArgs, null)) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndex(_ID));
                        new InstanceDeleter(instancesRepositoryProvider.create(projectId), formsRepositoryProvider.create(projectId)).delete(id);
                    }

                    count = cursor.getCount();
                }

                break;

            case INSTANCE_ID:
                long id = ContentUriHelper.getIdFromUri(uri);

                if (where == null) {
                    new InstanceDeleter(instancesRepositoryProvider.create(projectId), formsRepositoryProvider.create(projectId)).delete(id);
                } else {
                    try (Cursor cursor = dbQuery(projectId, new String[]{_ID}, where, whereArgs, null)) {
                        while (cursor.moveToNext()) {
                            if (cursor.getLong(cursor.getColumnIndex(_ID)) == id) {
                                new InstanceDeleter(instancesRepositoryProvider.create(), formsRepositoryProvider.create()).delete(id);
                                break;
                            }
                        }
                    }
                }

                count = 1;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        return 0;
    }

    private String getProjectId(@NonNull Uri uri) {
        String queryParam = uri.getQueryParameter("projectId");

        if (queryParam != null) {
            return queryParam;
        } else {
            final var projects = projectsRepository.getAll();
            if (projects.isEmpty()) {
                // create "default" project automatically as Collect is not launched before...
                Log.i("GTS", "creating project...");
                final var tmp = projectsRepository.save(new Project.New("GTS", "GTS", "#426fec")).getUuid();
                Log.i("GTS", "created project : " + tmp);
                return tmp;
            } else {
                final var tmp = projectsRepository.getAll().get(0).getUuid();
                Log.i("GTS", "returning project: " + tmp);
                return tmp;
            }
        }
    }

    private void logServerEvent(String projectId, String event) {
        AnalyticsUtils.logServerEvent(event, settingsProvider.getUnprotectedSettings(projectId));
    }

    static {
        URI_MATCHER.addURI(InstancesContract.AUTHORITY, "instances", INSTANCES);
        URI_MATCHER.addURI(InstancesContract.AUTHORITY, "instances/#", INSTANCE_ID);
    }

    //

    public Cursor queryGtsHookGrantPermissions(final Cursor rawCursor, final String projectId) {
        String extGtsTrackerDataJson = null;
        final var instancesDir = storagePathProvider.create(projectId).getInstancesDir();
        final var instanceFilePath = rawCursor.getString(rawCursor.getColumnIndex("instanceFilePath"));
        final var instanceAbsoluteFilePath = PathUtils.getAbsoluteFilePath(instancesDir, instanceFilePath);
        final var instanceFolder = new File(instanceAbsoluteFilePath).getParentFile();

        if (instanceFolder.exists()) {
            final var uris = new JSONArray();

            final var allFiles = listAllFiles(instanceFolder);
            for (final var file : allFiles) {
                final var uri = FileProvider.getUriForFile(
                        getContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                );
                final var callingPackage = getCallingPackage();
                getContext().grantUriPermission(
                        callingPackage,
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                uris.put(uri.toString());
            }

            extGtsTrackerDataJson = uris.toString(); // JSON array string
        }

        final var columnList = new ArrayList<>(Arrays.asList(rawCursor.getColumnNames()));
        columnList.add("ext_gts_tracker_data");

        final var matrixCursor = new MatrixCursor(columnList.toArray(new String[0]));
        rawCursor.moveToPosition(-1);
        while (rawCursor.moveToNext()) {
            final var row = new Object[columnList.size()];
            for (int i = 0; i < rawCursor.getColumnCount(); i++) {
                row[i] = rawCursor.getString(i);
            }
            row[rawCursor.getColumnCount()] = extGtsTrackerDataJson;
            matrixCursor.addRow(row);
        }
        rawCursor.close();
        return matrixCursor;
    }

    private List<File> listAllFiles(File root) {
        final var files = new ArrayList<File>();
        final var listed = root.listFiles();

        if (listed != null) {
            for (final var f : listed) {
                if (f.isDirectory()) {
                    files.addAll(listAllFiles(f));
                } else {
                    files.add(f);
                }
            }
        }

        return files;
    }

    //

    private Uri insertGtsHookStep1(final String projectId, final String instanceFolderNameZip) {
        final var instancesDir = storagePathProvider.create(projectId).getInstancesDir();
        final var instanceAbsoluteFilePath = PathUtils.getAbsoluteFilePath(instancesDir, instanceFolderNameZip);

        final var uri = FileProvider.getUriForFile(
                getContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                new File(instanceAbsoluteFilePath)
        );
        final var callingPackage = getCallingPackage();
        getContext().grantUriPermission(
                callingPackage,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        );
        Log.i("ODK_MIGRATION", "Granted write permission in " + instanceAbsoluteFilePath);
        return uri;
    }

    private Instance insertGtsHookStep2(final String projectId, final Instance instance) {
        final var instancesDir = storagePathProvider.create(projectId).getInstancesDir();
        final var instanceAbsoluteFilePath = PathUtils.getAbsoluteFilePath(instancesDir, Uri.parse(instance.getInstanceFilePath()).getLastPathSegment());
        final var instanceAbsoluteFile = new File(instanceAbsoluteFilePath);
        Log.i("ODK_MIGRATION", "Unzipping... : " + instanceAbsoluteFilePath);
        ZipUtils.unzip(new File[]{instanceAbsoluteFile});
        Log.i("ODK_MIGRATION", "Unzipped : " + instanceAbsoluteFilePath);
        instanceAbsoluteFile.deleteOnExit();

        final var instanceXmlFile = FormsProvider.getOneXmlFileOtherwiseError(new File(instanceAbsoluteFile.getAbsolutePath().replace(".zip", "")));
        Log.i("ODK_MIGRATION", "Found XML file : " + instanceXmlFile.getAbsolutePath());

        return new Instance.Builder(instance).instanceFilePath(instanceXmlFile.getAbsolutePath()).build();
    }
}
