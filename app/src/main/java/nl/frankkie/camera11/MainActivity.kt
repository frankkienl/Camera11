package nl.frankkie.camera11

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nl.frankkie.camera11lib.Camera11
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }

    private fun initUI() {
        val btnOpenCamera = findViewById<View>(R.id.btnOpenCamera)
        btnOpenCamera.setOnClickListener { openCamera() }
    }

    private fun openCamera() {
        val requestCode = 1337
        val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_DCIM), "photo.jpg")
        Camera11.openCamera(this, MediaStore.ACTION_IMAGE_CAPTURE, Uri.fromFile(outputFile), requestCode)
        Toast.makeText(this, "Opening Camera", Toast.LENGTH_LONG).show()
    }
}