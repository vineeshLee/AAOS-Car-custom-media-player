package com.example.musicplayer

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.widget.Toast.makeText(
            this, 
            "Opening Native Media Center. Select 'Music Player' from the App Selector (top left).", 
            android.widget.Toast.LENGTH_LONG
        ).show()

        try {
            val intent = android.content.Intent("android.car.intent.action.MEDIA_TEMPLATE").apply {
                putExtra("android.car.intent.extra.MEDIA_COMPONENT", android.content.ComponentName(this@MainActivity, VideoCarAppService::class.java).flattenToString())
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for Video App
            val fallbackIntent = android.content.Intent("androidx.car.app.action.NAVIGATE")
            startActivity(fallbackIntent)
        }

        finish()
    }
}
