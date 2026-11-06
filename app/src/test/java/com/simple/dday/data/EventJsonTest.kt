package com.simple.dday.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class EventJsonTest {
    @Test
    fun roundTripKeepsEveryField() {
        val events = listOf(
            Event(1, Kind.DDAY, "여행", LocalDate.of(2026, 12, 20), icon = "✈️", pinned = true),
            Event(2, Kind.TIME, "퇴근", LocalDate.of(2026, 11, 1), LocalTime.of(18, 0), daily = true, color = Pastel.SKY),
            Event(
                3, Kind.ANNIV, "할머니 생신", LocalDate.of(2026, 9, 25), annivMode = AnnivMode.YEARLY,
                birthYear = 1940, lunar = true, lunarMonth = 8, lunarDay = 15, archived = true,
            ),
            Event(4, Kind.ANNIV, "우리 만난 날", LocalDate.of(2025, 6, 1), startAsDayOne = false),
        )
        assertEquals(events, decodeEvents(encodeEvents(events).toString()))
    }

    @Test
    fun readsVersion1Array() {
        // 첫 버전은 버전 번호 없이 항목 배열만 저장했다
        val v1 = """[{"id":7,"kind":"ANNIV","title":"엄마 생일","date":"1968-01-15","time":"00:00",
            "annivMode":"YEARLY","dayOne":true,"daily":false,"color":"PINK"}]"""
        val expected = Event(7, Kind.ANNIV, "엄마 생일", LocalDate.of(1968, 1, 15), annivMode = AnnivMode.YEARLY, color = Pastel.PINK)
        assertEquals(listOf(expected), decodeEvents(v1))
    }
}
