package ch.novelt.gts.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import ch.novelt.gts.odk.collect.android.widgets.base.GeneralSelectOneWidgetTest;

/**
 * @author James Knight
 */
public class SelectOneSearchWidgetTest extends GeneralSelectOneWidgetTest<SelectOneSearchWidget> {

    @NonNull
    @Override
    public SelectOneSearchWidget createWidget() {
        return new SelectOneSearchWidget(activity, formEntryPrompt, false);
    }
}
