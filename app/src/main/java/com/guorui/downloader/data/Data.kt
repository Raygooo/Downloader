package com.guorui.downloader.data

import com.google.gson.annotations.SerializedName
import java.util.*


data class Download(
    @SerializedName("package_name") val packageName: String,
    @SerializedName("title") val title: String,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("download_url") val downloadUrl: String,
    @SerializedName("size_bytes") val sizeBytes: Long,
)


data class DownloadItem(
    val packageName: String,
    val title: String,
    val iconUrl: String,
    val downloadUrl: String,
    val state: DownloadTaskState = DownloadTaskState.READY_TO_DOWNLOAD,
    val downloaded: Long = 0,
    val total: Long = 0,
    val timeConsumed: Long = 0,
    val id: UUID = UUID.randomUUID()
)

enum class DownloadTaskState(val buttonString: String) {
    READY_TO_DOWNLOAD("START"),
    WAITING("CANCEL(QUEUED)"),
    DOWNLOADING("CANCEL"),
    FINISHED("VIEW"),
    ERROR("RETRY"),
}