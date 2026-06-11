package com.example.proyectazo.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.proyectazo.R

/**
 * Singleton that handles the creation and display of workout reminder notifications.
 * Notification channels are required on Android 8.0 (API 26) and above —
 * without a channel, notifications are silently ignored on modern devices.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "smartfit_entrenamiento"
    private const val CHANNEL_NAME = "Recordatorio de entrenamiento"
    private const val NOTIFICATION_ID = 1001 // Unique ID — using the same ID replaces the previous notification

    /**
     * Creates the notification channel required by Android 8.0+.
     * Safe to call multiple times — the system ignores it if the channel already exists.
     * Must be called before any notification is shown (done at app startup in MainActivity).
     */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Channels only exist from API 26 onwards
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // HIGH = shows as heads-up notification with sound
            ).apply {
                description = "Notificaciones de recordatorio de entrenamiento"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    /**
     * Builds and displays the workout reminder notification.
     * Called by EntrenamientoWorker 30 minutes before the user's scheduled workout time.
     */
    fun mostrarNotificacion(context: Context) {
        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logosinfondo)
            .setContentTitle("¡Hora de entrenar!")
            .setContentText("Tu sesión empieza en 30 minutos. ¡Prepárate!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Dismisses the notification automatically when the user taps it
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notificacion) // If a notification with this ID already exists, it is replaced
    }
}