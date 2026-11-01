package com.simple.dday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.dday.data.AnnivMode
import com.simple.dday.data.Event
import com.simple.dday.data.Kind
import com.simple.dday.data.Pastel
import com.simple.dday.logic.countDays
import com.simple.dday.logic.countTimeline
import com.simple.dday.logic.daysBetween
import com.simple.dday.logic.ddayLabel
import com.simple.dday.logic.formatDhm
import com.simple.dday.logic.formatDot
import com.simple.dday.logic.formatLong
import com.simple.dday.logic.formatMonthDay
import com.simple.dday.logic.formatNumber
import com.simple.dday.logic.formatPeriod
import com.simple.dday.logic.formatTime
import com.simple.dday.logic.formatWeeks
import com.simple.dday.logic.milestones
import com.simple.dday.logic.nextYearly
import com.simple.dday.logic.secondsUntil
import com.simple.dday.logic.timeTarget
import com.simple.dday.logic.weekdayShort
import com.simple.dday.logic.yearlyTimeline
import com.simple.dday.ui.theme.LabelGray
import com.simple.dday.ui.theme.Nunito
import com.simple.dday.ui.theme.SubOnPastel
import com.simple.dday.ui.theme.White
import com.simple.dday.ui.theme.inkColor
import java.time.LocalDateTime
import kotlin.math.abs

@Composable
fun DetailScreen(event: Event, onBack: () -> Unit, onEdit: () -> Unit) {
    val now = rememberNow()
    Column(
        Modifier
            .fillMaxSize()
            .background(White)
            .safeDrawingPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PlainIconButton(AppIcons.Back, "뒤로", onBack)
            Box(
                Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(role = Role.Button, onClick = onEdit)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("편집", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = LabelGray)
            }
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            when (event.kind) {
                Kind.DDAY -> DdayDetail(event, now)
                Kind.ANNIV -> if (event.annivMode == AnnivMode.YEARLY) YearlyDetail(event, now) else CountDetail(event, now)
                Kind.TIME -> TimeDetail(event, now)
            }
        }
    }
}

@Composable
private fun DdayDetail(e: Event, now: LocalDateTime) {
    val today = now.toLocalDate()
    val left = daysBetween(today, e.date)
    val hours = abs(secondsUntil(now, e.date.atStartOfDay())) / 3600
    Hero(e.color, "디데이", e.title) {
        BigNumber(ddayLabel(left), "", e.color.inkColor)
        HeroSub(formatLong(e.date))
        Tiles(
            (if (left >= 0) "남은 기간" else "지난 기간") to (if (left == 0L) "오늘이에요" else "${abs(left)}일"),
            "주 단위" to formatWeeks(abs(left)),
        )
    }
    InfoRows(
        listOf(
            "목표 날짜" to formatLong(e.date),
            "개월로" to formatPeriod(today, e.date, withDays = true),
            "시간으로" to "약 ${formatNumber(hours)}시간",
        ),
    )
}

@Composable
private fun CountDetail(e: Event, now: LocalDateTime) {
    val today = now.toLocalDate()
    val notStarted = e.date.isAfter(today)
    val n = countDays(e.date, today, e.startAsDayOne)
    val next = milestones(e.date, e.startAsDayOne).firstOrNull { !it.date.isBefore(today) }
    Hero(e.color, "기념일 · 일수", e.title) {
        if (notStarted) {
            BigNumber(ddayLabel(daysBetween(today, e.date)), "", e.color.inkColor)
        } else {
            BigNumber("$n", if (e.startAsDayOne) "일째" else "일", e.color.inkColor)
        }
        HeroSub("${formatLong(e.date)}부터")
        Tiles(
            "지나온 시간" to (if (notStarted) "시작 전" else formatPeriod(e.date, today, withDays = false)),
            "다음 기념일" to (next?.let { "${it.label} · ${ddayLabel(daysBetween(today, it.date))}" } ?: "-"),
        )
    }
    SectionHeader("기념일 타임라인", "100일 단위 · 주년")
    Timeline(countTimeline(e.date, e.startAsDayOne, today), e.color)
}

@Composable
private fun YearlyDetail(e: Event, now: LocalDateTime) {
    val today = now.toLocalDate()
    val next = nextYearly(e.date, today)
    val nth = next.year - e.date.year
    Hero(e.color, "기념일 · 매년", e.title) {
        BigNumber(ddayLabel(daysBetween(today, next)), "", e.color.inkColor)
        HeroSub(formatMonthDay(e.date, "매년 "))
        Tiles(
            "다음 날짜" to formatDot(next),
            "이번이" to (if (nth > 0) "${nth}번째" else "첫날"),
        )
    }
    SectionHeader("다가오는 날", "매년 반복")
    Timeline(yearlyTimeline(e.date, today), e.color)
}

@Composable
private fun TimeDetail(e: Event, now: LocalDateTime) {
    val target = timeTarget(e, now)
    val sec = secondsUntil(now, target)
    val passed = sec < 0
    val s = abs(sec)
    val hours = s / 3600
    val until = if (e.daily) {
        "매일 ${formatTime(e.time)}까지"
    } else {
        "${formatMonthDay(e.date)} (${weekdayShort(e.date)}) ${formatTime(e.time)}까지"
    }
    Hero(e.color, "시간", e.title) {
        HeroSub(until)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TimeTile("$hours", "시간", e.color, Modifier.weight(1f))
            TimeTile("%02d".format(s % 3600 / 60), "분", e.color, Modifier.weight(1f))
            TimeTile("%02d".format(s % 60), "초", e.color, Modifier.weight(1f))
        }
        Text(
            if (passed) "목표 시각에서 지난 시간이에요" else "1초마다 줄어들어요",
            fontSize = 12.sp,
            color = SubOnPastel,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
    val today = now.toLocalDate()
    val first = if (e.daily) {
        "다음 시각" to "${if (target.toLocalDate() == today) "오늘" else "내일"} ${formatTime(e.time)}"
    } else {
        "디데이" to ddayLabel(daysBetween(today, target.toLocalDate()))
    }
    InfoRows(
        listOf(
            first,
            "일 · 시간으로" to formatDhm(s) + if (passed) " 지남" else "",
            "분으로" to "${formatNumber(s / 60)}분",
            "목표 시각" to if (e.daily) "매일 ${formatTime(e.time)}" else "${formatDot(e.date)} ${formatTime(e.time)}",
            "반복" to if (e.daily) "매일" else "안 함",
        ),
    )
}

@Composable
private fun TimeTile(number: String, unit: String, pastel: Pastel, modifier: Modifier) {
    // 시간이 네 자리 이상이면 칸에 맞게 줄인다
    val size = when {
        number.length <= 3 -> 44.sp
        number.length == 4 -> 34.sp
        else -> 26.sp
    }
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(White)
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            number,
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = size,
            lineHeight = 48.sp,
            letterSpacing = (-1).sp,
            color = pastel.inkColor,
            maxLines = 1,
        )
        Text(unit, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SubOnPastel)
    }
}
