package nl.frankkie.camera11lib;

import android.content.ComponentName;
import android.content.pm.PackageInfo;

import java.util.List;
import java.util.Map;

public class CameraApp {
    public CameraApp(PackageInfo packageInfo, List<ComponentName> componentNames) {
        this.packageInfo = packageInfo;
        this.componentNames = componentNames;
    }

    public PackageInfo packageInfo;
    public List<ComponentName> componentNames;
}
