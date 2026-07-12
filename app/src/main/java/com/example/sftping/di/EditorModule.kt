package com.example.sftping.di

import android.content.Context
import androidx.room.Room
import com.example.sftping.data.editor.DataStoreEditorLocationRepository
import com.example.sftping.data.editor.EditorDatabase
import com.example.sftping.data.editor.EditorLocationRepository
import com.example.sftping.data.editor.PendingEditDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EditorRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindEditorLocationRepository(
        impl: DataStoreEditorLocationRepository
    ): EditorLocationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object EditorModule {

    @Provides
    @Singleton
    fun provideEditorDatabase(@ApplicationContext context: Context): EditorDatabase =
        Room.databaseBuilder(context, EditorDatabase::class.java, "sftping_editor.db").build()

    @Provides
    fun providePendingEditDao(db: EditorDatabase): PendingEditDao = db.pendingEditDao()
}
