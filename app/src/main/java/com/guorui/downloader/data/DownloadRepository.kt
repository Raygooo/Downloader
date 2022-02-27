package com.guorui.downloader.data


interface DownloadRepository {
    suspend fun getList()
}