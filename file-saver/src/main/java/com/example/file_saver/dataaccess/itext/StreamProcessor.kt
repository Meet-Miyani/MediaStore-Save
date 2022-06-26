package com.example.file_saver.dataaccess.itext

import com.example.file_saver.extensions.getFileOutputStream
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface StreamProcessor {

    suspend fun getStream(content: PdfContent): StreamResult {
        return suspendCoroutine { continuation ->
            val result = runCatching {
                getFileStream(content)
            }
            continuation.resume(result.getFileOutputStream())
        }
    }

    fun getFileStream(content: PdfContent): FileOutputStream

}