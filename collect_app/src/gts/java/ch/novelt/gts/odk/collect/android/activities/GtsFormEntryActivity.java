package ch.novelt.gts.odk.collect.android.activities;

import android.content.Intent;
import android.os.Bundle;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

public class GtsFormEntryActivity extends FormEntryActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntent().setAction(Intent.ACTION_EDIT);

        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_SAVE_MID, false);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_ACCESS_SETTINGS, false);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_MARK_AS_FINALIZED, false);
        GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_BASEMAP_SOURCE, GeneralKeys.BASEMAP_SOURCE_OSM);
    }

    @Override
    protected void createQuitDialog() {
        exitWithoutSaving();
    }
}


