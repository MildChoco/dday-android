package com.simple.dday.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.simple.dday.R
import com.simple.dday.data.Pastel

/** 화면에 쓰는 색 묶음. 라이트와 다크 두 벌이 있다 */
@Immutable
data class AppColors(
    val isDark: Boolean,
    /** 화면 바탕 */
    val paper: Color,
    /** 기본 글자 */
    val ink: Color,
    /** 보조 글자 */
    val gray: Color,
    /** 파스텔 바탕 위의 보조 글자 */
    val subOnPastel: Color,
    val labelGray: Color,
    val line: Color,
    val divider: Color,
    val chipBg: Color,
    val fieldBg: Color,
    val fieldBorder: Color,
    val track: Color,
    val placeholder: Color,
    /** 주요 버튼 바탕과 그 위의 글자 */
    val accent: Color,
    val onAccent: Color,
    /** 파스텔 강조색으로 칠한 알약 위의 글자 */
    val onPastelInk: Color,
    /** 파스텔 카드 안에 얹는 칸. Strong은 또렷한 칸, Soft는 비치는 칸 */
    val tileStrong: Color,
    val tileSoft: Color,
    /** 선택 막대에서 고른 칸 */
    val segSelected: Color,
    val dialog: Color,
)

private val LightColors = AppColors(
    isDark = false,
    paper = Color(0xFFFFFFFF),
    ink = Color(0xFF2B2B33),
    gray = Color(0xFF6B6B78),
    subOnPastel = Color(0xFF5E5E6B),
    labelGray = Color(0xFF4A4A55),
    line = Color(0xFFECECF2),
    divider = Color(0xFFF0F0F5),
    chipBg = Color(0xFFF3F3F7),
    fieldBg = Color(0xFFFAFAFC),
    fieldBorder = Color(0xFFE6E6EE),
    track = Color(0xFFD9D9E3),
    placeholder = Color(0xFF9A9AA6),
    accent = Color(0xFF2B2B33),
    onAccent = Color(0xFFFFFFFF),
    onPastelInk = Color(0xFFFFFFFF),
    tileStrong = Color(0xFFFFFFFF),
    tileSoft = Color(0xCCFFFFFF),
    segSelected = Color(0xFFFFFFFF),
    dialog = Color(0xFFFFFFFF),
)

private val DarkColors = AppColors(
    isDark = true,
    paper = Color(0xFF16161B),
    ink = Color(0xFFF2F2F5),
    gray = Color(0xFFA3A3B0),
    subOnPastel = Color(0xFFCFCFD8),
    labelGray = Color(0xFFC9C9D3),
    line = Color(0xFF2E2E38),
    divider = Color(0xFF26262E),
    chipBg = Color(0xFF2A2A33),
    fieldBg = Color(0xFF1E1E25),
    fieldBorder = Color(0xFF363641),
    track = Color(0xFF44444F),
    placeholder = Color(0xFF7E7E8C),
    accent = Color(0xFFF2F2F5),
    onAccent = Color(0xFF16161B),
    onPastelInk = Color(0xFF16161B),
    tileStrong = Color(0x40000000),
    tileSoft = Color(0x33000000),
    segSelected = Color(0xFF40404C),
    dialog = Color(0xFF24242C),
)

val LocalAppColors = staticCompositionLocalOf { LightColors }

// 화면 코드에서 색 이름만으로 쓸 수 있게 한 줄씩 꺼내 둔다
val Paper: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.paper
val Ink: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.ink
val Gray: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.gray
val SubOnPastel: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.subOnPastel
val LabelGray: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.labelGray
val Line: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.line
val Divider: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.divider
val ChipBg: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.chipBg
val FieldBg: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.fieldBg
val FieldBorder: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.fieldBorder
val Track: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.track
val Placeholder: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.placeholder
val Accent: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.accent
val OnAccent: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.onAccent
val OnPastelInk: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.onPastelInk
val TileStrong: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.tileStrong
val TileSoft: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.tileSoft
val SegSelected: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.segSelected
val DialogBg: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.dialog

/** 숫자 전용 글꼴. 한글은 시스템 글꼴을 쓴다 */
val Nunito = FontFamily(Font(R.font.nunito_extrabold, FontWeight.ExtraBold))

val Pastel.bgColor: Color
    @Composable @ReadOnlyComposable get() = Color(if (LocalAppColors.current.isDark) darkBg else bg)

val Pastel.inkColor: Color
    @Composable @ReadOnlyComposable get() = Color(if (LocalAppColors.current.isDark) darkInk else ink)

@Composable
fun DDayTheme(dark: Boolean, content: @Composable () -> Unit) {
    val c = if (dark) DarkColors else LightColors
    val scheme = if (dark) {
        darkColorScheme(
            primary = c.ink,
            onPrimary = c.paper,
            secondary = c.ink,
            onSecondary = c.paper,
            background = c.paper,
            onBackground = c.ink,
            surface = c.paper,
            onSurface = c.ink,
            surfaceVariant = c.chipBg,
            onSurfaceVariant = c.gray,
            outline = c.fieldBorder,
            outlineVariant = c.line,
            surfaceContainerLowest = c.paper,
            surfaceContainerLow = c.dialog,
            surfaceContainer = c.dialog,
            surfaceContainerHigh = c.dialog,
            surfaceContainerHighest = c.chipBg,
        )
    } else {
        lightColorScheme(
            primary = c.ink,
            onPrimary = c.paper,
            secondary = c.ink,
            onSecondary = c.paper,
            background = c.paper,
            onBackground = c.ink,
            surface = c.paper,
            onSurface = c.ink,
            surfaceVariant = c.chipBg,
            onSurfaceVariant = c.gray,
            outline = c.fieldBorder,
            outlineVariant = c.line,
            surfaceContainerLowest = c.paper,
            surfaceContainerLow = c.dialog,
            surfaceContainer = c.dialog,
            surfaceContainerHigh = c.dialog,
            surfaceContainerHighest = c.chipBg,
        )
    }
    val base = TextStyle(fontSize = 16.sp, lineHeight = 1.4.em, letterSpacing = 0.sp, color = c.ink)
    CompositionLocalProvider(LocalAppColors provides c) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography(
                bodyLarge = base,
                bodyMedium = base.copy(fontSize = 14.sp),
                bodySmall = base.copy(fontSize = 12.sp),
            ),
            content = content,
        )
    }
}
