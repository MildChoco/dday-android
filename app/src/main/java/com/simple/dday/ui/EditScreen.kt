package com.simple.dday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.dday.data.AnnivMode
import com.simple.dday.data.Event
import com.simple.dday.data.Kind
import com.simple.dday.data.Pastel
import com.simple.dday.data.defaultColor
import com.simple.dday.logic.formatLong
import com.simple.dday.logic.formatMonthDay
import com.simple.dday.logic.formatTime
import com.simple.dday.logic.summarize
import com.simple.dday.ui.theme.ChipBg
import com.simple.dday.ui.theme.FieldBg
import com.simple.dday.ui.theme.FieldBorder
import com.simple.dday.ui.theme.Gray
import com.simple.dday.ui.theme.Ink
import com.simple.dday.ui.theme.Line
import com.simple.dday.ui.theme.Placeholder
import com.simple.dday.ui.theme.SubOnPastel
import com.simple.dday.ui.theme.Track
import com.simple.dday.ui.theme.White
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
) {
    val now = rememberNow()
    var kind by rememberSaveable { mutableStateOf(initial?.kind ?: Kind.DDAY) }
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var date by rememberSaveable { mutableStateOf(initial?.date ?: LocalDate.now()) }
    var time by rememberSaveable {
        mutableStateOf(initial?.time ?: LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0))
    }
    var mode by rememberSaveable { mutableStateOf(initial?.annivMode ?: AnnivMode.COUNT) }
    var dayOne by rememberSaveable { mutableStateOf(initial?.startAsDayOne ?: true) }
    var daily by rememberSaveable { mutableStateOf(initial?.daily ?: false) }
    var color by rememberSaveable { mutableStateOf(initial?.color ?: Kind.DDAY.defaultColor) }
    var dialog by rememberSaveable { mutableStateOf<String?>(null) }

    val isAnniv = kind == Kind.ANNIV
    val isTime = kind == Kind.TIME
    val placeholder = when {
        isTime -> "예: 비행기 출발"
        isAnniv && mode == AnnivMode.YEARLY -> "예: 엄마 생일"
        isAnniv -> "예: 우리 만난 날"
        else -> "예: 여행 가는 날"
    }
    val dateLabel = when {
        isAnniv && mode == AnnivMode.YEARLY -> "기념일 날짜"
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
    )
    val canSave = draft.title.isNotEmpty()
    val accent = color.inkColor

    Column(
        Modifier
            .fillMaxSize()
            .background(White)
            .safeDrawingPadding(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        ) {
            Box(Modifier.align(Alignment.CenterStart)) { PlainIconButton(AppIcons.Close, "닫기", onClose) }
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
                    .background(if (canSave) Ink else Track)
                    .clickable(enabled = canSave, role = Role.Button) {
                        onSave(draft.copy(id = initial?.id ?: newId()))
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("저장", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = White)
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
                EventCard(draft.title.ifEmpty { placeholder.removePrefix("예: ") }, kind.label, s.sub, s.parts, color)
            }

            Group {
                SectionLabel("이름")
                FieldBox(Modifier.semantics { contentDescription = "이름" }) {
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it.take(40) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 16.sp, color = Ink),
                        cursorBrush = SolidColor(accent),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (title.isEmpty()) Text(placeholder, fontSize = 16.sp, color = Placeholder)
                                inner()
                            }
                        },
                    )
                }
            }

            if (!(isTime && daily)) {
                Group {
                    SectionLabel(dateLabel)
                    PickerField(formatLong(date), AppIcons.Calendar) { dialog = "date" }
                    if (isAnniv && mode == AnnivMode.YEARLY) {
                        Text("${formatMonthDay(date, "매년 ")}이 다가오면 다시 세요", fontSize = 12.sp, color = Gray)
                    }
                }
            }

            if (isTime) {
                Group {
                    SectionLabel("시각")
                    PickerField(formatTime(time), AppIcons.Clock) { dialog = "time" }
                }
                ToggleRow("매일 반복", "퇴근 · 마감처럼 매일 같은 시각까지", daily, accent) { daily = it }
            }

            if (isAnniv) {
                Group {
                    SectionLabel("세는 방식")
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ChipBg)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        ModeButton("일수로 세기", "100일 · 1000일", mode == AnnivMode.COUNT, Modifier.weight(1f)) {
                            mode = AnnivMode.COUNT
                        }
                        ModeButton("매년 반복", "생일 · 결혼기념일", mode == AnnivMode.YEARLY, Modifier.weight(1f)) {
                            mode = AnnivMode.YEARLY
                        }
                    }
                }
                if (mode == AnnivMode.COUNT) {
                    ToggleRow("시작한 날을 1일로 세기", "연애 기념일처럼 첫날부터 1일", dayOne, accent) { dayOne = it }
                }
            }

            Group {
                SectionLabel("색상")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Pastel.entries.forEach { p -> Swatch(p, selected = p == color) { color = p } }
                }
            }

            if (initial != null) {
                TextButton(onClick = { dialog = "delete" }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("이 항목 삭제", fontSize = 15.sp, color = Pastel.PINK.inkColor, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    when (dialog) {
        "date" -> DateDialog(date, color, onDismiss = { dialog = null }) { date = it; dialog = null }
        "time" -> TimeDialog(time, color, onDismiss = { dialog = null }) { time = it; dialog = null }
        "delete" -> AlertDialog(
            onDismissRequest = { dialog = null },
            containerColor = White,
            title = { Text("삭제할까요?", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Text("‘${initial?.title}’ 항목을 지우면 되돌릴 수 없어요.", fontSize = 14.sp, color = SubOnPastel) },
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
            .background(if (selected) p.bgColor else White)
            .border(2.dp, if (selected) p.inkColor else Line, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(kind.label, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(kind.desc, fontSize = 12.sp, color = SubOnPastel)
    }
}

@Composable
private fun FieldBox(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(FieldBg)
            .border(1.5.dp, FieldBorder, shape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
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

@Composable
private fun ModeButton(label: String, desc: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier
            .heightIn(min = 56.dp)
            .then(if (selected) Modifier.shadow(2.dp, shape) else Modifier)
            .clip(shape)
            .background(if (selected) White else Color.Transparent)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(desc, fontSize = 11.sp, color = SubOnPastel)
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
        containerColor = White,
        selectedDayContainerColor = pastel.inkColor,
        selectedDayContentColor = White,
        todayContentColor = pastel.inkColor,
        todayDateBorderColor = pastel.inkColor,
        selectedYearContainerColor = pastel.inkColor,
        selectedYearContentColor = White,
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
        containerColor = White,
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
