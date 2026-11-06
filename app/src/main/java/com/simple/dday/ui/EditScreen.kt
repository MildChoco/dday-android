package com.simple.dday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.dday.data.AnnivMode
import com.simple.dday.data.BasicIcons
import com.simple.dday.data.Event
import com.simple.dday.data.Kind
import com.simple.dday.data.Pastel
import com.simple.dday.data.defaultColor
import com.simple.dday.logic.IcuLunar
import com.simple.dday.logic.formatLong
import com.simple.dday.logic.formatMonthDay
import com.simple.dday.logic.formatTime
import com.simple.dday.logic.nextOccurrence
import com.simple.dday.logic.summarize
import com.simple.dday.ui.theme.Accent
import com.simple.dday.ui.theme.ChipBg
import com.simple.dday.ui.theme.DialogBg
import com.simple.dday.ui.theme.Gray
import com.simple.dday.ui.theme.Ink
import com.simple.dday.ui.theme.Line
import com.simple.dday.ui.theme.OnAccent
import com.simple.dday.ui.theme.OnPastelInk
import com.simple.dday.ui.theme.Paper
import com.simple.dday.ui.theme.SubOnPastel
import com.simple.dday.ui.theme.Track
import com.simple.dday.ui.theme.bgColor
import com.simple.dday.ui.theme.inkColor
import java.time.LocalDate
import java.time.LocalTime

private const val DAY_MS = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    initial: Event?,
    newId: () -> Long,
    onClose: () -> Unit,
    onSave: (Event) -> Unit,
    onDelete: () -> Unit,
    onSetArchived: (Boolean) -> Unit,
) {
    val now = rememberNow()
    var kind by rememberSaveable { mutableStateOf(initial?.kind ?: Kind.DDAY) }
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var icon by rememberSaveable { mutableStateOf(initial?.icon ?: "") }
    var date by rememberSaveable { mutableStateOf(initial?.date ?: LocalDate.now()) }
    var time by rememberSaveable {
        mutableStateOf(initial?.time ?: LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0))
    }
    var mode by rememberSaveable { mutableStateOf(initial?.annivMode ?: AnnivMode.COUNT) }
    var dayOne by rememberSaveable { mutableStateOf(initial?.startAsDayOne ?: true) }
    var daily by rememberSaveable { mutableStateOf(initial?.daily ?: false) }
    var lunar by rememberSaveable { mutableStateOf(initial?.lunar ?: false) }
    var lunarMonth by rememberSaveable { mutableStateOf(initial?.lunarMonth ?: 1) }
    var lunarDay by rememberSaveable { mutableStateOf(initial?.lunarDay ?: 1) }
    var birthYear by rememberSaveable { mutableStateOf(initial?.birthYear?.toString() ?: "") }
    var color by rememberSaveable { mutableStateOf(initial?.color ?: Kind.DDAY.defaultColor) }
    var dialog by rememberSaveable { mutableStateOf<String?>(null) }

    val isAnniv = kind == Kind.ANNIV
    val isTime = kind == Kind.TIME
    val isYearly = isAnniv && mode == AnnivMode.YEARLY
    val useLunar = isYearly && lunar && IcuLunar.supported
    val placeholder = when {
        isTime -> "예: 비행기 출발"
        isYearly -> "예: 엄마 생일"
        isAnniv -> "예: 우리 만난 날"
        else -> "예: 여행 가는 날"
    }
    val dateLabel = when {
        isYearly -> "기념일 날짜"
        isAnniv -> "시작한 날"
        else -> "날짜"
    }
    val draft = Event(
        id = initial?.id ?: 0L,
        kind = kind,
        title = title.trim(),
        date = date,
        time = time,
        annivMode = mode,
        startAsDayOne = dayOne,
        daily = daily,
        color = color,
        icon = icon,
        // 태어난 해는 매년 반복에서만 쓰고, 말이 되는 범위일 때만 받는다
        birthYear = birthYear.toIntOrNull()?.takeIf { isYearly && it in 1900..now.year },
        lunar = useLunar,
        lunarMonth = lunarMonth,
        lunarDay = lunarDay,
        pinned = initial?.pinned ?: false,
        archived = initial?.archived ?: false,
    )
    val canSave = draft.title.isNotEmpty()
    val accent = color.inkColor

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .safeDrawingPadding(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        ) {
            Box(Modifier.align(Alignment.CenterStart)) { PlainIconButton(AppIcons.Close, "닫기", onClick = onClose) }
            Text(
                if (initial == null) "새로 만들기" else "편집",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center),
            )
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (canSave) Accent else Track)
                    .clickable(enabled = canSave, role = Role.Button) {
                        onSave(draft.copy(id = initial?.id ?: newId()))
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("저장", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (canSave) OnAccent else Paper)
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Row(
                Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Kind.entries.forEach { k ->
                    CategoryCard(k, selected = k == kind, modifier = Modifier.weight(1f)) {
                        // 기본색을 쓰고 있었다면 새 카테고리의 기본색으로 바꾼다
                        if (color == kind.defaultColor) color = k.defaultColor
                        kind = k
                    }
                }
            }

            Group {
                SectionLabel("미리보기")
                val s = summarize(draft, now)
                EventCard(draft.title.ifEmpty { placeholder.removePrefix("예: ") }, kind.label, s.sub, s.parts, color, icon)
            }

            Group {
                SectionLabel("이름")
                TextInput(
                    value = title,
                    onValueChange = { title = it.take(40) },
                    placeholder = placeholder,
                    description = "이름",
                    cursor = accent,
                )
            }

            Group {
                SectionLabel("아이콘")
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconChoice("", selected = icon.isEmpty(), accent = accent) { icon = "" }
                    BasicIcons.forEach { emoji ->
                        IconChoice(emoji, selected = icon == emoji, accent = accent) { icon = emoji }
                    }
                }
            }

            if (isAnniv) {
                Group {
                    SectionLabel("세는 방식")
                    Segmented(
                        labels = listOf("일수로 세기", "매년 반복"),
                        selected = if (mode == AnnivMode.COUNT) 0 else 1,
                        descs = listOf("100일 · 1000일", "생일 · 결혼기념일"),
                    ) { mode = if (it == 0) AnnivMode.COUNT else AnnivMode.YEARLY }
                }
            }

            if (isYearly && IcuLunar.supported) {
                Group {
                    SectionLabel("달력")
                    Segmented(labels = listOf("양력", "음력"), selected = if (lunar) 1 else 0) { lunar = it == 1 }
                }
            }

            if (useLunar) {
                Group {
                    SectionLabel("음력 날짜")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownField("${lunarMonth}월", (1..12).map { "${it}월" }, Modifier.weight(1f)) { lunarMonth = it + 1 }
                        DropdownField("${lunarDay}일", (1..30).map { "${it}일" }, Modifier.weight(1f)) { lunarDay = it + 1 }
                    }
                    val next = nextOccurrence(draft, now.toLocalDate(), IcuLunar)
                    Hint("다음은 양력 ${formatLong(next)}이에요. 윤달은 평달로 세요")
                }
            } else if (!(isTime && daily)) {
                Group {
                    SectionLabel(dateLabel)
                    PickerField(formatLong(date), AppIcons.Calendar) { dialog = "date" }
                    if (isYearly) {
                        Hint("${formatMonthDay(date, "매년 ")}이 다가오면 다시 세요")
                    }
                }
            }

            if (isYearly) {
                Group {
                    SectionLabel("태어난 해 (선택)")
                    TextInput(
                        value = birthYear,
                        onValueChange = { v -> birthYear = v.filter(Char::isDigit).take(4) },
                        placeholder = "예: 1990",
                        description = "태어난 해",
                        cursor = accent,
                        numeric = true,
                    )
                    Hint("생일이라면 입력해 보세요. 몇 번째 생일인지와 만 나이를 보여줘요")
                }
            }

            if (isAnniv && !isYearly) {
                ToggleRow("시작한 날을 1일로 세기", "연애 기념일처럼 첫날부터 1일", dayOne, accent) { dayOne = it }
            }

            if (isTime) {
                Group {
                    SectionLabel("시각")
                    PickerField(formatTime(time), AppIcons.Clock) { dialog = "time" }
                }
                ToggleRow("매일 반복", "퇴근 · 마감처럼 매일 같은 시각까지", daily, accent) { daily = it }
            }

            Group {
                SectionLabel("색상")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Pastel.entries.forEach { p -> Swatch(p, selected = p == color) { color = p } }
                }
            }

            if (initial != null) {
                Column {
                    TextButton(onClick = { onSetArchived(!initial.archived) }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(
                            if (initial.archived) "보관함에서 꺼내기" else "보관함으로 보내기",
                            fontSize = 15.sp,
                            color = Gray,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    TextButton(onClick = { dialog = "delete" }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("이 항목 삭제", fontSize = 15.sp, color = Pastel.PINK.inkColor, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    when (dialog) {
        "date" -> DateDialog(date, color, onDismiss = { dialog = null }) { date = it; dialog = null }
        "time" -> TimeDialog(time, color, onDismiss = { dialog = null }) { time = it; dialog = null }
        "delete" -> AlertDialog(
            onDismissRequest = { dialog = null },
            containerColor = DialogBg,
            title = { Text("삭제할까요?", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Text("‘${initial?.title}’ 항목을 지우면 되돌릴 수 없어요.".keepWords(), fontSize = 14.sp, color = Gray) },
            confirmButton = {
                TextButton(onClick = { dialog = null; onDelete() }) {
                    Text("삭제", color = Pastel.PINK.inkColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { dialog = null }) { Text("취소", color = Gray) }
            },
        )
    }
}

@Composable
private fun Group(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
}

@Composable
private fun CategoryCard(kind: Kind, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val p = kind.defaultColor
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .fillMaxHeight()
            .heightIn(min = 84.dp)
            .clip(shape)
            .background(if (selected) p.bgColor else Paper)
            .border(2.dp, if (selected) p.inkColor else Line, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(kind.label, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(kind.desc.keepWords(), fontSize = 12.sp, color = SubOnPastel)
    }
}

@Composable
private fun PickerField(text: String, icon: ImageVector, onClick: () -> Unit) {
    FieldBox(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(icon, contentDescription = null, tint = Gray, modifier = Modifier.size(20.dp))
        }
    }
}

/** 누르면 목록이 펼쳐지는 입력칸 */
@Composable
private fun DropdownField(text: String, items: List<String>, modifier: Modifier, onSelect: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        PickerField(text, AppIcons.Down) { open = true }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            items.forEachIndexed { i, label ->
                DropdownMenuItem(
                    text = { Text(label, fontSize = 15.sp, color = Ink) },
                    onClick = {
                        open = false
                        onSelect(i)
                    },
                )
            }
        }
    }
}

/** 아이콘 고르기의 한 칸. emoji가 비어 있으면 "없음" */
@Composable
private fun IconChoice(emoji: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(ChipBg)
            .border(2.dp, if (selected) accent else Color.Transparent, CircleShape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (emoji.isEmpty()) {
            Text("없음", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Gray)
        } else {
            Text(emoji, fontSize = 20.sp)
        }
    }
}

@Composable
private fun Swatch(p: Pastel, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(2.dp, if (selected) p.inkColor else Color.Transparent, CircleShape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = p.label }
            .padding(5.dp)
            .clip(CircleShape)
            .background(p.bgColor)
            .border(1.dp, Ink.copy(alpha = 0.06f), CircleShape),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(initial: LocalDate, pastel: Pastel, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial.toEpochDay() * DAY_MS)
    val colors = DatePickerDefaults.colors(
        containerColor = DialogBg,
        selectedDayContainerColor = pastel.inkColor,
        selectedDayContentColor = OnPastelInk,
        todayContentColor = pastel.inkColor,
        todayDateBorderColor = pastel.inkColor,
        selectedYearContainerColor = pastel.inkColor,
        selectedYearContentColor = OnPastelInk,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = colors,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onPick(LocalDate.ofEpochDay(Math.floorDiv(it, DAY_MS))) } ?: onDismiss()
            }) { Text("확인", color = Ink, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소", color = Gray) } },
    ) {
        DatePicker(state = state, colors = colors)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(initial: LocalTime, pastel: Pastel, onDismiss: () -> Unit, onPick: (LocalTime) -> Unit) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogBg,
        confirmButton = {
            TextButton(onClick = { onPick(LocalTime.of(state.hour, state.minute)) }) {
                Text("확인", color = Ink, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소", color = Gray) } },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = ChipBg,
                    selectorColor = pastel.inkColor,
                    periodSelectorSelectedContainerColor = pastel.bgColor,
                    periodSelectorSelectedContentColor = pastel.inkColor,
                    timeSelectorSelectedContainerColor = pastel.bgColor,
                    timeSelectorSelectedContentColor = pastel.inkColor,
                ),
            )
        },
    )
}
