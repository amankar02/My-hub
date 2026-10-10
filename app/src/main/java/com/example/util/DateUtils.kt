package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val standardDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
    private val shortDayFormat = SimpleDateFormat("EEE", Locale.US)
    private val dayNumberFormat = SimpleDateFormat("d", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun getTodayString(): String {
        return standardDateFormat.format(Date())
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val date = standardDateFormat.parse(dateString) ?: return dateString
            val today = getTodayString()
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrow = standardDateFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -2)
            val yesterday = standardDateFormat.format(cal.time)

            when (dateString) {
                today -> "Today, ${SimpleDateFormat("MMM d", Locale.US).format(date)}"
                tomorrow -> "Tomorrow, ${SimpleDateFormat("MMM d", Locale.US).format(date)}"
                yesterday -> "Yesterday, ${SimpleDateFormat("MMM d", Locale.US).format(date)}"
                else -> displayDateFormat.format(date)
            }
        } catch (_: Exception) {
            dateString
        }
    }

    fun getMonthYearString(dateString: String): String {
        return try {
            val date = standardDateFormat.parse(dateString) ?: Date()
            monthYearFormat.format(date)
        } catch (_: Exception) {
            ""
        }
    }

    data class DayItem(
        val dateString: String,
        val dayOfWeek: String,
        val dayOfMonth: String,
        val isToday: Boolean
    )

    fun getWeekAround(selectedDateString: String, rangeDays: Int = 14): List<DayItem> {
        val list = mutableListOf<DayItem>()
        val cal = Calendar.getInstance()
        try {
            val baseDate = standardDateFormat.parse(selectedDateString) ?: Date()
            cal.time = baseDate
        } catch (_: Exception) {
            cal.time = Date()
        }

        val todayString = getTodayString()

        // Start 3 days before today or selected date
        cal.add(Calendar.DAY_OF_YEAR, -3)

        for (i in 0 until rangeDays) {
            val dStr = standardDateFormat.format(cal.time)
            list.add(
                DayItem(
                    dateString = dStr,
                    dayOfWeek = shortDayFormat.format(cal.time).uppercase(),
                    dayOfMonth = dayNumberFormat.format(cal.time),
                    isToday = dStr == todayString
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    fun getPastDays(count: Int = 7): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in (count - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            list.add(standardDateFormat.format(c.time))
        }
        return list
    }

    fun getShortDayName(dateString: String): String {
        return try {
            val date = standardDateFormat.parse(dateString) ?: return ""
            shortDayFormat.format(date).take(1)
        } catch (_: Exception) {
            ""
        }
    }
}
