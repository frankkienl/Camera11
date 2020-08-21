package nl.frankkie.camera11lib;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.provider.MediaStore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fr.xgouchet.axml.CompressedXmlParser;

class Util {

    public static List<CameraAppModel> getCameraAppsFromPackageInfos(List<PackageInfo> packageInfos) throws IOException {
        ArrayList<CameraAppModel> cameraApps = new ArrayList<CameraAppModel>();
        for (PackageInfo somePackage : packageInfos) {
            try {
                Document doc = readAndroidManifestFromPackageInfo(somePackage);
                List<ComponentName> componentNameList = getCameraComponentNamesFromDocument(doc);
                cameraApps.add(new CameraAppModel(somePackage, componentNameList));
            } catch (Exception e) {
                //Couldn't read this app.
            }
        }
        return cameraApps;
    }


    public static List<PackageInfo> getCameraAppsPackageInfos(List<PackageInfo> packageInfos) {
        //There seems to be no easy way to get IntentFilters from the PackageManager.
        //So... Let's look at the AndroidManifest.xml
        ArrayList<PackageInfo> cameraApps = new ArrayList<PackageInfo>();
        for (PackageInfo somePackage : packageInfos) {
            if (isCameraAppFromPackageInfo(somePackage)) {
                cameraApps.add(somePackage);
            }
        }
        return cameraApps;
    }

    public static List<ComponentName> getCameraAppsComponentNames(List<PackageInfo> packageInfos) throws IOException {
        ArrayList<ComponentName> componentNames = new ArrayList<ComponentName>();
        for (PackageInfo somePackage : packageInfos) {
            try {
                Document doc = readAndroidManifestFromPackageInfo(somePackage);
                List<ComponentName> componentNameList = getCameraComponentNamesFromDocument(doc);
                componentNames.addAll(componentNameList);
            } catch (Exception e) {
                //Couldn't read this app.
            }
        }
        return componentNames;
    }

    public static boolean isCameraAppFromPackageInfo(PackageInfo somePackage) {
        try {
            Document doc = readAndroidManifestFromPackageInfo(somePackage);
            return isCameraAppFromDocument(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<ComponentName> getCameraComponentNamesFromDocument(Document doc) {
        @SuppressLint("InlinedApi")
        String[] correctActions = {MediaStore.ACTION_IMAGE_CAPTURE, MediaStore.ACTION_IMAGE_CAPTURE_SECURE, MediaStore.ACTION_VIDEO_CAPTURE};
        ArrayList<ComponentName> componentNames = new ArrayList<ComponentName>();
        Element manifestElement = (Element) doc.getElementsByTagName("manifest").item(0);
        String packageName = manifestElement.getAttribute("package");
        Element applicationElement = (Element) manifestElement.getElementsByTagName("application").item(0);
        NodeList activities = applicationElement.getElementsByTagName("activity");
        for (int i = 0; i < activities.getLength(); i++) {
            Element activityElement = (Element) activities.item(i);
            String activityName = activityElement.getAttribute("android:name");
            NodeList intentFiltersList = activityElement.getElementsByTagName("intent-filter");
            for (int j = 0; j < intentFiltersList.getLength(); j++) {
                Element intentFilterElement = (Element) intentFiltersList.item(j);
                NodeList actionsList = intentFilterElement.getElementsByTagName("action");
                for (int k = 0; k < actionsList.getLength(); k++) {
                    Element actionElement = (Element) actionsList.item(k);
                    String actionName = actionElement.getAttribute("android:name");
                    for (String correctAction : correctActions) {
                        if (actionName.equals(correctAction)) {
                            //this activity has an intent filter with a correct action, add this to the list.
                            componentNames.add(new ComponentName(packageName, activityName));
                        }
                    }
                }
            }
        }
        return componentNames;
    }

    public static boolean isCameraAppFromDocument(Document doc) {
        List<ComponentName> componentNames = getCameraComponentNamesFromDocument(doc);
        return !componentNames.isEmpty();
    }

    public static Document readAndroidManifestFromPackageInfo(PackageInfo packageInfo) throws IOException {
        File apkFile = null;
        File publicSourceDir = new File(packageInfo.applicationInfo.publicSourceDir);
        File sourceDir = new File(packageInfo.applicationInfo.sourceDir);

        //Check if publicSourceDir is the APK file
        if (publicSourceDir.exists() && publicSourceDir.canRead() && publicSourceDir.getName().endsWith(".apk") && !publicSourceDir.isDirectory()) {
            apkFile = publicSourceDir;
        }
        //Check if sourceDir is the APK file
        else if (sourceDir.exists() && sourceDir.canRead() && sourceDir.getName().endsWith(".apk") && !sourceDir.isDirectory()) {
            apkFile = sourceDir;
        }
        //Check if the sourceDir is a directory
        else {
            File[] listFiles = sourceDir.listFiles();

            if (listFiles == null) {
                throw new IOException("Error reading sourceDir");
            }
            for (File someFile : listFiles) {
                if (someFile.getName().endsWith(".apk")) {
                    //Ladies and gentlemen, we've got em.
                    //(Assumung only 1 apk-file, no split apk bs, no dynamic features, etc.)
                    apkFile = someFile;
                    break;
                }
            }
        }
        //No APK file found.
        if (apkFile == null) {
            throw new IOException("APK file not found");
        }
        //Get AndroidManifest.xml from APK
        ZipFile apkZipFile = new ZipFile(apkFile, ZipFile.OPEN_READ);
        ZipEntry manifestEntry = apkZipFile.getEntry("AndroidManifest.xml");
        InputStream manifestInputStream = apkZipFile.getInputStream(manifestEntry);
        try {
            Document doc = new CompressedXmlParser().parseDOM(manifestInputStream);
            return doc;
        } catch (Exception e) {
            throw new IOException("Error reading AndroidManifest", e);
        }
    }
}
