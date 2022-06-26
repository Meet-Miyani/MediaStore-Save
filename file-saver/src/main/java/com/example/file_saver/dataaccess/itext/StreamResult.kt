package com.example.file_saver.dataaccess.itext

import java.io.FileOutputStream

interface StreamResult {

    data class StreamSuccess(val stream: FileOutputStream?) : StreamResult

    object MissingWritePermission : StreamResult

    data class StreamError(val exception: Throwable?) : StreamResult

}