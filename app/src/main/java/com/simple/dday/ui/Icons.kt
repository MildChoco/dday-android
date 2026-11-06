package com.simple.dday.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** 선으로 그린 24dp 아이콘. Icon의 tint로 색을 입힌다 */
object AppIcons {
    val Plus by lazy {
        strokeIcon(2.2f) {
            moveTo(12f, 5f); lineTo(12f, 19f)
            moveTo(5f, 12f); lineTo(19f, 12f)
        }
    }

    val Close by lazy {
        strokeIcon {
            moveTo(6f, 6f); lineTo(18f, 18f)
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
    }

    val Back by lazy {
        strokeIcon {
            moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f)
        }
    }

    val Forward by lazy {
        strokeIcon {
            moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f)
        }
    }

    val Down by lazy {
        strokeIcon {
            moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f)
        }
    }

    val Calendar by lazy {
        strokeIcon(1.8f) {
            moveTo(6.5f, 5f); horizontalLineTo(17.5f)
            arcTo(3f, 3f, 0f, false, true, 20.5f, 8f); verticalLineTo(17f)
            arcTo(3f, 3f, 0f, false, true, 17.5f, 20f); horizontalLineTo(6.5f)
            arcTo(3f, 3f, 0f, false, true, 3.5f, 17f); verticalLineTo(8f)
            arcTo(3f, 3f, 0f, false, true, 6.5f, 5f); close()
            moveTo(3.5f, 10f); horizontalLineTo(20.5f)
            moveTo(8f, 3f); verticalLineTo(7f)
            moveTo(16f, 3f); verticalLineTo(7f)
        }
    }

    val Clock by lazy {
        strokeIcon(1.8f) {
            circle(12f, 12f, 8.5f)
            moveTo(12f, 7.5f); verticalLineTo(12f); lineTo(15f, 14f)
        }
    }

    /** 설정: 손잡이가 달린 조절 막대 세 줄 */
    val Settings by lazy {
        strokeIcon(1.8f) {
            moveTo(4f, 7f); lineTo(13f, 7f)
            circle(15f, 7f, 2f)
            moveTo(17f, 7f); lineTo(20f, 7f)
            moveTo(4f, 12f); lineTo(7f, 12f)
            circle(9f, 12f, 2f)
            moveTo(11f, 12f); lineTo(20f, 12f)
            moveTo(4f, 17f); lineTo(12f, 17f)
            circle(14f, 17f, 2f)
            moveTo(16f, 17f); lineTo(20f, 17f)
        }
    }

    val Pin by lazy {
        strokeIcon(1.8f) {
            moveTo(9f, 4f); lineTo(15f, 4f); lineTo(14f, 10f); lineTo(17f, 14f); lineTo(7f, 14f); lineTo(10f, 10f); close()
            moveTo(12f, 14f); lineTo(12f, 20f)
        }
    }

    val Sort by lazy {
        strokeIcon {
            moveTo(4f, 7f); lineTo(20f, 7f)
            moveTo(7f, 12f); lineTo(17f, 12f)
            moveTo(10f, 17f); lineTo(14f, 17f)
        }
    }

    /** 보관함: 뚜껑 달린 상자 */
    val Archive by lazy {
        strokeIcon(1.8f) {
            moveTo(4f, 9f); lineTo(4f, 18f)
            arcTo(2f, 2f, 0f, false, false, 6f, 20f); lineTo(18f, 20f)
            arcTo(2f, 2f, 0f, false, false, 20f, 18f); lineTo(20f, 9f)
            moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 9f); lineTo(3f, 9f); close()
            moveTo(10f, 13f); lineTo(14f, 13f)
        }
    }
}

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcTo(r, r, 0f, true, true, cx + r, cy)
    arcTo(r, r, 0f, true, true, cx - r, cy)
    close()
}

private fun strokeIcon(width: Float = 2f, block: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathBuilder = block,
            )
        }
        .build()
