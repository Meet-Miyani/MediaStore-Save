package com.example.file_saver.dataaccess.main

import android.net.Uri
import com.example.file_saver.domains.FileBytesContent
import com.example.file_saver.domains.FileSaveResult
import com.example.file_saver.domains.FileStreamContent
import com.example.file_saver.extensions.toFileSaveResult
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal interface FileSaveProcessor {

    suspend fun save(file: FileStreamContent): FileSaveResult {
        return suspendCoroutine { continuation ->
            val result = runCatching {
                saveFile(file)
            }
            continuation.resume(result.toFileSaveResult())
        }
    }

    suspend fun save(file: FileBytesContent): FileSaveResult {
        return suspendCoroutine { continuation ->
            val result = runCatching {
                saveFile(file)
            }
            continuation.resume(result.toFileSaveResult())
        }
    }

    fun saveFile(content: FileStreamContent): Uri?

    fun saveFile(content: FileBytesContent): Uri?


}