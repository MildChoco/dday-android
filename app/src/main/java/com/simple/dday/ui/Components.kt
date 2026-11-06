package com.simple.dday.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.simple.dday.data.Pastel
import com.simple.dday.logic.Part
import com.simple.dday.logic.Step
import com.simple.dday.logic.TimelineItem
import com.simple.dday.ui.theme.ChipBg
import com.simple.dday.ui.theme.Divider
import com.simple.dday.ui.theme.Gray
import com.simple.dday.ui.theme.Ink
import com.simple.dday.ui.theme.LabelGray
import com.simple.dday.ui.theme.Nunito
import com.simple.dday.ui.theme.OnPastelInk
import com.simple.dday.ui.theme.Paper
import com.simple.dday.ui.theme.SegSelected
import com.simple.dday.ui.theme.SubOnPastel
import com.simple.dday.ui.theme.TileSoft
import com.simple.dday.ui.theme.Track
import com.simple.dday.ui.theme.bgColor
import com.simple.dday.ui.theme.inkColor
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/** 매초 정각에 갱신되는 현재 시각 */
@Composable
fun rememberNow(): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000L - System.currentTimeMillis() % 1000L)
        }
    }
    return now
}

@Composable
fun Tag(text: String, pastel: Pastel) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = pastel.inkColor,
        modifier = Modifier
            .background(TileSoft, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** "52시간 14분"처럼 숫자는 크게, 단위는 작게 */
@Composable
fun ValueParts(parts: List<Part>, color: Color, numberSize: TextUnit = 28.sp, unitSize: TextUnit = 13.sp) {
    Row {
        parts.forEachIndexed { i, p ->
            Text(
                p.number,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = numberSize,
                lineHeight = 1.1.em,
                letterSpacing = (-0.5).sp,
                color = color,
                modifier = Modifier.alignByBaseline(),
            )
            if (p.unit.isNotEmpty()) {
                Text(
                    p.unit,
                    fontSize = unitSize,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier
                        .alignByBaseline()
                        .padding(start = 2.dp, end = if (i < parts.lastIndex) 4.dp else 0.dp),
                )
            }
        }
    }
}

@Composable
fun EventCard(
    title: String,
    kindLabel: String,
    sub: String,
    parts: List<Part>,
    pastel: Pastel,
    icon: String = "",
    pinned: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(pastel.bgColor)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (icon.isNotEmpty()) Text(icon, fontSize = 16.sp)
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (pinned) {
                    Icon(AppIcons.Pin, contentDescription = "고정됨", tint = pastel.inkColor, modifier = Modifier.size(14.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Tag(kindLabel, pastel)
                Text(sub, fontSize = 13.sp, color = SubOnPastel, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        ValueParts(parts, pastel.inkColor)
    }
}

@Composable
fun PlainIconButton(icon: ImageVector, description: String, tint: Color = Ink, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(24.dp))
    }
}

/** 가운데 제목과 뒤로 버튼만 있는 위쪽 막대 */
@Composable
fun TitleBar(title: String, onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Box(Modifier.align(Alignment.CenterStart)) { PlainIconButton(AppIcons.Back, "뒤로", onClick = onBack) }
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LabelGray)
}

@Composable
fun SectionHeader(title: String, hint: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.alignByBaseline())
        Text(hint, fontSize = 12.sp, color = Gray, modifier = Modifier.alignByBaseline())
    }
}

/** 스위치가 달린 한 줄. 줄 전체가 터치 영역이다 */
@Composable
fun ToggleRow(title: String, desc: String, checked: Boolean, accent: Color, onChange: (Boolean) -> Unit) {
    val knob by animateDpAsState(if (checked) 20.dp else 0.dp, label = "knob")
    val track by animateColorAsState(if (checked) accent else Track, label = "track")
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(desc, fontSize = 12.sp, color = Gray)
        }
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .size(width = 52.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(track)
                .padding(3.dp),
        ) {
            Box(
                Modifier
                    .offset(x = knob)
                    .size(26.dp)
                    .shadow(1.dp, CircleShape)
                    .background(Color.White, CircleShape),
            )
        }
    }
}

/** 여러 개 중 하나를 고르는 막대. descs를 주면 이름 아래에 설명이 한 줄 붙는다 */
@Composable
fun Segmented(labels: List<String>, selected: Int, descs: List<String> = emptyList(), onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ChipBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        labels.forEachIndexed { i, label ->
            val on = i == selected
            val shape = RoundedCornerShape(12.dp)
            Column(
                Modifier
                    .weight(1f)
                    .heightIn(min = if (descs.isEmpty()) 44.dp else 56.dp)
                    .then(if (on) Modifier.shadow(2.dp, shape) else Modifier)
                    .clip(shape)
                    .background(if (on) SegSelected else Color.Transparent)
                    .selectable(selected = on, role = Role.RadioButton, onClick = { onSelect(i) })
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                descs.getOrNull(i)?.let { Text(it, fontSize = 11.sp, color = SubOnPastel) }
            }
        }
    }
}

/** 화면 너비를 채우는 보조 버튼 */
@Composable
fun SoftButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ChipBg)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LabelGray)
    }
}

// ---- 상세 화면 ----

@Composable
fun Hero(pastel: Pastel, tag: String, title: String, icon: String = "", content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .padding(start = 20.dp, end = 20.dp, top = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(pastel.bgColor)
            .padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Tag(tag, pastel)
        Row(
            Modifier.padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon.isNotEmpty()) Text(icon, fontSize = 20.sp)
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        content()
    }
}

@Composable
fun BigNumber(number: String, unit: String, color: Color) {
    Row {
        Text(
            number,
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 68.sp,
            lineHeight = 1.05.em,
            letterSpacing = (-2).sp,
            color = color,
            modifier = Modifier.alignByBaseline(),
        )
        if (unit.isNotEmpty()) {
            Text(
                unit,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier
                    .alignByBaseline()
                    .padding(start = 4.dp),
            )
        }
    }
}

@Composable
fun HeroSub(text: String) {
    Text(text, fontSize = 14.sp, color = SubOnPastel)
}

/** 히어로 카드 안의 정보 칸 두 개 */
@Composable
fun Tiles(vararg tiles: Pair<String, String>) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.forEach { (label, value) ->
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TileSoft)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(label, fontSize = 12.sp, color = SubOnPastel)
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoRows(rows: List<Pair<String, String>>) {
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp)) {
        rows.forEach { (label, value) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(label, fontSize = 14.sp, color = Gray)
                Text(
                    value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(thickness = 1.dp, color = Divider)
        }
    }
}

/** 점과 선으로 이은 기념일 목록. 지난 것은 채운 점, 다가올 것은 빈 점 */
@Composable
fun Timeline(items: List<TimelineItem>, pastel: Pastel) {
    val ink = pastel.inkColor
    val soft = lerp(Paper, ink, 0.15f)
    val futureDot = lerp(Paper, ink, 0.3f)
    val todayIdx = items.indexOfFirst { it.step == Step.TODAY }
    Column(Modifier.padding(horizontal = 20.dp)) {
        items.forEachIndexed { i, item ->
            val past = item.step == Step.PAST
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .heightIn(min = 52.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    Modifier
                        .width(16.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val top = if (i == 0) Color.Transparent else if (i <= todayIdx) ink else soft
                    val bottom = if (i == items.lastIndex) Color.Transparent else if (i < todayIdx) ink else soft
                    Box(
                        Modifier
                            .width(2.dp)
                            .height(20.dp)
                            .background(top),
                    )
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (past || item.step == Step.TODAY) ink else Paper)
                            .border(2.dp, if (item.step == Step.FUTURE) futureDot else ink, CircleShape),
                    )
                    Box(
                        Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(bottom),
                    )
                }
                Row(
                    Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (item.step == Step.TODAY) lerp(Paper, pastel.bgColor, 0.5f) else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        // 1000일 단위는 강조색과 큰 글자로 눈에 띄게 한다
                        Text(
                            item.label,
                            fontSize = if (item.highlight) 17.sp else 15.sp,
                            fontWeight = if (item.highlight) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (item.highlight) ink else if (past) Gray else Ink,
                        )
                        Text(item.date, fontSize = 13.sp, color = Gray)
                    }
                    if (item.right.isNotEmpty()) {
                        val (pillBg, pillFg) = when (item.step) {
                            Step.NEXT, Step.TODAY -> ink to OnPastelInk
                            Step.FUTURE -> ChipBg to LabelGray
                            Step.PAST -> Color.Transparent to Gray
                        }
                        Text(
                            item.right,
                            fontFamily = Nunito,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = pillFg,
                            modifier = Modifier
                                .background(pillBg, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
