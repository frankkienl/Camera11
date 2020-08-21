package nl.frankkie.camera11lib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Since Android 11, API30,
 * it's no longer possible to start a camera app, via the normal intent chooser way.
 * Therefor we scan the install apps, and find our own list.
 */
public class Camera11 {
    /**
     * Open camera app or chooser.
     * <p>
     * Open the camera app when there is only 1, on a chooser when there are multiple.
     * This will use the regular way on older Android versions, will show a custom chooser on Android 11+
     *
     * @param activity The activity
     * @param action   either MediaStore.ACTION_IMAGE_CAPTURE, MediaStore.ACTION_IMAGE_CAPTURE_SECURE or MediaStore.ACTION_VIDEO_CAPTURE
     */
    public static void openCamera(Activity activity, String action, Uri output, int requestCode) {
        //check if the Activity is not null
        if (activity == null) {
            throw new IllegalArgumentException("Activity must not be null");
        }

        //Check if the URI is not null
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }

        //Check if action is one of the allowed Intent actions, as found in MediaStore.
        @SuppressLint("InlinedApi")
        String[] correctActions = {MediaStore.ACTION_IMAGE_CAPTURE, MediaStore.ACTION_IMAGE_CAPTURE_SECURE, MediaStore.ACTION_VIDEO_CAPTURE};
        boolean isCorrectAction = false;
        for (String correctAction : correctActions) {
            if (action.equals(correctAction)) {
                isCorrectAction = true;
                break;
            }
        }
        if (!isCorrectAction) {
            //Not correct, throw.
            throw new IllegalArgumentException("action must be one of: [MediaStore.ACTION_IMAGE_CAPTURE, MediaStore.ACTION_IMAGE_CAPTURE_SECURE, MediaStore.ACTION_VIDEO_CAPTURE]");
        }

        //Check Android version
        if (Build.VERSION.SDK_INT < 30) {
            openCameraPre11(activity, action, output, requestCode);
        } else {
            openCameraPost11(activity, action, output, requestCode);
        }
    }

    private static void openCameraPre11(Activity activity, String action, Uri output, int requestCode) {
        //Make intent
        Intent intent = new Intent(action);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            try {
                //Start intent
                activity.startActivityForResult(intent, requestCode);
            } catch (RuntimeException e) {
                RuntimeException up = new RuntimeException("Error starting camera", e);
                //Throw up to caller
                throw up;
            }
        }
    }

    private static void openCameraPost11(Activity activity, String action, Uri output, int requestCode) {
        //Get a list of compatible apps
        PackageManager pm = activity.getPackageManager();
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
        //Filter on IntentFilters
        List<CameraAppModel> cameraApps = null;
        try {
            cameraApps = Util.getCameraAppsFromPackageInfos(cameraPermissionPackages);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (cameraApps == null) {
            throw new NullPointerException("no camera apps found");
        }
        //If there is only one app, use that one.
        if (cameraApps.size() == 1) {
            //Get correct component
            CameraAppModel cameraApp = cameraApps.get(0);
            ComponentName correctComponent = cameraApp.componentNames.get(0); //Big assumption here.

            //Make intent
            Intent intent = new Intent();
            intent.setComponent(correctComponent);
            intent.setAction(action);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                try {
                    //Start intent
                    activity.startActivityForResult(intent, requestCode);
                } catch (RuntimeException e) {
                    RuntimeException up = new RuntimeException("Error starting camera", e);
                    //Throw up to caller
                    throw up;
                }
            }
        }

        if (cameraApps.size() > 1) {
            //Show list to choose from
            showCameraChooser(activity, cameraApps);
        }
    }

    public static void showCameraChooser(Context context, List<CameraAppModel> cameraAppModels) {
        Toast.makeText(context, "TODO!!!!!", Toast.LENGTH_LONG).show();
    }

}
