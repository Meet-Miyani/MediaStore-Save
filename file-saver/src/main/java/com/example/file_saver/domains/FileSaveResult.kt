package com.example.file_saver.domains

import android.net.Uri

sealed interface FileSaveResult {

    data class SaveSuccess(val successData: Uri?) : FileSaveResult

    object MissingWritePermission : FileSaveResult

    data class SaveError(val exception: Throwable?) : FileSaveResult
}
