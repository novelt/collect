package ch.novelt.gts.odk.collect.android.activities;

import android.os.Bundle;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;

public class GtsSplashScreenActivity extends SplashScreenActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void startSplashScreen(String path) {
        super.startSplashScreen(path);

        findViewById(R.id.back).setOnClickListener(ev -> finish());

        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_SAVE_MID, false);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_ACCESS_SETTINGS, false);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_MARK_AS_FINALIZED, false);
    }

    @Override
    protected void endSplashScreen() {

    }
}

