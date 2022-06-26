package com.example.file_saver.dataaccess.itext

import android.content.Context
import java.io.FileOutputStream

internal class ITextLegacyProcessor(
    private val context: Context,
    private val fileProviderName: String?
) :StreamProcessor{

    override fun getFileStream(content: PdfContent): FileOutputStream {
        return FileOutputStream("")
    }
}