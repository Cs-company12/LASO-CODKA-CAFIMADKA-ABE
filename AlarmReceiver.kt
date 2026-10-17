package com.cs.jadwal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val svc = Intent(ctx, AlarmService::class.java).putExtras(intent)
        ContextCompat.startForegroundService(ctx, svc)
        Scheduler.schedule(ctx) // dhig alaarmka xiga
    }
}
