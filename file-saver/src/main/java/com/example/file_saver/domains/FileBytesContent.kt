package com.example.file_saver.domains

data class FileBytesContent(
    val data: ByteArray,
    override val fileNameWithoutSuffix: String,
    override val suffix: String,
    override val mimeType: String?,
    override val subfolderName: String?
) : SaveContent