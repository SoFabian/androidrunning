package com.sofa.nerdrunning.finishedrun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sofa.nerdrunning.R
import com.sofa.nerdrunning.confirmation.ConfirmDialog
import com.sofa.nerdrunning.formats.format
import com.sofa.nerdrunning.liverun.RunPartInfo
import com.sofa.nerdrunning.loading.Loaded
import com.sofa.nerdrunning.loading.Loading
import com.sofa.nerdrunning.loading.WaitScreen
import com.sofa.nerdrunning.maps.RunMapView
import com.sofa.nerdrunning.navigation.Navigation
import com.sofa.nerdrunning.navigation.Routes
import com.sofa.nerdrunning.persistentrun.RunId
import java.time.format.DateTimeFormatter

private val tabTitles =
    listOf(R.string.text_tabRunMainInfo, R.string.text_tabInterval, R.string.text_tabMap)

@Composable
fun FinishedRunView(
    runId: RunId,
    navigation: Navigation,
    vm: FinishedRunViewModel = hiltViewModel(),
    goBack: () -> Unit = { navigation.goBackTo(Routes.listFinishedRuns) },
) {
    LaunchedEffect(runId) {
        vm.loadRun(runId)
    }
    val finishedRunLoadingStatus = vm.finishedRunLoadingStatus.collectAsState()
    val deletedRunLoadingStatus = vm.deletedRunLoadingStatus.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    when (val finishedRunLoading = finishedRunLoadingStatus.value) {
        is Loading -> {
            WaitScreen(goBack)
        }
        is Loaded -> {
            when (deletedRunLoadingStatus.value) {
                DeletionStatus.NOT_TRIGGERED -> {
                    FinishedRunScreen(
                        finishedRunLoading.value,
                        { showConfirmDialog = true },
                        goBack
                    )
                }
                DeletionStatus.DELETING -> {
                    WaitScreen()
                }
                DeletionStatus.DELETED -> {
                    goBack()
                }
            }
        }
    }
    if (showConfirmDialog) {
        ConfirmDialog(title = "Confirm deletion", text = "Are you sure want to delete this run?",
            onConfirm = {
                showConfirmDialog = false
                vm.deleteRun(runId)
            },
            onDismiss = { showConfirmDialog = false })
    }
}

enum class DeletionStatus {
    NOT_TRIGGERED,
    DELETING,
    DELETED,
}

@Composable
fun FinishedRunScreen(
    run: FinishedRun,
    onClickDeleteRun: (() -> Unit)? = null,
    goBack: () -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    Column {
        TabRow(selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(text = { Text(stringResource(title), color = MaterialTheme.colors.onPrimary) },
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> FinishedRunMainInfoTab(run)
            1 -> FinishedIntervalTab(run)
            2 -> RunMapView(run.intervals.first().start, run.locations)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(goBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "List finished runs")
            }
            if (onClickDeleteRun != null) {
                Button(onClickDeleteRun) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete finished run")
                }
            }
        }
    }
}

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("E, dd.MM.yyyy HH:mm")

@Composable
fun ColumnScope.FinishedRunMainInfoTab(run: FinishedRun) {
    Column(
        Modifier
            .weight(1f)
            .fillMaxWidth(),
        Arrangement.SpaceAround,
        Alignment.CenterHorizontally
    ) {
        Text(run.startDateTime.format(dateTimeFormatter))
        Text(run.duration.format())
        RunPartInfo(run.distance.toString(), R.string.text_distance)
        RunPartInfo(run.meanPace.format(), R.string.text_meanPace)
        if (run.hasHeartRate) {
            RunPartInfo(run.meanHeartRate.toString(), R.string.text_meanHeartRate)
        }
        Row {
            RunPartInfo(run.altitudeUp.toString(), R.string.text_altitudeUp)
            RunPartInfo(" " + run.altitudeDown.toString(), R.string.text_altitudeDown)
        }
    }
}

private val columnWidths = listOf(.15f, .15f, .14f, .14f, .14f, .14f, .14f)
private val intervalTableHeaders = listOf(
    R.string.text_distance,
    R.string.text_distance,
    R.string.text_intervalPace,
    R.string.text_kmPace,
    R.string.text_meanHeartRate,
    R.string.text_altitudeUp,
    R.string.text_altitudeDown,
)
private val intervalTableHeadersWithoutHeartRate =
    intervalTableHeaders.filter { it != R.string.text_meanHeartRate }

@Composable
fun ColumnScope.FinishedIntervalTab(run: FinishedRun) {
    val headers =
        if (run.hasHeartRate) intervalTableHeaders else intervalTableHeadersWithoutHeartRate
    LazyColumn(
        Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        item {
            ProvideTextStyle(MaterialTheme.typography.caption) {
                Row(Modifier.background(Color.Gray)) {
                    headers.forEachIndexed { index, header ->
                        TableCell(stringResource(header), columnWidths[index])
                    }
                }
            }
        }
        items(run.intervals) {
            ProvideTextStyle(MaterialTheme.typography.subtitle1) {
                Row {
                    TableCell(it.startDistanceInRun.toString(), columnWidths[0])
                    TableCell(it.distance.toString(), columnWidths[1])
                    TableCell(it.duration.format(), columnWidths[2])
                    TableCell(it.kmPace.format(), columnWidths[3])
                    if (run.hasHeartRate) {
                        TableCell(it.meanHeartRate.toString(), columnWidths[4])
                    }
                    TableCell(it.altitudeUp.toString(), columnWidths[5])
                    TableCell(it.altitudeDown.toString(), columnWidths[6])
                }
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float
) {
    Text(
        text,
        Modifier
            .weight(weight)
            .padding(8.dp)
    )
}
