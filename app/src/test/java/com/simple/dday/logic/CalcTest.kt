package com.simple.dday.logic

import com.simple.dday.data.AnnivMode
import com.simple.dday.data.Event
import com.simple.dday.data.Kind
import com.simple.dday.data.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** 음력 날짜가 항상 양력 같은 월·일의 40일 뒤라고 치는 가짜 달력. 해를 넘기는 경우를 확인하는 데 쓴다 */
private object FakeLunar : LunarConverter {
    override fun toSolar(lunarYear: Int, month: Int, day: Int): LocalDate =
        LocalDate.of(lunarYear, month, minOf(day, 28)).plusDays(40)
}

class CalcTest {
    private val today = LocalDate.of(2026, 11, 1)
    private val now = LocalDateTime.of(2026, 11, 1, 9, 46)
    private val start = LocalDate.of(2025, 6, 1)

    private fun yearly(date: LocalDate, birthYear: Int? = null) =
        Event(1, Kind.ANNIV, "기념일", date, annivMode = AnnivMode.YEARLY, birthYear = birthYear)

    @Test
    fun ddayLabels() {
        assertEquals("D-19", ddayLabel(daysBetween(today, LocalDate.of(2026, 11, 20))))
        assertEquals("D-Day", ddayLabel(0))
        assertEquals("D+3", ddayLabel(-3))
    }

    @Test
    fun countDaysStartsAtOne() {
        assertEquals(519, countDays(start, today, dayOne = true))
        assertEquals(518, countDays(start, today, dayOne = false))
        assertEquals(1, countDays(today, today, dayOne = true))
    }

    @Test
    fun milestoneDates() {
        assertEquals(LocalDate.of(2025, 9, 8), milestoneDate(start, 100, dayOne = true))
        assertEquals(LocalDate.of(2027, 1, 21), milestoneDate(start, 600, dayOne = true))
        assertEquals(LocalDate.of(2028, 2, 25), milestoneDate(start, 1000, dayOne = true))
        assertEquals(LocalDate.of(2025, 9, 9), milestoneDate(start, 100, dayOne = false))
    }

    @Test
    fun countSummaryShowsNextMilestone() {
        val e = Event(1, Kind.ANNIV, "우리 만난 날", start)
        val s = summarize(e, now)
        assertEquals(listOf(Part("519", "일")), s.parts)
        assertEquals("600일까지 D-81", s.sub)
    }

    @Test
    fun countTimelineAroundToday() {
        val items = countTimeline(start, dayOne = true, today = today)
        assertEquals(listOf("400일", "500일", "오늘", "600일", "700일", "2주년", "800일", "900일"), items.map { it.label })
        assertEquals("519일째 · 2026.11.01 (일)", items[2].date)
        assertEquals("D-212", items[5].right)
    }

    @Test
    fun thousandDaysIsHighlighted() {
        // 오늘이 950일째면 다음 기념일이 1000일이다
        val items = countTimeline(today.minusDays(949), dayOne = true, today = today)
        val next = items.first { it.step == Step.NEXT }
        assertEquals("1000일", next.label)
        assertEquals(true, next.highlight)
        assertEquals(false, items.first { it.label == "900일" }.highlight)
    }

    @Test
    fun yearlyNextOccurrence() {
        assertEquals(LocalDate.of(2027, 1, 15), nextOccurrence(yearly(LocalDate.of(2026, 1, 15)), today, FakeLunar))
        assertEquals(LocalDate.of(2026, 11, 1), nextOccurrence(yearly(LocalDate.of(1990, 11, 1)), today, FakeLunar))
        assertEquals(LocalDate.of(2026, 12, 25), nextOccurrence(yearly(LocalDate.of(2026, 12, 25)), today, FakeLunar))
        // 윤일은 평년에 2월 28일
        assertEquals(LocalDate.of(2027, 2, 28), nextOccurrence(yearly(LocalDate.of(2024, 2, 29)), today, FakeLunar))
        // 기준일의 연도는 보지 않고 월·일만 쓴다
        assertEquals(LocalDate.of(2027, 3, 1), nextOccurrence(yearly(LocalDate.of(2030, 3, 1)), today, FakeLunar))
    }

    @Test
    fun yearlySummary() {
        val s = summarize(yearly(LocalDate.of(2026, 1, 15)), now, FakeLunar)
        assertEquals(listOf(Part("D-75")), s.parts)
        assertEquals("매년 1월 15일", s.sub)
    }

    @Test
    fun birthdayShowsNumberAndAge() {
        val e = yearly(LocalDate.of(1968, 1, 15), birthYear = 1968)
        assertEquals(59, birthdayNumber(e, nextOccurrence(e, today, FakeLunar)))
        val labels = yearlyTimeline(e, today, FakeLunar).map { it.label }
        assertEquals(listOf("58번째 생일", "오늘", "59번째 생일", "60번째 생일"), labels.take(4))
    }

    @Test
    fun yearlyWithoutBirthYearShowsYear() {
        val e = yearly(LocalDate.of(2026, 1, 15))
        assertNull(birthdayNumber(e, LocalDate.of(2027, 1, 15)))
        val items = yearlyTimeline(e, today, FakeLunar)
        assertEquals(listOf("2026년", "오늘", "2027년", "2028년"), items.map { it.label }.take(4))
        assertEquals(7, items.size)
        assertEquals("D-75", items[2].right)
    }

    @Test
    fun yearlyOnTheDay() {
        val items = yearlyTimeline(yearly(LocalDate.of(2000, 11, 1), birthYear = 2000), today, FakeLunar)
        assertEquals(listOf("25번째 생일", "오늘 · 26번째 생일", "27번째 생일"), items.map { it.label }.take(3))
        assertEquals("D-Day", items[1].right)
    }

    @Test
    fun lunarYearlyCrossesTheYear() {
        // 가짜 달력에서 음력 12월 20일은 다음 해 양력 1월 29일
        val e = yearly(today).copy(lunar = true, lunarMonth = 12, lunarDay = 20)
        assertEquals(LocalDate.of(2027, 1, 29), nextOccurrence(e, today, FakeLunar))
        assertEquals(LocalDate.of(2026, 1, 29), previousOccurrence(e, today, FakeLunar))
        val s = summarize(e, now, FakeLunar)
        assertEquals("매년 음력 12월 20일", s.sub)
        assertEquals(listOf(Part("D-89")), s.parts)
    }

    @Test
    fun timeCountdown() {
        val flight = Event(1, Kind.TIME, "제주행 비행기", LocalDate.of(2026, 11, 3), LocalTime.of(14, 0))
        assertEquals(listOf(Part("52", "시간"), Part("14", "분")), summarize(flight, now).parts)
        assertEquals("2일 4시간 14분", formatDhm(secondsUntil(now, timeTarget(flight, now))))

        val offWork = Event(2, Kind.TIME, "퇴근", today, LocalTime.of(18, 0), daily = true)
        assertEquals(listOf(Part("8", "시간"), Part("14", "분")), summarize(offWork, now).parts)
        // 그 시각이 지나면 내일 같은 시각까지
        val evening = LocalDateTime.of(2026, 11, 1, 19, 0)
        assertEquals(LocalDateTime.of(2026, 11, 2, 18, 0), timeTarget(offWork, evening))
    }

    @Test
    fun passedTimeGoesLast() {
        val past = Event(1, Kind.TIME, "회의", LocalDate.of(2026, 11, 1), LocalTime.of(8, 0))
        val s = summarize(past, now)
        assertNull(s.nextAt)
        assertEquals(listOf(Part("+1", "시간"), Part("46", "분")), s.parts)
    }

    @Test
    fun homeSortOrders() {
        val trip = Event(1, Kind.DDAY, "여행", LocalDate.of(2026, 12, 20))
        val exam = Event(2, Kind.DDAY, "시험", LocalDate.of(2026, 11, 10))
        val old = Event(3, Kind.DDAY, "가족 모임", LocalDate.of(2026, 10, 1))
        val items = listOf(trip, exam, old).map { it to summarize(it, now) }
        fun ids(order: SortOrder, list: List<Pair<Event, Summary>> = items) = sortForHome(list, order).map { it.first.id }

        // 지난 항목은 맨 뒤
        assertEquals(listOf(2L, 1L, 3L), ids(SortOrder.NEAREST))
        assertEquals(listOf(3L, 2L, 1L), ids(SortOrder.CREATED))
        assertEquals(listOf(3L, 2L, 1L), ids(SortOrder.NAME))
        // 고정한 항목은 어떤 정렬에서도 맨 앞
        val pinned = items.map { (e, s) -> (if (e.id == 1L) e.copy(pinned = true) else e) to s }
        assertEquals(listOf(1L, 2L, 3L), ids(SortOrder.NEAREST, pinned))
        assertEquals(listOf(1L, 3L, 2L), ids(SortOrder.NAME, pinned))
    }

    @Test
    fun formats() {
        assertEquals("2026.11.03 (화)", formatDot(LocalDate.of(2026, 11, 3)))
        assertEquals("오후 2:00", formatTime(LocalTime.of(14, 0)))
        assertEquals("오전 12:05", formatTime(LocalTime.of(0, 5)))
        assertEquals("1년 5개월", formatPeriod(start, today, withDays = false))
        assertEquals("2주 5일", formatWeeks(19))
        assertEquals("3,134분", "${formatNumber(188_048L / 60)}분")
    }
}
