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
    private static Method queryIntentActivitiesMethod = null;

    public static void init(IInterface m) {
        manager = m;
        if (manager == null) {
            Timber.e("Error in IPackageManager.init: manager is null");
            return;
        }
        CLASS = manager.getClass();
        Timber.d("IPackageManager initialized");
    }

    public static void destroy() {
        manager = null;
        CLASS = null;
        getPackageInfoMethod = null;
        queryIntentActivitiesMethod = null;
        Timber.d("IPackageManager destroyed");
    }

    private static Method getGetPackageInfoMethod() throws ReflectiveOperationException {
        if (getPackageInfoMethod == null) {
            if (CLASS == null) {
                Timber.e("Error in getGetPackageInfoMethod: CLASS is null");
                return null;
            }
            try {
                getPackageInfoMethod = CLASS.getDeclaredMethod("getPackageInfo", String.class, long.class, int.class);
            } catch (Throwable ignored) {
                getPackageInfoMethod = CLASS.getDeclaredMethod("getPackageInfo", String.class, int.class, int.class);
            }
        }
        return getPackageInfoMethod;
    }

    private static Method getQueryIntentActivitiesMethod() throws ReflectiveOperationException {
        if (queryIntentActivitiesMethod == null) {
            if (CLASS == null) {
                Timber.e("Error in getQueryIntentActivitiesMethod: CLASS is null");
                return null;
            }
            try {
                queryIntentActivitiesMethod = CLASS.getMethod("queryIntentActivities", Intent.class, String.class, long.class, int.class);
            } catch (Throwable ignored) {
                queryIntentActivitiesMethod = CLASS.getMethod("queryIntentActivities", Intent.class, String.class, int.class, int.class);
            }
        }
        return queryIntentActivitiesMethod;
    }

    public static PackageInfo getPackageInfo(String packageName, int flag, int userId) {
        try {
            return (PackageInfo) Objects.requireNonNull(getGetPackageInfoMethod()).invoke(manager, packageName, flag, userId);
        } catch (Throwable t) {
            Timber.e(t, "Error in getPackageInfo");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        try {
            Object object = Objects.requireNonNull(getQueryIntentActivitiesMethod()).invoke(manager, intent, resolvedType, flags, userId);
            try {
                return (List<ResolveInfo>) object;
            } catch (ClassCastException ignored) {
                return (List<ResolveInfo>) object.getClass().getMethod("getList").invoke(object);
            }
        } catch (Throwable t) {
            Timber.e(t, "Error in queryIntentActivities");
        }
        return null;
    }
}