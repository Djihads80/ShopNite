package com.djihad.shopnite.util

import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.getDefault())

    fun formatPrice(value: Int): String =
        NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)

    fun formatDate(iso: String?): String? = parseInstant(iso)
        ?.atZone(ZoneId.systemDefault())
        ?.format(dateFormatter)

    fun formatDateTime(iso: String?): String? = parseInstant(iso)
        ?.atZone(ZoneId.systemDefault())
        ?.format(dateTimeFormatter)

    fun formatTimeLeft(iso: String?): String? {
        val instant = parseInstant(iso) ?: return null
        val duration = Duration.between(Instant.now(), instant)
        if (duration.isNegative) return "Left shop"
        val hours = duration.toHours()
        val days = duration.toDays()
        return when {
            days >= 1 -> "$days day${if (days == 1L) "" else "s"} left"
            hours >= 1 -> "$hours hour${if (hours == 1L) "" else "s"} left"
            else -> "Leaving soon"
        }
    }

    fun leavesWithinDay(iso: String?): Boolean {
        val instant = parseInstant(iso) ?: return false
        val duration = Duration.between(Instant.now(), instant)
        return !duration.isNegative && duration <= Duration.ofHours(24)
    }

    private fun parseInstant(iso: String?): Instant? = try {
        iso?.let(Instant::parse)
    } catch (_: Exception) {
        null
    }
}
