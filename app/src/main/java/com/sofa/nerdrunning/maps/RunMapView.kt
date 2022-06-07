package com.sofa.nerdrunning.maps

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.sofa.nerdrunning.liverun.RunLocation
import com.sofa.nerdrunning.liverun.toLatLng

@Composable
fun RunMapView(
    location: RunLocation,
    locations: List<LatLng> = emptyList(),
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location.toLatLng(), InitialZoom)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    ) {
        Polyline(locations)
    }
}

private const val InitialZoom = 15f
