package com.example.sftping.di

import android.content.Context
import androidx.room.Room
import com.example.sftping.data.transfer.TransferDatabase
import com.example.sftping.data.transfer.TransferTaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TransferDatabase =
        Room.databaseBuilder(context, TransferDatabase::class.java, "sftping.db").build()

    @Provides
    fun provideTransferTaskDao(db: TransferDatabase): TransferTaskDao = db.transferTaskDao()
}
