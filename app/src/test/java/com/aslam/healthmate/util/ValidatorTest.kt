package com.aslam.healthmate.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ValidatorTest {

    private val today = LocalDate.of(2026, 11, 8)

    @Test
    fun validRecord_hasNoErrors() {
        val errors = Validator.validateRecord("2026-11-07", "7850", "420", "2000", today, false)
        assertFalse(errors.hasErrors)
    }

    @Test
    fun emptyFields_showErrors() {
        val errors = Validator.validateRecord("", "", "", "", today, false)
        assertEquals("Please choose a date", errors.date)
        assertEquals("Please enter Steps", errors.steps)
        assertEquals("Please enter Calories", errors.calories)
        assertEquals("Please enter Water", errors.water)
    }

    @Test
    fun notANumber_isRejected() {
        assertEquals("Steps must be a whole number", Validator.checkNumber("abc", "Steps", 100000))
        assertEquals("Steps must be a whole number", Validator.checkNumber("12.5", "Steps", 100000))
    }

    @Test
    fun negativeNumber_isRejected() {
        assertEquals("Water cannot be negative", Validator.checkNumber("-200", "Water", 10000))
    }

    @Test
    fun unrealisticNumber_isRejected() {
        assertEquals("Steps looks too high (max 100000)", Validator.checkNumber("500000", "Steps", 100000))
    }

    @Test
    fun zeroIsAllowed() {
        assertNull(Validator.checkNumber("0", "Steps", 100000))
    }

    @Test
    fun futureDate_isRejected() {
        assertEquals("Date cannot be in the future", Validator.checkDate("2026-11-09", today, false))
    }

    @Test
    fun todayIsAllowed() {
        assertNull(Validator.checkDate("2026-11-08", today, false))
    }

    @Test
    fun badDateFormat_isRejected() {
        assertEquals("Date must be in yyyy-MM-dd format", Validator.checkDate("08/11/2026", today, false))
        assertEquals("Date must be in yyyy-MM-dd format", Validator.checkDate("2026-02-30", today, false))
    }

    @Test
    fun duplicateDate_isRejected() {
        val error = Validator.checkDate("2026-11-07", today, true)
        assertTrue(error!!.contains("already exists"))
    }

    @Test
    fun customFood_needsNameCaloriesAndPortion() {
        val errors = Validator.validateFood("", "0", "", false)
        assertEquals("Please enter a food name", errors.name)
        assertEquals("Calories must be more than 0", errors.calories)
        assertEquals("Please enter a portion, like 1 cup", errors.portion)
    }

    @Test
    fun customFood_duplicateName_isRejected() {
        val errors = Validator.validateFood("Kottu", "800", "1 plate", true)
        assertEquals("This food is already in the list", errors.name)
    }
}
