package com.josh.tvhome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val apps = remember { loadApps(this) }
            // A home screen has nowhere to go back to.
            BackHandler(enabled = true) { }
            HomeScreen(apps = apps, onOpen = { startActivity(it.launch) })
        }
    }
}
