package nl.frankkie.camera11

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import nl.frankkie.camera11lib.Camera11
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }

    private fun initUI() {
        btnOpenCamera11.setOnClickListener { openCamera11() }
        btnOpenCameraNormal.setOnClickListener { openCameraNormal() }
        btnOpenCameraQueries.setOnClickListener { openCameraQueries() }
    }

    private fun openCamera11() {
        val requestCode = 1337
        val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_DCIM), "photo.jpg")
        Camera11.openCamera(this, MediaStore.ACTION_IMAGE_CAPTURE, Uri.fromFile(outputFile), requestCode)
        Toast.makeText(this, "Opening Camera via Camera11", Toast.LENGTH_LONG).show()
    }

    private fun openCameraNormal() {
        val requestCode = 1337
        val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_DCIM), "photo.jpg")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, outputFile) }
        startActivityForResult(intent, requestCode)
        Toast.makeText(this, "Opening Camera normally", Toast.LENGTH_LONG).show()
    }

    private fun openCameraQueries() {
        val requestCode = 1337
        val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_DCIM), "photo.jpg")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, outputFile) }
        val results = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        } else {
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        val chooser = Intent.createChooser(intent, "Camera app chooser")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        }
    }
}