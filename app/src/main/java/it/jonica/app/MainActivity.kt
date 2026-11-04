package it.jonica.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import it.jonica.app.ui.JonicaApp
import it.jonica.app.ui.theme.JonicaTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().apply {
            userAgentValue = "Jonica/1.0 (it.jonica.app)"
            osmdroidBasePath = cacheDir.resolve("osmdroid")
            osmdroidTileCache = cacheDir.resolve("osmdroid/tiles")
        }
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent {
            JonicaTheme {
                JonicaApp()
            }
        }
    }
}
