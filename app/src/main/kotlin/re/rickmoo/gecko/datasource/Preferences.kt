package re.rickmoo.gecko.datasource

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class Preferences(private val context: Context) {

    suspend fun add(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend operator fun set(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun remove(key: Preferences.Key<String>) {
        context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    suspend operator fun get(key: Preferences.Key<String>): String? {
        return context.dataStore.data
            .map { preferences -> preferences[key] }
            .firstOrNull()
    }

    object GeckoView {
        const val PREFIX = "geckoView"
        val ENV_ID = stringPreferencesKey("${PREFIX}.envName")
        val DEFAULT_URL = stringPreferencesKey("${PREFIX}.defaultUrl")
        val RESTORE_URL = stringPreferencesKey("${PREFIX}.restoreUrl")
    }

    object App {
        const val PREFIX = "app"
        val UPDATE_CHANNEL = stringPreferencesKey("${PREFIX}.updateChannel")
    }

}