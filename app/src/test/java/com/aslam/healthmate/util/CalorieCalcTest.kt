package com.aslam.healthmate.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CalorieCalcTest {

    @Test
    fun example_8000steps_170cm_70kg_male_isAbout209() {
        assertEquals(209, CalorieCalc.caloriesFromSteps(8000, 170, 70, true))
    }

    @Test
    fun strideLength_male_and_female() {
        assertEquals(70.55, CalorieCalc.strideCm(170, true), 0.001)
        assertEquals(70.21, CalorieCalc.strideCm(170, false), 0.001)
    }

    @Test
    fun distance_isInKilometres() {
        // 8000 x 70.55 cm = 564,400 cm = 5.644 km
        assertEquals(5.644, CalorieCalc.distanceKm(8000, 170, true), 0.001)
    }

    @Test
    fun zeroSteps_isZeroCalories() {
        assertEquals(0, CalorieCalc.caloriesFromSteps(0, 170, 70, true))
    }
}
