package com.airliftcompany.alp3.firmware;

import android.content.Context;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
//import com.microsoft.appcenter.ingestion.models.CommonProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

/* loaded from: classes.dex */
public class Util {
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static AmazonS3Client sS3Client;
    private static TransferUtility sTransferUtility;

    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(context.getApplicationContext(), Constants.S3_IDENTITY_POOL_ID, Regions.US_EAST_1);
        }
        return sCredProvider;
    }

    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()), Region.getRegion(Regions.US_EAST_1));
        }
        return sS3Client;
    }

    public static TransferUtility getTransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = TransferUtility.builder().context(context.getApplicationContext()).s3Client(getS3Client(context.getApplicationContext())).build();
        }
        return sTransferUtility;
    }

    public static String getBytesString(long j) {
        String[] strArr = {"KB", "MB", "GB", "TB"};
        double d = j;
        for (int i = 0; i < 4; i++) {
            d /= 1024.0d;
            if (d < 512.0d) {
                return String.format("%.2f", Double.valueOf(d)) + " " + strArr[i];
            }
        }
        return "";
    }

    public static File copyContentUriToFile(Context context, Uri uri) throws IOException {
        InputStream openInputStream = context.getContentResolver().openInputStream(uri);
        File file = new File(context.getDir("SampleImagesDir", 0), UUID.randomUUID().toString());
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] bArr = new byte[2046];
        while (true) {
            int read = openInputStream.read(bArr);
            if (read != -1) {
                fileOutputStream.write(bArr, 0, read);
            } else {
                fileOutputStream.flush();
                fileOutputStream.close();
                return file;
            }
        }
    }

//    public static void fillMap(Map<String, Object> map, TransferObserver transferObserver, boolean z) {
//        int bytesTransferred = (int) ((transferObserver.getBytesTransferred() * 100.0d) / transferObserver.getBytesTotal());
//        map.put(CommonProperties.f275ID, Integer.valueOf(transferObserver.getId()));
//        map.put("checked", Boolean.valueOf(z));
//        map.put("fileName", transferObserver.getAbsoluteFilePath());
//        map.put(NotificationCompat.CATEGORY_PROGRESS, Integer.valueOf(bytesTransferred));
//        map.put("bytes", getBytesString(transferObserver.getBytesTransferred()) + "/" + getBytesString(transferObserver.getBytesTotal()));
//        map.put("state", transferObserver.getState());
//        map.put("percentage", bytesTransferred + "%");
//    }

    public static Integer versionCompare(String str, String str2) {
        String[] split = str.split("\\.");
        String[] split2 = str2.split("\\.");
        int i = 0;
        while (i < split.length && i < split2.length && split[i].equals(split2[i])) {
            i++;
        }
        if (i < split.length && i < split2.length) {
            return Integer.valueOf(Integer.signum(Integer.valueOf(split[i]).compareTo(Integer.valueOf(split2[i]))));
        }
        return Integer.valueOf(Integer.signum(split.length - split2.length));
    }
}
