package com.guorui.downloader

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guorui.downloader.data.DownloadItem
import com.guorui.downloader.data.DownloadResult
import com.guorui.downloader.data.DownloadService
import com.guorui.downloader.data.DownloadTaskState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.math.roundToInt

class DownloadViewModel(private val fileDir: File) : ViewModel() {

    var items = mutableStateListOf<DownloadItem>()
        private set

    var toastInfo: String by mutableStateOf("")

    private var downloadingItems = mutableMapOf<UUID, Job>()

    private val queue = LinkedList<DownloadItem>()

    private val service: DownloadService = DownloadService()

    fun refresh() {
        items.clear()
        viewModelScope.launch {
            service.getList().forEach { download ->
                if (items.any { it.packageName == download.packageName }) return@forEach
                val file = File(fileDir, download.packageName)
                var startFrom = if (file.exists()) {
                    file.length()
                } else {
                    0L
                }
                Log.i("refresh", "startFrom = $startFrom")
                if (startFrom > download.sizeBytes) {
                    file.delete()
                    startFrom = 0L
                }
                items.add(
                    DownloadItem(
                        packageName = download.packageName,
                        title = download.title,
                        downloadUrl = download.downloadUrl,
                        iconUrl = download.iconUrl,
                        downloaded = startFrom,
                        total = download.sizeBytes,
                        state = if (startFrom == download.sizeBytes) DownloadTaskState.FINISHED else DownloadTaskState.READY_TO_DOWNLOAD
                    )
                )
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                deleteFile(fileDir)
                toastInfo = "All Files Cleared"
                refresh()
            }
        }
    }

    private fun deleteFile(file: File) {
        if (file.isDirectory) {
            val files = file.listFiles()
            var i = 0
            while (i < files.size) {
                val f = files[i]
                deleteFile(f)
                i++
            }
        } else if (file.exists()) {
            file.delete()
        }
    }

    fun onDownloadStart(item: DownloadItem) {
        Log.i("onDownloadStart", "name = ${item.title}")
        arrangeDownload(item)
    }


    fun onRetryStart(item: DownloadItem) {
        Log.i("onRetryStart", "name = ${item.title}")
        arrangeDownload(item)
    }

    private fun arrangeDownload(downloadItem: DownloadItem) {
        if (downloadItem.id in downloadingItems.keys) {
            Log.e("onDownloadStart", "${downloadItem.id} is already downloading")
            return
        }
        queue.offer(downloadItem)
        if (downloadingItems.size >= 2) {
            onItemStateChanged(
                downloadItem,
                state = DownloadTaskState.WAITING
            )
            return
        }
        executeQueuedDownload()
    }

    private fun executeQueuedDownload() {
        val item = queue.poll()
        item?.let { item ->
            downloadingItems[item.id] = viewModelScope.launch {
                service.downloadOrResume(
                    item.downloadUrl,
                    File(fileDir, item.packageName),
                    interval = 1000L / 60
                ).collect {
                    when (it) {
                        is DownloadResult.Initiating -> {
                            Log.i("collect", "init: ${it.message}")
                            onItemStateChanged(
                                item,
                                state = DownloadTaskState.DOWNLOADING
                            )
                        }
                        is DownloadResult.Downloading -> {
                            onItemStateChanged(
                                item,
                                state = DownloadTaskState.DOWNLOADING,
                                total = it.total,
                                downloaded = it.downloaded,
                                timeConsumed = it.timeConsumed
                            )
                        }
                        is DownloadResult.SUCCESS -> {
                            Log.i(
                                "collect",
                                "success: name = ${item.title} size = ${it.total}, time consumed = ${it.timeConsumed}"
                            )
                            onItemStateChanged(
                                item,
                                state = DownloadTaskState.FINISHED,
                                total = it.total,
                                timeConsumed = it.timeConsumed,
                                downloaded = it.downloaded
                            )
                            downloadingItems.remove(item.id)
                            executeQueuedDownload()
                        }
                        is DownloadResult.ERROR -> {
                            Log.i("collect", "failed: ${it.message}")
                            onItemStateChanged(
                                item,
                                state = DownloadTaskState.ERROR,
                                downloaded = 0L,
                                total = 0L,
                                timeConsumed = 0L
                            )
                            deleteFile(File(fileDir, item.packageName))
                            downloadingItems.remove(item.id)
                            executeQueuedDownload()
                        }
                    }
                }
            }
        }
    }

    fun onDownloadingCancel(item: DownloadItem) {
        Log.i("onDownloadingPause", "name = ${item.title}")
        val job = downloadingItems.remove(item.id)
        job?.cancel()
        deleteFile(File(fileDir, item.packageName))
        onItemStateChanged(
            item,
            state = DownloadTaskState.READY_TO_DOWNLOAD,
            total = 0L,
            downloaded = 0L,
            timeConsumed = 0L
        )
        executeQueuedDownload()
    }

    fun onWaitingCancel(item: DownloadItem) {
        queue.removeIf { item.id == it.id }
        deleteFile(File(fileDir, item.packageName))
        onItemStateChanged(
            item,
            state = DownloadTaskState.READY_TO_DOWNLOAD,
            total = 0L,
            downloaded = 0L,
            timeConsumed = 0L
        )
    }

    private fun onItemStateChanged(
        downloadItem: DownloadItem,
        state: DownloadTaskState = downloadItem.state,
        downloaded: Long = downloadItem.downloaded,
        total: Long = downloadItem.total,
        timeConsumed: Long = downloadItem.timeConsumed
    ) {
        for (i in items.indices) {
            if (items[i].id == downloadItem.id) {
                items[i] = downloadItem.copy(
                    downloaded = downloaded,
                    state = state,
                    total = total,
                    timeConsumed = timeConsumed
                )
            }
        }
    }

    fun onViewingFile(item: DownloadItem) {
        val file = File(fileDir, item.packageName)
        val fileSize = (file.length().toDouble() / 1000).roundToInt().toDouble() / 1000
        val info = "\nname = ${file.name}\nsize = ${fileSize}MB\ntime = ${item.timeConsumed}"
        toastInfo = info
    }

    fun toastShowed() {
        toastInfo = ""
    }

}

class DownloadViewModelFactory(private val fileDir: File) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DownloadViewModel::class.java)) {
            return DownloadViewModel(fileDir) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}