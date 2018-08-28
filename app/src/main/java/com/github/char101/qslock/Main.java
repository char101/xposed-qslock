package com.github.char101.qslock;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import android.util.Log;

public class Main implements IXposedHookLoadPackage {
    private static final String TAG = "qslock";

    private static final int DISABLE_EXPAND = 0x00010000;
    private static final int DISABLE_NOTIFICATION_ICONS = 0x00020000;
    private static final int DISABLE_NOTIFICATION_ALERTS = 0x00040000;
    private static final int DISABLE_NOTIFICATION_TICKER = 0x00080000;
    private static final int DISABLE_SYSTEM_INFO = 0x00100000;
    private static final int DISABLE_HOME = 0x00200000;
    private static final int DISABLE_RECENT = 0x01000000;
    private static final int DISABLE_BACK = 0x00400000;
    private static final int DISABLE_CLOCK = 0x00800000;
    private static final int DISABLE_SEARCH = 0x02000000;
    private static final int DISABLE_NONE = 0x00000000;

    private static final int DISABLE2_QUICK_SETTINGS = 1;
    private static final int DISABLE2_SYSTEM_ICONS = 1 << 1;
    private static final int DISABLE2_NOTIFICATION_SHADE = 1 << 2;
    private static final int DISABLE2_GLOBAL_ACTIONS = 1 << 3;
    private static final int DISABLE2_ROTATE_SUGGESTIONS = 1 << 4;
    private static final int DISABLE2_NONE = 0x00000000;

    public int getProp(Class<?> SystemProperties, String name, int defaultValue) {
        String val = (String) XposedHelpers.callStaticMethod(SystemProperties, "get", new Class<?>[]{String.class}, name);
        try {
            return val.equals("") ? defaultValue : Integer.parseInt(val);
        } catch (NumberFormatException ex) {
            Log.e(TAG, "invalid value for " + name + ": " + val);
            return defaultValue;
        }
    }

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        XposedBridge.log(TAG + ": started");

        final Class<?> SystemProperties = XposedHelpers.findClass("android.os.SystemProperties", null);

        XposedHelpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "adjustStatusBarLocked", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object mStatusBarManager = XposedHelpers.getObjectField(param.thisObject, "mStatusBarManager");
                if (mStatusBarManager != null) {
                    boolean mShowing = (boolean) XposedHelpers.getBooleanField(param.thisObject, "mShowing");
                    final int mode2 = getProp(SystemProperties,"persist.qslock.mode2", DISABLE2_QUICK_SETTINGS);
                    if (mShowing) {
                        final int mode = getProp(SystemProperties,"persist.qslock.mode", 0);
                        if (mode > 0) {
                            Log.i(TAG, "disable: " + mode);
                            XposedHelpers.callMethod(mStatusBarManager, "disable", new Class<?>[]{int.class}, mode);
                        }
                        if (mode2 > 0) {
                            Log.i(TAG, "disable2: " + mode2);
                            XposedHelpers.callMethod(mStatusBarManager, "disable2", new Class<?>[]{int.class}, mode2);
                        }
                    } else {
                        if (mode2 > 0) {
                            XposedHelpers.callMethod(mStatusBarManager, "disable2", new Class<?>[]{int.class}, DISABLE2_NONE);
                        }
                    }
                } else {
                    Log.e(TAG, "mStatusBarManager is null");
                }
            }
        });
    }
}
