package com.sofa.nerdrunning.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingItem() {
    CircularProgressIndicator(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentWidth(
                Alignment.CenterHorizontally
            )
    )
}

@Composable
fun WaitScreen(
    goBack: (() -> Unit)? = null,
) {
    Column {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            Arrangement.SpaceAround,
            Alignment.CenterHorizontally
        ) {
            LoadingItem()
        }
        if (goBack != null) {
            FloatingActionButton(goBack, Modifier.padding(5.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "go back")
            }
        }
    }
}
