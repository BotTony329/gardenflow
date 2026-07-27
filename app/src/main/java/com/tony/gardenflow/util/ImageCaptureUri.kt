package com.tony.gardenflow.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun createCameraImageUri(context: Context, prefix: String = "gardenflow"): Uri {
    val file = File.createTempFile("${prefix}_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
