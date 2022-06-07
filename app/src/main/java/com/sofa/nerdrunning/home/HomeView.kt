package com.sofa.nerdrunning.home

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.sofa.nerdrunning.heartrate.ScanHeartRateMonitorsView
import com.sofa.nerdrunning.liverun.LiveRun
import com.sofa.nerdrunning.liverun.RunScreen
import com.sofa.nerdrunning.liverun.toRunLocation
import com.sofa.nerdrunning.navigation.Navigation
import com.sofa.nerdrunning.navigation.Routes
import com.sofa.nerdrunning.ui.theme.NerdRunningTheme
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun HomeView(
    navigation: Navigation,
    vm: HomeViewModel = hiltViewModel(),
) {
    HomeMenu(
        { navigation.goTo(Routes.liveRun) },
        { navigation.goTo(Routes.listFinishedRuns) },
        vm::bondHR,
    )
}

@Composable
fun HomeMenu(
    onClickStartRun: () -> Unit,
    onClickListFinishedRuns: () -> Unit,
    onClickBondHR: (String) -> Unit,
) {
    Column(
        Modifier.fillMaxSize(),
        Arrangement.SpaceAround,
        Alignment.CenterHorizontally
    ) {
        Button(onClickStartRun) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Start running session")
        }
        Button(onClickListFinishedRuns) {
            Icon(Icons.Filled.List, contentDescription = "List finished runs")
        }
        ScanHeartRateMonitorsView(onClickBondHR)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
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
        Surface(color = MaterialTheme.colors.background) {
            RunScreen(
                LiveRun(
                    duration = Duration.between(start, LocalDateTime.now()),
                    distance = 1434,
                    locations = mutableListOf(loc.toRunLocation(start))
                )
            )
        }
    }
}
