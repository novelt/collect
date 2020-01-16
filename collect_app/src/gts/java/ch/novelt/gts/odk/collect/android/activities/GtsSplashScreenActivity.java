package ch.novelt.gts.odk.collect.android.activities;

import android.os.Bundle;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.application.Collect;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class GtsSplashScreenActivity extends SplashScreenActivity {

    private static final String FILE_PERMS_OK = Collect.ODK_ROOT + "/perms.gts";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void endSplashScreen() {
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PERMS_OK));
            writer.write("OK");
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        findViewById(R.id.back).setOnClickListener(ev -> {
            finish();
        });
    }
}

