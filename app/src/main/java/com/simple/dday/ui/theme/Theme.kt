package com.simple.dday.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.simple.dday.R
import com.simple.dday.data.Pastel

val White = Color(0xFFFFFFFF)
val Ink = Color(0xFF2B2B33)
val Gray = Color(0xFF6B6B78)

/** 파스텔 바탕 위의 보조 글자 */
val SubOnPastel = Color(0xFF5E5E6B)
val LabelGray = Color(0xFF4A4A55)
val Line = Color(0xFFECECF2)
val Divider = Color(0xFFF0F0F5)
val ChipBg = Color(0xFFF3F3F7)
val FieldBg = Color(0xFFFAFAFC)
val FieldBorder = Color(0xFFE6E6EE)
val Track = Color(0xFFD9D9E3)
val Placeholder = Color(0xFF9A9AA6)

/** 숫자 전용 글꼴. 한글은 시스템 글꼴을 쓴다 */
val Nunito = FontFamily(Font(R.font.nunito_extrabold, FontWeight.ExtraBold))

val Pastel.bgColor: Color get() = Color(bg)
val Pastel.inkColor: Color get() = Color(ink)

private val Base = TextStyle(fontSize = 16.sp, lineHeight = 1.4.em, letterSpacing = 0.sp, color = Ink)

@Composable
fun DDayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Ink,
            onPrimary = White,
            secondary = Ink,
            onSecondary = White,
            background = White,
            onBackground = Ink,
            surface = White,
            onSurface = Ink,
            surfaceVariant = ChipBg,
            onSurfaceVariant = Gray,
            outline = FieldBorder,
            outlineVariant = Line,
            surfaceContainerLowest = White,
            surfaceContainerLow = White,
            surfaceContainer = White,
            surfaceContainerHigh = White,
            surfaceContainerHighest = ChipBg,
        ),
        typography = Typography(
            bodyLarge = Base,
            bodyMedium = Base.copy(fontSize = 14.sp),
            bodySmall = Base.copy(fontSize = 12.sp),
        ),
        content = content,
    )
}
