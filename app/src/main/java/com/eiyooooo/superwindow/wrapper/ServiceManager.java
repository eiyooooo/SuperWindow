package com.eiyooooo.superwindow.wrapper;

import android.app.IActivityTaskManager;
import android.hardware.input.IInputManager;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.Method;

import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class ServiceManager {

    private static IInputManager inputManager;

    public static IInputManager getInputManager() {
        return inputManager;
    }

    private static IActivityTaskManager activityTaskManager;

    public static IActivityTaskManager getActivityTaskManager() {
        return activityTaskManager;
    }

    public static void setupManagers() throws Exception {
        IPackageManager.init(getService("package", "android.content.pm.IPackageManager"));
        DisplayManagerWrapper.init(getService("display", "android.hardware.display.IDisplayManager"));
        activityTaskManager = IActivityTaskManager.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("activity_task")));
        inputManager = IInputManager.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("input")));
    }

    public static void destroy() {
        IPackageManager.destroy();
        DisplayManagerWrapper.destroy();
        activityTaskManager = null;
        inputManager = null;
    }

    private static IInterface getService(String service, String type) throws Exception {
        IBinder binder = new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(service));
        Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
        return (IInterface) asInterfaceMethod.invoke(null, binder);
    }
}
