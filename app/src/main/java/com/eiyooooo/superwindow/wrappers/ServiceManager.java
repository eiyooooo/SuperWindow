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
    public static Boolean setupManagers() {
        try {
            InputManagerWrapper.init(getService("input", "android.hardware.input.IInputManager"));
            IPackageManager.init(getService("package", "android.content.pm.IPackageManager"));
            DisplayManagerWrapper.init(getService("display", "android.hardware.display.IDisplayManager"));
            return true;
        } catch (Exception e) {
            Timber.e(e, "Error in ServiceManager.setupManagers");
            destroy();
            return false;
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
