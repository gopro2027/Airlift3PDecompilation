package com.airliftcompany.alp3.comm;

/* loaded from: classes.dex */
public interface CommServiceListener {
    void onAuthorizationFailed();

    void onAuthorized();

    void onBleInitializationFailed();

    void onBleInitialized();

    void onCalibrationUpdated();

    void onConnectionFailed();

    void onSerialUpdated();

    void onSettingsUpdated();

    void onStatusUpdated();

    void onVersionUpdated();
}
