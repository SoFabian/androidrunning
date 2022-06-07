package com.sofa.nerdrunning.heartrate

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.benasher44.uuid.Uuid
import com.juul.kable.Advertisement
import com.sofa.nerdrunning.bluetooth.BluetoothAdapterStatus
import com.sofa.nerdrunning.bluetooth.BluetoothScanViewModel
import com.sofa.nerdrunning.bluetooth.IfBluetoothSupported

@Composable
fun ScanHeartRateMonitorsView(
    onClickBondHR: (String) -> Unit,
    vm: BluetoothScanViewModel = hiltViewModel(),
) {
    val ads: List<Advertisement> by vm.advertisements.collectAsState()
    val context = LocalContext.current
    Column {
        IfBluetoothSupported(
            vm.bluetoothAdapterStatus(),
            "Bluetooth is not supported on this device.\nHR monitor will not be available.",
            MaterialTheme.typography.caption,
        ) {
            Button(onClick = {
                if (vm.bluetoothAdapterStatus() == BluetoothAdapterStatus.NotEnabled) {
                    Toast.makeText(context, "Bluetooth is not enabled", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    vm.startScan(Uuid.fromString(HEART_RATE_SERVICE))
                }
            }) {
                Text("Search HR monitors", color = MaterialTheme.colors.onPrimary)
            }
            HeartRateMonitorList(ads, onClickBondHR)
        }
    }
}

@Composable
fun HeartRateMonitorList(
    hrMonitors: List<Advertisement>,
    onClickBondHR: (String) -> Unit,
) {
    var hrAddress by rememberSaveable { mutableStateOf("") }
    hrMonitors.forEach {
        ProvideTextStyle(MaterialTheme.typography.subtitle2) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text((it.name ?: "No name") + " ")
                if (hrAddress == it.address) {
                    Text("Connected")
                } else {
                    Button(onClick = { hrAddress = it.address; onClickBondHR(it.address) }) {
                        Text("Connect", color = MaterialTheme.colors.onPrimary)
                    }
                }
            }
        }
    }
}
