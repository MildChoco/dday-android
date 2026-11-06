package com.simple.dday.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

/** 항목 목록을 SharedPreferences에 JSON으로 저장한다. 화면은 [events]를 그대로 읽는다. */
class EventStore(context: Context) {
    private val prefs = context.getSharedPreferences("dday", Context.MODE_PRIVATE)

    /** 보관함에 있는 항목까지 모두 포함한다 */
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
        prefs.edit().putString(KEY, encodeEvents(list).toString()).apply()
    }

    private fun load(): List<Event> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching { decodeEvents(raw) }.getOrDefault(emptyList())
    }

    private companion object {
        const val KEY = "events"
    }
}

const val SCHEMA_VERSION = 2

fun encodeEvents(list: List<Event>): JSONObject = JSONObject()
    .put("version", SCHEMA_VERSION)
    .put("events", JSONArray().apply { list.forEach { put(it.toJson()) } })

/** 버전 1은 항목 배열만 저장했고, 버전 2부터는 버전 번호와 함께 객체로 저장한다 */
fun decodeEvents(raw: String): List<Event> {
    val text = raw.trim()
    val arr = if (text.startsWith("[")) JSONArray(text) else JSONObject(text).getJSONArray("events")
    return (0 until arr.length()).map { Event.fromJson(arr.getJSONObject(it)) }
}
