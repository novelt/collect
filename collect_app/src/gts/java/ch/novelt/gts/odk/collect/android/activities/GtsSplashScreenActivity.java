package ch.novelt.gts.odk.collect.android.activities;

import android.os.Bundle;
import android.view.View;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

public class GtsSplashScreenActivity extends SplashScreenActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void startSplashScreen(String path) {
        super.startSplashScreen(path);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onBackPressed();
               finish();
           }
        });

        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_SAVE_MID, false);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_ACCESS_SETTINGS, false);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_MARK_AS_FINALIZED, false);
        GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_BASEMAP_SOURCE, GeneralKeys.BASEMAP_SOURCE_OSM);
    }

    @Override
    protected void endSplashScreen() {

    }
}

