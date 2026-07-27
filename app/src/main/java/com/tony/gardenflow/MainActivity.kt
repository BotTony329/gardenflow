package com.tony.gardenflow

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import com.tony.gardenflow.ui.navigation.GardenFlowNavHost
import com.tony.gardenflow.ui.theme.GardenFlowTheme
import com.tony.gardenflow.util.GardenText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.navigationBarColor = Color.rgb(250, 246, 240)
        window.statusBarColor = Color.rgb(250, 246, 240)
        setContent {
            val languageVm: AppLanguageViewModel = hiltViewModel()
            val languageCode by languageVm.languageCode.collectAsState()
            LaunchedEffect(languageCode) {
                GardenText.setLanguage(languageCode)
            }
            GardenFlowTheme {
                if (Build.VERSION.SDK_INT >= 33) {
                    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
                    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                }
                GardenFlowNavHost()
            }
        }
    }
}
