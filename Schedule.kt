package com.cs.jadwal

import java.util.Calendar

object Schedule {
    val DAYS = arrayOf("Sabti", "Axad", "Isniin", "Talaado", "Arbaco", "Khamiis", "Jimce")
    data class Shift(val key: String, val label: String, val text: String, val h: Int, val m: Int)
    val SHIFTS = listOf(
        Shift("m", "Subax", "8:30 AM – 2:00 PM", 8, 30),
        Shift("a", "Galab", "2:00 PM – 8:00 PM", 14, 0),
        Shift("n", "Seexashada", "8:00 PM", 20, 0)
    )
    // null = dhakhtarka habeenka Jimcaha (Dr. Xaaris / Dr. Yaxye)
    val BASE: Array<Array<String?>> = arrayOf(
        arrayOf("Ibraheem Abdulkader", "Mustafe", "Hassan"),
        arrayOf("Haaruun", "Muse", "Ugaas Maxamuud"),
        arrayOf("Cumar", "Ibraheem Abdulkader", "Cumar"),
        arrayOf("Hassan", "Muse", "Muse"),
        arrayOf("Ciise", "Hassan", "Ciise"),          // Arbaco: subax + habeen → Ciise
        arrayOf("Haaruun", "Cumar", "Mustafe"),
        arrayOf("Ciise", "Hassan", null)
    )
    val DOCS = arrayOf("Dr. Xaaris", "Dr. Yaxye")

    private fun ref(): Calendar = Calendar.getInstance().apply { clear(); set(2026, Calendar.OCTOBER, 17) } // Sabti → Toddobaadka 1

    fun dayIdx(c: Calendar) = c.get(Calendar.DAY_OF_WEEK) % 7 // Sat=0 … Fri=6
    fun weekIdx(c: Calendar): Int {
        val sat = (c.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -dayIdx(c))
        }
        val weeks = Math.round((sat.timeInMillis - ref().timeInMillis) / (7.0 * 86400000.0)).toInt()
        return ((weeks % 2) + 2) % 2
    }
    fun cell(w: Int, di: Int, si: Int): String = BASE[di][si] ?: DOCS[w]

    data class Next(val time: Long, val shift: Shift, val dayName: String)

    fun next(name: String, from: Long): Next? {
        if (name.isBlank()) return null
        val d = Calendar.getInstance().apply { timeInMillis = from }
        repeat(15) {
            val di = dayIdx(d); val w = weekIdx(d)
            SHIFTS.forEachIndexed { si, s ->
                if (cell(w, di, si) == name) {
                    val t = (d.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, s.h); set(Calendar.MINUTE, s.m); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                    if (t.timeInMillis >= from) return Next(t.timeInMillis, s, DAYS[di])
                }
            }
            d.add(Calendar.DAY_OF_YEAR, 1)
            d.set(Calendar.HOUR_OF_DAY, 0); d.set(Calendar.MINUTE, 0); d.set(Calendar.SECOND, 0)
        }
        return null
    }
}
