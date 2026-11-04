package it.jonica.app.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.jonica.app.data.Product
import it.jonica.app.location.GeoPoint
import it.jonica.app.location.hasLocationPermission
import it.jonica.app.location.observeLocation
import it.jonica.app.ui.aware.AwareScreen
import it.jonica.app.ui.components.bouncyClick
import it.jonica.app.ui.season.ProductSheet
import it.jonica.app.ui.season.SeasonScreen
import it.jonica.app.ui.shops.ShopsScreen
import it.jonica.app.ui.today.TodayScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Tab(val label: String, val icon: ImageVector) {
    OGGI("Oggi", Icons.Rounded.WbSunny),
    STAGIONI("Stagioni", Icons.Rounded.CalendarMonth),
    BOTTEGHE("Botteghe", Icons.Rounded.Storefront),
    FILIERA("Filiera", Icons.Rounded.Favorite),
}

@Composable
fun JonicaApp() {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var location by remember { mutableStateOf<GeoPoint?>(null) }
    var permissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        permissionGranted = res.values.any { it }
    }

    DisposableEffect(permissionGranted) {
        val stop = observeLocation(context) { location = it }
        onDispose { stop() }
    }

    LaunchedEffect(showSplash) {
        if (!showSplash && !permissionGranted) {
            delay(600)
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(tween(380, easing = FastOutSlowInEasing)) { it / 6 * dir } + fadeIn(tween(320)))
                    .togetherWith(slideOutHorizontally(tween(320)) { -it / 8 * dir } + fadeOut(tween(220)))
            },
            label = "tabs",
        ) { t ->
            when (Tab.entries[t]) {
                Tab.OGGI -> TodayScreen(
                    location = location,
                    onProduct = { selectedProduct = it },
                    onOpenSeasons = { tab = Tab.STAGIONI.ordinal },
                    onOpenShops = { tab = Tab.BOTTEGHE.ordinal },
                )
                Tab.STAGIONI -> SeasonScreen(onProduct = { selectedProduct = it })
                Tab.BOTTEGHE -> ShopsScreen(
                    location = location,
                    permissionGranted = permissionGranted,
                    onRequestPermission = {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    },
                )
                Tab.FILIERA -> AwareScreen()
            }
        }

        BottomBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        selectedProduct?.let { p -> ProductSheet(p) { selectedProduct = null } }

        AnimatedVisibility(visible = showSplash, enter = fadeIn(), exit = fadeOut(tween(500))) {
            Splash(onDone = { showSplash = false })
        }
    }
}

@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = 0.25f)),
        ) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 8.dp)) {
                Tab.entries.forEachIndexed { i, t ->
                    val isSel = i == selected
                    val bg by animateColorAsState(
                        if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                        tween(300), label = "bg",
                    )
                    val fg by animateColorAsState(
                        if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        tween(300), label = "fg",
                    )
                    val label by animateColorAsState(
                        if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        tween(300), label = "lb",
                    )
                    val scale by androidx.compose.animation.core.animateFloatAsState(
                        if (isSel) 1f else 0.6f,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "ind",
                    )
                    Column(
                        Modifier.weight(1f).bouncyClick { onSelect(i) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier
                                .width(56.dp)
                                .height(32.dp)
                                .graphicsLayer { scaleX = scale }
                                .clip(RoundedCornerShape(16.dp))
                                .background(bg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(t.icon, contentDescription = t.label, tint = fg, modifier = Modifier.graphicsLayer { scaleX = 1f / scale })
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            t.label,
                            color = label,
                            fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Splash(onDone: () -> Unit) {
    val leaf = remember { Animatable(0f) }
    val text = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { leaf.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 120f)) }
        delay(350)
        text.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        delay(700)
        onDone()
    }
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.foundation.Image(
                androidx.compose.ui.res.painterResource(it.jonica.app.R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(132.dp)
                    .graphicsLayer {
                        scaleX = leaf.value; scaleY = leaf.value
                        rotationZ = (1f - leaf.value) * -40f
                    },
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Jonica",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.graphicsLayer { alpha = text.value; translationY = (1f - text.value) * 40f },
            )
            Text(
                "Mangia di stagione. Compra vicino.",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.graphicsLayer { alpha = text.value },
            )
        }
    }
}
