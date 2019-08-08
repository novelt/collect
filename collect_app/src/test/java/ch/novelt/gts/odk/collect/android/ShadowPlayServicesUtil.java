package ch.novelt.gts.odk.collect.android;

import android.content.Context;

import ch.novelt.gts.odk.collect.android.utilities.PlayServicesUtil;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(PlayServicesUtil.class)
public abstract class ShadowPlayServicesUtil {

    @Implementation
    public static boolean isGooglePlayServicesAvailable(Context context) {
        return true;
    }
}

