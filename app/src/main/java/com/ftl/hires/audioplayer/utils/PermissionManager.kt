package com.ftl.hires.audioplayer.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: ComponentActivity) {
    
    internal val _permissionsGranted = mutableStateOf(false)
    val permissionsGranted: State<Boolean> = _permissionsGranted
    
    init {
        checkPermissions()
    }
    
    // Permission launcher will be set by the composable
    private var permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>? = null
    
    fun setPermissionLauncher(launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>) {
        permissionLauncher = launcher
    }
    
    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }
    
    fun checkPermissions() {
        val permissions = getRequiredPermissions()
        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
        _permissionsGranted.value = allGranted
    }
    
    fun requestPermissions() {
        val permissions = getRequiredPermissions()
        permissionLauncher?.launch(permissions)
    }
    
    companion object {
        fun hasStoragePermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}

@Composable
fun rememberPermissionManager(activity: ComponentActivity): PermissionManager {
    val permissionManager = remember { PermissionManager(activity) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionManager._permissionsGranted.value = permissions.values.all { it }
    }
    
    LaunchedEffect(permissionLauncher) {
        permissionManager.setPermissionLauncher(permissionLauncher)
    }
    
    return permissionManager
}