package com.airliftcompany.alp3.firmware;

/* loaded from: classes.dex */
public class Constants {
    public static final Integer FIRMWARE_DOCUMENT_VERSION = 1;
    private static final String S3_BUCKET_NAME = "alp3le";
    private static final String S3_DEMO_BUCKET_NAME = "alp3ledemo";
    public static final String S3_IDENTITY_POOL_ID = "us-east-1:041d6096-ea83-428a-8c84-ad007c674721";
    private static final String S3_SANDBOX_BUCKET_NAME = "alp3lesandbox";
    public static final String VERSION_FILENAME = "version_android.json";
    public static final String androidVersionDictKey = "androidVersion";
    public static final String displayLeftFilenameKey = "displayLeftFilename";
    public static final String displayNormalFilenameKey = "displayNormalFilename";
    public static final String displayRightFilenameKey = "displayRightFilename";
    public static final String displayVersionKey = "displayVersion";
    public static final String documentVersionKey = "documentVersion";
    public static final String languageKey = "language";
    public static final String languagesKey = "languages";
    public static final String manifoldFilenameKey = "manifoldFilename";
    public static final String manifoldVersionKey = "manifoldVersion";
    public static final String minDisplayVersionKey = "minDisplayVersion";
    public static final String minManifoldVersionKey = "minManifoldVersion";
    public static final String minVersionKey = "minVersion";
    public static final String releaseNotesKey = "releaseNotes";
    public static final String versionKey = "version";

    public static final String S3BucketName() {
        return S3_BUCKET_NAME;
    }
}
