package com.simple.dday

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.simple.dday.data.EventStore
import com.simple.dday.data.Settings
import com.simple.dday.data.ThemeMode
import com.simple.dday.ui.ArchiveScreen
import com.simple.dday.ui.DetailScreen
import com.simple.dday.ui.EditScreen
import com.simple.dday.ui.HomeScreen
import com.simple.dday.ui.LicensesScreen
import com.simple.dday.ui.SettingsScreen
import com.simple.dday.ui.theme.DDayTheme
import com.simple.dday.ui.theme.Paper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = EventStore(applicationContext)
        val settings = Settings(applicationContext)
        setContent {
            val dark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // 상태바·내비게이션바 아이콘 색을 화면 바탕에 맞춘다
            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }
            DDayTheme(dark) { App(store, settings) }
        }
    }
}

private const val HOME = "home"
private const val EDIT_NEW = "edit/new"
private const val SETTINGS = "settings"
private const val ARCHIVE = "archive"
private const val LICENSES = "licenses"

/** 화면 이동은 경로 문자열 스택으로 관리한다: home, detail/{id}, edit/new, edit/{id}, settings, archive, licenses */
@Composable
private fun App(store: EventStore, settings: Settings) {
    var stack by rememberSaveable { mutableStateOf(listOf(HOME)) }
    fun push(route: String) {
        stack = stack + route
    }
    fun pop(count: Int = 1) {
        stack = stack.dropLast(count).ifEmpty { listOf(HOME) }
    }
    BackHandler(enabled = stack.size > 1) { pop() }

    AnimatedContent(
        targetState = stack.last(),
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
        label = "screen",
    ) { route ->
        val id = route.substringAfter('/', "").toLongOrNull()
        when {
            route == HOME -> HomeScreen(
                events = store.events,
                title = settings.homeTitle,
                sortOrder = settings.sortOrder,
                onSort = settings::setSort,
                onAdd = { push(EDIT_NEW) },
                onOpen = { push("detail/${it.id}") },
                onSettings = { push(SETTINGS) },
                onArchive = { push(ARCHIVE) },
            )

            route == SETTINGS -> SettingsScreen(
                settings = settings,
                store = store,
                onBack = { pop() },
                onOpenArchive = { push(ARCHIVE) },
                onOpenLicenses = { push(LICENSES) },
            )

            route == ARCHIVE -> ArchiveScreen(store.events, onBack = { pop() }, onOpen = { push("detail/${it.id}") })

            route == LICENSES -> LicensesScreen(onBack = { pop() })

            route.startsWith("detail/") -> {
                val event = id?.let(store::find)
                if (event == null) {
                    Blank()
                } else {
                    DetailScreen(
                        event,
                        onBack = { pop() },
                        onEdit = { push("edit/${event.id}") },
                        onSetPinned = { store.upsert(event.copy(pinned = it)) },
                        onSetArchived = { archived ->
                            // 보관함에 넣을 때는 고정도 푼다
                            store.upsert(event.copy(archived = archived, pinned = event.pinned && !archived))
                            pop()
                        },
                    )
                }
            }

            else -> {
                val event = id?.let(store::find)
                if (id != null && event == null) {
                    // 삭제 직후 사라지는 화면
                    Blank()
                } else {
                    EditScreen(
                        initial = event,
                        newId = store::nextId,
                        onClose = { pop() },
                        onSave = { store.upsert(it); pop() },
                        onDelete = { store.delete(id!!); pop(2) },
                        onSetArchived = { archived ->
                            store.upsert(event!!.copy(archived = archived, pinned = event.pinned && !archived))
                            pop(2)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun Blank() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Paper),
    )
}
