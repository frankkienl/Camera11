package nl.frankkie.camera11;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import nl.frankkie.camera11lib.Camera11;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {
        View btnOpenCamera = findViewById(R.id.btnOpenCamera);
        btnOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
    }

    private void openCamera() {
        int requestCode = 1337;
        File outputFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_DCIM), "photo.jpg");
        Camera11.openCamera(this, MediaStore.ACTION_IMAGE_CAPTURE, Uri.fromFile(outputFile), requestCode);
        Toast.makeText(this, "Opening Camera", Toast.LENGTH_LONG).show();
    }
}