package ch.novelt.gts.odk.collect.android.injection.config;

import android.app.Application;
import android.content.Context;
import android.telephony.SmsManager;
import android.webkit.MimeTypeMap;

import com.google.android.gms.analytics.Tracker;

import ch.novelt.gts.odk.collect.android.application.Collect;
import ch.novelt.gts.odk.collect.android.dao.FormsDao;
import ch.novelt.gts.odk.collect.android.dao.InstancesDao;
import ch.novelt.gts.odk.collect.android.events.RxEventBus;
import ch.novelt.gts.odk.collect.android.http.CollectServerClient;
import ch.novelt.gts.odk.collect.android.http.CollectThenSystemContentTypeMapper;
import ch.novelt.gts.odk.collect.android.http.OkHttpConnection;
import ch.novelt.gts.odk.collect.android.http.OpenRosaHttpInterface;
import ch.novelt.gts.odk.collect.android.tasks.sms.SmsSubmissionManager;
import ch.novelt.gts.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import ch.novelt.gts.odk.collect.android.utilities.DownloadFormListUtils;
import ch.novelt.gts.odk.collect.android.utilities.PermissionUtils;
import ch.novelt.gts.odk.collect.android.utilities.WebCredentialsUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Add dependency providers here (annotated with @Provides)
 * for objects you need to inject
 */
@Module
public class AppDependencyModule {

    @Provides
    public SmsManager provideSmsManager() {
        return SmsManager.getDefault();
    }

    @Provides
    SmsSubmissionManagerContract provideSmsSubmissionManager(Application application) {
        return new SmsSubmissionManager(application);
    }

    @Provides
    Context context(Application application) {
        return application;
    }

    @Provides
    public InstancesDao provideInstancesDao() {
        return new InstancesDao();
    }

    @Provides
    public FormsDao provideFormsDao() {
        return new FormsDao();
    }

    @Provides
    @Singleton
    RxEventBus provideRxEventBus() {
        return new RxEventBus();
    }

    @Provides
    MimeTypeMap provideMimeTypeMap() {
        return MimeTypeMap.getSingleton();
    }

    @Provides
    OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap) {
        return new OkHttpConnection(null, new CollectThenSystemContentTypeMapper(mimeTypeMap));
    }

    @Provides
    public CollectServerClient provideCollectServerClient(OpenRosaHttpInterface httpInterface, WebCredentialsUtils webCredentialsUtils) {
        return new CollectServerClient(httpInterface, webCredentialsUtils);
    }

    @Provides
    WebCredentialsUtils provideWebCredentials() {
        return new WebCredentialsUtils();
    }

    @Provides
    DownloadFormListUtils provideDownloadFormListUtils(
            Application application,
            CollectServerClient collectServerClient,
            WebCredentialsUtils webCredentialsUtils,
            FormsDao formsDao) {
        return new DownloadFormListUtils(
                application,
                collectServerClient,
                webCredentialsUtils,
                formsDao
        );
    }

    @Provides
    @Singleton
    public Tracker providesTracker(Application application) {
        return ((Collect) application).getDefaultTracker();
    }

    @Provides
    public PermissionUtils providesPermissionUtils() {
        return new PermissionUtils();
    }
}
