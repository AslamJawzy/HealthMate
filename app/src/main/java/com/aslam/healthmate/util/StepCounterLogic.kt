package com.aslam.healthmate.util

// What we remember between readings.
// date: the day these numbers belong to
// baseline: sensor value when counting started (start of day, or 0 after a restart)
// lastReading: the most recent sensor value
// offset: steps already counted today before the phone restarted
data class StepState(
    val date: String,
    val baseline: Long,
    val lastReading: Long,
    val offset: Long
)

data class StepResult(val stepsToday: Int, val newState: StepState)

// The phone's step sensor (TYPE_STEP_COUNTER) gives the total steps since the phone was
// switched on. It does not reset at midnight, and it goes back to 0 after a restart.
// This works out "steps today" from that total. Kept separate so it can be unit tested.
object StepCounterLogic {

    fun onReading(reading: Long, today: String, state: StepState): StepResult {
        // 1. First reading ever, or a new day has started: start counting from here
        if (state.date != today) {
            val fresh = StepState(date = today, baseline = reading, lastReading = reading, offset = 0)
            return StepResult(0, fresh)
        }

        // 2. Reading went down, so the phone was restarted. Keep the steps we already had.
        if (reading < state.lastReading) {
            val stepsBefore = state.offset + (state.lastReading - state.baseline)
            val after = StepState(date = today, baseline = 0, lastReading = reading, offset = stepsBefore)
            return StepResult((stepsBefore + reading).toInt(), after)
        }

        // 3. Normal counting
        val updated = state.copy(lastReading = reading)
        return StepResult((state.offset + reading - state.baseline).toInt(), updated)
    }
}
