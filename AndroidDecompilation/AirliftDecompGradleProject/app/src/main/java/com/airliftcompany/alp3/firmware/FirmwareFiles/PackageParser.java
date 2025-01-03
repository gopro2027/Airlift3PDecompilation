package com.airliftcompany.alp3.firmware.FirmwareFiles;

import android.util.Base64;
import android.util.Xml;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* loaded from: classes.dex */
public class PackageParser {
    private static final String namespace = null;

    public static FirmwarePackage Parse(InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            XmlPullParser newPullParser = Xml.newPullParser();
            newPullParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            newPullParser.setInput(inputStream, null);
            newPullParser.nextTag();
            return readFirmwarePackage(newPullParser);
        } catch (IOException e) {
            throw new IOException("Failed To Parse Firmware Package: " + e.getMessage());
        } catch (XmlPullParserException e2) {
            throw new XmlPullParserException("Failed To Parse Firmware Package: " + e2.getMessage());
        }
    }

    private static FirmwarePackage readFirmwarePackage(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        xmlPullParser.require(2, namespace, "Package");
        FirmwarePackage firmwarePackage = new FirmwarePackage();
        while (xmlPullParser.next() != 3) {
            if (xmlPullParser.getEventType() == 2) {
                String name = xmlPullParser.getName();
                if (name.equals("Identifier")) {
                    firmwarePackage.Identifier = readIdentifier(xmlPullParser);
                } else if (name.equals("Controllers")) {
                    firmwarePackage.Controllers = readControllers(xmlPullParser);
                } else {
                    skip(xmlPullParser);
                }
            }
        }
        return firmwarePackage;
    }

    private static List<Controller> readControllers(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        xmlPullParser.require(2, namespace, "Controllers");
        ArrayList arrayList = new ArrayList();
        while (xmlPullParser.next() != 3) {
            if (xmlPullParser.getEventType() == 2) {
                if (xmlPullParser.getName().equals("Controller")) {
                    arrayList.add(readController(xmlPullParser));
                } else {
                    skip(xmlPullParser);
                }
            }
        }
        return arrayList;
    }

    private static Controller readController(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        xmlPullParser.require(2, namespace, "Controller");
        Controller controller = new Controller();
        while (xmlPullParser.next() != 3) {
            if (xmlPullParser.getEventType() == 2) {
                String name = xmlPullParser.getName();
                if (name.equals("Identifier")) {
                    controller.Identifier = readIdentifier(xmlPullParser);
                } else if (name.equals("Images")) {
                    controller.Images = readImages(xmlPullParser);
                } else {
                    skip(xmlPullParser);
                }
            }
        }
        return controller;
    }

    private static List<Image> readImages(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        xmlPullParser.require(2, namespace, "Images");
        ArrayList arrayList = new ArrayList();
        while (xmlPullParser.next() != 3) {
            if (xmlPullParser.getEventType() == 2) {
                if (xmlPullParser.getName().equals("Image")) {
                    arrayList.add(readImage(xmlPullParser));
                } else {
                    skip(xmlPullParser);
                }
            }
        }
        return arrayList;
    }

    private static Image readImage(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        xmlPullParser.require(2, namespace, "Image");
        Image image = new Image();
        while (xmlPullParser.next() != 3) {
            if (xmlPullParser.getEventType() == 2) {
                String name = xmlPullParser.getName();
                if (name.equals("Address")) {
                    image.Address = readAddress(xmlPullParser);
                } else if (name.equals("Checksum")) {
                    image.Checksum = readChecksum(xmlPullParser);
                } else if (name.equals("Size")) {
                    image.Size = readSize(xmlPullParser);
                } else if (name.equals("Segments")) {
                    image.Segments = readSegments(xmlPullParser);
                } else {
                    skip(xmlPullParser);
                }
            }
        }
        return image;
    }

    private static List<Segment> readSegments(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        xmlPullParser.require(2, namespace, "Segments");
        ArrayList arrayList = new ArrayList();
        while (xmlPullParser.next() != 3) {
            if (xmlPullParser.getEventType() == 2) {
                if (xmlPullParser.getName().equals("Segment")) {
                    arrayList.add(readSegment(xmlPullParser));
                } else {
                    skip(xmlPullParser);
                }
            }
        }
        return arrayList;
    }

    private static Segment readSegment(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        xmlPullParser.require(2, namespace, "Segment");
        Segment segment = new Segment();
        while (xmlPullParser.next() != 3) {
            if (xmlPullParser.getEventType() == 2) {
                String name = xmlPullParser.getName();
                if (name.equals("Address")) {
                    segment.Address = readAddress(xmlPullParser);
                } else if (name.equals("Checksum")) {
                    segment.Checksum = readChecksum(xmlPullParser);
                } else if (name.equals("Size")) {
                    segment.Size = readSize(xmlPullParser);
                } else if (name.equals("Data")) {
                    segment.Data = readData(xmlPullParser);
                } else {
                    skip(xmlPullParser);
                }
            }
        }
        return segment;
    }

    private static byte[] readData(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        String str = namespace;
        xmlPullParser.require(2, str, "Data");
        byte[] readBytes = readBytes(xmlPullParser);
        xmlPullParser.require(3, str, "Data");
        return readBytes;
    }

    private static long readAddress(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        String str = namespace;
        xmlPullParser.require(2, str, "Address");
        long readLong = readLong(xmlPullParser);
        xmlPullParser.require(3, str, "Address");
        return readLong;
    }

    private static long readChecksum(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        String str = namespace;
        xmlPullParser.require(2, str, "Checksum");
        long readLong = readLong(xmlPullParser);
        xmlPullParser.require(3, str, "Checksum");
        return readLong;
    }

    private static long readSize(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        String str = namespace;
        xmlPullParser.require(2, str, "Size");
        long readLong = readLong(xmlPullParser);
        xmlPullParser.require(3, str, "Size");
        return readLong;
    }

    private static long readIdentifier(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        String str = namespace;
        xmlPullParser.require(2, str, "Identifier");
        long readLong = readLong(xmlPullParser);
        xmlPullParser.require(3, str, "Identifier");
        return readLong;
    }

    private static String readParameters(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        String str = namespace;
        xmlPullParser.require(2, str, "Parameters");
        String readText = readText(xmlPullParser);
        xmlPullParser.require(3, str, "Parameters");
        return readText;
    }

    private static void skip(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        if (xmlPullParser.getEventType() != 2) {
            throw new IllegalStateException();
        }
        int i = 1;
        while (i != 0) {
            int next = xmlPullParser.next();
            if (next == 1) {
                throw new IllegalStateException();
            }
            if (next == 2) {
                i++;
            } else if (next == 3) {
                i--;
            }
        }
    }

    private static String readText(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        if (xmlPullParser.next() != 4) {
            return "";
        }
        String text = xmlPullParser.getText();
        xmlPullParser.nextTag();
        return text;
    }

    private static long readLong(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        String str;
        if (xmlPullParser.next() == 4) {
            str = xmlPullParser.getText();
            xmlPullParser.nextTag();
        } else {
            str = "";
        }
        return Long.decode(str).longValue();
    }

    private static byte[] readBytes(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        String str;
        if (xmlPullParser.next() == 4) {
            str = xmlPullParser.getText();
            xmlPullParser.nextTag();
        } else {
            str = "";
        }
        return Base64.decode(str, 0);
    }
}
