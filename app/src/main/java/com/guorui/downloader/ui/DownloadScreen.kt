package com.guorui.downloader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.guorui.downloader.data.DownloadItem
import com.guorui.downloader.data.DownloadTaskState
import com.guorui.downloader.data.example

@Composable
fun DownloadScreen(
    items: List<DownloadItem>,
    onRefreshClick: () -> Unit,
    onClearClick: () -> Unit,
    onItemButtonClick: (DownloadItem) -> Unit
) {
    Column {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            items(items = items) {
                DownloadTaskRow(
                    download = it,
                    onItemButtonClick = onItemButtonClick
                )
            }
        }
        Button(
            onClick = onRefreshClick,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Refresh")
        }
        Button(
            onClick = onClearClick,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Clear")
        }
    }
}

@Composable
fun DownloadTaskRow(
    download: DownloadItem,
    onItemButtonClick: (DownloadItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Image(
                painter = rememberImagePainter(
                    data = download.iconUrl,
                    builder = {
                        crossfade(true)
                    }
                ),
                contentDescription = download.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f).height(64.dp)) {
                Text(
                    text = download.packageName,
                    fontSize = 12.sp
                )
                Text(
                    text = download.title,
                    fontSize = 16.sp
                )
            }
            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                Button(onClick = { onItemButtonClick(download) }) {
                    Text(text = download.state.buttonString)
                }
            }
        }
        DownloadProgress(download.total, download.downloaded, download.state)
    }
}


@Composable
fun DownloadProgress(
    total: Long,
    downloaded: Long,
    state: DownloadTaskState
) {
    val modifier = Modifier.fillMaxWidth()
    val isDownloading = state == DownloadTaskState.DOWNLOADING
    if (total <= 0L || downloaded <= 0L) {
        if (isDownloading) LinearProgressIndicator(modifier = modifier)
        else LinearProgressIndicator(progress = 0F, modifier = modifier)
    } else {
        LinearProgressIndicator(
            progress = downloaded.toFloat() / total,
            modifier = modifier
        )
    }
}

@Preview(name = "download row")
@Composable
fun DownloadTaskItemPreview() {
    val downloadTask = DownloadItem(
        packageName = "com.applovin.abc",
        title = "AppLovesIt",
        iconUrl = "https://play-lh.googleusercontent.com/XR8M2NDT-0PxcBtyaln0dvWh0COzvZAHWuxV-Za5XicEM7JMRVqNlvFmPp5Ub_rA0FQ=s180",
        downloadUrl = "https://apks.dev.al-array.com/com.YsoCorp.MonsterBall/v0.3.4/com.YsoCorp.MonsterBall_34v_armeabi-v7a_0dpi_21aal.zip?Expires=1646485353&KeyName=apk-url-sign&Signature=RsQ3D86iCjgjU6UqPClqRJzhXaQ="
    )
    DownloadTaskRow(download = downloadTask) {}
}

@Preview(name = "download screen")
@Composable
fun DownloadScreenPreview() {
    val downloadItems = example
    DownloadScreen(
        items = downloadItems,
        onRefreshClick = { /*TODO*/ },
        onItemButtonClick = {},
        onClearClick = {})
}