package mx.valdora.vmusic.data.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object IconManager {
    private val iconAliases = listOf(
        "mx.valdora.vmusic.MainActivityDefault",
        "mx.valdora.vmusic.MainActivityWhite",
        "mx.valdora.vmusic.MainActivityBlack"
    )
    
    fun changeIcon(context: Context, iconIndex: Int) {
        val packageManager = context.packageManager
        
        iconAliases.forEachIndexed { index, alias ->
            val componentName = ComponentName(context, alias)
            val state = if (index == iconIndex) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            
            packageManager.setComponentEnabledSetting(
                componentName,
                state,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
