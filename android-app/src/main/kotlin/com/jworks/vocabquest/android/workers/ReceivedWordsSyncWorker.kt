package com.jworks.vocabquest.android.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.jworks.vocabquest.core.data.remote.SupabaseClientFactory
import com.jworks.vocabquest.core.domain.model.ReceivedWord
import com.jworks.vocabquest.core.domain.repository.ReceivedWordsRepository
import com.jworks.vocabquest.core.domain.repository.VocabRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.postgrest.from
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit

/**
 * Background worker that pulls unread words from the shared Supabase
 * eq_received_words table (sent by EigoLens) and inserts them into the
 * local received_words SQLite table.
 *
 * Flow:
 * 1. Query eq_received_words WHERE target_app = 'eigoquest' AND pulled_at IS NULL
 * 2. Insert new words into local received_words via ReceivedWordsRepository
 * 3. Try to link each received word to an existing vocab word in the local DB
 * 4. Mark rows as pulled on Supabase (SET pulled_at = now())
 *
 * Runs every 15 minutes when network is available.
 */
@HiltWorker
class ReceivedWordsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val receivedWordsRepository: ReceivedWordsRepository,
    private val vocabRepository: VocabRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!SupabaseClientFactory.isInitialized()) {
            Log.d(TAG, "Supabase not initialized — skipping received words sync")
            return Result.success()
        }

        return try {
            val deviceId = DeviceIdProvider.getDeviceId(applicationContext)
            Log.d(TAG, "Pulling received words for device: $deviceId")

            val client = SupabaseClientFactory.getInstance()

            // 1. Fetch unpulled words targeted at this device
            val rows = client.from("eq_received_words")
                .select {
                    filter {
                        eq("target_device_id", deviceId)
                        eq("target_app", "eigoquest")
                        exact("pulled_at", null)
                    }
                }
                .decodeList<SupabaseReceivedWord>()

            if (rows.isEmpty()) {
                Log.d(TAG, "No new received words")
                return Result.success()
            }

            Log.i(TAG, "Found ${rows.size} new received word(s)")

            // 2. Insert into local DB
            val now = Clock.System.now().epochSeconds
            val localWords = rows.map { row ->
                ReceivedWord(
                    word = row.word,
                    ipa = row.ipa,
                    cefrLevel = row.cefrLevel ?: "B1",
                    sourceApp = row.sourceApp ?: "eigolens",
                    senderUserId = row.senderDeviceId ?: "unknown",
                    receivedAt = now
                )
            }

            receivedWordsRepository.insertBatch(localWords)
            Log.i(TAG, "Inserted ${localWords.size} received word(s) into local DB")

            // 3. Try to link to existing vocab words
            for (localWord in localWords) {
                try {
                    val vocabWord = vocabRepository.findByWord(localWord.word)
                    if (vocabWord != null) {
                        val inserted = receivedWordsRepository.getAll().find {
                            it.word == localWord.word && it.senderUserId == localWord.senderUserId
                        }
                        if (inserted != null) {
                            receivedWordsRepository.linkToWord(inserted.id, vocabWord.id)
                            Log.d(TAG, "Linked '${localWord.word}' to vocab word #${vocabWord.id}")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to link word '${localWord.word}': ${e.message}")
                }
            }

            // 4. Mark as pulled on Supabase
            val pulledIds = rows.map { it.id }
            try {
                val pullTime = Clock.System.now().epochSeconds
                client.from("eq_received_words")
                    .update({
                        set("pulled_at", pullTime)
                    }) {
                        filter {
                            isIn("id", pulledIds)
                        }
                    }
                Log.i(TAG, "Marked ${pulledIds.size} row(s) as pulled on Supabase")
            } catch (e: Exception) {
                // Non-fatal: words are already in local DB
                // The UNIQUE index on received_words(word, sender_user_id) prevents duplicates
                Log.w(TAG, "Failed to mark rows as pulled: ${e.message}")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Received words sync failed: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "ReceivedWordsSyncWorker"
        private const val WORK_NAME = "received_words_sync_periodic"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<ReceivedWordsSyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

            Log.i(TAG, "Received words sync scheduled (every 15 minutes)")
        }
    }
}

/**
 * DTO matching the Supabase eq_received_words table schema (migration 021).
 */
@Serializable
private data class SupabaseReceivedWord(
    val id: Long,
    val word: String,
    val ipa: String? = null,
    @SerialName("cefr_level") val cefrLevel: String? = "B1",
    @SerialName("source_app") val sourceApp: String? = "eigolens",
    @SerialName("sender_device_id") val senderDeviceId: String? = null,
    @SerialName("target_device_id") val targetDeviceId: String? = null,
    @SerialName("target_app") val targetApp: String? = "eigoquest",
    @SerialName("pulled_at") val pulledAt: Long? = null,
    @SerialName("created_at") val createdAt: String? = null
)
