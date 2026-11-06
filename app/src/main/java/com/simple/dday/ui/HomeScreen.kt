package com.simple.dday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.simple.dday.data.SortOrder
import com.simple.dday.logic.formatMonthDay
import com.simple.dday.logic.sortForHome
import com.simple.dday.logic.summarize
import com.simple.dday.logic.weekdayFull
import com.simple.dday.ui.theme.Accent
import com.simple.dday.ui.theme.ChipBg
import com.simple.dday.ui.theme.Gray
import com.simple.dday.ui.theme.Ink
import com.simple.dday.ui.theme.LabelGray
import com.simple.dday.ui.theme.Nunito
import com.simple.dday.ui.theme.OnAccent
import com.simple.dday.ui.theme.Paper

@Composable
fun HomeScreen(
    events: List<Event>,
    title: String,
    sortOrder: SortOrder,
    onSort: (SortOrder) -> Unit,
    onAdd: () -> Unit,
    onOpen: (Event) -> Unit,
    onSettings: () -> Unit,
    onArchive: () -> Unit,
) {
    val now = rememberNow()
    val today = now.toLocalDate()
    // null이면 전체
    var filter by rememberSaveable { mutableStateOf<Kind?>(null) }

    val archivedCount = events.count { it.archived }
    val all = sortForHome(events.filterNot { it.archived }.map { it to summarize(it, now) }, sortOrder)
    val shown = all.filter { filter == null || it.first.kind == filter }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .safeDrawingPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val dateText = "${formatMonthDay(today)} ${weekdayFull(today)}"
                if (title.isBlank()) {
                    // 제목을 정하지 않았으면 오늘 날짜가 제목 자리에 온다
                    Text(dateText, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                } else {
                    Text(dateText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Gray)
                    Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, maxLines = 1)
                }
            }
            Box(Modifier.height(48.dp), contentAlignment = Alignment.Center) {
                PlainIconButton(AppIcons.Settings, "설정", tint = LabelGray, onClick = onSettings)
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Accent)
                    .clickable(role = Role.Button, onClickLabel = "새로 만들기", onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.Plus, contentDescription = "새로 만들기", tint = OnAccent, modifier = Modifier.size(22.dp))
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

        SortMenu(sortOrder, onSort)

        if (shown.isEmpty()) {
            EmptyState(noneAtAll = all.isEmpty(), archivedCount = archivedCount, onArchive = onArchive)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(shown, key = { it.first.id }) { (e, s) ->
                    EventCard(e.title, e.kind.label, s.sub, s.parts, e.color, e.icon, e.pinned) { onOpen(e) }
                }
                if (archivedCount > 0) {
                    item(key = "archive") { ArchiveLink(archivedCount, onArchive) }
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
            .background(if (selected) Accent else ChipBg)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val fg = if (selected) OnAccent else LabelGray
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

/** 목록 오른쪽 위의 정렬 버튼. 누르면 정렬 방식을 고르는 메뉴가 열린다 */
@Composable
private fun SortMenu(current: SortOrder, onSort: (SortOrder) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box {
            Row(
                Modifier
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClickLabel = "정렬 바꾸기") { open = true }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(AppIcons.Sort, contentDescription = null, tint = Gray, modifier = Modifier.size(16.dp))
                Text(current.label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Gray)
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                SortOrder.entries.forEach { order ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                order.label,
                                fontSize = 15.sp,
                                fontWeight = if (order == current) FontWeight.Bold else FontWeight.Normal,
                                color = Ink,
                            )
                        },
                        onClick = {
                            open = false
                            onSort(order)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveLink(count: Int, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            Modifier
                .heightIn(min = 44.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(AppIcons.Archive, contentDescription = null, tint = Gray, modifier = Modifier.size(16.dp))
            Text("보관함 ${count}개", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Gray)
        }
    }
}

@Composable
private fun EmptyState(noneAtAll: Boolean, archivedCount: Int, onArchive: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            if (noneAtAll) "아직 등록한 날이 없어요" else "이 카테고리에는 아직 없어요",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "오른쪽 위 + 버튼으로 디데이, 기념일, 시간을 추가해 보세요".keepWords(),
            fontSize = 13.sp,
            color = Gray,
            textAlign = TextAlign.Center,
        )
        if (archivedCount > 0) ArchiveLink(archivedCount, onArchive)
    }
}
