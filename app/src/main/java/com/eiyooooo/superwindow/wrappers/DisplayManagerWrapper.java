package com.eiyooooo.superwindow.wrappers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.view.Surface;

import java.lang.reflect.Field;

import timber.log.Timber;

public final class DisplayManagerWrapper {

    private static DisplayManager displayManager;

    public static DisplayManager getInstance() {
        return displayManager;
    }

    /**
     * @noinspection JavaReflectionMemberAccess
     */
    @SuppressLint({"PrivateApi", "SoonBlockedPrivateApi"})
    public static void init(Object iDisplayManager) throws Exception {
        Object displayManagerGlobal = Class.forName("android.hardware.display.DisplayManagerGlobal")
                .getDeclaredConstructor(Class.forName("android.hardware.display.IDisplayManager"))
                .newInstance(iDisplayManager);
        displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
        Field mGlobalField = DisplayManager.class.getDeclaredField("mGlobal");
        mGlobalField.setAccessible(true);
        mGlobalField.set(displayManager, displayManagerGlobal);
        Timber.d("DisplayManagerWrapper initialized");
    }

    public static void destroy() {
        displayManager = null;
        Timber.d("DisplayManagerWrapper destroyed");
    }

    public static VirtualDisplay createVirtualDisplay(String name, int width, int height, int densityDpi, Surface surface) {
        try {
            return displayManager.createVirtualDisplay(name, width, height, densityDpi, surface, getDisplayFlags());
        } catch (Throwable t) {
            Timber.e(t, "Error in createVirtualDisplay");
            return null;
        }
    }

    private static int getDisplayFlags() {
        int VIRTUAL_DISPLAY_FLAG_PRESENTATION = 1 << 1;
        int VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY = 1 << 3;
        int VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH = 1 << 6;
        int VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL = 1 << 8;
        int VIRTUAL_DISPLAY_FLAG_TRUSTED = 1 << 10;
        int VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP = 1 << 11;
        int VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED = 1 << 12;

        return VIRTUAL_DISPLAY_FLAG_PRESENTATION
                | VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                | VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH
                | VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT_ON_REMOVAL
                | VIRTUAL_DISPLAY_FLAG_TRUSTED
                | VIRTUAL_DISPLAY_FLAG_OWN_DISPLAY_GROUP
                | VIRTUAL_DISPLAY_FLAG_ALWAYS_UNLOCKED;
    }
}
