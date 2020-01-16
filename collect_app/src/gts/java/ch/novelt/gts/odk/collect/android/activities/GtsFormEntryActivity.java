package ch.novelt.gts.odk.collect.android.activities;

import org.odk.collect.android.activities.FormEntryActivity;

public class GtsFormEntryActivity extends FormEntryActivity {

    @Override
    protected void createQuitDialog() {
        exitWithoutSaving();
    }
}


