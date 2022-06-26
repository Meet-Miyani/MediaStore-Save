package com.example.file_saver.dataaccess.itext

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.file_saver.extensions.getExistingStreamForIText
import com.example.file_saver.extensions.getNewStreamForIText
import java.io.FileOutputStream

sealed class Either {
    data class FileUri(val value: Uri) : Either()
    data class ContentData(val value: ContentValues) : Either()
}

@RequiresApi(Build.VERSION_CODES.Q)
class ITextProcessor(
    private val contentResolver: ContentResolver,
    private val context: Context
) : StreamProcessor {

    override fun getFileStream(content: PdfContent): FileOutputStream {
        val downloadsFolder =
            if (content.saveIn == SaveIn.DOWNLOADS) getDownloadFolderUri() else getDocumentFolderUri()
        with(content) {
            when (val content = getContentValues()
            )
            {
                is Either.FileUri -> {
                    Log.d(TAG, "getFileStream: $content")
                    return contentResolver.getExistingStreamForIText(content.value)
                }
                is Either.ContentData -> {
                    Log.d(TAG, "getFileStream: $content")
                    return contentResolver.getNewStreamForIText(downloadsFolder, content.value)!!
                }
            }
        }
    }

    private fun PdfContent.getContentValues(): Either {
        if (shouldOverwrite) {
            val uri = getExistingPdfUriOrNullQ(context, fileNameWithSuffix)
            if (uri != null) {
                return Either.FileUri(uri)
            }
        }
        return if (saveIn == SaveIn.DOWNLOADS) Either.ContentData(getDownloadContents()) else Either.ContentData(
            getDocumentContents()
        )
    }

    private fun PdfContent.getDownloadContents(): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileNameWithSuffix)
            mimeType?.let { put(MediaStore.Downloads.MIME_TYPE, it) }
            subfolderName?.let {
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/$it"
                )
            }
        }
    }

    private fun PdfContent.getDocumentContents(): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileNameWithSuffix)
            mimeType?.let { put(MediaStore.Files.FileColumns.MIME_TYPE, it) }
            subfolderName?.let {
                put(
                    MediaStore.Files.FileColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOCUMENTS}/$it"
                )
            }
        }
    }


    private fun getDocumentFolderUri(): Uri {
        return MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }

    private fun getDownloadFolderUri(): Uri {
        return MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }

    private fun PdfContent.getExistingPdfUriOrNullQ(context: Context, fileName: String): Uri? {
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val selection = "${MediaStore.Files.FileColumns.RELATIVE_PATH}=? AND ${MediaStore.Files.FileColumns.DISPLAY_NAME}=?"
        val relative = if (saveIn==SaveIn.DOWNLOADS) Environment.DIRECTORY_DOWNLOADS else Environment.DIRECTORY_DOCUMENTS
        val selectionArgs = arrayOf("$relative/$subfolderName",fileName)
        context.contentResolver.query(
            collection,
            null, selection, selectionArgs, null
        ).use { c ->
            Log.d(TAG, "getExistingPdfUriOrNullQ: ${c!!.count}")
            if (c.count >= 1) {
                c.moveToFirst().let {
                    val id = c.getLong(c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    return ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), id
                    )
                }
            }
        }
        return null
    }

    companion object {
        private const val TAG = "IText_Processor"
    }

}