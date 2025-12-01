package mx.valdora.vmusic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.preferences.PreferencesManager
import mx.valdora.vmusic.data.util.IconManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val preferencesManager = PreferencesManager(application)
    
    private val _accentColorIndex = MutableStateFlow(0)
    val accentColorIndex: StateFlow<Int> = _accentColorIndex
    
    private val _appIconIndex = MutableStateFlow(0)
    val appIconIndex: StateFlow<Int> = _appIconIndex
    
    init {
        viewModelScope.launch {
            preferencesManager.accentColorIndex.collect { index ->
                _accentColorIndex.value = index
            }
        }
        viewModelScope.launch {
            preferencesManager.appIconIndex.collect { index ->
                _appIconIndex.value = index
            }
        }
    }
    
    fun setAccentColor(index: Int) {
        viewModelScope.launch {
            preferencesManager.setAccentColor(index)
        }
    }
    
    fun setAppIcon(index: Int) {
        viewModelScope.launch {
            preferencesManager.setAppIcon(index)
            IconManager.changeIcon(getApplication(), index)
        }
    }
}
