package com.example.file_saver.dataaccess.legacy

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import com.example.file_saver.dataaccess.main.BitmapSaveProcessor
import com.example.file_saver.dataaccess.main.FileSaveProcessor
import com.example.file_saver.domains.BitmapContent
import com.example.file_saver.domains.FileBytesContent
import com.example.file_saver.domains.FileStreamContent
import com.example.file_saver.domains.SaveContent
import com.example.file_saver.extensions.getUriWithFileProviderIfPresent
import com.example.file_saver.extensions.saveToFile
import com.example.file_saver.extensions.startMediaScan
import java.io.File

internal class ImageFileSaveLegacyProcessor(
    private val context: Context,
    private val fileProviderName: String?
) : FileSaveProcessor, BitmapSaveProcessor {

    override fun saveFile(content: FileStreamContent): Uri {
        return content.data.saveToFile(content.fileNameWithSuffix, getDirectory(content))
            .getUriWithFileProviderIfPresent(fileProviderName, context)
            .also {
                it.startMediaScan(context)
            }
    }

    override fun saveFile(content: FileBytesContent): Uri {
        return content.data.saveToFile(content.fileNameWithSuffix, getDirectory(content))
            .getUriWithFileProviderIfPresent(fileProviderName, context)
            .also {
                it.startMediaScan(context)
            }
    }

    private fun getDirectory(content: SaveContent) = File(
        Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
            .toString() + content.subfolderName?.let { "/$it" }.orEmpty()
    )

    override fun saveBitmap(content: BitmapContent): Uri {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
                .toString() + content.subfolderName?.let { "/$it" }.orEmpty()
        )
        with(content) {
            val file = saveToFile(fileNameWithSuffix, directory) {
                bitmap.compress(format, quality, it)
            }

            return file.getUriWithFileProviderIfPresent(fileProviderName, context).also {
                it.startMediaScan(context)
            }
        }
    }
}