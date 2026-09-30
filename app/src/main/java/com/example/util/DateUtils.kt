package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object FinanceFormatter {
    /**
     * Formats an amount as:
     * Rs. 60,000
     * Rs. 15,000
     * Rs. 0
     * -Rs. 5,000 (if negative balance)
     */
    fun formatCurrency(amount: Long): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        return if (amount < 0) {
            "-Rs. ${numberFormat.format(-amount)}"
        } else {
            "Rs. ${numberFormat.format(amount)}"
        }
    }
}

object DateUtils {

    fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    fun getCurrentMonth(): Int {
        // Returns 1..12
        return Calendar.getInstance().get(Calendar.MONTH) + 1
    }

    fun getMonthDateRange(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        // Start of month
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startMillis = calendar.timeInMillis

        // End of month
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, maxDay)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endMillis = calendar.timeInMillis

        return Pair(startMillis, endMillis)
    }

    fun formatDisplayDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(timestamp)
    }

    fun formatDisplayDateWithTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(timestamp)
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    fun getMonthName(month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month - 1)
        val sdf = SimpleDateFormat("MMMM", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    fun getAvailableYears(): List<Int> {
        val currentYear = getCurrentYear()
        return (currentYear - 3..currentYear + 2).toList()
    }
}
