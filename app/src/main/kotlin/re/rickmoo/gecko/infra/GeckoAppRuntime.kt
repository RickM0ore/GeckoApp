package re.rickmoo.gecko.infra

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings


class GeckoRuntimeSettingsInitializer : Initializer<GeckoRuntimeSettings> {
    override fun create(context: Context): GeckoRuntimeSettings {
        return GeckoRuntimeSettings.Builder()
            .remoteDebuggingEnabled(true)
            .remoteDebuggingEnabled(true)
            .consoleOutput(true)
            .build()
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}

class GeckoRuntimeInitializer : Initializer<GeckoRuntime> {
    override fun create(context: Context): GeckoRuntime {
        return GeckoRuntime.create(
            context,
            AppInitializer
                .getInstance(context)
                .initializeComponent(GeckoRuntimeSettingsInitializer::class.java)
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return listOf(GeckoRuntimeSettingsInitializer::class.java)
    }

}
