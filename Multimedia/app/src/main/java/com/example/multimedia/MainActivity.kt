package com.example.multimedia;

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 123

    private val photoState = mutableStateOf<ImageBitmap?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { requestCameraPermissionAndOpenCamera() }) {
                    Text(text = "Tomar foto")
                }
                Button(onClick = { requestCameraPermissionAndRecordVideo() }) {
                    Text(text = "Grabar video")
                }


                Image(
                    bitmap = photoState.value ?: ImageBitmap(1, 1),
                    contentDescription = "Foto",
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
    private fun requestCameraPermissionAndRecordVideo() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraForVideo()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun openCameraForVideo() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            val videoUri = getOutputMediaFileUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            videoUri?.let {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, it)
                startActivityForResult(intent, CAMERA_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun getOutputMediaFileUri(contentUri: Uri): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        return contentResolver.insert(contentUri, values)
    }


    private fun requestCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun openCamera() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
            photoUri?.let {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, it)
                startActivityForResult(intent, CAMERA_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data?.data != null) {
                    // Se capturó una foto
                    val photoUri = data.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
                        .asImageBitmap()
                    photoState.value = imageBitmap
                } else if (data?.extras?.containsKey("data") == true) {
                    // Se capturó una miniatura de foto
                    val thumbnailBitmap = data.extras?.get("data") as? Bitmap
                    thumbnailBitmap?.let {
                        val imageBitmap = it.asImageBitmap()
                        photoState.value = imageBitmap
                    }
                } else {
                    // Se grabó un video
                    val videoUri = data?.data
                    // Aquí puedes hacer algo con el video grabado, como mostrarlo en un reproductor
                }
            } else {
                // El usuario canceló la captura de foto o la grabación de video
            }
        }
    }

}
