package com.josh.tvhome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.util.Locale
import kotlinx.coroutines.delay
import java.time.format.TextStyle as JTextStyle

private val Black = Color(0xFF000000)
private val White = Color(0xFFFFFFFF)
private val Dim = Color(0xFF8A8A8A)
private val GridDot = Color(0xFF1F1F1F)

// Sizes are dp. A 1080p TV renders at 960 x 540 dp, so these are half the px values in the concept.
private val RowPitch = 66.dp

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private val Doto = FontFamily(
    Font(R.font.doto, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.doto, FontWeight.Black, variationSettings = FontVariation.Settings(FontVariation.weight(900))),
)
private val Mono = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold),
)

@Composable
fun HomeScreen(apps: List<TvApp>, onOpen: (TvApp) -> Unit) {
    var focused by remember { mutableIntStateOf(0) }
    var launching by remember { mutableStateOf<TvApp?>(null) }
    var temp by remember { mutableStateOf<Int?>(null) }
    val requester = remember { FocusRequester() }
    LaunchedEffect(Unit) { requester.requestFocus() }

    // Refresh the outside temperature every 15 minutes; keep the last value if a request fails.
    LaunchedEffect(Unit) {
        while (true) {
            Weather.fetchFahrenheit()?.let { temp = it }
            delay(15 * 60_000L)
        }
    }

    val offset by animateDpAsState(RowPitch - RowPitch * focused, tween(450), label = "offset")
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(15_000)
        }
    }

    LaunchedEffect(launching) {
        val app = launching ?: return@LaunchedEffect
        delay(450)
        onOpen(app)
        delay(600)
        launching = null
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Black)
            .drawBehind {
                val step = 16.dp.toPx()
                val r = 1.dp.toPx()
                var y = step / 2
                while (y < size.height) {
                    var x = step / 2
                    while (x < size.width) {
                        drawCircle(GridDot, r, Offset(x, y))
                        x += step
                    }
                    y += step
                }
            }
            .focusRequester(requester)
            .focusable()
            .onPreviewKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown || launching != null || apps.isEmpty()) {
                    false
                } else when (e.key) {
                    Key.DirectionDown -> { focused = (focused + 1).coerceAtMost(apps.lastIndex); true }
                    Key.DirectionUp -> { focused = (focused - 1).coerceAtLeast(0); true }
                    Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> { launching = apps[focused]; true }
                    else -> false
                }
            }
    ) {
        label("HOME", Modifier.align(Alignment.TopStart).padding(start = 60.dp, top = 36.dp), 10.sp)

        // Clock, date, temperature
        Box(Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 60.dp)) {
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.End) {
                Row {
                    BasicText(
                        "%d:%02d".format((now.hour + 11) % 12 + 1, now.minute),
                        modifier = Modifier.alignByBaseline(),
                        style = TextStyle(fontFamily = Doto, fontWeight = FontWeight.Black, fontSize = 60.sp, letterSpacing = 0.04.em, color = White),
                    )
                    label(if (now.hour < 12) "AM" else "PM", Modifier.alignByBaseline().padding(start = 10.dp), 14.sp)
                }
                label(dateText(now), Modifier.padding(top = 6.dp), 11.sp)
                Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    label(BuildConfig.TV_LABEL, Modifier.padding(end = 12.dp), 10.sp)
                    BasicText(
                        temp?.let { "$it°F" } ?: "--°F",
                        style = TextStyle(fontFamily = Doto, fontWeight = FontWeight.Black, fontSize = 36.sp, letterSpacing = 0.02.em, color = White),
                    )
                }
            }
        }

        // App list, masked to fade at the top and bottom
        Box(
            Modifier
                .padding(start = 60.dp, top = 125.dp)
                .width(600.dp)
                .height(330.dp)
                .clipToBounds()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.18f to Color.Black,
                            0.66f to Color.Black,
                            1.0f to Color.Transparent,
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                }
        ) {
            Box(Modifier.offset(y = offset)) {
                apps.forEachIndexed { i, app ->
                    val isFocused = i == focused
                    Row(
                        Modifier.offset(y = RowPitch * i).height(RowPitch),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isFocused) {
                            Box(Modifier.size(18.dp).clip(CircleShape).background(Color(app.accent)))
                        } else {
                            Box(Modifier.size(18.dp).border(1.dp, Dim, CircleShape))
                        }
                        BasicText(
                            app.label,
                            modifier = Modifier.padding(start = 18.dp),
                            maxLines = 1,
                            style = TextStyle(
                                fontFamily = Doto,
                                fontWeight = if (isFocused) FontWeight.Black else FontWeight.Normal,
                                fontSize = 56.sp,
                                letterSpacing = 0.02.em,
                                color = if (isFocused) Color(app.accent) else Dim,
                            ),
                        )
                    }
                }
            }
        }

        label("▲ ▼  SELECT  /  OK OPEN", Modifier.align(Alignment.BottomStart).padding(start = 60.dp, bottom = 36.dp), 10.sp)
        label(
            "%02d / %02d".format(if (apps.isEmpty()) 0 else focused + 1, apps.size),
            Modifier.align(Alignment.BottomEnd).padding(end = 60.dp, bottom = 36.dp),
            10.sp,
        )

        // Launch flash: wipe in the app's brand color from the left with the app name
        AnimatedVisibility(
            visible = launching != null,
            enter = expandHorizontally(expandFrom = Alignment.Start, animationSpec = tween(350)),
        ) {
            // Keep the last app while the wipe animates out after launching is cleared
            val app = remember { mutableStateOf<TvApp?>(null) }.apply { launching?.let { value = it } }.value
            val wipe = app?.let { Color(it.accent) } ?: Black
            Box(Modifier.fillMaxSize().background(wipe)) {
                BasicText(
                    app?.label.orEmpty(),
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 60.dp),
                    // Black text on light brand colors (Hulu, Peacock, Starz) so the name stays readable
                    style = TextStyle(fontFamily = Doto, fontWeight = FontWeight.Black, fontSize = 56.sp, color = if (wipe.luminance() > 0.4f) Black else White),
                )
            }
        }
    }
}

@Composable
private fun label(text: String, modifier: Modifier, size: androidx.compose.ui.unit.TextUnit) {
    BasicText(
        text,
        modifier = modifier,
        style = TextStyle(fontFamily = Mono, fontSize = size, letterSpacing = 0.2.em, color = Dim),
    )
}

private fun dateText(t: LocalDateTime): String {
    val dow = t.dayOfWeek.getDisplayName(JTextStyle.SHORT, Locale.US).uppercase()
    val mon = t.month.getDisplayName(JTextStyle.SHORT, Locale.US).uppercase()
    return "%s %02d %s".format(dow, t.dayOfMonth, mon)
}
