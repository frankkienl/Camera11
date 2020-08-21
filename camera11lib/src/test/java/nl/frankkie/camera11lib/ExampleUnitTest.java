package nl.frankkie.camera11lib;

import org.junit.Test;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import nl.frankkie.camera11lib.Camera11Util;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testManifest1() throws Exception {
        Document doc = getManifest1();
        boolean aa = isCameraAppFromDocument(doc);
        assertFalse(aa);
    }

    private Document getManifest1() throws Exception {
        String someManifest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    package=\"nl.frankkie.camera11\">\n" +
                "\n" +
                "    <application\n" +
                "        android:allowBackup=\"true\"\n" +
                "        android:icon=\"@mipmap/ic_launcher\"\n" +
                "        android:label=\"@string/app_name\"\n" +
                "        android:roundIcon=\"@mipmap/ic_launcher_round\"\n" +
                "        android:supportsRtl=\"true\"\n" +
                "        android:theme=\"@style/Theme.Camera11\">\n" +
                "        <activity android:name=\".MainActivity\">\n" +
                "            <intent-filter>\n" +
                "                <action android:name=\"android.intent.action.MAIN\" />\n" +
                "                <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
                "            </intent-filter>\n" +
                "        </activity>\n" +
                "    </application>\n" +
                "\n" +
                "</manifest>";
        InputStream stringInputStream = new ByteArrayInputStream(someManifest.getBytes());
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stringInputStream);
        return doc;
    }

    @Test
    public void testManifest2() throws Exception {
        Document doc = getManifest2();
        boolean aa = isCameraAppFromDocument(doc);
        assertTrue(aa);
    }

    private Document getManifest2() throws Exception {
        String someManifest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    package=\"nl.frankkie.camera11\">\n" +
                "\n" +
                "    <application\n" +
                "        android:allowBackup=\"true\"\n" +
                "        android:icon=\"@mipmap/ic_launcher\"\n" +
                "        android:label=\"@string/app_name\"\n" +
                "        android:roundIcon=\"@mipmap/ic_launcher_round\"\n" +
                "        android:supportsRtl=\"true\"\n" +
                "        android:theme=\"@style/Theme.Camera11\">\n" +
                "        <activity android:name=\".MainActivity\">\n" +
                "            <intent-filter>\n" +
                "                <action android:name=\"android.media.action.IMAGE_CAPTURE\" />\n" +
                "                <category android:name=\"android.intent.category.DEFAULT\" />\n" +
                "            </intent-filter>\n" +
                "        </activity>\n" +
                "    </application>\n" +
                "\n" +
                "</manifest>";
        InputStream stringInputStream = new ByteArrayInputStream(someManifest.getBytes());
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stringInputStream);
        return doc;
    }
}