package it.jonica.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat

data class GeoPoint(val lat: Double, val lon: Double)

/** Centro del CAP 00138 (Fidene / Serpentara), usato se la posizione non è disponibile. */
val Default00138 = GeoPoint(41.9712, 12.5225)

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

/** Ultima posizione nota + aggiornamenti dal GPS/rete, senza Google Play Services. */
@SuppressLint("MissingPermission")
fun observeLocation(context: Context, onLocation: (GeoPoint) -> Unit): () -> Unit {
    if (!hasLocationPermission(context)) return {}
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = lm.getProviders(true)
    providers
        .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
        .maxByOrNull { it.time }
        ?.let { onLocation(GeoPoint(it.latitude, it.longitude)) }

    val listener = LocationListener { loc: Location -> onLocation(GeoPoint(loc.latitude, loc.longitude)) }
    providers.filter { it == LocationManager.GPS_PROVIDER || it == LocationManager.NETWORK_PROVIDER }.forEach {
        runCatching { lm.requestLocationUpdates(it, 15_000L, 25f, listener, Looper.getMainLooper()) }
    }
    return { lm.removeUpdates(listener) }
}

fun distanceMeters(a: GeoPoint, lat: Double, lon: Double): Float {
    val out = FloatArray(1)
    Location.distanceBetween(a.lat, a.lon, lat, lon, out)
    return out[0]
}

fun formatDistance(m: Float): String =
    when {
        m < 1000 -> "${(m / 10).toInt() * 10} m"
        m < 100_000 -> String.format(java.util.Locale.ITALY, "%.1f km", m / 1000)
        else -> "${(m / 1000).toInt()} km"
    }
