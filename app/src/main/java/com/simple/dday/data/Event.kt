package com.simple.dday.data

import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

enum class Kind(val label: String, val desc: String) {
    DDAY("디데이", "그날까지 남은 날"),
    ANNIV("기념일", "지난 날 세기 · 매년 반복"),
    TIME("시간", "남은 시간 카운트다운"),
}

/** 기념일을 세는 방식: 100일 단위로 세기, 또는 매년 같은 날 반복 */
enum class AnnivMode { COUNT, YEARLY }

/** 카드 색. bg는 카드 바탕, ink는 그 위의 숫자·강조 글자 */
enum class Pastel(val label: String, val bg: Long, val ink: Long) {
    PINK("핑크", 0xFFFFE4EA, 0xFFB03A63),
    LAVENDER("라벤더", 0xFFECE6FF, 0xFF6B4FC8),
    MINT("민트", 0xFFDDF5EC, 0xFF1F7A57),
    SKY("하늘", 0xFFDFEFFF, 0xFF2D6AA8),
    BUTTER("레몬", 0xFFFFF3C7, 0xFF8A6500),
    PEACH("피치", 0xFFFFE7D6, 0xFFA04814),
}

val Kind.defaultColor: Pastel
    get() = when (this) {
        Kind.DDAY -> Pastel.PINK
        Kind.ANNIV -> Pastel.LAVENDER
        Kind.TIME -> Pastel.MINT
    }

data class Event(
    val id: Long,
    val kind: Kind,
    val title: String,
    /** 디데이: 목표일, 기념일: 시작일(매년이면 기준일), 시간: 목표일(매일 반복이면 쓰지 않음) */
    val date: LocalDate,
    /** 시간 카테고리에서만 쓴다 */
    val time: LocalTime = LocalTime.MIDNIGHT,
    val annivMode: AnnivMode = AnnivMode.COUNT,
    /** 기념일(일수): 시작한 날을 1일로 센다 */
    val startAsDayOne: Boolean = true,
    /** 시간: 매일 같은 시각까지 센다 */
    val daily: Boolean = false,
    val color: Pastel = kind.defaultColor,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("kind", kind.name)
        .put("title", title)
        .put("date", date.toString())
        .put("time", time.toString())
        .put("annivMode", annivMode.name)
        .put("dayOne", startAsDayOne)
        .put("daily", daily)
        .put("color", color.name)

    companion object {
        fun fromJson(o: JSONObject): Event {
            val kind = Kind.valueOf(o.getString("kind"))
            return Event(
                id = o.getLong("id"),
                kind = kind,
                title = o.getString("title"),
                date = LocalDate.parse(o.getString("date")),
                time = LocalTime.parse(o.optString("time", "00:00")),
                annivMode = runCatching { AnnivMode.valueOf(o.getString("annivMode")) }.getOrDefault(AnnivMode.COUNT),
                startAsDayOne = o.optBoolean("dayOne", true),
                daily = o.optBoolean("daily", false),
                color = runCatching { Pastel.valueOf(o.getString("color")) }.getOrDefault(kind.defaultColor),
            )
        }
    }
}
