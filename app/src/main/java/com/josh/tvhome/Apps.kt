package com.josh.tvhome

import android.content.Context
import android.content.Intent

data class TvApp(
    val label: String,
    val packageName: String,
    val launch: Intent,
    val accent: Int,
)

// Apps shown first, in this order. Everything else follows alphabetically.
private val pinned = listOf(
    "Netflix", "YouTube", "Prime Video", "Disney+", "Max",
    "Hulu", "Apple TV", "Spotify", "Plex",
)

// Brand color per package, used for the focus dot, focused label and launch wipe.
// Black brand colors (Peacock, Starz) use their accent instead so they show on the black background.
private val accents = mapOf(
    "com.netflix.ninja" to 0xFFE50914,
    "com.google.android.youtube.tv" to 0xFFFF0000,
    "com.google.android.youtube.tvunplugged" to 0xFFFF0000,
    "com.google.android.youtube.tvmusic" to 0xFFFF0000,
    "com.amazon.amazonvideo.livingroom" to 0xFF1A98FF,
    "com.disney.disneyplus" to 0xFF113CCF,
    "com.wbd.stream" to 0xFF002BE7,
    "com.hulu.livingroomplus" to 0xFF1CE783,
    "com.apple.atve.androidtv.appletv" to 0xFF8E8E93,
    "com.spotify.tv.android" to 0xFF1DB954,
    "com.peacocktv.peacockandroid" to 0xFFFFC20E,
    "com.cbs.ott" to 0xFF0064FF,
    "com.crunchyroll.crunchyroid" to 0xFFF47521,
    "com.bydeluxe.d3.android.program.starz" to 0xFFFFFFFF,
    "com.dramafever.shudder" to 0xFFE10000,
    "tv.twitch.android.app" to 0xFF9146FF,
    "com.soundcloud.android" to 0xFFFF5500,
    "com.att.tv" to 0xFF00A6D6,
    "com.patreon.android" to 0xFFFF424D,
    "com.google.android.videos" to 0xFF4285F4,
    "com.plexapp.android" to 0xFFE5A00D,
).mapValues { it.value.toInt() }

// Used for any app without a brand color above.
private val DEFAULT_ACCENT = 0xFF0000FF.toInt()

// Package names to hide from the home screen. Fill in after the package inventory.
private val hidden = setOf<String>()

fun loadApps(ctx: Context): List<TvApp> {
    val pm = ctx.packageManager
    val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
    val found = pm.queryIntentActivities(query, 0)
        .filter { it.activityInfo.packageName != ctx.packageName }
        .filter { it.activityInfo.packageName !in hidden }
        .mapNotNull { ri ->
            val pkg = ri.activityInfo.packageName
            val intent = pm.getLeanbackLaunchIntentForPackage(pkg) ?: return@mapNotNull null
            val label = ri.loadLabel(pm).toString()
            TvApp(
                label = label,
                packageName = pkg,
                launch = intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                accent = accents[pkg] ?: DEFAULT_ACCENT,
            )
        }
        .distinctBy { it.packageName }

    fun rank(app: TvApp): Int = pinned.indexOf(app.label).let { if (it < 0) Int.MAX_VALUE else it }
    return found.sortedWith(compareBy<TvApp> { rank(it) }.thenBy { it.label.lowercase() })
}
