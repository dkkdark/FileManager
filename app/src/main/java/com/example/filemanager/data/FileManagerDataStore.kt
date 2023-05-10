package com.example.filemanager.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FileManagerDataStore @Inject constructor(
    @ApplicationContext var context: Context
): FileManagerDataStoreInterface {

    override val readFirstEntranceVal: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FIRST_ENTRANCE] ?: true
        }

    override suspend fun saveFirstEntranceVal(value: Boolean) {
        context.dataStore.edit { pref ->
            pref[FIRST_ENTRANCE] = value
        }
    }

    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "First entrance")
        val FIRST_ENTRANCE = booleanPreferencesKey("first_entrance")
    }
}