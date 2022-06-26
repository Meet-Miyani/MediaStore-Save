package com.example.file_saver.extensions

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.file_saver.dataaccess.itext.StreamResult
import com.example.file_saver.dataaccess.legacy.FileNameLegacyResolver
import com.example.file_saver.domains.FileSaveResult
import com.example.file_saver.domains.UnknownSaveError
import java.io.*

fun ContentResolver.saveFile(
    folder: Uri,
    contentDetails: ContentValues,
    stream: InputStream
): Uri {
    return saveFile(folder, contentDetails) {
        it.write(stream.buffered().readBytes())
    }
}

fun ContentResolver.saveFile(
    folder: Uri,
    contentDetails: ContentValues,
    bytes: ByteArray
): Uri {
    return saveFile(folder, contentDetails) {
        it.write(bytes)
    }
}

fun ContentResolver.saveFile(
    folder: Uri,
    contentDetails: ContentValues,
    writer: (OutputStream) -> Unit
): Uri {
    return insert(folder, contentDetails)?.also { contentUri ->
        openFileDescriptor(contentUri, "w")
            .use { parcelFileDescriptor ->
                writer(ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor))
            }
    } ?: throw UnknownSaveError
}

fun ContentResolver.updateFile(
    file: Uri,
    stream: InputStream
) {
    overWriteFile(file) { it.write(stream.buffered().readBytes()) }
}

fun ContentResolver.updateFile(
    file: Uri,
    bytes: ByteArray
) {
    overWriteFile(file) { it.write(bytes) }
}

fun ContentResolver.overWriteFile(
    file: Uri,
    writer: (OutputStream) -> Unit
) {
    openFileDescriptor(file, "w")
        .use { parcelFileDescriptor ->
            writer(ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor))
        }
}

fun ContentResolver.getNewStreamForIText(
    folder: Uri,
    contentDetails: ContentValues,
): FileOutputStream? {
    insert(folder, contentDetails)?.let {
        return FileOutputStream(openFileDescriptor(it, "w")?.fileDescriptor)
    }
    return null
}

fun ContentResolver.getExistingStreamForIText(
    file: Uri
): FileOutputStream {
    return FileOutputStream(openFileDescriptor(file, "w")?.fileDescriptor)
}

fun InputStream.saveToFile(
    fileName: String,
    attachmentPath: File
): File {
    use {
        return saveToFile(fileName, attachmentPath) { outputStream ->
            val readBytes = buffered().readBytes()
            outputStream.write(readBytes)
            outputStream.flush()
        }
    }
}

fun ByteArray.saveToFile(
    fileName: String,
    attachmentPath: File
): File {
    return saveToFile(fileName, attachmentPath) { outputStream ->
        outputStream.write(this)
        outputStream.flush()
    }
}

fun saveToFile(
    fileName: String,
    attachmentPath: File,
    writer: (OutputStream) -> Unit
): File {
    val uniqueFileName = FileNameLegacyResolver.getUniqueFileName(attachmentPath, fileName)
    val savedFile = File(attachmentPath, uniqueFileName)
    try {
        savedFile.parentFile?.mkdirs()

        FileOutputStream(savedFile).use { outputStream ->
            writer(outputStream)
        }
    } catch (e: IOException) {
        savedFile.delete()
        e.printStackTrace()
        throw e
    }
    return savedFile
}

fun Result<Uri?>.toFileSaveResult(): FileSaveResult {
    return when {
        isSuccess -> FileSaveResult.SaveSuccess(this.getOrThrow())
        else -> FileSaveResult.SaveError(exceptionOrNull())
    }
}

fun Uri.startMediaScan(context: Context) {
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    mediaScanIntent.data = this
    context.sendBroadcast(mediaScanIntent)
}

fun File.getUriWithFileProviderIfPresent(fileProviderName: String?, context: Context): Uri {
    return fileProviderName?.let {
        FileProvider.getUriForFile(context, fileProviderName, this)
    } ?: toUri()
}

fun Result<FileOutputStream>.getFileOutputStream(): StreamResult {
    return when {
        isSuccess -> StreamResult.StreamSuccess(this.getOrThrow())
        else -> StreamResult.StreamError(exceptionOrNull())
    }
}