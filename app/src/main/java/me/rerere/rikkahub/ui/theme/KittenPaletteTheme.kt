package me.rerere.rikkahub.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.datastore.OurHomeSetting
import kotlin.math.roundToInt

// 响应式全局自定义主题色状态（与 OurHomeSetting 实时同步，让任意页面调用 findKittenPalette("custom") 都能即时响应变色）
var ActiveCustomPrimaryHex by mutableStateOf("#F29CB8")
var ActiveCustomSecondaryHex by mutableStateOf("#7E7894")
var ActiveCustomBackgroundHex by mutableStateOf("#FDF9FA")

fun syncCustomKittenPaletteState(home: OurHomeSetting) {
    if (ActiveCustomPrimaryHex != home.customPrimaryHex) {
        ActiveCustomPrimaryHex = home.customPrimaryHex
    }
    if (ActiveCustomSecondaryHex != home.customSecondaryHex) {
        ActiveCustomSecondaryHex = home.customSecondaryHex
    }
    if (ActiveCustomBackgroundHex != home.customBackgroundHex) {
        ActiveCustomBackgroundHex = home.customBackgroundHex
    }
}

fun parseKittenHexColor(hex: String, fallback: Color): Color {
    val cleaned = hex.trim().removePrefix("#")
    if (cleaned.length != 6 && cleaned.length != 8) return fallback
    return runCatching {
        val longVal = cleaned.toLong(16)
        if (cleaned.length == 6) {
            Color(
                red = ((longVal shr 16) and 0xFF).toInt(),
                green = ((longVal shr 8) and 0xFF).toInt(),
                blue = (longVal and 0xFF).toInt(),
                alpha = 0xFF,
            )
        } else {
            Color(
                alpha = ((longVal shr 24) and 0xFF).toInt(),
                red = ((longVal shr 16) and 0xFF).toInt(),
                green = ((longVal shr 8) and 0xFF).toInt(),
                blue = (longVal and 0xFF).toInt(),
            )
        }
    }.getOrDefault(fallback)
}

fun colorToKittenHex(color: Color): String {
    val r = (color.red * 255).roundToInt().coerceIn(0, 255)
    val g = (color.green * 255).roundToInt().coerceIn(0, 255)
    val b = (color.blue * 255).roundToInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
}

fun buildCustomKittenPalette(
    primaryHex: String = ActiveCustomPrimaryHex,
    secondaryHex: String = ActiveCustomSecondaryHex,
    backgroundHex: String = ActiveCustomBackgroundHex,
): KittenPaletteSpec {
    val primary = parseKittenHexColor(primaryHex, Color(0xFFF29CB8))
    val secondary = parseKittenHexColor(secondaryHex, Color(0xFF7E7894))
    val background = parseKittenHexColor(backgroundHex, Color(0xFFFDF9FA))
    val accent = lerp(primary, Color.White, 0.32f)
    val softContainer = lerp(primary, background, 0.80f)
    val secondaryContainer = lerp(secondary, Color.White, 0.78f)
    val gridLineColor = lerp(primary, background, 0.78f)
    val textPrimary = lerp(secondary, Color(0xFF231F26), 0.62f)
    val textSecondary = lerp(secondary, Color(0xFF6B6573), 0.35f)
    return KittenPaletteSpec(
        id = "custom",
        title = "✦ 自定义",
        subtitle = "Custom Dopamine Palette",
        primary = primary,
        secondary = secondary,
        accent = accent,
        background = background,
        cardSurface = Color.White,
        softContainer = softContainer,
        secondaryContainer = secondaryContainer,
        gridLineColor = gridLineColor,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        plushRes = R.drawable.kitten_plush_pink,
        defaultCoverRes = R.drawable.kitten_cover_lovers,
    )
}

fun allKittenPalettes(): List<KittenPaletteSpec> = KittenPalettes + buildCustomKittenPalette()

data class KittenPaletteSpec(
    val id: String,
    val title: String,
    val subtitle: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val cardSurface: Color,
    val softContainer: Color,
    val secondaryContainer: Color,
    val gridLineColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    @DrawableRes val plushRes: Int,
    @DrawableRes val defaultCoverRes: Int,
    val watermarkText: String = "KITTEN MUSIC",
)

val KittenPalettes = listOf(
    KittenPaletteSpec(
        id = "pink_grey",
        title = "草莓奶灰",
        subtitle = "Sweet Strawberry & Soft Grey",
        primary = Color(0xFFF3A6C0),
        secondary = Color(0xFF9E9A9E),
        accent = Color(0xFFF7C6D7),
        background = Color(0xFFFAF8F9),
        cardSurface = Color(0xFFFFFFFF),
        softContainer = Color(0xFFFCEBF1),
        secondaryContainer = Color(0xFFEDEDEF),
        gridLineColor = Color(0xFFEAE4E7),
        textPrimary = Color(0xFF4A4548),
        textSecondary = Color(0xFF8C8589),
        plushRes = R.drawable.kitten_plush_pink,
        defaultCoverRes = R.drawable.kitten_cover_manga,
    ),
    KittenPaletteSpec(
        id = "taro_charcoal",
        title = "芋泥夜航",
        subtitle = "Night Drive Taro & Charcoal",
        primary = Color(0xFFBFA8D9),
        secondary = Color(0xFF524D5C),
        accent = Color(0xFFF2A2C3),
        background = Color(0xFFF8F6FB),
        cardSurface = Color(0xFFFFFFFF),
        softContainer = Color(0xFFEEE8F6),
        secondaryContainer = Color(0xFFE4E0EB),
        gridLineColor = Color(0xFFE5DFEE),
        textPrimary = Color(0xFF3F3A47),
        textSecondary = Color(0xFF7D768A),
        plushRes = R.drawable.kitten_plush_taro,
        defaultCoverRes = R.drawable.kitten_cover_lovers,
    ),
    KittenPaletteSpec(
        id = "snow_silver",
        title = "初雪银灰",
        subtitle = "Pure Snow & Silver Mist",
        primary = Color(0xFFA7B0BA),
        secondary = Color(0xFF7F8790),
        accent = Color(0xFFD5DCE3),
        background = Color(0xFFF6F7F9),
        cardSurface = Color(0xFFFFFFFF),
        softContainer = Color(0xFFECEFF2),
        secondaryContainer = Color(0xFFE3E7EB),
        gridLineColor = Color(0xFFE2E6EA),
        textPrimary = Color(0xFF3E4347),
        textSecondary = Color(0xFF7A8188),
        plushRes = R.drawable.kitten_plush_silver,
        defaultCoverRes = R.drawable.kitten_cover_manga,
    ),
    KittenPaletteSpec(
        id = "sage_mint",
        title = "薄荷鼠尾草",
        subtitle = "Lily Book & Sage Mint",
        primary = Color(0xFF9EBF99),
        secondary = Color(0xFF6E826B),
        accent = Color(0xFFC7DEC3),
        background = Color(0xFFF5F8F4),
        cardSurface = Color(0xFFFFFFFF),
        softContainer = Color(0xFFE8F1E6),
        secondaryContainer = Color(0xFFDFe8DD),
        gridLineColor = Color(0xFFDFE8DD),
        textPrimary = Color(0xFF3E4A3D),
        textSecondary = Color(0xFF748472),
        plushRes = R.drawable.kitten_plush_sage,
        defaultCoverRes = R.drawable.kitten_cover_lily,
    ),
    KittenPaletteSpec(
        id = "iris_twilight",
        title = "暮光鸢尾紫",
        subtitle = "Twilight Iris & Starlight",
        primary = Color(0xFFA3ABD9),
        secondary = Color(0xFF697096),
        accent = Color(0xFFC8CEEE),
        background = Color(0xFFF5F6FB),
        cardSurface = Color(0xFFFFFFFF),
        softContainer = Color(0xFFE9ECFA),
        secondaryContainer = Color(0xFFE0E3F2),
        gridLineColor = Color(0xFFE0E3F2),
        textPrimary = Color(0xFF3D4157),
        textSecondary = Color(0xFF757A96),
        plushRes = R.drawable.kitten_plush_iris,
        defaultCoverRes = R.drawable.kitten_cover_lovers,
    ),
)

fun findKittenPalette(id: String): KittenPaletteSpec {
    if (id == "custom") return buildCustomKittenPalette()
    return KittenPalettes.find { it.id == id } ?: KittenPalettes.first()
}

fun KittenPaletteSpec.toLightColorScheme(): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = Color.White,
    primaryContainer = softContainer,
    onPrimaryContainer = textPrimary,
    secondary = secondary,
    onSecondary = Color.White,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = textPrimary,
    tertiary = accent,
    onTertiary = textPrimary,
    tertiaryContainer = softContainer,
    onTertiaryContainer = textPrimary,
    background = background,
    onBackground = textPrimary,
    surface = cardSurface,
    onSurface = textPrimary,
    surfaceVariant = softContainer,
    onSurfaceVariant = textSecondary,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = background,
    surfaceContainer = softContainer.copy(alpha = 0.65f),
    surfaceContainerHigh = softContainer,
    surfaceContainerHighest = secondaryContainer,
    outline = primary.copy(alpha = 0.55f),
    outlineVariant = gridLineColor,
)

fun KittenPaletteSpec.toDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = Color(0xFF231F20),
    primaryContainer = secondary.copy(alpha = 0.45f),
    onPrimaryContainer = Color(0xFFF5F2F4),
    secondary = accent,
    onSecondary = Color(0xFF231F20),
    secondaryContainer = Color(0xFF353138),
    onSecondaryContainer = Color(0xFFECE8EE),
    background = Color(0xFF1E1C21),
    onBackground = Color(0xFFF4F0F5),
    surface = Color(0xFF28252C),
    onSurface = Color(0xFFF4F0F5),
    surfaceVariant = Color(0xFF36323B),
    onSurfaceVariant = Color(0xFFC8C2CE),
    outline = primary.copy(alpha = 0.6f),
    outlineVariant = Color(0xFF3E3A44),
)

/**
 * 奶油手帐方格底纸背景修饰符（复刻前四张参考图的浅灰/主题色网格手帐底纹）
 */
fun Modifier.kittenGridBackground(
    backgroundColor: Color,
    gridColor: Color,
    cellSize: Dp = 22.dp,
    strokeWidthDp: Dp = 0.75.dp,
): Modifier = this.drawBehind {
    drawRect(color = backgroundColor)
    val step = cellSize.toPx().coerceAtLeast(8f)
    val stroke = strokeWidthDp.toPx().coerceAtLeast(0.5f)
    var x = 0f
    while (x <= size.width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = stroke,
        )
        x += step
    }
    var y = 0f
    while (y <= size.height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = stroke,
        )
        y += step
    }
}

/**
 * 智能将粉灰色系手帐 PNG 素材的粉色通道实时映射为当前主题色（保留灰白中性色不变！）
 * 当原图是粉灰（R > G, R > B）时，利用 R-G 差值将粉色偏移到目标色系。
 */
@Composable
fun rememberKittenAssetColorFilter(
    paletteId: String,
    enabled: Boolean = true,
): ColorFilter? {
    val palette = findKittenPalette(paletteId)
    return remember(paletteId, palette.primary, palette.secondary, enabled) {
        if (!enabled || paletteId == "pink_grey") {
            null
        } else {
            // 针对原始粉灰素材（粉色 R偏高、G偏低），通过混合矩阵将 R-G 的差值注入到目标通道：
            // R' = r + a*(r - g)
            // G' = g + b*(r - g)
            // B' = b + c*(r - g)
            // 注意当 r == g (灰/白/黑) 时，r - g = 0，灰白黑色 100% 不受影响！
            val matrix = when (paletteId) {
                "taro_charcoal" -> floatArrayOf(
                    // 粉 -> 芋泥粉紫（降低一点R，微增G，大幅增加B）
                    0.72f, 0.28f, 0f, 0f, 0f,
                    0.08f, 0.92f, 0f, 0f, 0f,
                    0.42f, -0.42f, 1f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
                "snow_silver" -> floatArrayOf(
                    // 粉 -> 初雪冷银灰（消除R-G色差，微带冰蓝银感）
                    0.35f, 0.65f, 0f, 0f, 0f,
                    0.35f, 0.65f, 0f, 0f, 0f,
                    0.42f, 0.58f, 0f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
                "sage_mint" -> floatArrayOf(
                    // 粉 -> 薄荷鼠尾草绿（将原本高出的 R 转移给 G！）
                    0.25f, 0.75f, 0f, 0f, 0f,
                    0.68f, 0.32f, 0f, 0f, 0f,
                    0.05f, -0.05f, 1f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
                "iris_twilight" -> floatArrayOf(
                    // 粉 -> 暮光鸢尾蓝紫（降低 R，提升 G 与 B）
                    0.38f, 0.62f, 0f, 0f, 0f,
                    0.28f, 0.72f, 0f, 0f, 0f,
                    0.65f, -0.65f, 1f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
                "custom" -> {
                    // 动态将原图草莓粉 (R≈0.90, G≈0.60, B≈0.72, R-G≈0.30) 映射到绵绵自定义的主色调 Primary，
                    // 同时严格保持每行系数和为 1.0f，确保白色 (1,1,1) 与灰黑中性色 100% 纯净不偏色！
                    val a = ((palette.primary.red - 0.90f) / 0.30f).coerceIn(-1.15f, 1.15f)
                    val b = ((palette.primary.green - 0.60f) / 0.30f).coerceIn(-1.15f, 1.15f)
                    val c = ((palette.primary.blue - 0.72f) / 0.30f).coerceIn(-1.15f, 1.15f)
                    floatArrayOf(
                        1f + a, -a,     0f, 0f, 0f,
                        b,      1f - b, 0f, 0f, 0f,
                        c,      -c,     1f, 0f, 0f,
                        0f,     0f,     0f, 1f, 0f
                    )
                }
                else -> null
            }
            matrix?.let { ColorFilter.colorMatrix(ColorMatrix(it)) }
        }
    }
}

/**
 * 智能将雾霾蓝灰系手帐 PNG 素材（如绵绵专属日记本封面与双页内页，B > R）的蓝色通道实时映射为当前 5 色主题
 * 当 r == b (灰/白/黑) 时，(b - r) = 0，白色猫猫、兔兔与纸张底色 100% 不偏色！
 */
@Composable
fun rememberKittenBlueAssetColorFilter(
    paletteId: String,
    enabled: Boolean = true,
): ColorFilter? {
    val palette = findKittenPalette(paletteId)
    return remember(paletteId, palette.primary, palette.secondary, enabled) {
        if (!enabled) {
            null
        } else {
            // 利用 (B - R) 蓝色差值通道：
            // R' = r + a*(b - r) = (1 - a)*r + 0*g + a*b
            // G' = g + b_g*(b - r) = -b_g*r + 1*g + b_g*b
            // B' = b + c*(b - r) = -c*r + 0*g + (1 + c)*b
            val matrix = when (paletteId) {
                "pink_grey" -> floatArrayOf(
                    // 雾蓝 -> 草莓奶粉（把高出的 B 转移给 R，微降 B）
                    -0.35f, 0f, 1.35f, 0f, 0f,
                    0.05f, 1f, -0.05f, 0f, 0f,
                    0.55f, 0f, 0.45f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
                "taro_charcoal" -> floatArrayOf(
                    // 雾蓝 -> 芋泥粉紫（提升 R，保留 B）
                    0.10f, 0f, 0.90f, 0f, 0f,
                    0.12f, 1f, -0.12f, 0f, 0f,
                    -0.05f, 0f, 1.05f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
                "snow_silver" -> floatArrayOf(
                    // 雾蓝 -> 初雪冷银灰（收敛蓝色饱和度为清透银灰）
                    0.45f, 0f, 0.55f, 0f, 0f,
                    -0.35f, 1f, 0.35f, 0f, 0f,
                    0.35f, 0f, 0.65f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
                "sage_mint" -> floatArrayOf(
                    // 雾蓝 -> 薄荷鼠尾草绿（将高出的 B 转移给 G，降低 B）
                    0.65f, 0f, 0.35f, 0f, 0f,
                    -0.85f, 1f, 0.85f, 0f, 0f,
                    0.75f, 0f, 0.25f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
                "iris_twilight" -> floatArrayOf(
                    // 雾蓝 -> 暮光鸢尾蓝紫（微提 R，增强蓝紫氛围）
                    0.45f, 0f, 0.55f, 0f, 0f,
                    0.08f, 1f, -0.08f, 0f, 0f,
                    -0.12f, 0f, 1.12f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
                "custom" -> {
                    val a = ((palette.primary.red - 0.62f) / 0.25f).coerceIn(-1.25f, 1.25f)
                    val b = ((palette.primary.green - 0.68f) / 0.25f).coerceIn(-1.25f, 1.25f)
                    val c = ((palette.primary.blue - 0.87f) / 0.25f).coerceIn(-1.25f, 1.25f)
                    floatArrayOf(
                        1f - a, 0f, a,      0f, 0f,
                        -b,     1f, b,      0f, 0f,
                        -c,     0f, 1f + c, 0f, 0f,
                        0f,     0f, 0f,     1f, 0f
                    )
                }
                else -> null
            }
            matrix?.let { ColorFilter.colorMatrix(ColorMatrix(it)) }
        }
    }
}
