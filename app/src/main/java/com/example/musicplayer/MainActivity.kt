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
                putExtra("android.car.intent.extra.MEDIA_COMPONENT", android.content.ComponentName(this@MainActivity, MusicBrowserService::class.java).flattenToString())
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to standard music intent if MEDIA_TEMPLATE isn't found
            val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                addCategory(android.content.Intent.CATEGORY_APP_MUSIC)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(fallbackIntent)
        }

        finish()
    }
}
