package com.example.ridertipstracker.di

import android.content.Context
import androidx.room.Room
import com.example.ridertipstracker.data.local.db.AppDatabase
import com.example.ridertipstracker.data.local.dao.RiderShiftDao
import com.example.ridertipstracker.data.local.datastore.PreferencesManager
import com.example.ridertipstracker.utils.CsvExporter
import com.example.ridertipstracker.utils.CsvImporter
import com.example.ridertipstracker.utils.PdfExporter
import com.example.ridertipstracker.utils.PredictionEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRiderShiftDao(database: AppDatabase): RiderShiftDao {
        return database.riderShiftDao()
    }

    @Provides
    @Singleton
    fun providePredictionEngine(): PredictionEngine {
        return PredictionEngine()
    }
    
    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
    
    @Provides
    fun provideCsvImporter(@ApplicationContext context: Context): CsvImporter {
        return CsvImporter(context)
    }
    
    @Provides
    fun provideCsvExporter(@ApplicationContext context: Context): CsvExporter {
        return CsvExporter(context)
    }
    
    @Provides
    fun providePdfExporter(@ApplicationContext context: Context): PdfExporter {
        return PdfExporter(context)
    }
}
