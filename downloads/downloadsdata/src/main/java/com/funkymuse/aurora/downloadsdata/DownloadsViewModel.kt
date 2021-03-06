package com.funkymuse.aurora.downloadsdata

import android.app.Application
import android.os.FileObserver
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.funkymuse.aurora.commonextensions.downloads
import com.funkymuse.aurora.downloadsdestination.BOOK_ID_PARAM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Created by funkymuse on 8/12/21 to long live and prosper !
 */

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val bookName get() = savedStateHandle.get<String>(BOOK_ID_PARAM)
    private val highlightDownloadedBookData : Channel<String?> = Channel(Channel.BUFFERED)
    val highlightDownloadedBook = highlightDownloadedBookData.receiveAsFlow()

    private val downloads: File = application.downloads()

    private val filesData: MutableStateFlow<DownloadsModel> =
        MutableStateFlow(DownloadsModel.Loading)
    val files = filesData.asStateFlow()

    @Suppress("DEPRECATION")
    private val fileObserver = object : FileObserver(downloads.path, ALL_EVENTS) {
        override fun onEvent(event: Int, path: String?) {
            if (event == CLOSE_WRITE || event == DELETE || event == DELETE_SELF) {
                loadDownloads()
            }
        }
    }

    init {
        viewModelScope.launch {
            highlightDownloadedBookData.send(bookName)
        }
    }

    private fun loadDownloads() {
        filesData.value = DownloadsModel.Loading
        val filesList = downloads.listFiles()?.toList()?.map {
            FileModel(
                it.nameWithoutExtension.substringBefore("-").trim(),
                it.length(),
                it.extension,
                it.nameWithoutExtension.substringAfter("(").removeSuffix(")"),
                it
            )
        } ?: emptyList()
        val removedDups = filesList.distinctBy { it.bookId }
        filesData.value =
            if (removedDups.isEmpty()) DownloadsModel.Empty else DownloadsModel.Success(removedDups)
    }

    init {
        fileObserver.startWatching()
        loadDownloads()
    }

    override fun onCleared() {
        super.onCleared()
        fileObserver.stopWatching()
    }

    fun retry() {
        loadDownloads()
    }
}