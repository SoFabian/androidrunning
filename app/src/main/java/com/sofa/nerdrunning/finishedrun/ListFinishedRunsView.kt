package com.sofa.nerdrunning.finishedrun

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sofa.nerdrunning.R
import com.sofa.nerdrunning.formats.format
import com.sofa.nerdrunning.images.ImageLoaderAccessor
import com.sofa.nerdrunning.loading.LoadingItem
import com.sofa.nerdrunning.log.logDebug
import com.sofa.nerdrunning.navigation.Navigation
import com.sofa.nerdrunning.navigation.Routes
import com.sofa.nerdrunning.persistentrun.Run
import com.sofa.nerdrunning.persistentrun.RunId
import java.time.format.DateTimeFormatter

@Composable
fun ListFinishedRunsView(
    navigation: Navigation,
    vm: ListFinishedRunsViewModel = hiltViewModel(),
) {
    val runs = Pager(
        PagingConfig(pageSize = 10), pagingSourceFactory = vm::loadFinishedRuns
    ).flow.collectAsLazyPagingItems()
    ListFinishedRunsScreen(runs, navigation::goBackToHome) { run ->
        navigation.goTo(Routes.finishedRun(run.id))
    }
}

@Composable
fun ListFinishedRunsScreen(
    runs: LazyPagingItems<Run>,
    goBack: () -> Unit,
    goToRun: (Run) -> Unit,
) {
    Column(verticalArrangement = Arrangement.SpaceBetween) {
        runs.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    LoadingItem()
                }
                loadState.append is LoadState.Loading -> {
                    LoadingItem()
                }
                loadState.refresh is LoadState.Error -> {
                    Text("Error loading runs.")
                }
                loadState.append is LoadState.Error -> {
                    Text("Error loading runs.")
                }
            }
        }
        LazyColumn(
            Modifier
                .weight(1f)
                .padding(bottom = 5.dp)
                .padding(horizontal = 20.dp)
        ) {
            items(runs) { run ->
                run?.let { PersistentRunItem(it, goToRun) }
            }
        }
        FloatingActionButton(goBack, Modifier.padding(5.dp)) {
            Icon(Icons.Filled.Home, contentDescription = "Go to home")
        }
    }
}

@Composable
fun PersistentRunItem(
    run: Run,
    goToRun: (Run) -> Unit,
) {
    ProvideTextStyle(MaterialTheme.typography.subtitle2) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(10.dp))
                .clickable { goToRun(run) },
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StaticMapsImage(run.id)
            Column(horizontalAlignment = Alignment.End) {
                Text(run.startDateTime.format(dateTimeFormatter))
                Text(run.duration.format())
                Row {
                    Text(run.distance.toString())
                    Text(" " + stringResource(R.string.text_distance))
                }
                Row {
                    Text(run.meanPace.format())
                    Text(" " + stringResource(R.string.text_meanPace))
                }
                Text(run.numberOfIntervals.toString() + " intervals")
            }
        }
    }
}

@Composable
fun StaticMapsImage(
    runId: RunId,
    context: Context = LocalContext.current,
    imageRequestBuilder: ImageRequest.Builder = ImageRequest.Builder(context),
    imageLoader: ImageLoader = ImageLoaderAccessor.get(context),
) {
    AsyncImage(
        model = imageRequestBuilder
            .data(runId)
            .crossfade(true)
            .build(),
        contentDescription = "Map of run $runId",
        imageLoader = imageLoader,
        placeholder = painterResource(R.drawable.ic_stat_runner),
        error = painterResource(R.drawable.ic_stat_runner),
        onError = { error ->
            logDebug(
                "StaticMapsImage", "Error loading persistent image. " +
                        "Cause: ${error.result.throwable.message}. ${error.result.throwable.stackTraceToString()}"
            )
        },
    )
}

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("E dd.MM.yyyy HH:mm")
