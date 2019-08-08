package ch.novelt.gts.odk.collect.android.regression;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import ch.novelt.gts.odk.collect.android.activities.MainMenuActivity;

public class BaseRegressionTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> main = new ActivityTestRule<>(MainMenuActivity.class);
}