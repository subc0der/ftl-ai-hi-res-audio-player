package com.ftl.hires.audioplayer.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily
import com.ftl.hires.audioplayer.utils.PermissionManager

/**
 * Permission Request Screen
 * Cyberpunk-themed screen for requesting storage permissions
 */
@Composable
fun PermissionScreen(
    permissionManager: PermissionManager,
    modifier: Modifier = Modifier
) {
    val hasPermissions by permissionManager.permissionsGranted

    LaunchedEffect(hasPermissions) {
        if (!hasPermissions) {
            // Check if we should show rationale or request directly
            permissionManager.checkPermissions()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack),
        contentAlignment = Alignment.Center
    ) {
        // Background cyberpunk glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SubcoderColors.ElectricBlue.copy(alpha = 0.1f),
                            SubcoderColors.NeonCyan.copy(alpha = 0.05f),
                            SubcoderColors.PureBlack
                        )
                    ),
                    RoundedCornerShape(150.dp)
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Permission icon
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = "Storage Permission",
                tint = SubcoderColors.ElectricBlue,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "STORAGE ACCESS REQUIRED",
                style = FTLAudioTypography.librarySection,
                color = SubcoderColors.ElectricBlue,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "FTL Hi-Res Audio Player needs access to your device storage to scan and play your music files.",
                style = MaterialTheme.typography.bodyLarge,
                color = SubcoderColors.NeonCyan.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Grant permission button
            Button(
                onClick = {
                    permissionManager.requestPermissions()
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SubcoderColors.ElectricBlue,
                    contentColor = SubcoderColors.PureBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GRANT ACCESS",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info text
            Text(
                text = "This permission is required to access your music library",
                style = MaterialTheme.typography.bodySmall,
                color = SubcoderColors.LightGrey,
                textAlign = TextAlign.Center
            )
        }
    }
}