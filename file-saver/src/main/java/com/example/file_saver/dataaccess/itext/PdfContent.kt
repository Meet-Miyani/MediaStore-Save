package com.example.file_saver.dataaccess.itext

import com.example.file_saver.domains.SaveContent

data class PdfContent(
    override val fileNameWithoutSuffix: String,
    override val suffix: String,
    override val mimeType: String?,
    override val subfolderName: String?,
    val saveIn: SaveIn = SaveIn.DOCUMENTS,
    val shouldOverwrite: Boolean = true,
) : SaveContent

enum class SaveIn {
    DOWNLOADS,
    DOCUMENTS
}