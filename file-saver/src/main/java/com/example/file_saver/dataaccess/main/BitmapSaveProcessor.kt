package com.example.file_saver.dataaccess.main

import android.net.Uri
import com.example.file_saver.domains.BitmapContent
import com.example.file_saver.domains.FileSaveResult
import com.example.file_saver.extensions.toFileSaveResult
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface BitmapSaveProcessor {

    suspend fun save(content: BitmapContent): FileSaveResult {
        return suspendCoroutine { continuation ->
            val result = runCatching {
                saveBitmap(content)
            }
            continuation.resume(result.toFileSaveResult())
        }
    }

    fun saveBitmap(content: BitmapContent): Uri
}