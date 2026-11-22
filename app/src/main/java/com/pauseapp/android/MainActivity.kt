package com.pauseapp.android

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import kotlinx.coroutines.delay

private const val FILTER_HOST = "family.cloudflare-dns.com"
private const val FILTER_TEST = "https://nudity.testcategory.com/"
private const val RESET_MILLIS = 60_000L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val checkIns = CheckInStore(getSharedPreferences("check_ins", MODE_PRIVATE))
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Violet,
                    onPrimary = Color.White,
                    background = Paper,
                    onBackground = Ink,
                    surface = Paper,
                    onSurface = Ink,
                    onSurfaceVariant = Muted,
                    secondaryContainer = Lime,
                    onSecondaryContainer = Ink,
                    surfaceContainerLow = Lavender,
                    surfaceContainerHigh = Peach,
                ),
            ) {
                PauseApp(
                    checkIns = checkIns,
                    copyHostname = {
                        getSystemService(ClipboardManager::class.java)
                            .setPrimaryClip(ClipData.newPlainText("Private DNS hostname", FILTER_HOST))
                    },
                    openSettings = {
                        tryOpen(Intent(Settings.ACTION_WIRELESS_SETTINGS)) ||
                            tryOpen(Intent(Settings.ACTION_SETTINGS))
                    },
                    openTest = { tryOpen(Intent(Intent.ACTION_VIEW, FILTER_TEST.toUri())) },
                )
            }
        }
    }

    private fun tryOpen(intent: Intent): Boolean = try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}

@Composable
private fun PauseApp(
    checkIns: CheckInStore,
    copyHostname: () -> Unit,
    openSettings: () -> Boolean,
    openTest: () -> Boolean,
) {
    var screen by rememberSaveable { mutableStateOf("home") }
    var resetDeadline by rememberSaveable { mutableLongStateOf(0L) }
    var resetAction by rememberSaveable { mutableStateOf(checkIns.resetAction()) }

    BackHandler(enabled = screen != "home") { screen = "home" }

    val scrollState = key(screen) { rememberScrollState() }

    Scaffold(containerColor = Paper, contentWindowInsets = WindowInsets.safeDrawing) { insets ->
        Box(
            modifier = Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 540.dp).fillMaxWidth()
                    .verticalScroll(scrollState).padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (screen == "home") {
                    PauseWordmark()
                    HomeScreen(
                        onReset = {
                            if (resetDeadline <= SystemClock.elapsedRealtime()) {
                                resetDeadline = SystemClock.elapsedRealtime() + RESET_MILLIS
                            }
                            screen = "reset"
                        },
                        onSetup = { screen = "setup" },
                        onCheckIn = { screen = "check-in" },
                    )
                } else {
                    TextButton(onClick = { screen = "home" }) { Text("←  Back to home") }
                    when (screen) {
                        "setup" -> SetupScreen(copyHostname, openSettings, openTest)
                        "reset" -> ResetScreen(
                            deadline = resetDeadline,
                            action = resetAction,
                            onAction = {
                                resetAction = it
                                checkIns.saveResetAction(it)
                            },
                            onCheckIn = { screen = "check-in" },
                            onDone = { screen = "home" },
                        )
                        "check-in" -> CheckInScreen(checkIns)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(onReset: () -> Unit, onSetup: () -> Unit, onCheckIn: () -> Unit) {
    Surface(color = Night, contentColor = Paper, shape = RoundedCornerShape(30.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(HeroBrush).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OrbitArt(Modifier.fillMaxWidth().height(144.dp))
            Text(
                "Take a minute.",
                modifier = Modifier.align(Alignment.Start),
                fontFamily = FontFamily.Serif,
                fontSize = 47.sp,
                lineHeight = 49.sp,
            )
            Text(
                "When an urge hits, take 60 seconds before you act.",
                modifier = Modifier.align(Alignment.Start),
                color = Color(0xFFF0E7FC),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Ink),
            ) { Text("Help me reset", fontWeight = FontWeight.Bold) }
        }
    }
    HomeAction(
        title = "Adult-site filter",
        detail = "Set up or test blocking",
        color = Lavender,
        isFilter = true,
        onClick = onSetup,
    )
    HomeAction(
        title = "Private check-in",
        detail = "Notice what helped",
        color = Peach,
        isFilter = false,
        onClick = onCheckIn,
    )
}

@Composable
private fun HomeAction(title: String, detail: String, color: Color, isFilter: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, color = color, contentColor = Ink, shape = RoundedCornerShape(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(Paper),
                contentAlignment = Alignment.Center,
            ) {
                ActionIcon(isFilter, Modifier.size(23.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
            CanvasArrow()
        }
    }
}

@Composable
private fun Section(title: String, description: String, content: @Composable () -> Unit) {
    Surface(color = Lavender, contentColor = Ink, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(description, color = Muted, style = MaterialTheme.typography.bodyMedium)
            content()
        }
    }
}

@Composable
private fun SetupScreen(copyHostname: () -> Unit, openSettings: () -> Boolean, openTest: () -> Boolean) {
    var setupMessage by rememberSaveable { mutableStateOf("") }
    var testMessage by rememberSaveable { mutableStateOf("") }
    ScreenTitle("Adult-site filter")
    Section("Set up Private DNS", "In Android Settings, look for Private DNS. On Samsung, check Connections → More connection settings.") {
        Text("Choose Private DNS provider hostname, paste this address, then save.")
        Surface(color = Paper, shape = RoundedCornerShape(12.dp)) {
            Text(
                FILTER_HOST,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                fontFamily = FontFamily.Monospace,
                color = Ink,
            )
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                copyHostname()
                setupMessage = "Hostname copied."
            },
        ) { Text("Copy hostname") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { if (!openSettings()) setupMessage = "Open Android Settings and search for Private DNS." },
        ) { Text("Open Android settings") }
        if (setupMessage.isNotEmpty()) Text(setupMessage, color = Muted)
    }
    Section("Check in your browser", "If normal websites load and Cloudflare’s test page fails to load, the filter is likely working in that browser. Try Wi-Fi and mobile data separately.") {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                testMessage = if (openTest()) "" else "Open a browser and visit nudity.testcategory.com."
            },
        ) { Text("Open safe test page") }
        if (testMessage.isNotEmpty()) Text(testMessage, color = Muted)
    }
    Text("The filter works on domains. Individual posts inside apps can still appear.", color = Muted)
}

@Composable
private fun ResetScreen(
    deadline: Long,
    action: String,
    onAction: (String) -> Unit,
    onCheckIn: () -> Unit,
    onDone: () -> Unit,
) {
    var remaining by remember(deadline) { mutableLongStateOf(remainingSeconds(deadline)) }
    LaunchedEffect(deadline) {
        while (remaining > 0) {
            delay(100)
            remaining = remainingSeconds(deadline)
        }
    }
    val running = remaining > 0
    val progress by animateFloatAsState(
        targetValue = remaining / 60f,
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label = "Time remaining",
    )
    ScreenTitle(if (running) "Take a breath." else "Minute complete")
    Surface(color = Night, contentColor = Paper, shape = RoundedCornerShape(30.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(HeroBrush).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BreathingRing(running, progress, Modifier.size(210.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (remaining == 60L) "1:00" else "0:${remaining.toString().padStart(2, '0')}",
                        fontSize = 42.sp,
                        lineHeight = 47.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        if (running) "Follow the glow" else "You took a minute",
                        style = MaterialTheme.typography.labelLarge,
                        color = Lime,
                    )
                }
            }
            Text(action, style = MaterialTheme.typography.titleMedium)
        }
    }
    if (remaining == 0L) {
        Button(onClick = onCheckIn, modifier = Modifier.fillMaxWidth()) { Text("Add an optional check-in") }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    } else {
        Section("Choose a different action", "Pick something you can do right now.") {
            resetActions.forEach { option ->
                if (option != action) {
                    OutlinedButton(onClick = { onAction(option) }, modifier = Modifier.fillMaxWidth()) {
                        Text(option)
                    }
                }
            }
        }
    }
}

private fun remainingSeconds(deadline: Long): Long =
    ((deadline - SystemClock.elapsedRealtime() + 999L) / 1_000L).coerceIn(0L, 60L)

@Composable
internal fun ScreenTitle(title: String) {
    Text(title, style = MaterialTheme.typography.headlineLarge, fontFamily = FontFamily.Serif)
}
