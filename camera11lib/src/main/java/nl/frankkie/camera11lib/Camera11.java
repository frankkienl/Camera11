package nl.frankkie.camera11lib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        List<CameraApp> cameraApps = Camera11Util.getCameraApps(activity);
        //No apps found (should never happen)
        if (cameraApps.size() == 0) {
            Log.e("Camera11", "no camera apps found");
        }
        //If there is only one app, use that one.
        if (cameraApps.size() == 1) {
            startIntentForCameraApp(activity, cameraApps.get(0), action, output, requestCode);
        }
        //Otherwise, show chooser
        if (cameraApps.size() > 1) {
            //Show list to choose from
            showCameraChooser(activity, cameraApps, action, output, requestCode);
        }
    }

    private static void startIntentForCameraApp(Activity activity, CameraApp cameraApp, String action, Uri output, int requestCode) {
        //Get correct component
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

    private static void showCameraChooser(final Activity activity, final List<CameraApp> cameraAppModels, final String action, final Uri output, final int requestCode) {
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.chooser_dialog);
        dialog.setCancelable(true);
        ListView listView = dialog.findViewById(R.id.chooserDialogListView);
        listView.setAdapter(new CameraAppListViewAdapter(activity, cameraAppModels));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CameraApp cameraApp = cameraAppModels.get(position);
                startIntentForCameraApp(activity, cameraApp, action, output, requestCode);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    static class CameraAppListViewAdapter extends ArrayAdapter<CameraApp> {
        private Activity activity;
        private PackageManager pm;

        CameraAppListViewAdapter(Activity activity, List<CameraApp> cameraApps) {
            super(activity, R.layout.chooser_item, cameraApps);
            this.activity = activity;
            this.pm = activity.getPackageManager();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            //Get info
            CameraApp cameraApp = getItem(position);
            ComponentName componentName = cameraApp.componentNames.get(0);
            CharSequence appName = pm.getApplicationLabel(cameraApp.packageInfo.applicationInfo);
            CharSequence appDesc = null; //cameraApp.componentNames.get(0).getShortClassName();
            Drawable appIcon = null;
            try {
                appIcon = pm.getActivityIcon(componentName);
            } catch (Exception e) {
                //ignore
            }

            //Set UI
            if (convertView == null) {
                convertView = activity.getLayoutInflater().inflate(R.layout.chooser_item, parent, false);
            }
            TextView firstLine = convertView.findViewById(R.id.firstLine);
            firstLine.setText(appName);
            TextView secondLine = convertView.findViewById(R.id.secondLine);
            if (appDesc == null) {
                secondLine.setVisibility(View.GONE);
                secondLine.setText("");
            } else {
                secondLine.setVisibility(View.VISIBLE);
                secondLine.setText(appDesc);
            }
            ImageView imageView = convertView.findViewById(R.id.icon);
            imageView.setImageResource(R.drawable.ic_android_black_24dp); //reset
            if (appIcon != null) {
                imageView.setImageDrawable(appIcon);
            }
            return convertView;
        }
    }

}
