package nl.frankkie.camera11lib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

public class Camera11Util {

    /**
     * Get a list of CameraApps
     * @param context
     * @return list of CameraApps
     * @see CameraApp
     */
    public static List<CameraApp> getCameraApps(Context context) {
        //Step 1 - Get apps with Camera permission
        List<PackageInfo> cameraPermissionPackages = getPackageInfosWithCameraPermission(context);
        //Step 2 - Filter out apps with the correct intent-filter(s)
        List<CameraApp> cameraApps = new ArrayList<CameraApp>();
        for (PackageInfo somePackage : cameraPermissionPackages) {
            try {
                //Step 2a - Get the AndroidManifest.xml
                Document doc = readAndroidManifestFromPackageInfo(somePackage);
                //Step 2b - Get Camera ComponentNames from Manifest
                List<ComponentName> componentNames = getCameraComponentNamesFromDocument(doc);
                if (componentNames.size() == 0) {
                    continue; //This is not a Camera app
                }
                //Step 2c - Create CameraAppModel
                CameraApp cameraApp = new CameraApp(somePackage, componentNames);
                cameraApps.add(cameraApp);
            } catch (Exception e) {
                //ignore
            }
        }

        return cameraApps;
    }

    /**
     * Get a list of PackageInfo, of apps with the Camera permission granted.
     * @param context
     * @return list of PackageInfo, of apps with the Camera permission
     */
    public static List<PackageInfo> getPackageInfosWithCameraPermission(Context context){
        //Get a list of compatible apps
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        ArrayList<PackageInfo> cameraPermissionPackages = new ArrayList<PackageInfo>();
        //filter out only camera apps
        for (PackageInfo somePackage : installedPackages) {
            //- A camera app should have the Camera permission
            boolean hasCameraPermission = false;
            if (somePackage.requestedPermissions == null || somePackage.requestedPermissions.length == 0) {
                continue;
            }
            for (String requestPermission : somePackage.requestedPermissions) {
                if (requestPermission.equals(Manifest.permission.CAMERA)) {
                    //Ask for Camera permission, now see if it's granted.
                    if (pm.checkPermission(Manifest.permission.CAMERA, somePackage.packageName) == PackageManager.PERMISSION_GRANTED) {
                        hasCameraPermission = true;
                        break;
                    }
                }
            }
            if (hasCameraPermission) {
                cameraPermissionPackages.add(somePackage);
            }
        }
        return cameraPermissionPackages;
    }

    /**
     * Read the AndroidManifest.xml of a PackageInfo into a Document
     * @param packageInfo
     * @return document containing AndroidManifest
     * @throws IOException when AndroidManifest could not be read.
     */
    public static Document readAndroidManifestFromPackageInfo(PackageInfo packageInfo) throws IOException {
        File apkFile  = new File(packageInfo.applicationInfo.publicSourceDir);
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

    /**
     * Get ComponentNames (Activities) that have the correct IntentFilter from Document (AndroidManifest.xml)
     * @param doc the AndroidManifest as Document
     * @return list of ComponentNames with the correct IntentFilter.
     */
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
}
