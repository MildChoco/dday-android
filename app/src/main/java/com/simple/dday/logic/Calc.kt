package com.simple.dday.logic

import com.simple.dday.data.AnnivMode
import com.simple.dday.data.Event
import com.simple.dday.data.Kind
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

data class Milestone(val label: String, val date: LocalDate)

enum class Step { PAST, TODAY, NEXT, FUTURE }

data class TimelineItem(val step: Step, val label: String, val date: String, val right: String)

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
    val days = (100..36_500 step 100).map { Milestone("${it}일", milestoneDate(start, it, dayOne)) }
    val years = (1..100).map { Milestone("${it}주년", start.plusYears(it.toLong())) }
    return (days + years).sortedBy { it.date }
}

/** 그 해의 같은 월·일. 2월 29일은 평년에 2월 28일이 된다 */
fun yearlyIn(date: LocalDate, year: Int): LocalDate = date.withYear(year)

/** 매년 돌아오는 날 중 오늘(포함) 이후 가장 가까운 날 */
fun nextYearly(date: LocalDate, today: LocalDate): LocalDate {
    if (!date.isBefore(today)) return date
    val thisYear = yearlyIn(date, today.year)
    return if (thisYear.isBefore(today)) yearlyIn(date, today.year + 1) else thisYear
}

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

fun summarize(e: Event, now: LocalDateTime): Summary {
    val today = now.toLocalDate()
    return when (e.kind) {
        Kind.DDAY -> {
            val left = daysBetween(today, e.date)
            Summary(listOf(Part(ddayLabel(left))), formatDot(e.date), if (left >= 0) e.date.atStartOfDay() else null)
        }

        Kind.ANNIV -> when {
            e.annivMode == AnnivMode.YEARLY -> {
                val next = nextYearly(e.date, today)
                Summary(listOf(Part(ddayLabel(daysBetween(today, next)))), formatMonthDay(e.date, "매년 "), next.atStartOfDay())
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
    return past.map { TimelineItem(Step.PAST, it.label, formatDot(it.date), "지남") } +
        TimelineItem(Step.TODAY, todayLabel, "$countText · ${formatDot(today)}", if (todays.isEmpty()) "" else "D-Day") +
        future.mapIndexed { i, m ->
            TimelineItem(if (i == 0) Step.NEXT else Step.FUTURE, m.label, formatDot(m.date), ddayLabel(daysBetween(today, m.date)))
        }
}

/** 매년 반복 기념일 타임라인: 지난번, 오늘, 앞으로 5번 */
fun yearlyTimeline(date: LocalDate, today: LocalDate): List<TimelineItem> {
    val next = nextYearly(date, today)
    val items = mutableListOf<TimelineItem>()
    if (next.year - 1 >= date.year) {
        val prev = yearlyIn(date, next.year - 1)
        if (prev.isBefore(today)) items += TimelineItem(Step.PAST, nthLabel(date, prev), formatDot(prev), "지남")
    }
    val first = if (next == today) {
        items += TimelineItem(Step.TODAY, "오늘 · " + nthLabel(date, next), formatDot(today), "D-Day")
        1
    } else {
        items += TimelineItem(Step.TODAY, "오늘", formatDot(today), "")
        0
    }
    (first until first + 5).forEachIndexed { i, k ->
        val d = yearlyIn(date, next.year + k)
        items += TimelineItem(if (i == 0) Step.NEXT else Step.FUTURE, nthLabel(date, d), formatDot(d), ddayLabel(daysBetween(today, d)))
    }
    return items
}

/** 기준일로부터 몇 번째 맞는 날인지. 기준일 당일은 "첫날" */
fun nthLabel(origin: LocalDate, occurrence: LocalDate): String {
    val n = occurrence.year - origin.year
    return if (n > 0) "${n}번째" else "첫날"
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
