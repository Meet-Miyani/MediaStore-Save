package com.example.file_saver.domains

import com.example.file_saver.utils.DOT


interface SaveContent {
    val fileNameWithoutSuffix: String
    val suffix: String
    val mimeType: String?
    val subfolderName: String?

    val fileNameWithSuffix: String
        get() = "$fileNameWithoutSuffix$DOT${suffix}"

}