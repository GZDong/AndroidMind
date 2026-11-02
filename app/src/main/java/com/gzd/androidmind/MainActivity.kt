package com.gzd.androidmind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gzd.androidmind.ui.AndroidMindApp
import com.gzd.androidmind.ui.AndroidMindTheme

/**
 * @Author GZDong(A80788)
 * @Date 2026/11/2
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidMindTheme {
                AndroidMindApp()
            }
        }
    }
}
