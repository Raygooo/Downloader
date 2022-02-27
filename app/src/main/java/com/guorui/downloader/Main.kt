package com.guorui.downloader

import com.guorui.downloader.data.DownloadService
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

fun main() {
    runBlocking {
        val service = DownloadService()

        val deferred = async {
            return@async service.getList()
        }

        val list = deferred.await()
        val jobMap = ConcurrentHashMap<String, Job>()
        for (item in list) {
            if (item.packageName in jobMap.keys) continue
//            if (!item.packageName.contains("MonsterBall")) continue
//            jobMap[item.packageName] =
//                launch {
//                    service.downloadOrResume(
//                        item.downloadUrl,
//                        File("${item.title.replace(" ", "_")}.apk")
//                    ).collect {
//                        print("file[${item.title}]:")
//                        when (it) {
//                            is DownloadResult.Initiating -> println("init: ${it.message}")
//                            is DownloadResult.Downloading -> println("progress: ${it.downloaded * 100 / it.total}%")
//                            is DownloadResult.SUCCESS -> println("success: size = ${it.total}, time consumed = ${it.timeConsumed}")
//                            is DownloadResult.ERROR -> println("failed: ${it.message}")
//                        }
//                    }
//                }
            println("packageName:\t${item.packageName}")
            println("title:\t${item.title}")
            println("iconUrl:\t${item.iconUrl}")
            println("downloadUrl:\t${item.downloadUrl}")
            println("sizeBytes:\t${item.sizeBytes}")
        }
        delay(1000)
        jobMap.forEach { (s, job) -> if (s.contains("MonsterBall")) job.cancel() }
    }
}