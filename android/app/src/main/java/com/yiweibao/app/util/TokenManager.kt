package com.yiweibao.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "yiweibao_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_REAL_NAME = stringPreferencesKey("real_name")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    suspend fun saveLogin(token: String, userId: Long, username: String, role: Int, realName: String) {
        context.dataStore.edit {
            it[KEY_TOKEN] = token
            it[KEY_USER_ID] = userId.toString()
            it[KEY_USERNAME] = username
            it[KEY_ROLE] = role.toString()
            it[KEY_REAL_NAME] = realName
        }
    }

    suspend fun getToken(): String? {
        var result: String? = null
        context.dataStore.data.collect { prefs ->
            result = prefs[KEY_TOKEN]
            return@collect
        }
        return result
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun getUsername(): String? {
        var result: String? = null
        context.dataStore.data.collect { prefs ->
            result = prefs[KEY_USERNAME]
            return@collect
        }
        return result
    }

    suspend fun getRealName(): String? {
        var result: String? = null
        context.dataStore.data.collect { prefs ->
            result = prefs[KEY_REAL_NAME]
            return@collect
        }
        return result
    }
}
