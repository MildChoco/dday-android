package com.simple.dday.logic

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** 기기에 내장된 한국 음력 달력의 변환 결과를 실제 명절 날짜와 대조한다 */
@RunWith(AndroidJUnit4::class)
class IcuLunarTest {
    private fun solar(year: Int, month: Int, day: Int) = IcuLunar.toSolar(year, month, day)

    @Test
    fun deviceSupportsKoreanLunarCalendar() {
        assertTrue(IcuLunar.supported)
    }

    @Test
    fun seollal() {
        assertEquals(LocalDate.of(2024, 2, 10), solar(2024, 1, 1))
        assertEquals(LocalDate.of(2025, 1, 29), solar(2025, 1, 1))
        assertEquals(LocalDate.of(2026, 2, 17), solar(2026, 1, 1))
        assertEquals(LocalDate.of(2029, 2, 13), solar(2029, 1, 1))
        assertEquals(LocalDate.of(2030, 2, 3), solar(2030, 1, 1))
    }

    @Test
    fun seollalDiffersFromChineseNewYear() {
        // 합삭이 한국 시각으로 자정을 넘겨, 한국 설날이 중국 춘절보다 하루 늦은 해
        assertEquals(LocalDate.of(2027, 2, 7), solar(2027, 1, 1))
        assertEquals(LocalDate.of(2028, 1, 27), solar(2028, 1, 1))
    }

    @Test
    fun chuseok() {
        assertEquals(LocalDate.of(2024, 9, 17), solar(2024, 8, 15))
        assertEquals(LocalDate.of(2025, 10, 6), solar(2025, 8, 15))
        assertEquals(LocalDate.of(2026, 9, 25), solar(2026, 8, 15))
        assertEquals(LocalDate.of(2027, 9, 15), solar(2027, 8, 15))
        assertEquals(LocalDate.of(2028, 10, 3), solar(2028, 8, 15))
    }

    @Test
    fun buddhasBirthday() {
        assertEquals(LocalDate.of(2025, 5, 5), solar(2025, 4, 8))
        assertEquals(LocalDate.of(2026, 5, 24), solar(2026, 4, 8))
        assertEquals(LocalDate.of(2027, 5, 13), solar(2027, 4, 8))
    }

    @Test
    fun leapMonthIsSkipped() {
        // 2025년에는 윤6월이 있다. 평달 6월과 그 뒤의 7월이 윤달에 밀리지 않아야 한다
        assertEquals(LocalDate.of(2025, 7, 9), solar(2025, 6, 15))
        assertEquals(LocalDate.of(2025, 8, 29), solar(2025, 7, 7))
    }

    @Test
    fun monthTwelveFallsInNextSolarYear() {
        // 음력 섣달그믐은 다음 해 설날 전날
        assertEquals(LocalDate.of(2027, 2, 6), solar(2026, 12, 30))
    }

    @Test
    fun missingDayIsClampedToLastDay() {
        // 2026년 음력 2월은 29일까지라 30일은 말일(양력 4월 16일)로 맞춘다
        assertEquals(LocalDate.of(2026, 4, 16), solar(2026, 2, 30))
        assertEquals(LocalDate.of(2026, 4, 16), solar(2026, 2, 29))
    }
}
