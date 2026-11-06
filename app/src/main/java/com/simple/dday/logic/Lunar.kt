package com.simple.dday.logic

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import java.time.LocalDate

/** 음력 날짜를 양력으로 바꾼다. 계산은 기기에 내장된 달력에 맡기고, 테스트에서는 가짜 구현을 끼운다 */
interface LunarConverter {
    /**
     * 음력 [lunarYear]년 [month]월 [day]일(평달)의 양력 날짜.
     * 그 달에 없는 날(29일까지인 달의 30일)은 말일로 맞춘다. 변환할 수 없으면 null
     */
    fun toSolar(lunarYear: Int, month: Int, day: Int): LocalDate?
}

/** Android에 내장된 ICU의 한국 음력(dangi) 달력 */
object IcuLunar : LunarConverter {
    private const val DAY_MS = 86_400_000L

    /** 기기가 한국 음력 달력을 지원하는지. 지원하지 않으면 ICU가 말없이 양력 달력을 돌려주므로 종류를 확인한다 */
    val supported: Boolean by lazy { runCatching { newCalendar().type == "dangi" }.getOrDefault(false) }

    private fun newCalendar(): Calendar =
        Calendar.getInstance(TimeZone.GMT_ZONE, ULocale("ko_KR@calendar=dangi"))

    override fun toSolar(lunarYear: Int, month: Int, day: Int): LocalDate? {
        if (!supported) return null
        return runCatching {
            val cal = newCalendar()
            // 양력 3월 1일은 항상 그 해 음력 1~2월(평달)이라, 음력 연도를 맞추는 기준점으로 쓴다
            cal.timeInMillis = LocalDate.of(lunarYear, 3, 1).toEpochDay() * DAY_MS + DAY_MS / 2
            cal.set(Calendar.IS_LEAP_MONTH, 0)
            cal.set(Calendar.MONTH, month - 1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, minOf(day, lastDay))
            LocalDate.ofEpochDay(Math.floorDiv(cal.timeInMillis, DAY_MS))
        }.getOrNull()
    }
}
