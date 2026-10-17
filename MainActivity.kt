package com.cs.jadwal

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.speech.tts.TextToSpeech
import android.media.AudioManager
import android.media.AudioAttributes
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onDestroy() { tts?.shutdown(); super.onDestroy() }
    private lateinit var web: WebView
    private var tts: TextToSpeech? = null

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.settings.allowFileAccess = true
        volumeControlStream = AudioManager.STREAM_MUSIC
        tts = TextToSpeech(this) { st ->
            if (st == TextToSpeech.SUCCESS) { tts?.language = Locale("ar"); tts?.setSpeechRate(0.8f); tts?.setPitch(0.9f)
                tts?.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()) }
        }
        web.webViewClient = WebViewClient()
        web.addJavascriptInterface(Bridge(), "Android")
        web.loadUrl("file:///android_asset/index.html")
        askPermissions()
        Scheduler.schedule(this)
    }

    private fun askPermissions() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName))
            try { startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))) } catch (e: Exception) {}
    }

    inner class Bridge {
        @JavascriptInterface fun speak(t: String) {
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            try { am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0) } catch (e: Exception) {}
            val p = Bundle(); p.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            tts?.speak(t, TextToSpeech.QUEUE_FLUSH, p, "line")
        }
        @JavascriptInterface fun stopSpeak() { tts?.stop() }
        @JavascriptInterface fun setName(n: String) { Scheduler.setName(this@MainActivity, n) }
        @JavascriptInterface fun getName(): String = Scheduler.name(this@MainActivity)
        @JavascriptInterface fun nextText(): String = Scheduler.nextText(this@MainActivity)
        @JavascriptInterface fun test() {
            val i = Intent(this@MainActivity, AlarmService::class.java)
                .putExtra("label", "Tijaabo").putExtra("text", "Codka alaarmka").putExtra("day", "Maanta").putExtra("time", Scheduler.fmt(System.currentTimeMillis()))
            ContextCompat.startForegroundService(this@MainActivity, i)
        }
        @JavascriptInterface fun canExact(): Boolean {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        }
        @JavascriptInterface fun openExactSettings() {
            if (Build.VERSION.SDK_INT >= 31) runOnUiThread { try { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) } catch (e: Exception) {} }
        }
    }
}
