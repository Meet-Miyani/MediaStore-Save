package com.example.file_saver.usecase

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.file_saver.dataaccess.itext.ITextLegacyProcessor
import com.example.file_saver.dataaccess.itext.ITextProcessor
import com.example.file_saver.dataaccess.itext.StreamProcessor
import com.example.file_saver.dataaccess.legacy.DownloadsSaveLegacyProcessor
import com.example.file_saver.dataaccess.legacy.ImageFileSaveLegacyProcessor
import com.example.file_saver.dataaccess.main.BitmapSaveProcessor
import com.example.file_saver.dataaccess.main.FileSaveProcessor
import com.example.file_saver.dataaccess.newer.DownloadsSaveProcessor
import com.example.file_saver.dataaccess.newer.ImageFileSaveProcessor

internal data class ProcessorProvider(
    private val context: Context,
    private val fileProviderName: String?) {

    val downloadsProcessor: FileSaveProcessor =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            DownloadsSaveProcessor(context.contentResolver, context)
        } else {
            DownloadsSaveLegacyProcessor(context, fileProviderName)
        }
    val imagesFileSaveProcessor: FileSaveProcessor =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ImageFileSaveProcessor(context.contentResolver, context)
        } else {
            ImageFileSaveLegacyProcessor(context, fileProviderName)
        }
    val bitmapSaveProcessor: BitmapSaveProcessor =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ImageFileSaveProcessor(context.contentResolver, context)
        } else {
            ImageFileSaveLegacyProcessor(context, fileProviderName)
        }

    val iTextProcessor : StreamProcessor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ITextProcessor(context.contentResolver, context)
    } else {
        Log.d(TAG, "LEGACY MODE: ")
        ITextLegacyProcessor(context, fileProviderName)
    }

    companion object{
        private const val TAG = "Processor_Provider"
    }
}