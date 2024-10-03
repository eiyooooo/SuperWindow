package com.eiyooooo.superwindow.wrappers;

import static com.eiyooooo.superwindow.MyApplicationKt.getApplication;

import android.annotation.TargetApi;
import android.content.AttributionSource;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.os.Build;
import android.os.Process;

public final class FakeContext extends MutableContextWrapper {

    public static final String PACKAGE_NAME = "com.android.shell";
    public static final int ROOT_UID = 0; // Like android.os.Process.ROOT_UID, but before API 29

    private static FakeContext INSTANCE = null;

    public static FakeContext get() {
        if (INSTANCE == null) {
            INSTANCE = new FakeContext(getApplication());
        }
        return INSTANCE;
    }

    private FakeContext(Context context) {
        super(context);
    }

    @Override
    public String getPackageName() {
        return PACKAGE_NAME;
    }

    @Override
    public String getOpPackageName() {
        return PACKAGE_NAME;
    }

    @TargetApi(Build.VERSION_CODES.S)
    @Override
    public AttributionSource getAttributionSource() {
        AttributionSource.Builder builder = new AttributionSource.Builder(Process.SHELL_UID);
        builder.setPackageName(PACKAGE_NAME);
        return builder.build();
    }

    // @Override to be added on SDK upgrade for Android 14
    @SuppressWarnings("unused")
    public int getDeviceId() {
        return 0;
    }

    @Override
    public Context getApplicationContext() {
        return this;
    }
}
