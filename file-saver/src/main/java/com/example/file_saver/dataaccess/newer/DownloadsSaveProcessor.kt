package com.example.file_saver.dataaccess.newer

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
import com.example.file_saver.dataaccess.main.FileSaveProcessor
import com.example.file_saver.domains.FileBytesContent
import com.example.file_saver.domains.FileStreamContent
import com.example.file_saver.domains.SaveContent
import com.example.file_saver.extensions.overWriteFile
import com.example.file_saver.extensions.saveFile
import com.example.file_saver.extensions.startMediaScan
import com.example.file_saver.extensions.updateFile


@RequiresApi(Build.VERSION_CODES.Q)
internal class DownloadsSaveProcessor(
    private val contentResolver: ContentResolver,
    private val context: Context
) : FileSaveProcessor {

    private val TAG = "Downloads_Save"

    override fun saveFile(content: FileStreamContent): Uri? {
        val downloadsFolder = getDownloadFolderUri()
        with(content) {
            when (val contentDetails = getContentValues()) {
                is ContentValues -> {
                    return contentResolver.saveFile(downloadsFolder, contentDetails, data).also {
                        it.startMediaScan(context)
                    }
                }
                is Uri -> {
                    contentResolver.updateFile(contentDetails,data)
                    return null
                }
                else -> {
                    return null
                }
            }
        }
    }

    private fun SaveContent.getContentValues(): Any {
        val uri = getExistingPdfUriOrNullQ(context, fileNameWithSuffix)
        if (uri != null) {
            return uri
        }

        val contentDetails = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileNameWithSuffix)
            mimeType?.let { put(MediaStore.Downloads.MIME_TYPE, it) }
            subfolderName?.let {
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/$it"
                )
            }
        }
        return contentDetails
    }

    private fun SaveContent.getContentValuesWithUri(): Pair<Uri?, ContentValues> {
        val uri = getExistingPdfUriOrNullQ(context, fileNameWithSuffix)

        val contentDetails = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileNameWithSuffix)
            mimeType?.let { put(MediaStore.Downloads.MIME_TYPE, it) }
            subfolderName?.let {
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/$it"
                )
            }
        }

        return Pair(uri,contentDetails)
    }

    override fun saveFile(content: FileBytesContent): Uri? {
        val downloadsFolder = getDownloadFolderUri()

        with(content) {
            when (val contentDetails = getContentValues()) {
                is ContentValues -> {
                    return contentResolver.saveFile(downloadsFolder, contentDetails, data).also {
                        it.startMediaScan(context)
                    }
                }
                is Uri -> {
                    Log.d(TAG, "saveFile: $contentDetails")
                    return null
                }
                else -> {
                    return null
                }
            }
        }
    }

    private fun getDownloadFolderUri(): Uri {
        return MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }


    private fun getExistingPdfUriOrNullQ(context: Context, fileName: String): Uri? {
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME}=?"
        val selectionArgs = arrayOf(fileName)
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
}