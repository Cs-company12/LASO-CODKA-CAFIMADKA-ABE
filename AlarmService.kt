package com.cs.jadwal

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.*
import android.os.*
import androidx.core.app.NotificationCompat

class AlarmService : Service() {
    companion object { const val CH = "alarm_ch"; const val NID = 7; var running = false }
    private var player: MediaPlayer? = null
    private var vib: Vibrator? = null
    private val stopper = Handler(Looper.getMainLooper())

    override fun onBind(i: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val label = intent?.getStringExtra("label") ?: "Shaqo"
        val text = intent?.getStringExtra("text") ?: ""
        val day = intent?.getStringExtra("day") ?: ""
        val time = intent?.getStringExtra("time") ?: ""
        createChannel()
        val full = Intent(this, AlarmActivity::class.java).apply {
            putExtras(intent ?: Intent()); flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullPi = PendingIntent.getActivity(this, 1, full, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stopPi = PendingIntent.getService(this, 2, Intent(this, AlarmService::class.java).setAction("STOP"), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        if (intent?.action == "STOP") { stopSelf(); return START_NOT_STICKY }

        val n = NotificationCompat.Builder(this, CH)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("⏰ Waqtigaaga shaqo — ${Scheduler.name(this)}")
            .setContentText("$day $time · $label · $text")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setFullScreenIntent(fullPi, true)
            .setContentIntent(fullPi)
            .addAction(0, "Waan ogahay", stopPi)
            .build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(NID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) else startForeground(NID, n)
        running = true
        play(); vibrate()
        try { startActivity(full) } catch (e: Exception) {}
        stopper.removeCallbacksAndMessages(null)
        stopper.postDelayed({ stopSelf() }, 3 * 60_000L)
        return START_NOT_STICKY
    }

    private fun play() {
        if (player != null) return
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try { am.setStreamVolume(AudioManager.STREAM_ALARM, am.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0) } catch (e: Exception) {}
        var uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                setDataSource(this@AlarmService, uri)
                isLooping = true; setVolume(1f, 1f); prepare(); start()
            }
        } catch (e: Exception) { player = null }
    }

    private fun vibrate() {
        vib = if (Build.VERSION.SDK_INT >= 31) (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
              else @Suppress("DEPRECATION") getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vib?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 300, 600, 300), 0))
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CH) != null) return
        val ch = NotificationChannel(CH, "Alaarmka shaqada", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Wacitaanka waqtiga shaqada"; setSound(null, null); enableVibration(true); setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(ch)
    }

    override fun onDestroy() {
        running = false
        try { player?.stop(); player?.release() } catch (e: Exception) {}
        player = null; vib?.cancel(); stopper.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
