package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ReminderManager {

    fun triggerBreakFinishedReminder(context: Context) {
        // Run asynchronously to not block UI/timer ticker
        CoroutineScope(Dispatchers.Default).launch {
            playTone()
            playNotificationSound(context)
            vibrate(context)
        }
    }

    private fun playTone() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 90)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
            Thread.sleep(700)
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
            toneGenerator.release()
        } catch (_: Exception) {
            // ToneGenerator unavailable or silent mode
        }
    }

    private fun playNotificationSound(context: Context) {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, notificationUri)
            ringtone?.play()
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate(context: Context) {
        try {
            val timings = longArrayOf(0, 300, 150, 300, 150, 450)
            val amplitudes = intArrayOf(0, 200, 0, 255, 0, 255)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibratorManager?.vibrate(CombinedVibration.createParallel(effect))
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator?.vibrate(effect)
                } else {
                    vibrator?.vibrate(timings, -1)
                }
            }
        } catch (_: Exception) {
            // Vibrator not available or permission denied in testing
        }
    }
}
