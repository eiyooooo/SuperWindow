package com.eiyooooo.superwindow.wrappers;

import android.hardware.input.IInputManager;
import android.os.IBinder;
import android.os.IInterface;
import android.view.MotionEvent;

import java.lang.reflect.Method;

import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;
import timber.log.Timber;

public class ServiceManager {

    private static Method setDisplayIdMethod;

    /** @noinspection JavaReflectionMemberAccess*/
    public static Method getSetDisplayIdMethod() {
        if (setDisplayIdMethod == null) {
            try {
                setDisplayIdMethod = MotionEvent.class.getMethod("setDisplayId", int.class);
            } catch (Exception e) {
                Timber.e(e, "Error in ServiceManager.getSetDisplayIdMethod");
            }
        }
        return setDisplayIdMethod;
    }

    private static IInputManager inputManager;

    public static IInputManager getInputManager() {
        if (inputManager == null) {
            try {
                inputManager = IInputManager.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("input")));
            } catch (Exception e) {
                Timber.e(e, "Error in ServiceManager.getInputManager");
            }
        }
        return inputManager;
    }

    public static Boolean setupManagers() {
        try {
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
        inputManager = null;
        IPackageManager.destroy();
        DisplayManagerWrapper.destroy();
    }

    private static IInterface getService(String service, String type) throws Exception {
        IBinder binder = new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(service));
        Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
        return (IInterface) asInterfaceMethod.invoke(null, binder);
    }
}
