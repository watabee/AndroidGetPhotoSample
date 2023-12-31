package io.github.watabee.getphotosample

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.core.os.bundleOf

private const val SAVED_STATE_KEY = "TakePicture"
private const val TAKE_PICTURE_URI = "TakePictureUri"

fun interface TakePictureLauncher {
    fun launch(filename: String)
}

/**
 * カメラを起動するための [TakePictureLauncher] を返す
 *
 * val takePicture = rememberTakePictureLauncher { uri ->
 *     if (uri != null) {
 *         // 成功時の処理...
 *     }
 * }
 *
 * ...
 *
 * val filename =
 *     "${SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(System.currentTimeMillis())}.jpeg"
 * takePicture.launch(filename = filename)
 */
@Composable
fun rememberTakePictureLauncher(onResult: (Uri?) -> Unit): TakePictureLauncher {
    val registry = LocalSavedStateRegistryOwner.current.savedStateRegistry
    var takePictureUri: Uri? = remember { null }
    val updatedOnResult by rememberUpdatedState(onResult)

    DisposableEffect(registry) {
        takePictureUri = registry.consumeRestoredStateForKey(SAVED_STATE_KEY)?.getParcelable(TAKE_PICTURE_URI)
        registry.registerSavedStateProvider(SAVED_STATE_KEY) {
            bundleOf(TAKE_PICTURE_URI to takePictureUri)
        }
        onDispose {
            registry.unregisterSavedStateProvider(SAVED_STATE_KEY)
        }
    }
    val contentResolver = LocalContext.current.contentResolver

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { isSuccessful ->
            val resultUri = if (isSuccessful) takePictureUri else null
            if (!isSuccessful) {
                takePictureUri?.let { uri -> contentResolver.delete(uri, null, null) }
            }

            updatedOnResult(resultUri)
            takePictureUri = null
        }
    )

    return remember(takePicture, contentResolver) {
        TakePictureLauncher { filename ->
            check(isValidFilename(filename)) {
                "The filename must have a jpeg extension."
            }

            val uri = createTakePictureUri(contentResolver, filename)
            takePictureUri = uri
            takePicture.launch(uri)
        }
    }
}

private fun isValidFilename(filename: String): Boolean {
    if (filename.isBlank()) {
        return false
    }
    return filename.contains("""\.jpe?g$""".toRegex(RegexOption.IGNORE_CASE))
}

private fun createTakePictureUri(
    contentResolver: ContentResolver,
    filename: String
): Uri {
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    return contentResolver.insert(collection,
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
    ) ?: throw IllegalStateException("Failed to create a media file.")
}
