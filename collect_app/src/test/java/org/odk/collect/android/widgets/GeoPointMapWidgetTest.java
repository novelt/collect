package org.odk.collect.android.widgets;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.MAPS;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.PLACEMENT_MAP;
import static org.odk.collect.android.widgets.GeoPointMapWidget.DRAGGABLE_ONLY;
import static org.odk.collect.android.widgets.GeoPointWidget.ACCURACY_THRESHOLD;
import static org.odk.collect.android.widgets.GeoPointWidget.DEFAULT_LOCATION_ACCURACY;
import static org.odk.collect.android.widgets.GeoPointWidget.LOCATION;
import static org.odk.collect.android.widgets.GeoPointWidget.READ_ONLY;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearance;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */

@RunWith(RobolectricTestRunner.class)
public class GeoPointMapWidgetTest {

    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final List<double[]> answerDoubles = getRandomDoubleArrayList();
    private final String answer = stringFromDoubleList(answerDoubles);

    private QuestionDef questionDef;
    private WaitingForDataRegistry waitingForDataRegistry;

    @Before
    public void setup() {
        questionDef = mock(QuestionDef.class);
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        when(questionDef.getAdditionalAttribute(anyString(), anyString())).thenReturn(null);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowTheGeoButton() {
        GeoPointMapWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.binding.simpleButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsNull() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), equalTo(null));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveConvertibleString_returnsNull() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.getAnswer(), equalTo(null));
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertThat(widget.getAnswer().getDisplayText(), equalTo(answer));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_textViewDisplaysEmptyString() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.binding.geoAnswerText.getText().toString(), equalTo(""));
    }

    @Test
    public void whenPromptAnswerDoesNotHaveConvertibleString_textViewDisplaysEmptyString() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.binding.geoAnswerText.getText().toString(), equalTo(""));
    }

    @Test
    public void whenPromptHasAnswer_textViewDisplaysAnswer() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        String[] parts = answer.split(" ");
        assertThat(widget.binding.geoAnswerText.getText().toString(), equalTo(widget.getContext().getString(
                R.string.gps_result,
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(widget.getContext(), Double.parseDouble(parts[0]), "lat"),
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(widget.getContext(), Double.parseDouble(parts[1]), "lon"),
                GeoWidgetUtils.truncateDouble(parts[2]),
                GeoWidgetUtils.truncateDouble(parts[3])
        )));
    }

    @Test
    public void whenPromptIsReadOnly_buttonShowsCorrectText() {
        GeoPointMapWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.binding.simpleButton.getText().toString(), equalTo(widget.getContext().getString(R.string.geopoint_view_read_only)));
    }

    @Test
    public void whenPromptDoesNotHaveHasAnswer_buttonShowsCorrectText() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.binding.simpleButton.getText(), equalTo(widget.getContext().getString(R.string.get_point)));
    }

    @Test
    public void whenPromptHasAnswer_buttonShowsCorrectText() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertThat(widget.binding.simpleButton.getText(), equalTo(widget.getContext().getString(R.string.view_change_location)));
    }

    @Test
    public void whenPermissionIsNotGranted_buttonClickShouldNotLaunchAnyIntent() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        stubLocationPermissions(widget, false);
        widget.binding.simpleButton.performClick();
        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();

        assertNull(startedIntent);
    }

    @Test
    public void whenPermissionIsGranted_buttonClickLaunchesIntentAndWaitsForLocationData() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        GeoPointMapWidget widget = createWidget(prompt);
        stubLocationPermissions(widget, true);
        widget.binding.simpleButton.performClick();

        verify(waitingForDataRegistry).waitForData(prompt.getIndex());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_buttonShouldLaunchCorrectIntent() {
        GeoPointMapWidget widget = createWidget(promptWithAnswer(null));
        stubLocationPermissions(widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPointMapActivity.class)));
        assertBundleArgumentEquals(bundle, null, DEFAULT_LOCATION_ACCURACY, false, true);
    }

    @Test
    public void whenPromptHasAnswerAndAccuracyThresholdValue_buttonShouldLaunchCorrectIntent() {
        when(questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD)).thenReturn("2.0");

        GeoPointMapWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        stubLocationPermissions(widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPointMapActivity.class)));
        assertBundleArgumentEquals(bundle, GeoWidgetUtils.getLocationParamsFromStringAnswer(answer), 2.0, false, true);
    }

    @Test
    public void ifWidgetHasPlacementMapsAppearance_buttonShouldLaunchCorrectIntent() {
        GeoPointMapWidget widget = createWidget(promptWithAppearance(PLACEMENT_MAP));
        stubLocationPermissions(widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPointMapActivity.class)));
        assertBundleArgumentEquals(bundle, null, DEFAULT_LOCATION_ACCURACY, false, true);
    }

    @Test
    public void ifWidgetHasMapsAppearance_buttonShouldLaunchCorrectIntent() {
        GeoPointMapWidget widget = createWidget(promptWithAppearance(MAPS));
        stubLocationPermissions(widget, true);
        widget.binding.simpleButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPointMapActivity.class)));
        assertBundleArgumentEquals(bundle, null, DEFAULT_LOCATION_ACCURACY, false, false);
    }

    private GeoPointMapWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointMapWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"), questionDef, waitingForDataRegistry);
    }

    private void assertBundleArgumentEquals(Bundle bundle, double[] location, double accuracyThreshold, boolean readOnly, boolean draggableOnly) {
        assertThat(bundle.getDoubleArray(LOCATION), equalTo(location));
        assertThat(bundle.getBoolean(READ_ONLY), equalTo(readOnly));
        assertThat(bundle.getBoolean(DRAGGABLE_ONLY), equalTo(draggableOnly));
        assertThat(bundle.getDouble(ACCURACY_THRESHOLD), equalTo(accuracyThreshold));
    }

    protected void stubLocationPermissions(GeoPointMapWidget widget, boolean isGranted) {
        permissionUtils.setPermissionGranted(isGranted);
        widget.setPermissionUtils(permissionUtils);
    }

    private String stringFromDoubleList(List<double[]> doubleList) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (double[] doubles : doubleList) {
            if (!first) {
                b.append("; ");
            }
            first = false;
            b.append(stringFromDoubles(doubles));
        }
        return b.toString();
    }

    private String stringFromDoubles(double[] doubles) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < doubles.length; i++) {
            b.append(doubles[i]);
            if (i != doubles.length - 1) {
                b.append(' ');
            }
        }

        return b.toString();
    }

    private ArrayList<double[]> getRandomDoubleArrayList() {
        Random random = new Random();
        ArrayList<double[]> doubleList = new ArrayList<>();

        int pointCount = Math.max(1, random.nextInt() % 5);
        for (int i = 0; i < pointCount; ++i) {
            doubleList.add(getRandomDoubleArray());
        }

        return doubleList;
    }

    private double[] getRandomDoubleArray() {
        Random random = new Random();
        return new double[]{
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble()
        };
    }
}