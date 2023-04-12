package com.sofa.nerdrunning.liverun

import android.location.Location
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.sofa.nerdrunning.R
import com.sofa.nerdrunning.confirmation.ConfirmDialog
import com.sofa.nerdrunning.finishedrun.FinishedRunScreen
import com.sofa.nerdrunning.formats.format
import com.sofa.nerdrunning.loading.LoadingItem
import com.sofa.nerdrunning.log.logDebug
import com.sofa.nerdrunning.maps.RunMapView
import com.sofa.nerdrunning.navigation.Navigation
import com.sofa.nerdrunning.ui.theme.NerdRunningTheme
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun LiveRunView(
    navigation: Navigation,
    vm: LiveRunViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    DisposableEffect(lifecycleOwner) {
        vm.startLiveRun()
        onDispose { vm.unbindLiveRun() }
    }
    val bound by vm.liveRunServiceBoundFlow.collectAsState(false)
    val finishedRun by vm.finishedRunFlow.collectAsState()
    if (finishedRun != null) {
        finishedRun?.let {
            FinishedRunScreen(it) {
                navigation.goBackToHome()
            }
        }
    } else if (bound) {
        val run by vm.liveRunFlow().collectAsState(LiveRun())
        logDebug("LiveRunView", "render tick ${run.tick} @ ${run.duration.toMillis()} ms")
        if (run.tick < LiveRunFlow.START_COUNTDOWN) {
            Countdown(run.tick, LiveRunFlow.START_COUNTDOWN) {
                vm.cancelLiveRun()
                navigation.goBackToHome()
            }
        } else {
            RunScreen(run, vm::finishLiveRun, vm::intervalRequest)
        }
    } else {
        LoadingItem()
    }
}

@Composable
fun Countdown(
    tick: Int,
    max: Int,
    stopRun: () -> Unit
) {
    Column {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            Arrangement.SpaceAround,
            Alignment.CenterHorizontally
        ) {
            Text("-${max - tick}")
        }
        FloatingActionButton(stopRun, Modifier.padding(5.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Cancel start of running session")
        }
    }
}

private val tabTitles =
    listOf(R.string.text_tabRunMainInfo, R.string.text_tabInterval, R.string.text_tabMap)

@Composable
fun RunScreen(
    run: LiveRun,
    stopRun: (() -> Unit)? = null,
    nextInterval: ((IntervalTarget) -> Unit)? = null
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
            0 -> RunMainInfoTab(run, stopRun)
            1 -> IntervalTab(run, nextInterval)
            2 -> RunMapView(run.locations.lastOrNull() ?: RunLocation())
        }
    }
}

@Composable
fun RunMainInfoTab(run: LiveRun, stopRun: (() -> Unit)? = null) {
    Column {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            Arrangement.SpaceAround,
            Alignment.CenterHorizontally
        ) {
            Text(run.duration.format())
            RunPartInfo(run.distance.toString(), R.string.text_distance)
            var showLiveInfo by remember { mutableStateOf(true) }
            val changeShownInfo = { showLiveInfo = !showLiveInfo }
            if (showLiveInfo) {
                RunPartInfo(run.livePace.format(), R.string.text_livePace, changeShownInfo)
                if (run.liveHeartRate > 0) {
                    RunPartInfo(
                        run.liveHeartRate.toString(),
                        R.string.text_liveHeartRate,
                        changeShownInfo
                    )
                }
            } else {
                RunPartInfo(run.meanPace.format(), R.string.text_meanPace, changeShownInfo)
                if (run.hasHeartRate) {
                    RunPartInfo(
                        run.meanHeartRate.toString(),
                        R.string.text_meanHeartRate,
                        changeShownInfo
                    )
                }
            }
        }
        if (stopRun != null) {
            var showConfirmDialog by remember { mutableStateOf(false) }
            FloatingActionButton(onClick = { showConfirmDialog = true }, Modifier.padding(5.dp)) {
                Icon(Icons.Filled.Done, contentDescription = "Finish running session")
            }
            if (showConfirmDialog) {
                ConfirmDialog(title = "Confirm finish running",
                    text = "Are you sure want to finish your run?",
                    onConfirm = {
                        showConfirmDialog = false
                        stopRun()
                    },
                    onDismiss = { showConfirmDialog = false })
            }
            // Divider()
            // if (run.locations.isNotEmpty()) ShowLocation(run.locations.last())
        }
    }
}

@Composable
fun RunPartInfo(text: String, @StringRes captionId: Int, onClick: (() -> Unit)? = null) {
    Row(Modifier.clickable(onClick != null, onClick = onClick ?: {})) {
        Text(text, Modifier.alignByBaseline())
        Text(
            " " + stringResource(captionId),
            Modifier.alignByBaseline(),
            style = MaterialTheme.typography.body2
        )
    }
}

//@Composable
//fun ShowLocation(location: RunLocation) {
//    val dfOnePlace = DecimalFormat("0.#")
//    val dfSixPlaces = DecimalFormat("0.######")
//    ProvideTextStyle(MaterialTheme.typography.body2) {
//        Row(
//            Modifier.fillMaxWidth(),
//            Arrangement.SpaceEvenly
//        ) {
//            Row {
//                Text(dfSixPlaces.format(location.latitude))
//                Text(" " + stringResource(R.string.text_latitude))
//            }
//            Row {
//                Text(dfSixPlaces.format(location.longitude))
//                Text(" " + stringResource(R.string.text_longitude))
//            }
//        }
//        Row(
//            Modifier.fillMaxWidth(),
//            Arrangement.SpaceEvenly
//        ) {
//            Row {
//                Text(dfOnePlace.format(location.accuracy))
//                Text(" " + stringResource(R.string.text_accuracy))
//            }
//            Row {
//                Text(dfOnePlace.format(location.altitude))
//                Text(" " + stringResource(R.string.text_altitude))
//            }
//        }
//        Row(
//            Modifier.fillMaxWidth(),
//            Arrangement.SpaceEvenly
//        ) {
//            Row {
//                Text(dfOnePlace.format(location.speed))
//                Text(" " + stringResource(R.string.text_speed))
//            }
//            Row {
//                Text(dfOnePlace.format(location.speedAccuracy))
//                Text(" " + stringResource(R.string.text_speedAccuracy))
//            }
//        }
//    }
//}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreviewInterval() {
    val start = LocalDateTime.now()
        .minusMinutes(23)
        .minusSeconds(44)
    val loc = Location("").apply {
        latitude = 48.35325325
        longitude = 22.34344
        speed = 10.234f
        accuracy = 20.0f
        altitude = 280.0
    }
    NerdRunningTheme {
        IntervalTab(
            LiveRun(
                duration = Duration.between(start, LocalDateTime.now()),
                distance = 1434,
                intervals = mutableListOf(LiveRunInterval()),
                locations = mutableListOf(loc.toRunLocation(start))
            )
        )
    }
}

val defaultIntervalTargets = listOf(
    IntervalTarget(100, TargetUnit.METER),
    IntervalTarget(200, TargetUnit.METER),
    IntervalTarget(400, TargetUnit.METER),
    IntervalTarget(800, TargetUnit.METER),
    IntervalTarget(1000, TargetUnit.METER),
    IntervalTarget(1500, TargetUnit.METER),
    IntervalTarget(5, TargetUnit.MINUTE),
)

@Composable
fun IntervalTab(run: LiveRun, nextInterval: ((IntervalTarget) -> Unit)? = null) {
    Column {
        Row(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            Arrangement.SpaceAround
        ) {
            if (run.intervals.isNotEmpty()) {
                LastInterval(run.intervals.last(), run.hasHeartRate)
            }
            CurrentInterval(run.currentInterval)
        }
        if (nextInterval != null) {
            Box(modifier = Modifier
                .wrapContentSize(Alignment.BottomStart)
                .padding(5.dp)) {
                var expanded by remember { mutableStateOf(false) }
                Button(onClick = { expanded = true }, Modifier.padding(5.dp)) {
                    Text(
                        stringResource(R.string.text_startInterval),
                        color = MaterialTheme.colors.onPrimary
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    defaultIntervalTargets.forEach {
                        DropdownMenuItem(onClick = { nextInterval(it); expanded = false }) {
                            ShowIntervalTarget(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LastInterval(runInterval: LiveRunInterval, hasHeartRate: Boolean) {
    Column(
        Modifier.fillMaxHeight(),
        Arrangement.SpaceAround,
        Alignment.CenterHorizontally
    ) {
        ProvideTextStyle(MaterialTheme.typography.body2) {
            ShowIntervalTarget(runInterval.target)
            Text(runInterval.duration.format())
            Text(runInterval.distance.toString())
            Text(runInterval.pace.format())
            if (hasHeartRate) {
                Text(runInterval.heartRate.toString())
            }
        }
    }
}

@Composable
fun CurrentInterval(runInterval: LiveRunInterval) {
    Column(
        Modifier.fillMaxHeight(),
        Arrangement.SpaceAround,
        Alignment.CenterHorizontally
    ) {
        ShowIntervalTarget(runInterval.target)
        Text(runInterval.duration.format())
        RunPartInfo(runInterval.distance.toString(), R.string.text_distance)
        RunPartInfo(runInterval.pace.format(), R.string.text_intervalPace)
        if (runInterval.heartRate > 0) {
            RunPartInfo(runInterval.heartRate.toString(), R.string.text_meanHeartRate)
        }
    }
}

@Composable
fun ShowIntervalTarget(target: IntervalTarget) {
    Row {
        Text(target.targetValue.toString())
        Text(" " + stringResource(target.targetUnit.stringId()))
    }
}

fun TargetUnit.stringId(): Int =
    when (this) {
        TargetUnit.METER -> {
            R.string.text_meter
        }
        TargetUnit.MINUTE -> {
            R.string.text_minute
        }
        TargetUnit.SECOND -> {
            R.string.text_second
        }
    }
