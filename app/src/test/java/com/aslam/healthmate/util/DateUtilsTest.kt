package com.aslam.healthmate.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun week_runsMondayToSunday() {
        // Sunday 8 Nov 2026 belongs to the week Mon 2 Nov to Sun 8 Nov
        val (start, end) = DateUtils.weekRange(LocalDate.of(2026, 11, 8))
        assertEquals(LocalDate.of(2026, 11, 2), start)
        assertEquals(LocalDate.of(2026, 11, 8), end)
    }

    @Test
    fun week_fromMonday_staysOnSameWeek() {
        val (start, end) = DateUtils.weekRange(LocalDate.of(2026, 11, 9))
        assertEquals(LocalDate.of(2026, 11, 9), start)
        assertEquals(LocalDate.of(2026, 11, 15), end)
    }

    @Test
    fun month_isTheWholeMonth() {
        val (start, end) = DateUtils.monthRange(LocalDate.of(2026, 11, 18))
        assertEquals(LocalDate.of(2026, 11, 1), start)
        assertEquals(LocalDate.of(2026, 11, 30), end)
    }

    @Test
    fun february_inLeapYear_has29Days() {
        val (_, end) = DateUtils.monthRange(LocalDate.of(2028, 2, 10))
        assertEquals(LocalDate.of(2028, 2, 29), end)
    }

    @Test
    fun dateText_roundTrip() {
        val date = LocalDate.of(2026, 8, 18)
        assertEquals("2026-08-18", DateUtils.toText(date))
        assertEquals(date, DateUtils.parse("2026-08-18"))
    }

    @Test
    fun invalidDate_returnsNull() {
        assertNull(DateUtils.parse("hello"))
        assertNull(DateUtils.parse("2026-13-01"))
    }

    @Test
    fun pickerMillis_roundTrip() {
        val date = LocalDate.of(2026, 11, 8)
        assertEquals(date, DateUtils.fromPickerMillis(DateUtils.toPickerMillis(date)))
    }
}
