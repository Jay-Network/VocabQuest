package com.jworks.vocabquest.android.workers

import android.content.Context
import android.provider.Settings

/**
 * Provides a stable device identifier for cross-app Supabase queries.
 *
 * Uses Settings.Secure.ANDROID_ID which is deterministic per device and
 * consistent across all apps — both EigoQuest and EigoLens will get the
 * same value without needing shared prefs or sharedUserId.
 *
 * This ID is used as target_device_id in the eq_received_words table
 * so EigoLens can send words to this specific device's EigoQuest.
 */
object DeviceIdProvider {

    @Suppress("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
