package com.example.filesavingproejct_git

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.file_saver.dataaccess.itext.PdfContent
import com.example.file_saver.dataaccess.itext.SaveIn
import com.example.file_saver.dataaccess.itext.StreamResult
import com.example.file_saver.usecase.FileSaveController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val saveController: FileSaveController =
        FileSaveController.getInstance(getApplication())

    val downloadPendingJob: LiveData<Boolean> = MutableLiveData(false)
    val pdfSaveResult: LiveData<StreamResult> = MutableLiveData(object : StreamResult {})

    var first = true

    fun getPdfStream() {
        val name = if (first) "Meet".also { first = false } else "Miyani".also { first = true }
        startStreamJob(pdfSaveResult.asMutable()) {
            saveController.getPdfFileStream(
                PdfContent(
                    name,
                    "pdf",
                    "application/pdf",
                    "TestLibs",
                    saveIn = SaveIn.DOCUMENTS
                )
            )
        }
    }

    private fun startStreamJob(
        resultData: MutableLiveData<StreamResult>,
        function: suspend () -> StreamResult
    ) {
        if (downloadPendingJob.value == false) {
            GlobalScope.launch {
                downloadPendingJob.asMutable().postValue(true)
                withContext(Dispatchers.IO) {
                    resultData.postValue(function())
                }
                downloadPendingJob.asMutable().postValue(false)
            }
        }

    }

    private fun <T> LiveData<T>.asMutable() = this as MutableLiveData<T>

    private fun application() = getApplication<Application>()


}