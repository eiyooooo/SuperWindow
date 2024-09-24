package com.eiyooooo.superwindow.wrappers;

import android.annotation.SuppressLint;
import android.view.InputEvent;

import java.lang.reflect.Method;
import java.util.Objects;

import timber.log.Timber;

public final class InputManagerWrapper {

    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;

    private static Object manager;
    private static Class<?> CLASS;
    private static Method injectInputEventMethod = null;
    private static Method setDisplayIdMethod = null;

    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    public static void init(Object iInputManager) throws Exception {
        manager = Class.forName("android.hardware.input.InputManagerGlobal")
                .getDeclaredConstructor(Class.forName("android.hardware.input.IInputManager"))
                .newInstance(iInputManager);
        CLASS = manager.getClass();
    }

    private static Method getInjectInputEventMethod() throws ReflectiveOperationException {
        if (injectInputEventMethod == null) {
            if (CLASS == null) {
                Timber.e("Error in getInjectInputEventMethod: CLASS is null");
                return null;
            }
            injectInputEventMethod = CLASS.getMethod("injectInputEvent", InputEvent.class, int.class);
        }
        return injectInputEventMethod;
    }

    /** @noinspection JavaReflectionMemberAccess*/
    private static Method getSetDisplayIdMethod() throws ReflectiveOperationException {
        if (setDisplayIdMethod == null) {
            if (CLASS == null) {
                Timber.e("Error in getSetDisplayIdMethod: CLASS is null");
                return null;
            }
            setDisplayIdMethod = InputEvent.class.getMethod("setDisplayId", int.class);
        }
        return setDisplayIdMethod;
    }

    public static void setDisplayId(InputEvent inputEvent, int displayId) throws Exception {
        Objects.requireNonNull(getSetDisplayIdMethod()).invoke(inputEvent, displayId);
    }

    public static void injectInputEvent(InputEvent inputEvent, int mode) throws Exception {
        Objects.requireNonNull(getInjectInputEventMethod()).invoke(manager, inputEvent, mode);
    }
}
