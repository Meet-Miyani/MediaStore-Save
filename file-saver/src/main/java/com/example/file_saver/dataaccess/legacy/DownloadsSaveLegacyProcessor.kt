package com.example.file_saver.dataaccess.legacy

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import com.example.file_saver.dataaccess.main.FileSaveProcessor
import com.example.file_saver.domains.FileBytesContent
import com.example.file_saver.domains.FileStreamContent
import com.example.file_saver.domains.SaveContent
import com.example.file_saver.extensions.getUriWithFileProviderIfPresent
import com.example.file_saver.extensions.saveToFile
import com.example.file_saver.extensions.startMediaScan
import java.io.File

internal class DownloadsSaveLegacyProcessor(
    private val context: Context,
    private val fileProviderName: String?
) : FileSaveProcessor {

    override fun saveFile(content: FileStreamContent): Uri {
        return content.data.saveToFile(content.fileNameWithSuffix, File(getDirectory(content)))
            .getUriWithFileProviderIfPresent(fileProviderName, context).also {
                it.startMediaScan(context)
            }
    }

    override fun saveFile(content: FileBytesContent): Uri {
        return content.data.saveToFile(content.fileNameWithSuffix, File(getDirectory(content)))
            .getUriWithFileProviderIfPresent(fileProviderName, context).also {
                it.startMediaScan(context)
            }
    }

    private fun getDirectory(content: SaveContent): String {
        return getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() +
                content.subfolderName?.let { "/$it" }.orEmpty()
    }
}
