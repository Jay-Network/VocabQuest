package com.jworks.eigojourney.android.di

import android.content.Context
import android.provider.Settings
import app.cash.sqldelight.db.SqlDriver
import com.jworks.eigojourney.android.BuildConfig
import com.jworks.eigojourney.core.data.DatabaseDriverFactory
import com.jworks.eigojourney.core.collection.WordEncounterEngine
import com.jworks.eigojourney.core.collection.WordLevelEngine
import com.jworks.eigojourney.core.data.CollectionRepositoryImpl
import com.jworks.eigojourney.core.data.FeedbackRepositoryImpl
import com.jworks.eigojourney.core.data.ReceivedWordsRepositoryImpl
import com.jworks.eigojourney.core.data.JCoinRepositoryImpl
import com.jworks.eigojourney.core.data.SessionRepositoryImpl
import com.jworks.eigojourney.core.data.SrsRepositoryImpl
import com.jworks.eigojourney.core.data.SubscriptionRepositoryImpl
import com.jworks.eigojourney.core.data.UserRepositoryImpl
import com.jworks.eigojourney.core.data.VocabRepositoryImpl
import com.jworks.eigojourney.core.domain.repository.CollectionRepository
import com.jworks.eigojourney.core.domain.repository.FeedbackRepository
import com.jworks.eigojourney.core.domain.repository.ReceivedWordsRepository
import com.jworks.eigojourney.core.domain.repository.JCoinRepository
import com.jworks.eigojourney.core.domain.repository.SessionRepository
import com.jworks.eigojourney.core.domain.repository.SrsRepository
import com.jworks.eigojourney.core.domain.repository.SubscriptionRepository
import com.jworks.eigojourney.core.domain.repository.UserRepository
import com.jworks.eigojourney.core.domain.repository.VocabRepository
import com.jworks.eigojourney.core.domain.usecase.CheckWordMasteryUseCase
import com.jworks.eigojourney.core.domain.usecase.CompleteSessionUseCase
import com.jworks.eigojourney.core.scoring.ScoringEngine
import com.jworks.eigojourney.core.srs.Sm2Algorithm
import com.jworks.eigojourney.core.srs.SrsAlgorithm
import com.jworks.eigojourney.db.EigoJourneyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSqlDriver(@ApplicationContext context: Context): SqlDriver {
        return DatabaseDriverFactory(context).createDriver()
    }

    @Provides
    @Singleton
    fun provideDatabase(driver: SqlDriver): EigoJourneyDatabase {
        return EigoJourneyDatabase(driver)
    }

    @Provides
    @Singleton
    fun provideVocabRepository(driver: SqlDriver): VocabRepository {
        return VocabRepositoryImpl(driver)
    }

    @Provides
    @Singleton
    fun provideUserRepository(driver: SqlDriver): UserRepository {
        return UserRepositoryImpl(driver)
    }

    @Provides
    @Singleton
    fun provideSessionRepository(driver: SqlDriver): SessionRepository {
        return SessionRepositoryImpl(driver)
    }

    @Provides
    @Singleton
    fun provideSrsRepository(driver: SqlDriver): SrsRepository {
        return SrsRepositoryImpl(driver)
    }

    @Provides
    @Singleton
    fun provideSubscriptionRepository(driver: SqlDriver): SubscriptionRepository {
        return SubscriptionRepositoryImpl(driver)
    }

    @Provides
    @Singleton
    @Suppress("HardwareIds")
    fun provideJCoinRepository(
        db: EigoJourneyDatabase,
        @ApplicationContext context: Context
    ): JCoinRepository {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return JCoinRepositoryImpl(
            database = db,
            appKey = BuildConfig.JCOIN_APP_KEY,
            deviceId = deviceId
        )
    }

    @Provides
    @Singleton
    fun provideCollectionRepository(db: EigoJourneyDatabase): CollectionRepository {
        return CollectionRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideWordEncounterEngine(
        collectionRepository: CollectionRepository,
        vocabRepository: VocabRepository,
        jCoinRepository: JCoinRepository
    ): WordEncounterEngine {
        return WordEncounterEngine(collectionRepository, vocabRepository, jCoinRepository)
    }

    @Provides
    @Singleton
    fun provideWordLevelEngine(
        collectionRepository: CollectionRepository
    ): WordLevelEngine {
        return WordLevelEngine(collectionRepository)
    }

    @Provides
    @Singleton
    fun provideReceivedWordsRepository(db: EigoJourneyDatabase): ReceivedWordsRepository {
        return ReceivedWordsRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideFeedbackRepository(): FeedbackRepository {
        return FeedbackRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideSrsAlgorithm(): SrsAlgorithm {
        return Sm2Algorithm()
    }

    @Provides
    @Singleton
    fun provideScoringEngine(): ScoringEngine {
        return ScoringEngine()
    }

    @Provides
    fun provideCheckWordMasteryUseCase(
        receivedWordsRepository: ReceivedWordsRepository,
        jCoinRepository: JCoinRepository
    ): CheckWordMasteryUseCase {
        return CheckWordMasteryUseCase(receivedWordsRepository, jCoinRepository)
    }

    @Provides
    fun provideCompleteSessionUseCase(
        userRepository: UserRepository,
        sessionRepository: SessionRepository,
        scoringEngine: ScoringEngine,
        jCoinRepository: JCoinRepository,
        srsRepository: SrsRepository
    ): CompleteSessionUseCase {
        return CompleteSessionUseCase(
            userRepository = userRepository,
            sessionRepository = sessionRepository,
            scoringEngine = scoringEngine,
            jCoinRepository = jCoinRepository,
            srsRepository = srsRepository
        )
    }
}
