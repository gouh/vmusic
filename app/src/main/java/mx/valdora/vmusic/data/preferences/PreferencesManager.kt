package mx.valdora.vmusic.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        private val ACCENT_COLOR_KEY = intPreferencesKey("accent_color")
        private val APP_ICON_KEY = intPreferencesKey("app_icon")
    }
    
    val accentColorIndex: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[ACCENT_COLOR_KEY] ?: 0 }
    
    val appIconIndex: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[APP_ICON_KEY] ?: 0 }
    
    suspend fun setAccentColor(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[ACCENT_COLOR_KEY] = index
        }
    }
    
    suspend fun setAppIcon(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[APP_ICON_KEY] = index
        }
    }
}
