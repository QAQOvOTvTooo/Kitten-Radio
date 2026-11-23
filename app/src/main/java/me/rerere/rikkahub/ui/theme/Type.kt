/*
 * 橘瓣 OrangeChat
 * 衍生自 RikkaHub (https://github.com/rikkahub/rikkahub)，原作者 RE
 * 本项目基于 GNU AGPL v3 开源，详见根目录 LICENSE 文件
 */

package me.rerere.rikkahub.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.datastore.ChatFontFamily
import java.io.File

// Set of Material typography styles to start with
val Typography = Typography()

@OptIn(ExperimentalTextApi::class)
val JetbrainsMono = FontFamily(
    Font(
        resId = R.font.jetbrains_mono,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
        )
    )
)

@OptIn(ExperimentalTextApi::class)
fun buildCustomFileFontFamily(fontFile: File, weightVal: Int = 400): FontFamily {
    val safeWeight = weightVal.coerceIn(100, 900)
    return runCatching {
        FontFamily(
            Font(
                file = fontFile,
                weight = FontWeight.Normal,
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(safeWeight),
                ),
            )
        )
    }.getOrDefault(FontFamily.Default)
}

fun resolveChatFontFamily(
    chatFontFamily: ChatFontFamily,
    customFontPath: String,
    weightVal: Int = 400,
): FontFamily {
    return when (chatFontFamily) {
        ChatFontFamily.DEFAULT -> FontFamily.Default
        ChatFontFamily.SERIF -> FontFamily.Serif
        ChatFontFamily.MONOSPACE -> FontFamily.Monospace
        ChatFontFamily.CUSTOM -> {
            if (customFontPath.isNotBlank()) {
                val file = File(customFontPath)
                if (file.exists()) {
                    buildCustomFileFontFamily(file, weightVal)
                } else {
                    FontFamily.Default
                }
            } else {
                FontFamily.Default
            }
        }
    }
}
