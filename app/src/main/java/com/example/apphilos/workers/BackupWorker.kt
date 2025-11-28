package com.example.apphilos.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.apphilos.R
import kotlinx.coroutines.delay

class BackupWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        // Crea el canal de notificación (necesario para Android 8.0+)
        makeStatusNotification("Iniciando respaldo...", applicationContext)

        return try {
            Log.d("BackupWorker", "Iniciando respaldo en segundo plano.")
            // Simula un trabajo de 5 segundos
            delay(5000)
            Log.d("BackupWorker", "Respaldo completado.")

            // Muestra la notificación de éxito
            makeStatusNotification("Respaldo completado exitosamente", applicationContext)

            Result.success()
        } catch (e: Exception) {
            Log.e("BackupWorker", "Error durante el respaldo", e)
            makeStatusNotification("Error en el respaldo", applicationContext)
            Result.failure()
        }
    }

    private fun makeStatusNotification(message: String, context: Context) {
        // Crea un canal de notificación si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AppHilos WorkManager"
            val descriptionText = "Notificaciones del estado de las tareas de fondo"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("WORKMANAGER_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Crea la notificación
        val notification = NotificationCompat.Builder(context, "WORKMANAGER_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tarea de Fondo")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Muestra la notificación
        notificationManager.notify(1, notification)
    }
}
