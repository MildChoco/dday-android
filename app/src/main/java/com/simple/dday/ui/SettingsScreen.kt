package com.simple.dday.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.dday.R
import com.simple.dday.data.Event
import com.simple.dday.data.EventStore
import com.simple.dday.data.Pastel
import com.simple.dday.data.Settings
import com.simple.dday.data.SortOrder
import com.simple.dday.data.Support
import com.simple.dday.data.ThemeMode
import com.simple.dday.logic.summarize
import com.simple.dday.ui.theme.Divider
import com.simple.dday.ui.theme.Gray
import com.simple.dday.ui.theme.Paper
import com.simple.dday.ui.theme.inkColor

@Composable
fun SettingsScreen(
    settings: Settings,
    store: EventStore,
    onBack: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenLicenses: () -> Unit,
) {
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "-"
    }
    val archivedCount = store.events.count { it.archived }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .safeDrawingPadding(),
    ) {
        TitleBar("설정", onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            Block("화면 테마") {
                Segmented(
                    labels = ThemeMode.entries.map { it.label },
                    selected = settings.themeMode.ordinal,
                ) { settings.setTheme(ThemeMode.entries[it]) }
            }

            Block("홈 화면 제목") {
                TextInput(
                    value = settings.homeTitle,
                    onValueChange = { settings.changeHomeTitle(it.take(20)) },
                    placeholder = "예: 나의 디데이",
                    description = "홈 화면 제목",
                    cursor = Pastel.LAVENDER.inkColor,
                )
                Note("비워 두면 오늘 날짜가 제목 자리에 보여요")
            }

            Block("목록 정렬") {
                Segmented(
                    labels = SortOrder.entries.map { it.label },
                    selected = settings.sortOrder.ordinal,
                ) { settings.setSort(SortOrder.entries[it]) }
                Note("고정한 항목은 항상 맨 위에 보여요")
            }

            Block("정리") {
                LinkRow("보관함", "홈 목록에서 숨긴 항목", trailing = "${archivedCount}개", onClick = onOpenArchive)
            }

            // 후원 받는 곳이 정해져 있을 때만 보인다. 누르면 브라우저나 해당 앱이 열린다
            val supportLinks = Support.links
            if (supportLinks.isNotEmpty()) {
                Block("응원하기") {
                    Column {
                        supportLinks.forEach { (name, url) ->
                            LinkRow(name) {
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                            }
                        }
                    }
                    Note("이 앱은 무료이고 광고가 없어요. 도움이 됐다면 응원을 보내 주세요")
                }
            }

            Block("정보") {
                Column {
                    LinkRow("오픈소스 라이선스", onClick = onOpenLicenses)
                    LinkRow("버전", trailing = version)
                }
            }
        }
    }
}

@Composable
private fun Block(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(title)
        content()
    }
}

@Composable
private fun Note(text: String) {
    Text(text, fontSize = 12.sp, color = Gray)
}

/** 설정의 한 줄. onClick이 없으면 값만 보여주는 줄이다 */
@Composable
private fun LinkRow(title: String, desc: String? = null, trailing: String? = null, onClick: (() -> Unit)? = null) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                if (desc != null) Text(desc, fontSize = 12.sp, color = Gray)
            }
            if (trailing != null) Text(trailing, fontSize = 14.sp, color = Gray)
            if (onClick != null) Icon(AppIcons.Forward, contentDescription = null, tint = Gray, modifier = Modifier.size(18.dp))
        }
        HorizontalDivider(thickness = 1.dp, color = Divider)
    }
}

/** 보관함: 홈 목록에서 숨긴 항목들. 누르면 상세 화면에서 복구할 수 있다 */
@Composable
fun ArchiveScreen(events: List<Event>, onBack: () -> Unit, onOpen: (Event) -> Unit) {
    val now = rememberNow()
    val archived = events.filter { it.archived }.sortedByDescending { it.id }
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .safeDrawingPadding(),
    ) {
        TitleBar("보관함", onBack)
        if (archived.isEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp, vertical = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("보관함이 비어 있어요", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "지난 디데이는 상세 화면에서 보관함으로 보낼 수 있어요",
                    fontSize = 13.sp,
                    color = Gray,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(archived, key = { it.id }) { e ->
                    val s = summarize(e, now)
                    EventCard(e.title, e.kind.label, s.sub, s.parts, e.color, e.icon) { onOpen(e) }
                }
            }
        }
    }
}

@Composable
fun LicensesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val text = remember { context.resources.openRawResource(R.raw.nunito_ofl).bufferedReader().use { it.readText() } }
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .safeDrawingPadding(),
    ) {
        TitleBar("오픈소스 라이선스", onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Nunito", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("숫자 표시에 쓰는 글꼴", fontSize = 13.sp, color = Gray)
            Text(text, fontSize = 12.sp, color = Gray)
        }
    }
}
