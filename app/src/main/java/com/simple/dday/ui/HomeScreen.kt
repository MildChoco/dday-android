package com.simple.dday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.dday.data.Event
import com.simple.dday.data.Kind
import com.simple.dday.logic.Summary
import com.simple.dday.logic.formatMonthDay
import com.simple.dday.logic.summarize
import com.simple.dday.logic.weekdayFull
import com.simple.dday.ui.theme.ChipBg
import com.simple.dday.ui.theme.Gray
import com.simple.dday.ui.theme.Ink
import com.simple.dday.ui.theme.LabelGray
import com.simple.dday.ui.theme.Nunito
import com.simple.dday.ui.theme.White

@Composable
fun HomeScreen(events: List<Event>, onAdd: () -> Unit, onOpen: (Event) -> Unit) {
    val now = rememberNow()
    val today = now.toLocalDate()
    // null이면 전체
    var filter by rememberSaveable { mutableStateOf<Kind?>(null) }

    val all = events
        .map { it to summarize(it, now) }
        .sortedWith(
            compareBy<Pair<Event, Summary>> { it.second.nextAt == null }
                .thenBy { it.second.nextAt }
                .thenBy { it.first.id },
        )
    val shown = all.filter { filter == null || it.first.kind == filter }

    Column(
        Modifier
            .fillMaxSize()
            .background(White)
            .safeDrawingPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${formatMonthDay(today)} ${weekdayFull(today)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Gray)
                Text("나의 디데이", fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Ink)
                    .clickable(role = Role.Button, onClickLabel = "새로 만들기", onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.Plus, contentDescription = "새로 만들기", tint = White, modifier = Modifier.size(22.dp))
            }
        }

        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip("전체", all.size, filter == null) { filter = null }
            Kind.entries.forEach { k ->
                FilterChip(k.label, all.count { it.first.kind == k }, filter == k) { filter = k }
            }
        }
        Spacer(Modifier.height(18.dp))

        if (shown.isEmpty()) {
            EmptyState(filter == null)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(shown, key = { it.first.id }) { (e, s) ->
                    EventCard(e.title, e.kind.label, s.sub, s.parts, e.color) { onOpen(e) }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) Ink else ChipBg)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val fg = if (selected) White else LabelGray
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = fg)
        Text(
            "$count",
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = fg,
            modifier = Modifier.alpha(0.75f),
        )
    }
}

@Composable
private fun EmptyState(noneAtAll: Boolean) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            if (noneAtAll) "아직 등록한 날이 없어요" else "이 카테고리에는 아직 없어요",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "오른쪽 위 + 버튼으로 디데이, 기념일, 시간을 추가해 보세요",
            fontSize = 13.sp,
            color = Gray,
            textAlign = TextAlign.Center,
        )
    }
}
