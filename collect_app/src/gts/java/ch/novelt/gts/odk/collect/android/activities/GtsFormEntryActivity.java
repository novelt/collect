package ch.novelt.gts.odk.collect.android.activities;

import android.content.Intent;
import android.os.Bundle;

import org.odk.collect.android.activities.FormEntryActivity;

public class GtsFormEntryActivity extends FormEntryActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntent().setAction(Intent.ACTION_EDIT);
    }

    @Override
    protected void createQuitDialog() {
        exitWithoutSaving();
    }
}


