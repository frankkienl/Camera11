package nl.frankkie.camera11lib;

import android.content.ComponentName;
import android.content.pm.PackageInfo;

import java.util.List;
import java.util.Map;

class CameraAppModel {
    public CameraAppModel(PackageInfo packageInfo, List<ComponentName> componentNames) {
        this.packageInfo = packageInfo;
        this.componentNames = componentNames;
    }

    public PackageInfo packageInfo;
    public List<ComponentName> componentNames;
}
