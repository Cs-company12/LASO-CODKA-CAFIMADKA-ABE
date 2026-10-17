package com.cs.jadwal

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true)
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(this, null)
        } else @Suppress("DEPRECATION") window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_alarm)
        val me = Scheduler.name(this)
        val label = intent.getStringExtra("label") ?: ""; val text = intent.getStringExtra("text") ?: ""
        val day = intent.getStringExtra("day") ?: ""; val time = intent.getStringExtra("time") ?: ""
        findViewById<TextView>(R.id.avatar).text = me.take(1).ifEmpty { "!" }
        findViewById<TextView>(R.id.name).text = me
        findViewById<TextView>(R.id.shift).text = "$label · $text"
        findViewById<TextView>(R.id.time).text = time
        findViewById<TextView>(R.id.day).text = day
        findViewById<Button>(R.id.ok).setOnClickListener { stopService(Intent(this, AlarmService::class.java)); finish() }
        findViewById<Button>(R.id.snooze).setOnClickListener {
            stopService(Intent(this, AlarmService::class.java)); Scheduler.snooze(this, label, text, day, time); finish()
        }
    }
    @Deprecated("Deprecated in Java") override fun onBackPressed() { /* ha xirin alaarmka badhanka dib */ }
}
