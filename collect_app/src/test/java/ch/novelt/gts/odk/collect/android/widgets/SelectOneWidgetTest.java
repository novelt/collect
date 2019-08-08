package ch.novelt.gts.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import ch.novelt.gts.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;

/**
 * @author James Knight
 */

public class SelectOneWidgetTest extends GeneralSelectOneWidgetTest<AbstractSelectOneWidget> {

    @NonNull
    @Override
    public SelectOneWidget createWidget() {
        return new SelectOneWidget(activity, formEntryPrompt, false);
    }
}
