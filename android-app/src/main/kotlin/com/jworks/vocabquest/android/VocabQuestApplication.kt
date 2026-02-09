package com.jworks.vocabquest.android

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jworks.vocabquest.android.workers.CoinSyncWorker
import com.jworks.vocabquest.core.data.remote.SupabaseClientFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VocabQuestApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        initializeSupabase()
        CoinSyncWorker.schedule(this)
    }

    private fun initializeSupabase() {
        val supabaseUrl = BuildConfig.SUPABASE_URL
        val supabaseKey = BuildConfig.SUPABASE_ANON_KEY

        if (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            try {
                SupabaseClientFactory.initialize(supabaseUrl, supabaseKey)
                Log.i("VocabQuest", "Supabase initialized for J Coin sync")
            } catch (e: Exception) {
                Log.w("VocabQuest", "Failed to initialize Supabase: ${e.message}")
            }
        } else {
            Log.i("VocabQuest", "Supabase credentials not configured - J Coin running in offline-only mode")
        }
    }
}
