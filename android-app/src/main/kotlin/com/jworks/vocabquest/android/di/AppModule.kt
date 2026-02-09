package com.jworks.vocabquest.android.di

import android.content.Context
import com.jworks.vocabquest.core.data.DatabaseDriverFactory
import com.jworks.vocabquest.core.data.JCoinRepositoryImpl
import com.jworks.vocabquest.core.data.VocabRepositoryImpl
import com.jworks.vocabquest.core.domain.repository.JCoinRepository
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
    fun provideDatabase(@ApplicationContext context: Context): VocabQuestDatabase {
        val driver = DatabaseDriverFactory(context).createDriver()
        return VocabQuestDatabase(driver)
    }

    @Provides
    @Singleton
    fun provideVocabRepository(db: VocabQuestDatabase): VocabRepository {
        return VocabRepositoryImpl(db)
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
}
