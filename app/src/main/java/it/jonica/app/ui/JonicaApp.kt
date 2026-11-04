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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import it.jonica.app.update.UpdateController
import it.jonica.app.update.UpdateSheet
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
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val updates = remember { UpdateController(scope) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        permissionGranted = res.values.any { it }
    }

    DisposableEffect(permissionGranted) {
        val stop = observeLocation(context) { location = it }
        onDispose { stop() }
    }

    LaunchedEffect(showSplash) {
        if (!showSplash) updates.check(manual = false)
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
                Tab.FILIERA -> AwareScreen(updates)
            }
        }

        BottomBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        selectedProduct?.let { p -> ProductSheet(p) { selectedProduct = null } }

        if (!showSplash) UpdateSheet(updates)

        AnimatedVisibility(visible = showSplash, enter = fadeIn(), exit = fadeOut(tween(500))) {
            Splash(onDone = { showSplash = false })
        }
    }
}

/**
 * Barra fluttuante con pillola "liquida": i due bordi della pillola si muovono con molle diverse,
 * quello di testa scatta verso la destinazione e quello di coda lo insegue, così la pillola si
 * allunga, si assottiglia e rimbalza come una goccia di slime.
 */
@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val pillColor = MaterialTheme.colorScheme.primary
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black.copy(alpha = 0.25f)),
        ) {
            BoxWithConstraints(Modifier.padding(6.dp)) {
                val count = Tab.entries.size
                val slot = constraints.maxWidth.toFloat() / count
                val left = remember { Animatable(selected * slot) }
                val right = remember { Animatable((selected + 1) * slot) }
                LaunchedEffect(selected, slot) {
                    val targetLeft = selected * slot
                    val targetRight = (selected + 1) * slot
                    val goingRight = targetLeft > left.value
                    val lead = spring<Float>(dampingRatio = 0.6f, stiffness = 700f)
                    val trail = spring<Float>(dampingRatio = 0.45f, stiffness = 160f)
                    launch { left.animateTo(targetLeft, if (goingRight) trail else lead) }
                    launch { right.animateTo(targetRight, if (goingRight) lead else trail) }
                }
                Canvas(Modifier.matchParentSize()) {
                    val width = (right.value - left.value).coerceAtLeast(slot * 0.6f)
                    // più si allunga, più si assottiglia (conservazione del "volume")
                    val stretch = ((width - slot) / slot).coerceIn(-0.4f, 2f)
                    val h = size.height * (1f - 0.22f * stretch).coerceIn(0.62f, 1.08f)
                    val top = (size.height - h) / 2f
                    drawRoundRect(
                        color = pillColor,
                        topLeft = Offset(left.value, top),
                        size = Size(width, h),
                        cornerRadius = CornerRadius(h / 2f),
                    )
                }
                Row {
                    Tab.entries.forEachIndexed { i, t ->
                        val isSel = i == selected
                        val fg by animateColorAsState(
                            if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            tween(260, delayMillis = if (isSel) 90 else 0), label = "fg",
                        )
                        val lift by animateFloatAsState(
                            if (isSel) 1f else 0f,
                            spring(dampingRatio = 0.4f, stiffness = 300f),
                            label = "lift",
                        )
                        Column(
                            Modifier
                                .weight(1f)
                                .height(58.dp)
                                .bouncyClick { onSelect(i) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                t.icon,
                                contentDescription = t.label,
                                tint = fg,
                                modifier = Modifier.graphicsLayer {
                                    val s = 1f + 0.12f * lift
                                    scaleX = s; scaleY = s
                                    translationY = -2.dp.toPx() * lift
                                },
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                t.label,
                                color = fg,
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
        Modifier.fillMaxSize().background(it.jonica.app.ui.theme.Olive),
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
                color = it.jonica.app.ui.theme.Cream,
                modifier = Modifier.graphicsLayer { alpha = text.value; translationY = (1f - text.value) * 40f },
            )
            Text(
                "Mangia di stagione. Compra vicino.",
                color = it.jonica.app.ui.theme.Cream.copy(alpha = 0.8f),
                modifier = Modifier.graphicsLayer { alpha = text.value },
            )
        }
    }
}
