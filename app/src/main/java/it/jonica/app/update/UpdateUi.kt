package it.jonica.app.update

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import it.jonica.app.R
import it.jonica.app.ui.components.ScreenPadding
import it.jonica.app.ui.components.bouncyClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val release: Updater.Release) : UpdateState
    data class Downloading(val release: Updater.Release, val progress: Float) : UpdateState
    data class Ready(val release: Updater.Release, val file: File) : UpdateState
    data class Failed(val message: String) : UpdateState
}

/** Stato condiviso degli aggiornamenti: controllo automatico all'avvio e manuale da Filiera. */
class UpdateController(private val scope: CoroutineScope) {
    var state by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set
    var sheetVisible by mutableStateOf(false)
    private var job: Job? = null
    private var installWhenAllowed = false

    fun check(manual: Boolean) {
        if (job?.isActive == true) return
        job = scope.launch {
            state = UpdateState.Checking
            state = runCatching { Updater.checkForUpdate() }.fold(
                onSuccess = { r -> if (r != null) UpdateState.Available(r) else UpdateState.UpToDate },
                onFailure = { UpdateState.Failed("Impossibile controllare gli aggiornamenti. Verifica la connessione.") },
            )
            // all'avvio la scheda compare solo se c'è davvero qualcosa di nuovo
            if (manual || state is UpdateState.Available) sheetVisible = true
        }
    }

    fun download(context: Context, release: Updater.Release) {
        job = scope.launch {
            state = UpdateState.Downloading(release, 0f)
            state = runCatching {
                Updater.download(context, release) { p -> state = UpdateState.Downloading(release, p) }
            }.fold(
                onSuccess = { UpdateState.Ready(release, it) },
                onFailure = { UpdateState.Failed("Download non riuscito. Riprova tra poco.") },
            )
            (state as? UpdateState.Ready)?.let { install(context, it.file) }
        }
    }

    fun install(context: Context, file: File) {
        if (Updater.canInstall(context)) {
            Updater.install(context, file)
        } else {
            installWhenAllowed = true
            Updater.openInstallPermissionSettings(context)
        }
    }

    /** Al ritorno dalle impostazioni, se il permesso è stato concesso, installa. */
    fun onResume(context: Context) {
        val ready = state as? UpdateState.Ready ?: return
        if (installWhenAllowed && Updater.canInstall(context)) {
            installWhenAllowed = false
            Updater.install(context, ready.file)
        }
    }

    fun dismiss() {
        sheetVisible = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateSheet(controller: UpdateController) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) controller.onResume(context) }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }
    if (!controller.sheetVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = controller::dismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = controller.state::class,
                transitionSpec = { fadeIn(tween(250)).togetherWith(fadeOut(tween(150))) },
                label = "update",
            ) { _ ->
                when (val s = controller.state) {
                    UpdateState.Idle, UpdateState.Checking -> Status(
                        title = "Controllo in corso…",
                        text = "Sto cercando la versione più recente di Jonica.",
                        busy = true,
                    )
                    UpdateState.UpToDate -> Status(
                        title = "Sei aggiornato",
                        text = "Hai già l'ultima versione di Jonica (${Updater.currentVersion}).",
                        icon = Icons.Rounded.CheckCircle,
                    ) { PrimaryButton("Ok", Icons.Rounded.CheckCircle, controller::dismiss) }
                    is UpdateState.Failed -> Status(
                        title = "Qualcosa è andato storto",
                        text = s.message,
                        icon = Icons.Rounded.Refresh,
                    ) { PrimaryButton("Riprova", Icons.Rounded.Refresh) { controller.check(manual = true) } }
                    is UpdateState.Available -> Available(s.release, onLater = controller::dismiss) {
                        controller.download(context, s.release)
                    }
                    is UpdateState.Downloading -> Downloading(s.release, s.progress)
                    is UpdateState.Ready -> Status(
                        title = "Pronto da installare",
                        text = "Jonica ${s.release.version} è stata scaricata. Se Android te lo chiede, consenti a Jonica di installare app: serve solo per gli aggiornamenti.",
                        icon = Icons.Rounded.InstallMobile,
                    ) { PrimaryButton("Installa", Icons.Rounded.InstallMobile) { controller.install(context, s.file) } }
                }
            }
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Box(
        Modifier.size(88.dp).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.size(88.dp))
    }
    Spacer(Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
    Spacer(Modifier.height(4.dp))
    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Available(release: Updater.Release, onLater: () -> Unit, onUpdate: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Header("Nuova versione disponibile", "Jonica ${Updater.currentVersion}  →  ${release.version}")
        if (release.notes.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Novità", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                notesLines(release.notes).forEach { (bullet, line) ->
                    Row {
                        if (bullet) {
                            Box(
                                Modifier
                                    .padding(top = 7.dp, end = 10.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                        }
                        Text(line, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            "Aggiorna" + if (release.sizeBytes > 0) " · ${"%.1f".format(release.sizeBytes / 1_048_576f)} MB" else "",
            Icons.Rounded.Download,
            onUpdate,
        )
        TextButton(onClick = onLater, modifier = Modifier.padding(top = 4.dp)) {
            Text("Più tardi", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Downloading(release: Updater.Release, progress: Float) {
    val p by animateFloatAsState(progress, tween(200), label = "dl")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Header("Download in corso", "Jonica ${release.version}")
        Spacer(Modifier.height(24.dp))
        LinearProgressIndicator(
            progress = { p },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            drawStopIndicator = {},
        )
        Spacer(Modifier.height(10.dp))
        Text("${(p * 100).toInt()}%", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun Status(
    title: String,
    text: String,
    busy: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (busy) {
                CircularProgressIndicator(Modifier.size(36.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, strokeWidth = 3.dp)
            } else if (icon != null) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}

@Composable
private fun PrimaryButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier.fillMaxWidth().height(54.dp),
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold)
    }
}

/** Righe delle note di rilascio (Markdown semplice) con flag "elenco puntato". */
private fun notesLines(md: String): List<Pair<Boolean, String>> =
    md.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { l ->
            val bullet = l.startsWith("- ") || l.startsWith("* ")
            bullet to l.removePrefix("- ").removePrefix("* ").replace("**", "").replace("#", "").trim()
        }

/** Card "Info e aggiornamenti" in fondo alla schermata Filiera. */
@Composable
fun UpdateCard(controller: UpdateController, modifier: Modifier = Modifier) {
    val available = controller.state as? UpdateState.Available
    Row(
        modifier
            .padding(horizontal = ScreenPadding)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .bouncyClick {
                if (available != null) controller.sheetVisible = true else controller.check(manual = true)
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(R.drawable.logo), null, Modifier.size(52.dp).clip(CircleShape))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Jonica ${Updater.currentVersion}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                if (available != null) "Disponibile la versione ${available.release.version}" else "Tocca per controllare gli aggiornamenti",
                style = MaterialTheme.typography.bodySmall,
                color = if (available != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (controller.state is UpdateState.Checking) {
                CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.SystemUpdate, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }
        }
    }
}
