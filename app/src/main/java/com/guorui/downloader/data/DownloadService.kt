package com.guorui.downloader.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.io.FileOutputStream


/**
 * FileDownloader Class
 *
 * Inspired by Pikoko from StackOverFlow [https://stackoverflow.com/questions/39915855/pause-and-resume-downloads-using-retrofit]
 */
class DownloadService {
    private val fileDownloadService by lazy {
        Retrofit.Builder()
            .baseUrl("https://apks.dev.al-array.com/")
            .build()
            .create(FileDownloadService::class.java)
    }

    private val downloadListService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.dev.al-array.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DownloadListService::class.java)
    }

    suspend fun getList(): List<Download> {
        val response = downloadListService.getList()
        return if (response.isSuccessful) {
            response.body()!!
        } else emptyList()
    }

    fun downloadOrResume(
        url: String,
        file: File,
        interval: Long = 100L
    ): Flow<DownloadResult> {
        val startFrom: Long
        val headers: HashMap<String, String> = HashMap()
        if (file.exists()) {
            startFrom = file.length()
            headers["Range"] = "bytes=${startFrom}-"
        }
        return download(url, file, headers, interval)
    }

    fun download(
        url: String,
        file: File,
        headers: HashMap<String, String> = HashMap(),
        interval: Long = 100L
    ): Flow<DownloadResult> {
        return flow {
            val start = System.currentTimeMillis()
            emit(DownloadResult.Initiating("initializing download at ${file.length()} bytes"))
            val response = fileDownloadService.downloadFile(fileUrl = url, headers = headers)
            if (!response.isSuccessful) {
                emit(DownloadResult.ERROR("HTTP ERROR: code[${response.code()}]"))
                return@flow
            }

            val responseBody = response.body()!!
            val total = responseBody.contentLength() + file.length()
            val inputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(file, true)
            try {
                inputStream.use { input ->
                    outputStream.use output@{
                        var read: Int
                        val buffer = ByteArray(8 * 1024)
                        var lastEmitDownloadingTime = 0L
                        while (input.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastEmitDownloadingTime > interval) {
                                emit(DownloadResult.Downloading(total, file.length(), currentTime - start))
                                lastEmitDownloadingTime = System.currentTimeMillis()
                            }
                        }
                        outputStream.flush()
                    }
                }
            } catch (e: Throwable) {
                emit(DownloadResult.ERROR("Exception: ${e.message}"))
                return@flow
            }
            if (file.length() == total) {
                emit(DownloadResult.SUCCESS(total, System.currentTimeMillis() - start))
            } else {
                emit(DownloadResult.ERROR("fileSize[${file.length()}] does not match content length[${total}]"))
            }
        }.flowOn(Dispatchers.IO)
    }
}

interface FileDownloadService {
    @Streaming
    @GET
    suspend fun downloadFile(
        @Url fileUrl: String,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}

interface DownloadListService {
    @GET("demo/1.0/apps")
    suspend fun getList(): Response<List<Download>>
}

