package com.cs.jadwal

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Scheduler {
    private const val REQ = 1001
    private const val REQ_SNOOZE = 1002

    fun name(ctx: Context): String = ctx.getSharedPreferences("jadwal", 0).getString("me", "") ?: ""
    fun setName(ctx: Context, n: String) { ctx.getSharedPreferences("jadwal", 0).edit().putString("me", n).apply(); schedule(ctx) }

    fun schedule(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pending(ctx, REQ, null)
        am.cancel(pi)
        val nx = Schedule.next(name(ctx), System.currentTimeMillis() + 1000) ?: return
        val pi2 = pending(ctx, REQ, Bundle(nx.shift.label, nx.shift.text, nx.dayName, fmt(nx.time)))
        setExact(am, nx.time, pi2, ctx)
    }

    fun snooze(ctx: Context, label: String, text: String, day: String, time: String) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pending(ctx, REQ_SNOOZE, Bundle(label, text, day, time))
        setExact(am, System.currentTimeMillis() + 5 * 60_000L, pi, ctx)
    }

    private fun setExact(am: AlarmManager, at: Long, pi: PendingIntent, ctx: Context) {
        val show = PendingIntent.getActivity(ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val canExact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        if (canExact) am.setAlarmClock(AlarmManager.AlarmClockInfo(at, show), pi)
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }

    data class Bundle(val label: String, val text: String, val day: String, val time: String)

    private fun pending(ctx: Context, req: Int, b: Bundle?): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java)
        if (b != null) { i.putExtra("label", b.label); i.putExtra("text", b.text); i.putExtra("day", b.day); i.putExtra("time", b.time) }
        return PendingIntent.getBroadcast(ctx, req, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun fmt(t: Long): String = SimpleDateFormat("HH:mm", Locale.US).format(Date(t))

    fun nextText(ctx: Context): String {
        val nx = Schedule.next(name(ctx), System.currentTimeMillis()) ?: return "Ma jirto"
        return "${nx.dayName} ${fmt(nx.time)} · ${nx.shift.label}"
    }
}
