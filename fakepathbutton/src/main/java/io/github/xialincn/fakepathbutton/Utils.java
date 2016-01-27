package io.github.xialincn.fakepathbutton;

/**
 * Created by lin on 2016/1/25.
 */

import android.os.Build;
import android.view.View;

import java.lang.reflect.Field;

final class Utils {

    private Utils() {
    }

    static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static View.OnClickListener storeParentLayoutOnClickListener(View view) {
        Field listenerInfoField = null;
        try {
            listenerInfoField = Class.forName("android.view.View").getDeclaredField("mListenerInfo");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        if (listenerInfoField != null) {
            listenerInfoField.setAccessible(true);
        }
        Object listenerObj = null;
        try {
            listenerObj = listenerInfoField.get(view);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Field listenerField = null;
        try {
            listenerField = Class.forName("android.view.View$ListenerInfo").getDeclaredField("mOnClickListener");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        View.OnClickListener listener = null;
        if (listenerField != null && listenerObj != null) {
            try {
                listener = (View.OnClickListener) listenerField.get(listenerObj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return listener;
    }
}
