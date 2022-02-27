package com.guorui.downloader.data

// https://stackoverflow.com/questions/67812248/elegant-way-of-handling-error-using-retrofit-kotlin-flow
// inspired by Teo, user of StackOverFlow

enum class DownloadStatus {
    INITIATING,
    DOWNLOADING,
    SUCCESS,
    ERROR
}

sealed class DownloadResult(
    val status: DownloadStatus,
    val total: Long,
    val downloaded: Long,
    val message: String?,
    val timeConsumed: Long
) {
    data class Initiating(val _message: String? = "") : DownloadResult(
        status = DownloadStatus.INITIATING,
        total = -1,
        downloaded = -1,
        message = null,
        timeConsumed = 0
    )

    data class Downloading(val _total: Long, val _downloaded: Long, val _timeConsumed: Long) :
        DownloadResult(
            status = DownloadStatus.DOWNLOADING,
            total = _total,
            downloaded = _downloaded,
            message = null,
            timeConsumed = _timeConsumed
        )

    data class SUCCESS(val _total: Long, val _timeConsumed: Long) : DownloadResult(
        status = DownloadStatus.SUCCESS,
        total = _total,
        downloaded = _total,
        message = null,
        timeConsumed = _timeConsumed
    )

    data class ERROR(val _message: String?) :
        DownloadResult(
            status = DownloadStatus.ERROR,
            total = -1,
            downloaded = -1,
            message = _message,
            timeConsumed = -1
        )
}