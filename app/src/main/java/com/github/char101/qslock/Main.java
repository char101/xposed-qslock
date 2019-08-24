package com.github.char101.qslock;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import android.app.AndroidAppHelper;

import android.content.Context;
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

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        XposedBridge.log(TAG + ": started");

        XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardDisplayManager", lpparam.classLoader, "show", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.i(TAG, "show");
                Context context = (Context) AndroidAppHelper.currentApplication();
                if (context == null) {
                    Log.e(TAG, "context is null");
                    return;
                }
                Object statusBarManager = context.getSystemService("statusbar");
                if (statusBarManager == null) {
                    Log.e(TAG, "statusBarManager is null");
                    return;
                }
                XposedHelpers.callMethod(statusBarManager, "disable2", new Class<?>[]{Integer.class}, DISABLE2_QUICK_SETTINGS | DISABLE2_NOTIFICATION_SHADE);
            }
        });

        XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardDisplayManager", lpparam.classLoader, "hide", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.i(TAG, "hide");
                Context context = (Context) AndroidAppHelper.currentApplication();
                if (context == null) {
                    Log.e(TAG, "context is null");
                    return;
                }
                Object statusBarManager = context.getSystemService("statusbar");
                if (statusBarManager == null) {
                    Log.e(TAG, "statusBarManager is null");
                    return;
                }
                XposedHelpers.callMethod(statusBarManager, "disable2", new Class<?>[]{Integer.class}, DISABLE2_NONE);
            }
        });
    }
}
