package com.example.proyectazo.notificaciones

import android.content.Context
import android.service.autofill.Validators.and
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Singleton responsible for scheduling and cancelling workout reminder notifications.
 * Uses WorkManager to guarantee execution even if the app is closed or the device restarts.
 */
object NotificationScheduler {

    private const val WORK_TAG = "smartfit_recordatorio" // Tag used to cancel and replace existing work

    /**
     * Schedules a notification 30 minutes before the user's workout time.
     * If that time has already passed today, it schedules for the same time tomorrow.
     * The delay is calculated at runtime so the worker fires at exactly the right moment.
     */
    fun programar(context: Context, horaStr: String) {
        // Parse the time string — fall back to 20:00 if the format is unexpected
        val partes = horaStr.split(":")
        val hora = partes.getOrNull(0)?.toIntOrNull() ?: 20 // Default to 20:00. Prevents underflow to 23:30 if set to 0, ensuring the 30-minute advance reminder fires at 19:30.
        val minuto = partes.getOrNull(1)?.toIntOrNull() ?: 0

        // Build the target time: workout hour minus 30 minutes
        val ahora = Calendar.getInstance()
        val objetivo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -30) // Notify 30 minutes before the session starts
        }

        // If the target time has already passed today, reschedule for tomorrow
        if (objetivo.before(ahora)) {
            objetivo.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Calculate how many milliseconds from now until the target time
        val demora = objetivo.timeInMillis - ahora.timeInMillis

        // No network or charging constraints needed — the worker only shows a local notification
        val constraints = Constraints.Builder()
            .build()

        // OneTimeWorkRequest fires once after the calculated delay
        val workRequest = OneTimeWorkRequestBuilder<EntrenamientoWorker>()
            .setConstraints(constraints)
            .setInitialDelay(demora, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        // Cancel any previously scheduled reminder before enqueuing the new one —
        // prevents duplicate notifications if the user changes their workout time
        WorkManager.getInstance(context).apply {
            cancelAllWorkByTag(WORK_TAG)
            enqueue(workRequest)
        }
    }
    /**
     * Cancels any pending workout reminder.
     * Called when the user disables notifications or deletes their account.
     */
    fun cancelar(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
}