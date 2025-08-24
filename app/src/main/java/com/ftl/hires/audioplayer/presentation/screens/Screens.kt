package com.ftl.hires.audioplayer.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography

@Composable
fun CommandCenterScreen(
    onNavigateToAudioBridge: () -> Unit,
    onNavigateToAudioArchive: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "COMMAND CENTER",
                style = FTLAudioTypography.librarySection,
                color = SubcoderColors.Cyan,
                textAlign = TextAlign.Center
            )
            Text(
                text = "System control hub - Under construction",
                color = SubcoderColors.LightGrey,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AudioMatrixScreen(
    onPresetSelected: (String) -> Unit,
    onCustomEQSaved: (Map<String, Float>) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "AUDIO MATRIX",
                style = FTLAudioTypography.librarySection,
                color = SubcoderColors.Cyan,
                textAlign = TextAlign.Center
            )
            Text(
                text = "32-band parametric EQ control - Under construction",
                color = SubcoderColors.LightGrey,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SystemConfigScreen(
    onAudioSettingsChanged: (Map<String, Any>) -> Unit,
    onThemeChanged: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "SYSTEM CONFIG",
                style = FTLAudioTypography.librarySection,
                color = SubcoderColors.Cyan,
                textAlign = TextAlign.Center
            )
            Text(
                text = "System configuration panel - Under construction",
                color = SubcoderColors.LightGrey,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AudioBridgeScreen(
    onNavigateToAudioMatrix: () -> Unit,
    onNavigateToAudioArchive: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "AUDIO BRIDGE",
                style = FTLAudioTypography.librarySection,
                color = SubcoderColors.Cyan,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Now Playing control - Under construction",
                color = SubcoderColors.LightGrey,
                textAlign = TextAlign.Center
            )
        }
    }
}