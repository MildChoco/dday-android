package com.simple.dday.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray

/** 항목 목록을 SharedPreferences에 JSON으로 저장한다. 화면은 [events]를 그대로 읽는다. */
class EventStore(context: Context) {
    private val prefs = context.getSharedPreferences("dday", Context.MODE_PRIVATE)

    var events: List<Event> by mutableStateOf(load())
        private set

    fun find(id: Long): Event? = events.firstOrNull { it.id == id }

    fun nextId(): Long = (events.maxOfOrNull { it.id } ?: 0L) + 1

    fun upsert(event: Event) {
        val i = events.indexOfFirst { it.id == event.id }
        save(if (i < 0) events + event else events.toMutableList().also { it[i] = event })
    }

    fun delete(id: Long) = save(events.filterNot { it.id == id })

    private fun save(list: List<Event>) {
        events = list
        val json = JSONArray().apply { list.forEach { put(it.toJson()) } }
        prefs.edit().putString(KEY, json.toString()).apply()
    }

    private fun load(): List<Event> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { Event.fromJson(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val KEY = "events"
    }
}
