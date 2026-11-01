package com.simple.dday.logic

import com.simple.dday.data.AnnivMode
import com.simple.dday.data.Event
import com.simple.dday.data.Kind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CalcTest {
    private val today = LocalDate.of(2026, 11, 1)
    private val now = LocalDateTime.of(2026, 11, 1, 9, 46)
    private val start = LocalDate.of(2025, 6, 1)

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
    fun yearlyNextOccurrence() {
        assertEquals(LocalDate.of(2027, 1, 15), nextYearly(LocalDate.of(2026, 1, 15), today))
        assertEquals(LocalDate.of(2026, 11, 1), nextYearly(LocalDate.of(1990, 11, 1), today))
        assertEquals(LocalDate.of(2026, 12, 25), nextYearly(LocalDate.of(2026, 12, 25), today))
        // 윤일은 평년에 2월 28일
        assertEquals(LocalDate.of(2027, 2, 28), nextYearly(LocalDate.of(2024, 2, 29), today))
    }

    @Test
    fun yearlySummary() {
        val e = Event(1, Kind.ANNIV, "엄마 생일", LocalDate.of(2026, 1, 15), annivMode = AnnivMode.YEARLY)
        val s = summarize(e, now)
        assertEquals(listOf(Part("D-75")), s.parts)
        assertEquals("매년 1월 15일", s.sub)
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
    fun formats() {
        assertEquals("2026.11.03 (화)", formatDot(LocalDate.of(2026, 11, 3)))
        assertEquals("오후 2:00", formatTime(LocalTime.of(14, 0)))
        assertEquals("오전 12:05", formatTime(LocalTime.of(0, 5)))
        assertEquals("1년 5개월", formatPeriod(start, today, withDays = false))
        assertEquals("2주 5일", formatWeeks(19))
        assertEquals("3,134분", "${formatNumber(188_048L / 60)}분")
    }
}
