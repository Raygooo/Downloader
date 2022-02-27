package com.guorui.downloader

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.guorui.downloader.data.DownloadTaskState
import com.guorui.downloader.ui.DownloadScreen
import com.guorui.downloader.ui.theme.DownloaderTheme

class MainActivity : ComponentActivity() {

    private val downloadViewModel by viewModels<DownloadViewModel> {
        DownloadViewModelFactory(applicationContext.filesDir)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DownloaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    DownloadActivityScreen(downloadViewModel = downloadViewModel)
                }
            }
        }
        downloadViewModel.refresh()
    }
}

@Composable
fun DownloadActivityScreen(downloadViewModel: DownloadViewModel) {
    val context = LocalContext.current
    val toastInfo = downloadViewModel.toastInfo
    if (toastInfo.isNotBlank()) {
        Toast.makeText(context, toastInfo, Toast.LENGTH_SHORT).show()
        downloadViewModel.toastShowed()
    }

    DownloadScreen(
        items = downloadViewModel.items,
        onRefreshClick = {
            downloadViewModel.refresh()
        },
        onItemButtonClick = {
            Log.i("onItemButtonClick", "item: ${it.title}")
            when (it.state) {
                DownloadTaskState.READY_TO_DOWNLOAD -> downloadViewModel.onDownloadStart(it)
                DownloadTaskState.DOWNLOADING -> downloadViewModel.onDownloadingCancel(it)
                DownloadTaskState.FINISHED -> downloadViewModel.onViewingFile(it)
                DownloadTaskState.ERROR -> downloadViewModel.onRetryStart(it)
                DownloadTaskState.WAITING -> downloadViewModel.onWaitingCancel(it)
            }
        },
        onClearClick = {
            downloadViewModel.clear()
        }
    )
}