package ch.novelt.gts.odk.collect.android.externalintents;

import androidx.test.filters.Suppress;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import ch.novelt.gts.odk.collect.android.activities.SplashScreenActivity;

import java.io.IOException;

import static ch.novelt.gts.odk.collect.android.externalintents.ExportedActivitiesUtils.testDirectories;

@Suppress
// Frequent failures: https://github.com/opendatakit/collect/issues/796
public class SplashScreenActivityTest {

    @Rule
    public ActivityTestRule<SplashScreenActivity> splashScreenActivityRule =
            new ExportedActivityTestRule<>(SplashScreenActivity.class);

    @Test
    public void splashScreenActivityMakesDirsTest() throws IOException {
        testDirectories();
    }

}
