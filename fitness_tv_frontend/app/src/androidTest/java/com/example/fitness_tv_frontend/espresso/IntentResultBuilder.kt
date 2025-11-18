package com.example.fitness_tv_frontend.espresso

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent

/**
 * Small helper to build Instrumentation.ActivityResult for Espresso Intents stubbing
 * without depending on external utilities.
 */
object IntentResultBuilder {
    fun ok(data: Intent?): Instrumentation.ActivityResult {
        return Instrumentation.ActivityResult(Activity.RESULT_OK, data)
    }

    fun canceled(): Instrumentation.ActivityResult {
        return Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
    }
}
