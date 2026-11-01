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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.simple.dday.data.EventStore
import com.simple.dday.ui.DetailScreen
import com.simple.dday.ui.EditScreen
import com.simple.dday.ui.HomeScreen
import com.simple.dday.ui.theme.DDayTheme
import com.simple.dday.ui.theme.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 흰 배경이므로 상태바·내비게이션바 아이콘을 항상 어둡게
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        val store = EventStore(applicationContext)
        setContent {
            DDayTheme { App(store) }
        }
    }
}

private const val HOME = "home"
private const val EDIT_NEW = "edit/new"

/** 화면 이동은 경로 문자열 스택으로 관리한다: home, detail/{id}, edit/new, edit/{id} */
@Composable
private fun App(store: EventStore) {
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
                onAdd = { push(EDIT_NEW) },
                onOpen = { push("detail/${it.id}") },
            )

            route.startsWith("detail/") -> {
                val event = id?.let(store::find)
                if (event == null) {
                    Blank()
                } else {
                    DetailScreen(event, onBack = { pop() }, onEdit = { push("edit/${event.id}") })
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
            .background(White),
    )
}
