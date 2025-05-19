package ch.novelt.gts.odk.collect.android.external;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import org.odk.collect.android.external.FormUriActivity;
import org.odk.collect.settings.keys.ProjectKeys;
import org.odk.collect.settings.keys.ProtectedProjectKeys;

public class GtsFormUriActivity extends FormUriActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntent().setAction(Intent.ACTION_EDIT);

        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_MID, false);
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, false);
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_ACCESS_SETTINGS, false);
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SEND_FINALIZED, true);
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY, true);
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_OSM);
    }
}
