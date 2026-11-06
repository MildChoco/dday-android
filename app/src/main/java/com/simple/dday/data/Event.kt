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

/** 카드 색. bg는 카드 바탕, ink는 그 위의 숫자·강조 글자. dark로 시작하는 값은 다크 모드용 */
enum class Pastel(val label: String, val bg: Long, val ink: Long, val darkBg: Long, val darkInk: Long) {
    PINK("핑크", 0xFFFFE4EA, 0xFFB03A63, 0xFF472733, 0xFFFFB2C8),
    LAVENDER("라벤더", 0xFFECE6FF, 0xFF6B4FC8, 0xFF352D55, 0xFFC9BBFF),
    MINT("민트", 0xFFDDF5EC, 0xFF1F7A57, 0xFF1F4034, 0xFF8FE3BF),
    SKY("하늘", 0xFFDFEFFF, 0xFF2D6AA8, 0xFF1F3A55, 0xFFA5D2FF),
    BUTTER("레몬", 0xFFFFF3C7, 0xFF8A6500, 0xFF453A14, 0xFFFFDF7A),
    PEACH("피치", 0xFFFFE7D6, 0xFFA04814, 0xFF4A2F1E, 0xFFFFC29E),
}

val Kind.defaultColor: Pastel
    get() = when (this) {
        Kind.DDAY -> Pastel.PINK
        Kind.ANNIV -> Pastel.LAVENDER
        Kind.TIME -> Pastel.MINT
    }

/** 카드 아이콘으로 고를 수 있는 기본 이모지 */
val BasicIcons = listOf(
    "🎂", "💕", "✈️", "📚", "🎓", "💍", "🎄", "🏖️", "🎉", "💼", "🏃",
    "👶", "🐶", "🏠", "⭐", "💰", "🎵", "⚽", "🍀", "📝", "⏰", "🌙",
)

data class Event(
    val id: Long,
    val kind: Kind,
    val title: String,
    /** 디데이: 목표일, 기념일: 시작일(매년이면 월·일만 쓴다), 시간: 목표일(매일 반복이면 쓰지 않음) */
    val date: LocalDate,
    /** 시간 카테고리에서만 쓴다 */
    val time: LocalTime = LocalTime.MIDNIGHT,
    val annivMode: AnnivMode = AnnivMode.COUNT,
    /** 기념일(일수): 시작한 날을 1일로 센다 */
    val startAsDayOne: Boolean = true,
    /** 시간: 매일 같은 시각까지 센다 */
    val daily: Boolean = false,
    val color: Pastel = kind.defaultColor,
    /** 이름 앞에 붙는 이모지. 비어 있으면 표시하지 않는다 */
    val icon: String = "",
    /** 기념일(매년): 태어난 해. 있으면 "N번째 생일 · 만 N세"로 보여준다 */
    val birthYear: Int? = null,
    /** 기념일(매년): 음력 날짜로 센다. 이때는 date 대신 lunarMonth·lunarDay를 쓴다 */
    val lunar: Boolean = false,
    val lunarMonth: Int = 1,
    val lunarDay: Int = 1,
    /** 홈 목록 맨 위에 고정 */
    val pinned: Boolean = false,
    /** 보관함에 넣어 홈 목록에서 숨김 */
    val archived: Boolean = false,
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
        .put("icon", icon)
        .putOpt("birthYear", birthYear)
        .put("lunar", lunar)
        .put("lunarMonth", lunarMonth)
        .put("lunarDay", lunarDay)
        .put("pinned", pinned)
        .put("archived", archived)

    companion object {
        /** 예전 버전이 저장한 데이터에는 없는 필드가 있으므로, 없으면 기본값으로 읽는다 */
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
                icon = o.optString("icon", ""),
                birthYear = if (o.has("birthYear") && !o.isNull("birthYear")) o.getInt("birthYear") else null,
                lunar = o.optBoolean("lunar", false),
                lunarMonth = o.optInt("lunarMonth", 1).coerceIn(1, 12),
                lunarDay = o.optInt("lunarDay", 1).coerceIn(1, 30),
                pinned = o.optBoolean("pinned", false),
                archived = o.optBoolean("archived", false),
            )
        }
    }
}

val Event.isYearly: Boolean get() = kind == Kind.ANNIV && annivMode == AnnivMode.YEARLY
