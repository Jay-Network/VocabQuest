package com.jworks.vocabquest.android.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import com.jworks.vocabquest.core.data.DatabaseDriverFactory
import com.jworks.vocabquest.core.data.JCoinRepositoryImpl
import com.jworks.vocabquest.core.data.SessionRepositoryImpl
import com.jworks.vocabquest.core.data.SrsRepositoryImpl
import com.jworks.vocabquest.core.data.SubscriptionRepositoryImpl
import com.jworks.vocabquest.core.data.UserRepositoryImpl
import com.jworks.vocabquest.core.data.VocabRepositoryImpl
import com.jworks.vocabquest.core.domain.repository.JCoinRepository
import com.jworks.vocabquest.core.domain.repository.SessionRepository
import com.jworks.vocabquest.core.domain.repository.SrsRepository
import com.jworks.vocabquest.core.domain.repository.SubscriptionRepository
import com.jworks.vocabquest.core.domain.repository.UserRepository
import com.jworks.vocabquest.core.domain.repository.VocabRepository
import com.jworks.vocabquest.core.domain.usecase.CompleteSessionUseCase
import com.jworks.vocabquest.core.scoring.ScoringEngine
import com.jworks.vocabquest.core.srs.Sm2Algorithm
import com.jworks.vocabquest.core.srs.SrsAlgorithm
import com.jworks.vocabquest.db.VocabQuestDatabase
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
    fun provideDatabase(driver: SqlDriver): VocabQuestDatabase {
        return VocabQuestDatabase(driver)
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
    fun provideJCoinRepository(db: VocabQuestDatabase): JCoinRepository {
        return JCoinRepositoryImpl(db)
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
    fun provideCompleteSessionUseCase(
        userRepository: UserRepository,
        sessionRepository: SessionRepository,
        scoringEngine: ScoringEngine,
        jCoinRepository: JCoinRepository
    ): CompleteSessionUseCase {
        return CompleteSessionUseCase(
            userRepository = userRepository,
            sessionRepository = sessionRepository,
            scoringEngine = scoringEngine,
            jCoinRepository = jCoinRepository
        )
    }
}
