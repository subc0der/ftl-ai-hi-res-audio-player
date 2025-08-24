package com.ftl.hires.audioplayer.presentation.screens.library.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.ftl.hires.audioplayer.data.repository.MediaScannerRepository
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography
import com.ftl.hires.audioplayer.presentation.theme.OrbitronFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryEmptyState(
    isScanning: Boolean,
    scanProgress: MediaScannerRepository.ScanProgress?,
    onStartScan: () -> Unit,
    onStartQuickScan: () -> Unit,
    onStopScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        // Neural network pattern background
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SubcoderColors.ElectricBlue.copy(alpha = 0.1f),
                            SubcoderColors.NeonCyan.copy(alpha = 0.05f)
                        )
                    ),
                    RoundedCornerShape(100.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SubcoderColors.NeonCyan.copy(alpha = 0.5f),
                            SubcoderColors.ElectricBlue.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(100.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                ScanProgressIndicator(scanProgress = scanProgress)
            } else {
                Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = "Empty library",
                    tint = SubcoderColors.ElectricBlue.copy(alpha = 0.7f),
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title and description
        AnimatedContent(
            targetState = isScanning,
            transitionSpec = {
                slideInVertically { it } + fadeIn() with
                slideOutVertically { -it } + fadeOut()
            }
        ) { scanning ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (scanning) "SCANNING AUDIO MATRIX" else "AUDIO ARCHIVE EMPTY",
                    style = FTLAudioTypography.librarySection,
                    color = SubcoderColors.ElectricBlue,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (scanning) {
                        scanProgress?.let { progress ->
                            when {
                                progress.isComplete -> "Scan completed successfully"
                                progress.error != null -> "Error: ${progress.error}"
                                progress.currentFile.isNotEmpty() -> {
                                    val percentage = if (progress.totalFiles > 0) {
                                        (progress.filesScanned.toFloat() / progress.totalFiles * 100).toInt()
                                    } else 0
                                    "Processing: ${progress.currentFile}\n$percentage% (${ progress.filesScanned}/${progress.totalFiles})"
                                }
                                else -> "Initializing neural scan protocols..."
                            }
                        } ?: "Initializing scan..."
                    } else {
                        "No audio files detected in the system matrix.\nInitiate scan protocol to populate the archive."
                    },
                    style = FTLAudioTypography.settingDescription,
                    color = SubcoderColors.LightGrey,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action buttons
        AnimatedContent(
            targetState = isScanning,
            transitionSpec = {
                slideInVertically { it } + fadeIn() with
                slideOutVertically { -it } + fadeOut()
            }
        ) { scanning ->
            if (scanning) {
                // Scan in progress - show progress and stop button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    scanProgress?.let { progress ->
                        if (progress.totalFiles > 0 && !progress.isComplete) {
                            LinearProgressIndicator(
                                progress = progress.filesScanned.toFloat() / progress.totalFiles,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = SubcoderColors.ElectricBlue,
                                trackColor = SubcoderColors.DarkGrey
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onStopScan,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SubcoderColors.WarningOrange
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    SubcoderColors.WarningOrange,
                                    SubcoderColors.WarningOrange.copy(alpha = 0.5f)
                                )
                            )
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TERMINATE SCAN",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Not scanning - show scan options
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onStartScan,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SubcoderColors.ElectricBlue,
                            contentColor = SubcoderColors.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INITIATE FULL SCAN",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onStartQuickScan,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SubcoderColors.NeonCyan
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    SubcoderColors.NeonCyan,
                                    SubcoderColors.ElectricBlue
                                )
                            )
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "QUICK SCAN UPDATE",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Help text
        if (!isScanning) {
            Text(
                text = "Supported formats: FLAC, DSD, WAV, APE, MP3, AAC, OGG",
                style = FTLAudioTypography.caption,
                color = SubcoderColors.MediumGrey,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScanProgressIndicator(
    scanProgress: MediaScannerRepository.ScanProgress?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = SubcoderColors.ElectricBlue,
            trackColor = SubcoderColors.DarkGrey,
            strokeWidth = 3.dp,
            progress = scanProgress?.let { progress ->
                if (progress.totalFiles > 0) {
                    progress.filesScanned.toFloat() / progress.totalFiles
                } else 0f
            } ?: 0f
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = scanProgress?.let { progress ->
                if (progress.totalFiles > 0) {
                    "${progress.filesScanned}/${progress.totalFiles}"
                } else {
                    "${progress.filesScanned}"
                }
            } ?: "0",
            style = FTLAudioTypography.bodySmall,
            color = SubcoderColors.NeonCyan,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}