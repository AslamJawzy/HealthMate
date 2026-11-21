package com.aslam.healthmate.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters

// Small helpers for dates. The database always stores dates as "yyyy-MM-dd" text.
object DateUtils {

    private val prettyFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
    private val shortFormat = DateTimeFormatter.ofPattern("d MMM")

    fun today(): LocalDate = LocalDate.now()

    fun toText(date: LocalDate): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    // Returns null if the text is not a real date (for example 2026-02-30)
    fun parse(text: String): LocalDate? {
        return try {
            LocalDate.parse(text.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun pretty(date: LocalDate): String = date.format(prettyFormat)

    fun pretty(text: String): String = parse(text)?.let { pretty(it) } ?: text

    fun short(date: LocalDate): String = date.format(shortFormat)

    // Week runs Monday to Sunday
    fun weekRange(date: LocalDate): Pair<LocalDate, LocalDate> {
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return Pair(monday, monday.plusDays(6))
    }

    // Whole month, from the 1st to the last day
    fun monthRange(date: LocalDate): Pair<LocalDate, LocalDate> {
        return Pair(date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth()))
    }

    // The Material date picker works in UTC milliseconds
    fun fromPickerMillis(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    fun toPickerMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}
