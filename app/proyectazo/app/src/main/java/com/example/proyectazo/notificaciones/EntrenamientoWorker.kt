package com.example.proyectazo.notificaciones

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Background worker responsible for triggering the workout reminder notification.
 * Extends CoroutineWorker to support Kotlin coroutines inside the task.
 *
 * WorkManager automatically manages the thread lifecycle, ensuring doWork()
 * always runs off the main thread — preventing UI freezes or ANR errors.
 */
class EntrenamientoWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    /**
     * Entry point called by WorkManager when the scheduled task fires.
     * Always runs on Dispatchers.Default (background thread) — never on the main thread.
     * Returns Result.success() if the notification was shown, Result.failure() otherwise.
     * WorkManager can use Result.retry() to reschedule if needed.
     */
    override suspend fun doWork(): Result {
        return try {
            NotificationHelper.mostrarNotificacion(context)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}