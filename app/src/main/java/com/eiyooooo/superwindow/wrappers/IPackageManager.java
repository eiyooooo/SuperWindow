package com.eiyooooo.superwindow.wrappers;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.IInterface;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class IPackageManager {

    private static IInterface manager;
    private static Class<?> CLASS;
    private static Method getPackageInfoMethod = null;
    private static Method getQueryIntentActivitiesMethod = null;
    private static Method getInstalledPackagesMethod = null;

    public static void init(IInterface m) {
        manager = m;
        if (manager == null) {
            Timber.e("Error in IPackageManager.init: manager is null");
            return;
        }
        CLASS = manager.getClass();
    }

    public static void destroy() {
        manager = null;
        CLASS = null;
        getPackageInfoMethod = null;
        getQueryIntentActivitiesMethod = null;
        getInstalledPackagesMethod = null;
        Timber.d("IPackageManager destroyed");
    }

    private static Method getGetPackageInfoMethod() throws ReflectiveOperationException {
        if (getPackageInfoMethod == null) {
            if (CLASS == null) {
                Timber.e("Error in getGetPackageInfoMethod: CLASS is null");
                return null;
            }
            getPackageInfoMethod = CLASS.getDeclaredMethod("getPackageInfo", String.class, int.class);
            getPackageInfoMethod.setAccessible(true);
        }
        return getPackageInfoMethod;
    }

    private static Method getQueryIntentActivitiesMethod() throws ReflectiveOperationException {
        if (getQueryIntentActivitiesMethod == null) {
            if (CLASS == null) {
                Timber.e("Error in getGetPackageInfoMethod: CLASS is null");
                return null;
            }
            try {
                getQueryIntentActivitiesMethod = CLASS.getMethod("queryIntentActivities", Intent.class, String.class, long.class, int.class);
            } catch (Exception ignored) {
                getQueryIntentActivitiesMethod = CLASS.getMethod("queryIntentActivities", Intent.class, String.class, int.class, int.class);
            }
        }
        return getQueryIntentActivitiesMethod;
    }

    private static Method getGetInstalledPackagesMethod() throws ReflectiveOperationException {
        if (getInstalledPackagesMethod == null) {
            if (CLASS == null) {
                Timber.e("Error in getGetPackageInfoMethod: CLASS is null");
                return null;
            }
            getInstalledPackagesMethod = CLASS.getMethod("getAllPackages");
        }
        return getInstalledPackagesMethod;
    }

    public static PackageInfo getPackageInfo(String packageName, int flag) {
        try {
            return (PackageInfo) Objects.requireNonNull(getGetPackageInfoMethod()).invoke(manager, new Object[]{packageName, flag});
        } catch (Exception e) {
            Timber.e(e, "Error in getPackageInfo");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        try {
            Object parceledListSlice = Objects.requireNonNull(getQueryIntentActivitiesMethod()).invoke(manager, intent, resolvedType, flags, userId);
            return (List<ResolveInfo>) Objects.requireNonNull(parceledListSlice).getClass().getMethod("getList").invoke(parceledListSlice);
        } catch (Exception e) {
            Timber.e(e, "Error in queryIntentActivities");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getInstalledPackages(int flag) {
        try {
            return (List<String>) Objects.requireNonNull(getGetInstalledPackagesMethod()).invoke(manager, new Object[]{flag});
        } catch (Exception e) {
            Timber.e(e, "Error in getInstalledPackages");
        }
        return null;
    }
}