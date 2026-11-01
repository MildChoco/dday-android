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
            moveTo(3.5f, 12f)
            arcTo(8.5f, 8.5f, 0f, true, true, 20.5f, 12f)
            arcTo(8.5f, 8.5f, 0f, true, true, 3.5f, 12f); close()
            moveTo(12f, 7.5f); verticalLineTo(12f); lineTo(15f, 14f)
        }
    }
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
