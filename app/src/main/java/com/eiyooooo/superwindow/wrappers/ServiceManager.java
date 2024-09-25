package com.eiyooooo.superwindow.wrappers;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.Method;

import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;
import timber.log.Timber;

public class ServiceManager {

    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    public static void setupManagers() {
        for (int i = 0; i < 3; i++) {
            try {
                switch (i) {
                    case 0:
                        InputManagerWrapper.init(getService("input", "android.hardware.input.IInputManager"));
                        break;
                    case 1:
                        IPackageManager.init(getService("package", "android.content.pm.IPackageManager"));
                        break;
                    case 2:
                        DisplayManagerWrapper.init(getService("display", "android.hardware.display.IDisplayManager"));
                        break;
                }
            } catch (Exception e) {
                Timber.e(e, "Error in ServiceManager.setupManagers");
            }
        }
    }

    public static void destroy() {
        InputManagerWrapper.destroy();
        IPackageManager.destroy();
        DisplayManagerWrapper.destroy();
    }

    private static IInterface getService(String service, String type) throws Exception {
        IBinder binder = new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(service));
        Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
        return (IInterface) asInterfaceMethod.invoke(null, binder);
    }
}
