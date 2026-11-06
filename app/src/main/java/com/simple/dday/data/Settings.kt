package com.simple.dday.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode(val label: String) {
    SYSTEM("시스템 설정"),
    LIGHT("라이트"),
    DARK("다크"),
}

enum class SortOrder(val label: String) {
    NEAREST("가까운 순"),
    CREATED("만든 순"),
    NAME("이름 순"),
}

/** 앱 설정. 바꾸면 바로 저장하고 화면에 반영된다 */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var themeMode: ThemeMode by mutableStateOf(read(KEY_THEME, ThemeMode.SYSTEM))
        private set

    var sortOrder: SortOrder by mutableStateOf(read(KEY_SORT, SortOrder.NEAREST))
        private set

    /** 홈 화면 맨 위에 보여줄 제목. 비어 있으면 오늘 날짜가 그 자리에 온다 */
    var homeTitle: String by mutableStateOf(prefs.getString(KEY_TITLE, "") ?: "")
        private set

    fun changeHomeTitle(text: String) {
        homeTitle = text
        prefs.edit().putString(KEY_TITLE, text).apply()
    }

    fun setTheme(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    fun setSort(order: SortOrder) {
        sortOrder = order
        prefs.edit().putString(KEY_SORT, order.name).apply()
    }

    private inline fun <reified T : Enum<T>> read(key: String, default: T): T =
        prefs.getString(key, null)?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_SORT = "sort"
        const val KEY_TITLE = "homeTitle"
    }
}
