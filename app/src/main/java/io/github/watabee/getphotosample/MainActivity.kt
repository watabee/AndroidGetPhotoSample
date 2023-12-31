package io.github.watabee.getphotosample

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.watabee.getphotosample.ui.theme.AndroidGetPhotoSampleTheme
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidGetPhotoSampleTheme {
                var imageUri: Uri by rememberSaveable { mutableStateOf(Uri.EMPTY) }

                val takePicture = rememberTakePictureLauncher { uri ->
                    Timber.d("takePicture uri = $uri")
                    if (uri != null) {
                        imageUri = uri
                    }
                }

                fun takePicture() {
                    val filename =
                        "${SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(System.currentTimeMillis())}.jpeg"
                    takePicture.launch(filename = filename)
                }

                val requestWriteStoragePermission =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted ->
                        if (granted) {
                            takePicture()
                        }
                    }

                val pickImage = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
                    Timber.d("pickImage uri: $uri")
                    uri?.let { imageUri = uri }
                }

                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            model = imageUri,
                            contentScale = ContentScale.Fit,
                            contentDescription = null
                        )
                        Row {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    pickImage.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            ) {
                                Text(text = "Pick image")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        takePicture()
                                    } else {
                                        requestWriteStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    }
                                }
                            ) {
                                Text(text = "Take picture")
                            }
                        }
                    }
                }
            }
        }
    }
}
