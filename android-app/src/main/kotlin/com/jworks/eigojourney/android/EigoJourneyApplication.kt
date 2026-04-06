package com.jworks.eigojourney.android

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jworks.eigojourney.android.workers.CoinSyncWorker
import com.jworks.eigojourney.android.workers.ReceivedWordsSyncWorker
import com.jworks.eigojourney.core.data.remote.SupabaseClientFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EigoJourneyApplication : Application(), Configuration.Provider {

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
        ReceivedWordsSyncWorker.schedule(this)
    }

    private fun initializeSupabase() {
        val supabaseUrl = BuildConfig.SUPABASE_URL
        val supabaseKey = BuildConfig.SUPABASE_ANON_KEY

        if (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            try {
                SupabaseClientFactory.initialize(supabaseUrl, supabaseKey)
                Log.i("EigoJourney", "Supabase initialized for J Coin sync")
            } catch (e: Exception) {
                Log.w("EigoJourney", "Failed to initialize Supabase: ${e.message}")
            }
        } else {
            Log.i("EigoJourney", "Supabase credentials not configured - J Coin running in offline-only mode")
        }
    }
}
