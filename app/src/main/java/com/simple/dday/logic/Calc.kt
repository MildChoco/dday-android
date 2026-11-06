package com.simple.dday.logic

import com.simple.dday.data.AnnivMode
import com.simple.dday.data.Event
import com.simple.dday.data.Kind
import com.simple.dday.data.SortOrder
import java.text.Collator
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/** 큰 숫자와 단위 한 조각. 예: "52" + "시간" */
data class Part(val number: String, val unit: String = "")

/** 목록 카드에 보여줄 요약 */
data class Summary(
    val parts: List<Part>,
    val sub: String,
    /** 다음으로 다가오는 시각. 이미 지난 항목은 null이고 목록 맨 뒤로 간다 */
    val nextAt: LocalDateTime?,
)

/** major는 1000일 단위처럼 크게 챙기는 기념일 */
data class Milestone(val label: String, val date: LocalDate, val major: Boolean = false)

enum class Step { PAST, TODAY, NEXT, FUTURE }

data class TimelineItem(
    val step: Step,
    val label: String,
    val date: String,
    val right: String,
    val highlight: Boolean = false,
)

fun daysBetween(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

fun ddayLabel(daysLeft: Long): String = when {
    daysLeft > 0 -> "D-$daysLeft"
    daysLeft == 0L -> "D-Day"
    else -> "D+${-daysLeft}"
}

/** 시작일부터 오늘까지 센 날 수. dayOne이면 시작일이 1일이다 */
fun countDays(start: LocalDate, today: LocalDate, dayOne: Boolean): Long =
    daysBetween(start, today) + if (dayOne) 1 else 0

fun milestoneDate(start: LocalDate, n: Int, dayOne: Boolean): LocalDate =
    start.plusDays(n.toLong() - if (dayOne) 1 else 0)

/** 100일 단위와 N주년을 날짜순으로 */
fun milestones(start: LocalDate, dayOne: Boolean): List<Milestone> {
    val days = (100..36_500 step 100).map { Milestone("${it}일", milestoneDate(start, it, dayOne), major = it % 1000 == 0) }
    val years = (1..100).map { Milestone("${it}주년", start.plusYears(it.toLong())) }
    return (days + years).sortedBy { it.date }
}

// ---- 매년 반복 ----

/** 그 해의 같은 월·일. 2월 29일은 평년에 2월 28일이 된다 */
fun yearlyIn(date: LocalDate, year: Int): LocalDate = date.withYear(year)

/** 매년 반복 항목이 [year]년에 돌아오는 양력 날짜. 음력이면 음력 [year]년의 날짜를 양력으로 바꾼다 */
fun occurrenceIn(e: Event, year: Int, lunar: LunarConverter): LocalDate =
    (if (e.lunar) lunar.toSolar(year, e.lunarMonth, e.lunarDay) else null) ?: yearlyIn(e.date, year)

/** [from](포함) 이후에 돌아오는 날짜를 가까운 순으로 [count]개 */
fun upcomingOccurrences(e: Event, from: LocalDate, count: Int, lunar: LunarConverter): List<LocalDate> =
    // 음력 12월은 양력으로 다음 해 1~2월이라 한 해 앞부터 본다
    (from.year - 1..from.year + count + 1)
        .map { occurrenceIn(e, it, lunar) }
        .filter { !it.isBefore(from) }
        .sorted()
        .take(count)

fun nextOccurrence(e: Event, today: LocalDate, lunar: LunarConverter): LocalDate =
    upcomingOccurrences(e, today, 1, lunar).first()

/** [before] 전에 마지막으로 돌아온 날짜 */
fun previousOccurrence(e: Event, before: LocalDate, lunar: LunarConverter): LocalDate =
    (before.year - 2..before.year).map { occurrenceIn(e, it, lunar) }.filter { it.isBefore(before) }.max()

/** 태어난 해가 있으면 그 날이 몇 번째 생일(=만 나이)인지. 태어난 해가 없거나 태어나기 전이면 null */
fun birthdayNumber(e: Event, occurrence: LocalDate): Int? =
    e.birthYear?.let { occurrence.year - it }?.takeIf { it > 0 }

/** 타임라인에 쓰는 이름. 태어난 해가 있으면 "59번째 생일", 없으면 "2027년" */
fun occurrenceLabel(e: Event, occurrence: LocalDate): String =
    birthdayNumber(e, occurrence)?.let { "${it}번째 생일" } ?: "${occurrence.year}년"

/** "1월 15일" 또는 "음력 8월 15일" */
fun yearlyDateText(e: Event): String =
    if (e.lunar) "음력 ${e.lunarMonth}월 ${e.lunarDay}일" else formatMonthDay(e.date)

// ---- 시간 ----

/** 시간 카테고리의 목표 시각. 매일 반복이면 지금 이후 가장 가까운 그 시각 */
fun timeTarget(e: Event, now: LocalDateTime): LocalDateTime {
    if (!e.daily) return e.date.atTime(e.time)
    val todayAt = now.toLocalDate().atTime(e.time)
    return if (todayAt.isAfter(now)) todayAt else todayAt.plusDays(1)
}

fun secondsUntil(now: LocalDateTime, target: LocalDateTime): Long = Duration.between(now, target).seconds

fun hmParts(totalSeconds: Long): List<Part> {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = s % 3600 / 60
    return when {
        h > 0 -> listOf(Part("$h", "시간"), Part("$m", "분"))
        m > 0 -> listOf(Part("$m", "분"))
        else -> listOf(Part("$s", "초"))
    }
}

// ---- 목록 ----

fun summarize(e: Event, now: LocalDateTime, lunar: LunarConverter = IcuLunar): Summary {
    val today = now.toLocalDate()
    return when (e.kind) {
        Kind.DDAY -> {
            val left = daysBetween(today, e.date)
            Summary(listOf(Part(ddayLabel(left))), formatDot(e.date), if (left >= 0) e.date.atStartOfDay() else null)
        }

        Kind.ANNIV -> when {
            e.annivMode == AnnivMode.YEARLY -> {
                val next = nextOccurrence(e, today, lunar)
                Summary(listOf(Part(ddayLabel(daysBetween(today, next)))), "매년 ${yearlyDateText(e)}", next.atStartOfDay())
            }

            e.date.isAfter(today) ->
                Summary(listOf(Part(ddayLabel(daysBetween(today, e.date)))), "${formatDot(e.date)} 시작", e.date.atStartOfDay())

            else -> {
                val next = milestones(e.date, e.startAsDayOne).firstOrNull { !it.date.isBefore(today) }
                val count = Part("${countDays(e.date, today, e.startAsDayOne)}", "일")
                val sub = when {
                    next == null -> "${formatDot(e.date)}부터"
                    next.date == today -> "오늘이 ${next.label}이에요"
                    else -> "${next.label}까지 ${ddayLabel(daysBetween(today, next.date))}"
                }
                Summary(listOf(count), sub, next?.date?.atStartOfDay())
            }
        }

        Kind.TIME -> {
            val target = timeTarget(e, now)
            val sec = secondsUntil(now, target)
            val sub = if (e.daily) "매일 ${formatTime(e.time)}" else "${formatShort(e.date)} ${formatTime(e.time)}"
            if (sec >= 0) {
                Summary(hmParts(sec), sub, target)
            } else {
                val parts = hmParts(-sec)
                Summary(listOf(parts.first().copy(number = "+" + parts.first().number)) + parts.drop(1), "$sub · 지남", null)
            }
        }
    }
}

/** 홈 목록 순서. 고정한 항목이 항상 먼저 오고, 그 안에서 [order]를 따른다 */
fun sortForHome(items: List<Pair<Event, Summary>>, order: SortOrder): List<Pair<Event, Summary>> {
    val byOrder: Comparator<Pair<Event, Summary>> = when (order) {
        SortOrder.NEAREST -> compareBy<Pair<Event, Summary>> { it.second.nextAt == null }
            .thenBy { it.second.nextAt }
            .thenBy { it.first.id }

        // 번호는 만든 순서대로 커진다. 최근에 만든 것이 위로
        SortOrder.CREATED -> compareByDescending<Pair<Event, Summary>> { it.first.id }

        SortOrder.NAME -> compareBy<Pair<Event, Summary>, String>(Collator.getInstance(Locale.KOREAN)) { it.first.title }
            .thenBy { it.first.id }
    }
    return items.sortedWith(compareByDescending<Pair<Event, Summary>> { it.first.pinned }.then(byOrder))
}

// ---- 타임라인 ----

/** 일수 기념일 타임라인: 지난 기념일 2개, 오늘, 다가오는 기념일 5개 */
fun countTimeline(start: LocalDate, dayOne: Boolean, today: LocalDate): List<TimelineItem> {
    val all = milestones(start, dayOne)
    val past = all.filter { it.date.isBefore(today) }.takeLast(2)
    val todays = all.filter { it.date == today }
    val future = all.filter { it.date.isAfter(today) }.take(5)
    val n = countDays(start, today, dayOne)
    val countText = when {
        start.isAfter(today) -> "시작까지 ${ddayLabel(daysBetween(today, start))}"
        dayOne -> "${n}일째"
        else -> "${n}일"
    }
    val todayLabel = if (todays.isEmpty()) "오늘" else "오늘 · " + todays.joinToString(" · ") { it.label }
    return past.map { TimelineItem(Step.PAST, it.label, formatDot(it.date), "지남", it.major) } +
        TimelineItem(
            Step.TODAY,
            todayLabel,
            "$countText · ${formatDot(today)}",
            if (todays.isEmpty()) "" else "D-Day",
            todays.any { it.major },
        ) +
        future.mapIndexed { i, m ->
            TimelineItem(
                if (i == 0) Step.NEXT else Step.FUTURE,
                m.label,
                formatDot(m.date),
                ddayLabel(daysBetween(today, m.date)),
                m.major,
            )
        }
}

/** 매년 반복 기념일 타임라인: 지난번, 오늘, 앞으로 5번 */
fun yearlyTimeline(e: Event, today: LocalDate, lunar: LunarConverter = IcuLunar): List<TimelineItem> {
    val upcoming = upcomingOccurrences(e, today, 6, lunar)
    val isToday = upcoming.first() == today
    val prev = previousOccurrence(e, today, lunar)
    val items = mutableListOf(TimelineItem(Step.PAST, occurrenceLabel(e, prev), formatDot(prev), "지남"))
    items += if (isToday) {
        TimelineItem(Step.TODAY, "오늘 · " + occurrenceLabel(e, today), formatDot(today), "D-Day")
    } else {
        TimelineItem(Step.TODAY, "오늘", formatDot(today), "")
    }
    upcoming.drop(if (isToday) 1 else 0).take(5).forEachIndexed { i, d ->
        items += TimelineItem(
            if (i == 0) Step.NEXT else Step.FUTURE,
            occurrenceLabel(e, d),
            formatDot(d),
            ddayLabel(daysBetween(today, d)),
        )
    }
    return items
}

// ---- 표시 형식 ----

private val KO = Locale.KOREAN

fun weekdayShort(d: LocalDate): String = d.dayOfWeek.getDisplayName(TextStyle.SHORT, KO)

fun weekdayFull(d: LocalDate): String = d.dayOfWeek.getDisplayName(TextStyle.FULL, KO)

/** 2026.11.03 (화) */
fun formatDot(d: LocalDate): String =
    "%04d.%02d.%02d (%s)".format(Locale.ROOT, d.year, d.monthValue, d.dayOfMonth, weekdayShort(d))

/** 11.03 (화) */
fun formatShort(d: LocalDate): String =
    "%02d.%02d (%s)".format(Locale.ROOT, d.monthValue, d.dayOfMonth, weekdayShort(d))

/** 2026년 11월 3일 (화) */
fun formatLong(d: LocalDate): String = "${d.year}년 ${d.monthValue}월 ${d.dayOfMonth}일 (${weekdayShort(d)})"

/** 11월 3일 */
fun formatMonthDay(d: LocalDate, prefix: String = ""): String = "$prefix${d.monthValue}월 ${d.dayOfMonth}일"

/** 오후 2:00 */
fun formatTime(t: LocalTime): String {
    val h12 = (t.hour % 12).let { if (it == 0) 12 else it }
    return "${if (t.hour < 12) "오전" else "오후"} $h12:%02d".format(Locale.ROOT, t.minute)
}

/** 1년 5개월 / 1개월 24일 / 19일 */
fun formatPeriod(from: LocalDate, to: LocalDate, withDays: Boolean): String {
    val p = if (from.isAfter(to)) Period.between(to, from) else Period.between(from, to)
    val parts = buildList {
        if (p.years > 0) add("${p.years}년")
        if (p.months > 0) add("${p.months}개월")
        if (p.days > 0 && (withDays || isEmpty())) add("${p.days}일")
    }
    return parts.ifEmpty { listOf("0일") }.joinToString(" ")
}

/** 2주 5일 */
fun formatWeeks(days: Long): String {
    val w = days / 7
    val d = days % 7
    return listOfNotNull(if (w > 0) "${w}주" else null, if (d > 0 || w == 0L) "${d}일" else null).joinToString(" ")
}

/** 2일 4시간 14분 */
fun formatDhm(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val d = s / 86_400
    val h = s % 86_400 / 3600
    val m = s % 3600 / 60
    return listOfNotNull(if (d > 0) "${d}일" else null, if (d > 0 || h > 0) "${h}시간" else null, "${m}분").joinToString(" ")
}

fun formatNumber(n: Long): String = "%,d".format(Locale.KOREA, n)
