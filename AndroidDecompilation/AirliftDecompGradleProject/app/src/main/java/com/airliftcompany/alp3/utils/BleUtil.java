package com.airliftcompany.alp3.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/* loaded from: classes.dex */
public final class BleUtil {
    private static final String TAG = "BleUtil";

    public BleAdvertisedData parseAdertisedData(byte[] bArr) {
        byte b;
        ArrayList arrayList = new ArrayList();
        String str = null;
        if (bArr == null) {
            return new BleAdvertisedData(arrayList, null);
        }
        ByteBuffer order = ByteBuffer.wrap(bArr).order(ByteOrder.LITTLE_ENDIAN);
        while (order.remaining() > 2 && (b = order.get()) != 0) {
            byte b2 = order.get();
            if (b2 == 2 || b2 == 3) {
                while (b >= 2) {
                    arrayList.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", Short.valueOf(order.getShort()))));
                    b = (byte) (b - 2);
                }
            } else if (b2 == 6 || b2 == 7) {
                while (b >= 16) {
                    arrayList.add(new UUID(order.getLong(), order.getLong()));
                    b = (byte) (b - 16);
                }
            } else if (b2 == 9) {
                byte[] bArr2 = new byte[b - 1];
                order.get(bArr2);
                try {
                    str = new String(bArr2, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                order.position((order.position() + b) - 1);
            }
        }
        return new BleAdvertisedData(arrayList, str);
    }

    public class BleAdvertisedData {
        private String mName;
        private List<UUID> mUuids;

        public BleAdvertisedData(List<UUID> list, String str) {
            this.mUuids = list;
            this.mName = str;
        }

        public List<UUID> getUuids() {
            return this.mUuids;
        }

        public String getName() {
            return this.mName;
        }
    }
}
