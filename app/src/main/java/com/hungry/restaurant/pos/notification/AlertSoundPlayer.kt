package com.hungry.restaurant.pos.notification

import android.content.Context
import android.media.RingtoneManager
import android.util.Log

/**
 * Plays the device's default notification sound - screen 02's "Test the alert
 * sound" (E2) and, when Settings > Ring until accepted is on, the repeating
 * ring for a new incoming order (screen 03). No bundled asset: the terminal's
 * own notification tone is always available and respects the user's own
 * volume/silent settings, unlike a raw asset played through a fixed stream.
 */
class AlertSoundPlayer(private val context: Context) {

    fun playOnce() {
        runCatching {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getValidRingtoneUri(context)
            RingtoneManager.getRingtone(context, uri)?.play()
        }.onFailure { Log.e(TAG, "Couldn't play the alert sound", it) }
    }

    private companion object {
        const val TAG = "AlertSoundPlayer"
    }
}
