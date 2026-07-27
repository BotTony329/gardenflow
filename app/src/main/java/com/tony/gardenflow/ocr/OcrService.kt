package com.tony.gardenflow.ocr

import android.net.Uri

interface OcrService {
    suspend fun recognizeText(imageUri: Uri): Result<String>
}
