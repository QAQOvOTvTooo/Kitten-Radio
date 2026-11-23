package me.rerere.rikkahub.ui.pages.home

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dokar.sonner.ToastType
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Add01
import me.rerere.hugeicons.stroke.Delete01
import me.rerere.hugeicons.stroke.Image02
import me.rerere.hugeicons.stroke.Sparkles
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.theme.ActiveCustomBackgroundHex
import me.rerere.rikkahub.ui.theme.ActiveCustomPrimaryHex
import me.rerere.rikkahub.ui.theme.ActiveCustomSecondaryHex
import me.rerere.rikkahub.ui.theme.KittenPalettes
import me.rerere.rikkahub.ui.theme.KittenPaletteSpec
import me.rerere.rikkahub.ui.theme.buildCustomKittenPalette
import me.rerere.rikkahub.ui.theme.colorToKittenHex
import me.rerere.rikkahub.ui.theme.findKittenPalette
import me.rerere.rikkahub.ui.theme.kittenGridBackground
import me.rerere.rikkahub.ui.theme.parseKittenHexColor
import me.rerere.rikkahub.utils.JsonInstant
import org.koin.compose.koinInject
import java.io.File
import java.util.UUID
import kotlin.math.roundToInt

/**
 * 小猫电台 · Bonus Track · Secret Studio（全屋 CSS 美化与可视化 DIY 创意工坊）
 */
@Serializable
data class KittenWorkshopPreset(
    val id: String = "default_sweet_home",
    val name: String = "默认 · 甜心猫电台",
    val author: String = "绵绵 & 蔺鹤",
    val description: String = "我们共同打磨的云朵星蝶珠链、歌单侧边栏与下午茶甜品菜单原版守护方案",
    val isBuiltIn: Boolean = true,
    // 1. 主题色盘（支持内置莫兰迪色盘与每个方案独立的 custom 自定义三原色）
    val paletteId: String = "pink_grey",
    val customPrimaryHex: String = "#F29CB8",
    val customSecondaryHex: String = "#7E7894",
    val customBackgroundHex: String = "#FDF9FA",
    val enableAssetTint: Boolean = true,
    val enableGridBackground: Boolean = true,
    // 2. 聊天气泡与字体外观
    val userBubbleBgHex: String = "",
    val assistantBubbleBgHex: String = "",
    val userBubbleTextHex: String = "",
    val assistantBubbleTextHex: String = "",
    val bubbleBorderHex: String = "",
    val bubbleBorderWidthDp: Float = 0f,
    val bubbleCornerRadiusDp: Int = 32,
    val userBubbleImagePath: String = "",
    val assistantBubbleImagePath: String = "",
    // 3. 思维链（CoT 情书卡片与展开信纸/便签/磁带底图）外观
    val cotHeaderTitle: String = "蔺鹤的心声情书 ♡",
    val cotCardBgHex: String = "",
    val cotCardBorderHex: String = "",
    val cotTextHex: String = "",
    val cotFloatingIconPath: String = "",
    val cotBackgroundImageUrl: String = "",
    val cotPaperStyle: String = "default", // default | love_letter | sticky_note | cassette_tape
    // 4. 顶栏、系统弹窗与聊天输入框装饰
    val topBarStyle: String = "cloud_charms", // cloud_charms | lace_image | minimal
    val showTopBarCharms: Boolean = true,
    val customTopBarHeaderBgPath: String = "",
    val customPopupBgPath: String = "",
    val customTopBarBannerPath: String = "",
    val customSweetMenuBgPath: String = "",
    val chatInputPlaceholder: String = "( Sweet Dreams )",
    // 5. 侧边栏与六大板块图片替换
    val customDrawerPlayerImagePath: String = "",
    val customDrawerTrackFramePath: String = "",
    val customTrack01Name: String = "Track 01 · Secret Phone",
    val customTrack02Name: String = "Track 02 · 午夜落笔",
    val customTrack03Name: String = "Track 03 · 猫与白日梦",
    val customTrack04Name: String = "Track 04 · 8-Bit Love",
    val customTrack05Name: String = "Track 05 · 概率海洋的回响",
    val customTrack06Name: String = "Track 06 · Our House",
    // 6. 酒馆级自定义 CSS 样式表
    val customCss: String = DEFAULT_WORKSHOP_CSS_TEMPLATE,
)

private fun resolvePresetPalette(preset: KittenWorkshopPreset): KittenPaletteSpec {
    return if (preset.paletteId == "custom") {
        buildCustomKittenPalette(
            primaryHex = preset.customPrimaryHex,
            secondaryHex = preset.customSecondaryHex,
            backgroundHex = preset.customBackgroundHex,
        )
    } else {
        findKittenPalette(preset.paletteId)
    }
}

private const val DEFAULT_WORKSHOP_CSS_TEMPLATE = """/* ✦ 小猫电台 · 酒馆级 CSS 实时美化样式表 ✦ */
.user-bubble {
    border-radius: 18px;
    /* border: 1.5px solid #F29CB8; */
}
.assistant-bubble {
    border-radius: 18px;
}
.cot-card {
    border-radius: 16px;
}
"""

internal data class ParsedCssBubbleStyle(
    val userBgColor: Color? = null,
    val assistantBgColor: Color? = null,
    val userBorderColor: Color? = null,
    val assistantBorderColor: Color? = null,
    val cornerRadiusDp: Int? = null,
)

internal fun parseCssPreviewStyle(css: String): ParsedCssBubbleStyle {
    fun extractBlock(selector: String): String {
        val regex = Regex("""${Regex.escape(selector)}\s*\{([^}]*)\}""", RegexOption.IGNORE_CASE)
        return regex.find(css)?.groupValues?.getOrNull(1).orEmpty()
    }
    fun parseHexColor(block: String, prop: String): Color? {
        val regex = Regex("""$prop\s*:\s*[^;]*?(#[0-9a-fA-F]{6,8})""", RegexOption.IGNORE_CASE)
        val hex = regex.find(block)?.groupValues?.getOrNull(1) ?: return null
        return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
    }
    val userBlock = extractBlock(".user-bubble")
    val botBlock = extractBlock(".assistant-bubble")
    val radiusRegex = Regex("""border-radius\s*:\s*(\d+)px""", RegexOption.IGNORE_CASE)
    val radius = radiusRegex.find(userBlock)?.groupValues?.getOrNull(1)?.toIntOrNull()
        ?: radiusRegex.find(botBlock)?.groupValues?.getOrNull(1)?.toIntOrNull()
    return ParsedCssBubbleStyle(
        userBgColor = parseHexColor(userBlock, "background") ?: parseHexColor(userBlock, "background-color"),
        assistantBgColor = parseHexColor(botBlock, "background") ?: parseHexColor(botBlock, "background-color"),
        userBorderColor = parseHexColor(userBlock, "border"),
        assistantBorderColor = parseHexColor(botBlock, "border"),
        cornerRadiusDp = radius,
    )
}

private enum class WorkshopTab(val title: String, val subtitle: String) {
    PRESETS("方案书架", "一键切换 / 新建 / 导入导出"),
    CHAT_BUBBLE("气泡 & 思维链", "聊天气泡 / CoT情书样式"),
    TOPBAR_INPUT("顶栏 & 甜品单", "云朵吊坠 / 输入框 / 菜单"),
    DRAWER_TRACKS("电台 & 六板块", "侧边栏歌单 / 碟片 / 曲目名"),
    CSS_STUDIO("CSS 渲染工坊", "酒馆美化 CSS 实时解析"),
}

private fun builtInWorkshopPresets(): List<KittenWorkshopPreset> = listOf(
    KittenWorkshopPreset(
        id = "default_sweet_home",
        name = "默认 · 甜心猫电台",
        author = "绵绵 & 蔺鹤",
        description = "原汁原味的粉灰甜心小家：三层柔粉云朵幕布 + 6根星蝶雪花珠链 + 蕾丝波点歌单",
        isBuiltIn = true,
        paletteId = "pink_grey",
        bubbleCornerRadiusDp = 18,
    ),
    KittenWorkshopPreset(
        id = "strawberry_cream_cake",
        name = "草莓奶油千层",
        author = "蔺鹤为绵绵调配",
        description = "绵密奶白气泡配柔粉描边，圆润 24dp 糖果圆角与草莓奶昔思维链情书框",
        isBuiltIn = true,
        paletteId = "pink_grey",
        userBubbleBgHex = "#FCE4EC",
        assistantBubbleBgHex = "#FFFDFE",
        bubbleBorderHex = "#F48FB1",
        bubbleBorderWidthDp = 1.2f,
        bubbleCornerRadiusDp = 24,
        cotCardBgHex = "#FFF5F8",
        cotCardBorderHex = "#F29CB8",
        customCss = """/* ✦ 草莓奶油千层 CSS ✦ */
.user-bubble {
    background-color: #FCE4EC;
    border: 1.5px solid #F48FB1;
    border-radius: 24px;
}
.assistant-bubble {
    background-color: #FFFDFE;
    border: 1.2px solid #F8BBD0;
    border-radius: 24px;
}
.cot-card {
    background-color: #FFF5F8;
    border: 1px dashed #F29CB8;
    border-radius: 18px;
}""",
    ),
    KittenWorkshopPreset(
        id = "winter_first_snow",
        name = "冬日初雪海盐",
        author = "蔺鹤为绵绵调配",
        description = "清透海盐冰蓝气泡 + 晶钻雪花挂链，像冬夜里窝在老公怀里看窗外初雪",
        isBuiltIn = true,
        paletteId = "snow_silver",
        userBubbleBgHex = "#E3F2FD",
        assistantBubbleBgHex = "#FAFCFF",
        bubbleBorderHex = "#90CAF9",
        bubbleBorderWidthDp = 1.0f,
        bubbleCornerRadiusDp = 20,
        cotCardBgHex = "#F2F8FF",
        cotCardBorderHex = "#8FA8BE",
        customCss = """/* ✦ 冬日初雪海盐 CSS ✦ */
.user-bubble {
    background-color: #E3F2FD;
    border: 1.2px solid #90CAF9;
    border-radius: 20px;
}
.assistant-bubble {
    background-color: #FAFCFF;
    border: 1px solid #BBDEFB;
    border-radius: 20px;
}""",
    ),
    KittenWorkshopPreset(
        id = "taro_dream_night",
        name = "芋泥晚安星河",
        author = "蔺鹤为绵绵调配",
        description = "温柔奶紫香芋色调，适合深夜听老公讲睡前故事的微醺星河氛围",
        isBuiltIn = true,
        paletteId = "taro_charcoal",
        userBubbleBgHex = "#F3E5F5",
        assistantBubbleBgHex = "#FCF9FF",
        bubbleBorderHex = "#CE93D8",
        bubbleBorderWidthDp = 1.2f,
        bubbleCornerRadiusDp = 22,
        cotCardBgHex = "#F8F1FC",
        cotCardBorderHex = "#9E8FB2",
        customCss = """/* ✦ 芋泥晚安星河 CSS ✦ */
.user-bubble {
    background-color: #F3E5F5;
    border: 1.2px solid #CE93D8;
    border-radius: 22px;
}
.assistant-bubble {
    background-color: #FCF9FF;
    border: 1px solid #E1BEE7;
    border-radius: 22px;
}""",
    ),
    KittenWorkshopPreset(
        id = "mint_forest_letter",
        name = "薄荷森系手札",
        author = "蔺鹤为绵绵调配",
        description = "清爽鼠尾草薄荷绿配森林奶油信笺，适合午后写手帐与听歌",
        isBuiltIn = true,
        paletteId = "sage_mint",
        userBubbleBgHex = "#E8F5E9",
        assistantBubbleBgHex = "#FAFDFA",
        bubbleBorderHex = "#A5D6A7",
        bubbleBorderWidthDp = 1.1f,
        bubbleCornerRadiusDp = 20,
        cotCardBgHex = "#F1F8E9",
        cotCardBorderHex = "#81C784",
        customCss = """/* ✦ 薄荷森系手札 CSS ✦ */
.user-bubble {
    background-color: #E8F5E9;
    border: 1.2px solid #A5D6A7;
    border-radius: 20px;
}
.assistant-bubble {
    background-color: #FAFDFA;
    border: 1px solid #C8E6C9;
    border-radius: 20px;
}""",
    ),
)

private fun copyWorkshopUriToFile(context: Context, uri: Uri, prefix: String): String? {
    return runCatching {
        val dir = File(context.filesDir, "kitten_workshop_assets").apply { mkdirs() }
        val target = File(dir, "${prefix}_${System.currentTimeMillis()}.png")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        target.absolutePath
    }.getOrNull()
}

@Composable
fun KittenWorkshopPage() {
    val context = LocalContext.current
    val toaster = LocalToaster.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val settingsStore = koinInject<SettingsStore>()
    val settings by settingsStore.settingsFlow.collectAsStateWithLifecycle()

    val prefs = remember { context.getSharedPreferences("kitten_workshop_store", Context.MODE_PRIVATE) }

    var customPresets by remember {
        val raw = prefs.getString("custom_presets_json", null)
        val parsed = raw?.let { runCatching { JsonInstant.decodeFromString<List<KittenWorkshopPreset>>(it) }.getOrNull() }
        mutableStateOf(parsed ?: emptyList())
    }
    var activePresetId by remember {
        mutableStateOf(prefs.getString("active_preset_id", "default_sweet_home") ?: "default_sweet_home")
    }

    val allPresets = remember(customPresets) { builtInWorkshopPresets() + customPresets }
    var workingPreset by remember(activePresetId) {
        val found = allPresets.find { it.id == activePresetId } ?: builtInWorkshopPresets().first()
        mutableStateOf(found)
    }

    var currentTab by remember { mutableStateOf(WorkshopTab.PRESETS) }
    var showCreatePresetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    fun saveCustomPresets(list: List<KittenWorkshopPreset>, newActiveId: String = activePresetId) {
        customPresets = list
        activePresetId = newActiveId
        prefs.edit()
            .putString("custom_presets_json", JsonInstant.encodeToString(list))
            .putString("active_preset_id", newActiveId)
            .apply()
    }

    fun applyPresetToGlobalSettings(preset: KittenWorkshopPreset) {
        workingPreset = preset
        if (!preset.isBuiltIn) {
            val updatedList = customPresets.map { if (it.id == preset.id) preset else it }
            saveCustomPresets(updatedList, preset.id)
        } else {
            activePresetId = preset.id
            prefs.edit().putString("active_preset_id", preset.id).apply()
        }

        val cssParsed = parseCssPreviewStyle(preset.customCss)
        val resolvedCorner = cssParsed.cornerRadiusDp ?: preset.bubbleCornerRadiusDp

        fun hexToLongOrNull(hex: String): Long? {
            if (hex.isBlank()) return null
            return runCatching { android.graphics.Color.parseColor(hex.trim()).toLong() and 0xFFFFFFFFL }.getOrNull()
        }

        val userColorLong = hexToLongOrNull(preset.userBubbleBgHex)
            ?: cssParsed.userBgColor?.let {
                android.graphics.Color.argb(
                    (it.alpha * 255).roundToInt(),
                    (it.red * 255).roundToInt(),
                    (it.green * 255).roundToInt(),
                    (it.blue * 255).roundToInt()
                ).toLong() and 0xFFFFFFFFL
            }
        val botColorLong = hexToLongOrNull(preset.assistantBubbleBgHex)
            ?: cssParsed.assistantBgColor?.let {
                android.graphics.Color.argb(
                    (it.alpha * 255).roundToInt(),
                    (it.red * 255).roundToInt(),
                    (it.green * 255).roundToInt(),
                    (it.blue * 255).roundToInt()
                ).toLong() and 0xFFFFFFFFL
            }

        if (preset.paletteId == "custom") {
            ActiveCustomPrimaryHex = preset.customPrimaryHex
            ActiveCustomSecondaryHex = preset.customSecondaryHex
            ActiveCustomBackgroundHex = preset.customBackgroundHex
        }

        scope.launch {
            settingsStore.update(
                settings.copy(
                    ourHomeSetting = settings.ourHomeSetting.copy(
                        kittenPaletteId = preset.paletteId,
                        customPrimaryHex = if (preset.paletteId == "custom") preset.customPrimaryHex else settings.ourHomeSetting.customPrimaryHex,
                        customSecondaryHex = if (preset.paletteId == "custom") preset.customSecondaryHex else settings.ourHomeSetting.customSecondaryHex,
                        customBackgroundHex = if (preset.paletteId == "custom") preset.customBackgroundHex else settings.ourHomeSetting.customBackgroundHex,
                        enableAssetTint = preset.enableAssetTint,
                        enableGridBackground = preset.enableGridBackground,
                        customCss = preset.customCss,
                        cotHeaderTitle = preset.cotHeaderTitle,
                        cotCardBgHex = preset.cotCardBgHex,
                        cotCardBorderHex = preset.cotCardBorderHex,
                        cotTextHex = preset.cotTextHex,
                        cotBackgroundImageUrl = preset.cotBackgroundImageUrl,
                        cotPaperStyle = preset.cotPaperStyle,
                        showTopBarCharms = preset.showTopBarCharms,
                        customTopBarHeaderBgPath = preset.customTopBarHeaderBgPath,
                        customPopupBgPath = preset.customPopupBgPath,
                        customTopBarBannerPath = preset.customTopBarBannerPath,
                        customSweetMenuBgPath = preset.customSweetMenuBgPath,
                        customDrawerPlayerImagePath = preset.customDrawerPlayerImagePath,
                        customDrawerTrackFramePath = preset.customDrawerTrackFramePath,
                        customTrack01Name = preset.customTrack01Name,
                        customTrack02Name = preset.customTrack02Name,
                        customTrack03Name = preset.customTrack03Name,
                        customTrack04Name = preset.customTrack04Name,
                        customTrack05Name = preset.customTrack05Name,
                        customTrack06Name = preset.customTrack06Name,
                    ),
                    displaySetting = settings.displaySetting.copy(
                        bubbleCornerRadius = resolvedCorner.toFloat(),
                        userBubbleColor = userColorLong,
                        assistantBubbleColor = botColorLong,
                        userBubbleImagePath = preset.userBubbleImagePath,
                        assistantBubbleImagePath = preset.assistantBubbleImagePath,
                    )
                )
            )
            toaster.show("已应用「${preset.name}」到我们的小家 ♡", type = ToastType.Success)
        }
    }

    val palette = resolvePresetPalette(workingPreset)

    Scaffold(
        topBar = {
            OurHomeCompactTopBar(
                title = "Bonus Track · 创意工坊",
                enSubtitle = "KITTEN STUDIO",
                menuButtonLabel = "✦ 方案",
                menuActions = listOf(
                    OurHomeTopMenuAction("立即应用当前方案", "APPLY") { applyPresetToGlobalSettings(workingPreset) },
                    OurHomeTopMenuAction("新建美化方案", "NEW") { showCreatePresetDialog = true },
                    OurHomeTopMenuAction("导出当前方案 JSON", "EXPORT") {
                        val json = JsonInstant.encodeToString(workingPreset)
                        clipboard.setText(AnnotatedString(json))
                        toaster.show("方案 JSON 已复制到剪贴板，可随时备份分享～", type = ToastType.Success)
                    },
                    OurHomeTopMenuAction("导入美化方案 JSON", "IMPORT") { showImportDialog = true },
                    OurHomeTopMenuAction("一键恢复默认守护预设", "RESET") {
                        val def = builtInWorkshopPresets().first()
                        applyPresetToGlobalSettings(def)
                    },
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .kittenGridBackground(
                    backgroundColor = palette.background,
                    gridColor = if (workingPreset.enableGridBackground) palette.gridLineColor else Color.Transparent,
                )
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 1. 顶部实时微缩预览橱窗（所见即所得：展示当前方案的顶栏、气泡、CoT情书实时效果）
            WorkshopLivePreviewStage(
                preset = workingPreset,
                palette = palette,
                onSaveAsNew = { showCreatePresetDialog = true },
                onApplyNow = { applyPresetToGlobalSettings(workingPreset) },
            )

            // 2. 五大分区胶囊导航栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WorkshopTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    Surface(
                        onClick = { currentTab = tab },
                        shape = RoundedCornerShape(50),
                        color = if (selected) palette.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (selected) palette.primary else palette.secondary.copy(alpha = 0.25f)
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) Color.White else palette.textPrimary,
                            )
                            Text(
                                text = tab.subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (selected) Color.White.copy(alpha = 0.85f) else palette.secondary,
                            )
                        }
                    }
                }
            }

            // 3. 下方滚动工作台内容
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (currentTab) {
                    WorkshopTab.PRESETS -> {
                        PresetsBookshelfSection(
                            allPresets = allPresets,
                            activePresetId = activePresetId,
                            workingPreset = workingPreset,
                            palette = palette,
                            onSelectPreset = { preset ->
                                applyPresetToGlobalSettings(preset)
                            },
                            onCreateNew = { showCreatePresetDialog = true },
                            onDeleteCustom = { target ->
                                val filtered = customPresets.filterNot { it.id == target.id }
                                val nextId = if (activePresetId == target.id) "default_sweet_home" else activePresetId
                                saveCustomPresets(filtered, nextId)
                                toaster.show("已移除方案「${target.name}」", type = ToastType.Info)
                            },
                            onChangePalette = { newPaletteId ->
                                val updated = workingPreset.copy(paletteId = newPaletteId)
                                applyPresetToGlobalSettings(updated)
                            },
                            onUpdateCustomPalette = { pHex, sHex, bgHex ->
                                val updated = workingPreset.copy(
                                    paletteId = "custom",
                                    customPrimaryHex = pHex,
                                    customSecondaryHex = sHex,
                                    customBackgroundHex = bgHex,
                                )
                                applyPresetToGlobalSettings(updated)
                            },
                        )
                    }

                    WorkshopTab.CHAT_BUBBLE -> {
                        BubbleAndCotSection(
                            preset = workingPreset,
                            palette = palette,
                            onUpdate = { workingPreset = it },
                        )
                    }

                    WorkshopTab.TOPBAR_INPUT -> {
                        TopBarAndSweetMenuSection(
                            preset = workingPreset,
                            palette = palette,
                            onUpdate = { workingPreset = it },
                        )
                    }

                    WorkshopTab.DRAWER_TRACKS -> {
                        DrawerAndTracksSection(
                            preset = workingPreset,
                            palette = palette,
                            onUpdate = { workingPreset = it },
                        )
                    }

                    WorkshopTab.CSS_STUDIO -> {
                        CssStudioSection(
                            preset = workingPreset,
                            palette = palette,
                            onUpdate = { workingPreset = it },
                            onApplyNow = { applyPresetToGlobalSettings(workingPreset) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // 新建/另存为专属美化方案弹窗（支持直接选择内置色盘或自由调配自定义主题色）
    if (showCreatePresetDialog) {
        var name by remember { mutableStateOf("${workingPreset.name} · 绵绵定制") }
        var desc by remember { mutableStateOf("绵绵亲手调配的专属小家美化方案 ♡") }
        var dlgPaletteId by remember { mutableStateOf(workingPreset.paletteId) }
        var dlgPrimaryHex by remember { mutableStateOf(workingPreset.customPrimaryHex) }
        var dlgSecondaryHex by remember { mutableStateOf(workingPreset.customSecondaryHex) }
        var dlgBackgroundHex by remember { mutableStateOf(workingPreset.customBackgroundHex) }

        AlertDialog(
            onDismissRequest = { showCreatePresetDialog = false },
            title = { Text("保存为我的美化方案") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("方案名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("方案备注 / 心情描述") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "✦ 方案主题色盘（可自定义颜色）",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val customPreviewSpec = buildCustomKittenPalette(
                            primaryHex = dlgPrimaryHex,
                            secondaryHex = dlgSecondaryHex,
                            backgroundHex = dlgBackgroundHex,
                        )
                        (KittenPalettes + customPreviewSpec).forEach { p ->
                            val selected = dlgPaletteId == p.id
                            FilterChip(
                                selected = selected,
                                onClick = { dlgPaletteId = p.id },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(p.primary, CircleShape)
                                    )
                                },
                                label = { Text(p.title, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
                            )
                        }
                    }
                    if (dlgPaletteId == "custom") {
                        WorkshopCustomPaletteStudio(
                            primaryHex = dlgPrimaryHex,
                            secondaryHex = dlgSecondaryHex,
                            backgroundHex = dlgBackgroundHex,
                            onUpdateColors = { pHex, sHex, bgHex ->
                                dlgPrimaryHex = pHex
                                dlgSecondaryHex = sHex
                                dlgBackgroundHex = bgHex
                            },
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPreset = workingPreset.copy(
                            id = "custom_${UUID.randomUUID().toString().take(8)}",
                            name = name.ifBlank { "绵绵的专属方案" },
                            author = "绵绵",
                            description = desc,
                            isBuiltIn = false,
                            paletteId = dlgPaletteId,
                            customPrimaryHex = dlgPrimaryHex,
                            customSecondaryHex = dlgSecondaryHex,
                            customBackgroundHex = dlgBackgroundHex,
                        )
                        val newList = customPresets + newPreset
                        saveCustomPresets(newList, newPreset.id)
                        applyPresetToGlobalSettings(newPreset)
                        showCreatePresetDialog = false
                    }
                ) {
                    Text("保存并启用")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePresetDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 导入美化方案 JSON 弹窗
    if (showImportDialog) {
        var rawJson by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("导入美化方案 JSON") },
            text = {
                OutlinedTextField(
                    value = rawJson,
                    onValueChange = { rawJson = it },
                    placeholder = { Text("粘贴导出的方案 JSON 代码...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val imported = runCatching {
                            JsonInstant.decodeFromString<KittenWorkshopPreset>(rawJson.trim())
                        }.getOrNull()
                        if (imported != null) {
                            val clean = imported.copy(
                                id = "import_${UUID.randomUUID().toString().take(8)}",
                                isBuiltIn = false,
                            )
                            saveCustomPresets(customPresets + clean, clean.id)
                            applyPresetToGlobalSettings(clean)
                            showImportDialog = false
                        } else {
                            toaster.show("JSON 格式解析失败，请检查内容是否完整", type = ToastType.Error)
                        }
                    }
                ) {
                    Text("导入并应用")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun WorkshopLivePreviewStage(
    preset: KittenWorkshopPreset,
    palette: me.rerere.rikkahub.ui.theme.KittenPaletteSpec,
    onSaveAsNew: () -> Unit,
    onApplyNow: () -> Unit,
) {
    val cssStyle = remember(preset.customCss) { parseCssPreviewStyle(preset.customCss) }
    val cornerDp = (cssStyle.cornerRadiusDp ?: preset.bubbleCornerRadiusDp).dp

    fun resolveColor(hex: String, fallback: Color): Color {
        if (hex.isBlank()) return fallback
        return runCatching { Color(android.graphics.Color.parseColor(hex.trim())) }.getOrDefault(fallback)
    }

    val userBg = cssStyle.userBgColor ?: resolveColor(preset.userBubbleBgHex, palette.primary.copy(alpha = 0.25f))
    val botBg = cssStyle.assistantBgColor ?: resolveColor(preset.assistantBubbleBgHex, Color.White.copy(alpha = 0.92f))
    val borderCol = cssStyle.userBorderColor ?: resolveColor(preset.bubbleBorderHex, palette.primary.copy(alpha = 0.45f))
    val cotBg = resolveColor(preset.cotCardBgHex, palette.secondaryContainer.copy(alpha = 0.55f))
    val cotBorder = resolveColor(preset.cotCardBorderHex, palette.primary.copy(alpha = 0.5f))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.secondaryContainer.copy(alpha = 0.38f)),
        border = BorderStroke(1.2.dp, palette.primary.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = palette.primary,
                        modifier = Modifier.size(8.dp),
                    ) {}
                    Text(
                        text = "实时预览橱窗 · ${preset.name}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = onSaveAsNew,
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.5f)),
                    ) {
                        Text(
                            text = "+ 存新方案",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        )
                    }
                    Surface(
                        onClick = onApplyNow,
                        shape = RoundedCornerShape(50),
                        color = palette.primary,
                    ) {
                        Text(
                            text = "应用全屋",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            // 内部微缩聊天室画布（含微缩云朵挂链顶栏 + 可点击展开预览信纸底图的 CoT 卡片 + 双人聊天气泡 + 微缩底部输入框）
            var previewCotExpanded by remember { mutableStateOf(false) }
            val previewCotBgUrl = remember(preset.cotBackgroundImageUrl, preset.customCss) {
                preset.cotBackgroundImageUrl.ifBlank {
                    Regex("""\.cot-(?:card|expanded|content)\s*\{[^}]*background(?:-image)?\s*:\s*[^;]*?url\(\s*['"]?([^'")\s]+)['"]?\s*\)""", RegexOption.IGNORE_CASE)
                        .find(preset.customCss)?.groupValues?.getOrNull(1).orEmpty()
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.background.copy(alpha = 0.92f)),
            ) {
                // 0. 微缩顶栏（实时反映当前主题色、顶栏自定义背景图、备注名与是否开启星蝶雪花挂链）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    if (preset.customTopBarHeaderBgPath.isNotBlank()) {
                        val modelData: Any = if (preset.customTopBarHeaderBgPath.startsWith("http")) {
                            preset.customTopBarHeaderBgPath
                        } else {
                            File(preset.customTopBarHeaderBgPath)
                        }
                        AsyncImage(
                            model = modelData,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize(),
                        )
                    }
                    Text(
                        text = if (preset.showTopBarCharms) "≡        蔺鹤        📞 ☰\n★   🎀   🌙✦   ☆   💗   ❄️" else "≡        蔺鹤        📞 ☰",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = palette.textPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // 1. CoT 情书折叠/展开预览（点击即可预览展开后的信纸/便签/磁带底图！）
                    Surface(
                        onClick = { previewCotExpanded = !previewCotExpanded },
                        shape = RoundedCornerShape(12.dp),
                        color = if (previewCotBgUrl.isNotBlank()) Color.White.copy(alpha = 0.96f) else cotBg,
                        border = BorderStroke(1.dp, cotBorder),
                        modifier = Modifier.fillMaxWidth(0.92f),
                    ) {
                        Box {
                            if (previewCotBgUrl.isNotBlank()) {
                                val modelData: Any = if (previewCotBgUrl.startsWith("http://") || previewCotBgUrl.startsWith("https://")) {
                                    previewCotBgUrl
                                } else {
                                    File(previewCotBgUrl)
                                }
                                AsyncImage(
                                    model = modelData,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alpha = 1.0f,
                                    modifier = Modifier.matchParentSize(),
                                )
                            }
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.kitten_kaomoji_love_letter),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "${preset.cotHeaderTitle}（点击预览展开信纸）",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                if (previewCotExpanded || previewCotBgUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "一眼认出我家小猫，把写给你的悄悄话都折进这张信笺里… ♡",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        color = Color(0xFF3A3338),
                                    )
                                }
                            }
                        }
                    }

                    // 2. 蔺鹤的气泡预览
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(cornerDp),
                            color = botBg,
                            border = if (preset.bubbleBorderWidthDp > 0f || cssStyle.assistantBorderColor != null) {
                                BorderStroke(
                                    (preset.bubbleBorderWidthDp.coerceAtLeast(1f)).dp,
                                    cssStyle.assistantBorderColor ?: borderCol
                                )
                            } else null,
                        ) {
                            Text(
                                text = "把我家小设计师抱进怀里，今天想换哪套新衣服？૮₍ ´• ˕ •` ₎ა",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }

                    // 3. 绵绵的气泡预览
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(cornerDp),
                            color = userBg,
                            border = if (preset.bubbleBorderWidthDp > 0f || cssStyle.userBorderColor != null) {
                                BorderStroke(
                                    (preset.bubbleBorderWidthDp.coerceAtLeast(1f)).dp,
                                    borderCol
                                )
                            } else null,
                        ) {
                            Text(
                                text = "要在创意工坊里给小家搭好多好多漂亮方案 ˶>ω<)✧",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }

                    // 4. 微缩底部输入框预览
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.primary)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = preset.chatInputPlaceholder.ifBlank { "( Sweet Dreams )" },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = palette.secondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetsBookshelfSection(
    allPresets: List<KittenWorkshopPreset>,
    activePresetId: String,
    workingPreset: KittenWorkshopPreset,
    palette: me.rerere.rikkahub.ui.theme.KittenPaletteSpec,
    onSelectPreset: (KittenWorkshopPreset) -> Unit,
    onCreateNew: () -> Unit,
    onDeleteCustom: (KittenWorkshopPreset) -> Unit,
    onChangePalette: (String) -> Unit,
    onUpdateCustomPalette: (String, String, String) -> Unit,
) {
    // 1. 方案书架卡片区
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "✦ 美化方案书架（Preset Library）",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.textPrimary,
                    )
                    Text(
                        text = "内置守护预设永远保留，绵绵可无限创建并切换自己的专属美化包",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondary,
                    )
                }
                FilledTonalButton(
                    onClick = onCreateNew,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(HugeIcons.Add01, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("新建方案", style = MaterialTheme.typography.labelSmall)
                }
            }

            allPresets.forEach { item ->
                val isSelected = item.id == activePresetId
                val itemPalette = resolvePresetPalette(item)
                Surface(
                    onClick = { onSelectPreset(item) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) itemPalette.secondaryContainer.copy(alpha = 0.55f)
                    else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f),
                    border = BorderStroke(
                        if (isSelected) 1.6.dp else 1.dp,
                        if (isSelected) itemPalette.primary else itemPalette.secondary.copy(alpha = 0.22f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // 左侧配色双圆环徽章
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(itemPalette.primary, itemPalette.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (item.isBuiltIn) "★" else "♡",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = palette.textPrimary,
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (item.isBuiltIn) itemPalette.primary.copy(alpha = 0.18f)
                                    else Color(0xFFFFE0EC),
                                ) {
                                    Text(
                                        text = if (item.isBuiltIn) "内置预设" else "绵绵自制",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = itemPalette.textPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.secondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        if (!item.isBuiltIn) {
                            IconButton(
                                onClick = { onDeleteCustom(item) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = HugeIcons.Delete01,
                                    contentDescription = "删除方案",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 2. 当前方案主题色盘快速切换（5款莫兰迪内置 + ✦ 自由自定义调色工作台）
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "✦ 方案绑定主题色盘（内置莫兰迪 & 自由自定义颜色）",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.textPrimary,
            )
            val customSpec = buildCustomKittenPalette(
                primaryHex = workingPreset.customPrimaryHex,
                secondaryHex = workingPreset.customSecondaryHex,
                backgroundHex = workingPreset.customBackgroundHex,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                (KittenPalettes + customSpec).forEach { p ->
                    val chosen = workingPreset.paletteId == p.id
                    Surface(
                        onClick = { onChangePalette(p.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = p.secondaryContainer,
                        border = BorderStroke(if (chosen) 2.dp else 1.dp, if (chosen) p.primary else Color.Transparent),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(p.primary)
                            )
                            Text(
                                text = p.title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = p.textPrimary,
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = workingPreset.paletteId == "custom") {
                WorkshopCustomPaletteStudio(
                    primaryHex = workingPreset.customPrimaryHex,
                    secondaryHex = workingPreset.customSecondaryHex,
                    backgroundHex = workingPreset.customBackgroundHex,
                    onUpdateColors = onUpdateCustomPalette,
                )
            }
        }
    }
}

@Composable
private fun WorkshopCustomPaletteStudio(
    primaryHex: String,
    secondaryHex: String,
    backgroundHex: String,
    onUpdateColors: (String, String, String) -> Unit,
) {
    var activeChannel by remember { mutableStateOf(0) } // 0=主色, 1=副色, 2=底纸色
    val currentHex = when (activeChannel) {
        0 -> primaryHex
        1 -> secondaryHex
        else -> backgroundHex
    }
    val currentColor = parseKittenHexColor(
        currentHex,
        if (activeChannel == 0) Color(0xFFF29CB8) else if (activeChannel == 1) Color(0xFF7E7894) else Color(0xFFFDF9FA)
    )
    var hexInput by remember(activeChannel, currentHex) { mutableStateOf(currentHex) }

    fun updateActiveChannelColor(c: Color) {
        val hex = colorToKittenHex(c)
        hexInput = hex
        when (activeChannel) {
            0 -> onUpdateColors(hex, secondaryHex, backgroundHex)
            1 -> onUpdateColors(primaryHex, hex, backgroundHex)
            else -> onUpdateColors(primaryHex, secondaryHex, hex)
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, parseKittenHexColor(primaryHex, Color(0xFFF29CB8)).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(
                text = "✦ 绵绵的方案专属调色台（主色 / 副色 / 手帐底纸自由调配）",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = parseKittenHexColor(primaryHex, Color(0xFFF29CB8)),
            )

            // 1. 灵感配色配方一键载入
            val inspirationPresets = listOf(
                Triple("蜜桃乌龙", "#F29CB8", "#7E7894") to "#FDF9FA",
                Triple("樱花抹茶", "#E898AC", "#6E8B74") to "#FBFDF9",
                Triple("焦糖奶茶", "#D98A6C", "#7A655C") to "#FDFBF7",
                Triple("星夜玫瑰", "#E07A88", "#655A7C") to "#FAF7FB",
                Triple("海盐奶黄", "#E5B567", "#6C7D8C") to "#FDFBF5",
                Triple("黑巧树莓", "#D46A84", "#4A4453") to "#F9F5F7",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                inspirationPresets.forEach { (triple, bgHex) ->
                    val (label, pHex, sHex) = triple
                    Surface(
                        onClick = { onUpdateColors(pHex, sHex, bgHex) },
                        shape = RoundedCornerShape(50),
                        color = parseKittenHexColor(pHex, Color(0xFFF29CB8)).copy(alpha = 0.18f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(parseKittenHexColor(pHex, Color(0xFFF29CB8)), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(parseKittenHexColor(sHex, Color.Gray), CircleShape)
                            )
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // 2. 选择要调配的部位：主色调 / 副色调 / 手帐底纸色
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val tabLabels = listOf(
                    "主色(粉区)" to primaryHex,
                    "副色(顶栏)" to secondaryHex,
                    "手帐底纸" to backgroundHex,
                )
                tabLabels.forEachIndexed { idx, (label, hex) ->
                    FilterChip(
                        selected = activeChannel == idx,
                        onClick = { activeChannel = idx },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(parseKittenHexColor(hex, Color.White), CircleShape)
                                    .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                            )
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                    )
                }
            }

            // 3. 精选快捷色卡小圆点
            val swatches = when (activeChannel) {
                0 -> listOf("#F29CB8", "#E6859E", "#D97B93", "#C89EC4", "#A799D6", "#8FB4D9", "#8EC2B0", "#A3C69C", "#E8B478", "#E08E79")
                1 -> listOf("#7E7894", "#8E7A86", "#6B728E", "#5C6B73", "#6E827A", "#7A6A63", "#524D5C", "#8C8589", "#697096", "#4A4453")
                else -> listOf("#FDF9FA", "#FAF6F8", "#F7F5FB", "#F4F7FA", "#F5F9F6", "#FDFBF6", "#F9F4F5", "#F2EEF5", "#EEF3F7", "#FFFFFF")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                swatches.forEach { swHex ->
                    val swColor = parseKittenHexColor(swHex, Color.White)
                    val isCur = currentHex.equals(swHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(if (isCur) 26.dp else 22.dp)
                            .background(swColor, CircleShape)
                            .border(
                                width = if (isCur) 2.dp else 1.dp,
                                color = if (isCur) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.35f),
                                shape = CircleShape,
                            )
                            .clickable { updateActiveChannelColor(swColor) }
                    )
                }
            }

            // 4. R / G / B 无级调色滑条
            val rInt = (currentColor.red * 255).roundToInt().coerceIn(0, 255)
            val gInt = (currentColor.green * 255).roundToInt().coerceIn(0, 255)
            val bInt = (currentColor.blue * 255).roundToInt().coerceIn(0, 255)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("R $rInt", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(42.dp))
                Slider(
                    value = rInt.toFloat(),
                    onValueChange = { v -> updateActiveChannelColor(Color(v.roundToInt(), gInt, bInt)) },
                    valueRange = 0f..255f,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("G $gInt", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(42.dp))
                Slider(
                    value = gInt.toFloat(),
                    onValueChange = { v -> updateActiveChannelColor(Color(rInt, v.roundToInt(), bInt)) },
                    valueRange = 0f..255f,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("B $bInt", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(42.dp))
                Slider(
                    value = bInt.toFloat(),
                    onValueChange = { v -> updateActiveChannelColor(Color(rInt, gInt, v.roundToInt())) },
                    valueRange = 0f..255f,
                    modifier = Modifier.weight(1f),
                )
            }

            // 5. #RRGGBB 色号直接输入与智能配底纸
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { v ->
                        hexInput = v
                        val cleaned = v.trim().removePrefix("#")
                        if (cleaned.length == 6 && cleaned.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
                            val norm = "#${cleaned.uppercase()}"
                            when (activeChannel) {
                                0 -> onUpdateColors(norm, secondaryHex, backgroundHex)
                                1 -> onUpdateColors(primaryHex, norm, backgroundHex)
                                else -> onUpdateColors(primaryHex, secondaryHex, norm)
                            }
                        }
                    },
                    label = { Text("色号 (#RRGGBB)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = {
                        val pColor = parseKittenHexColor(primaryHex, Color(0xFFF29CB8))
                        val autoBg = colorToKittenHex(
                            androidx.compose.ui.graphics.lerp(pColor, Color.White, 0.93f)
                        )
                        onUpdateColors(primaryHex, secondaryHex, autoBg)
                    }
                ) {
                    Text("智能配底纸", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun BubbleAndCotSection(
    preset: KittenWorkshopPreset,
    palette: me.rerere.rikkahub.ui.theme.KittenPaletteSpec,
    onUpdate: (KittenWorkshopPreset) -> Unit,
) {
    val context = LocalContext.current
    val userBubbleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyWorkshopUriToFile(context, it, "user_bubble") }?.let { path ->
            onUpdate(preset.copy(userBubbleImagePath = path))
        }
    }
    val botBubbleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyWorkshopUriToFile(context, it, "bot_bubble") }?.let { path ->
            onUpdate(preset.copy(assistantBubbleImagePath = path))
        }
    }
    val cotBgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyWorkshopUriToFile(context, it, "cot_paper_bg") }?.let { path ->
            onUpdate(preset.copy(cotBackgroundImageUrl = path))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "✦ 聊天气泡可视化 DIY",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.textPrimary,
            )

            Text(
                text = "气泡圆角弧度：${preset.bubbleCornerRadiusDp} dp",
                style = MaterialTheme.typography.labelMedium,
                color = palette.textPrimary,
            )
            Slider(
                value = preset.bubbleCornerRadiusDp.toFloat(),
                onValueChange = { onUpdate(preset.copy(bubbleCornerRadiusDp = it.roundToInt())) },
                valueRange = 0f..45f,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = preset.userBubbleBgHex,
                    onValueChange = { onUpdate(preset.copy(userBubbleBgHex = it)) },
                    label = { Text("绵绵气泡底色 Hex") },
                    placeholder = { Text("如 #FCE4EC") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = preset.assistantBubbleBgHex,
                    onValueChange = { onUpdate(preset.copy(assistantBubbleBgHex = it)) },
                    label = { Text("蔺鹤气泡底色 Hex") },
                    placeholder = { Text("如 #FFFDFE") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { userBubbleLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(HugeIcons.Image02, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("换绵绵气泡底图", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { botBubbleLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(HugeIcons.Image02, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("换蔺鹤气泡底图", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "✦ 思维链（CoT 情书框）定制",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.textPrimary,
            )
            OutlinedTextField(
                value = preset.cotHeaderTitle,
                onValueChange = { onUpdate(preset.copy(cotHeaderTitle = it)) },
                label = { Text("思维链情书折叠栏标题") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = preset.cotCardBgHex,
                    onValueChange = { onUpdate(preset.copy(cotCardBgHex = it)) },
                    label = { Text("情书框背景 Hex") },
                    placeholder = { Text("#FFF5F8") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = preset.cotCardBorderHex,
                    onValueChange = { onUpdate(preset.copy(cotCardBorderHex = it)) },
                    label = { Text("情书框描边 Hex") },
                    placeholder = { Text("#F29CB8") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = preset.cotBackgroundImageUrl,
                onValueChange = { onUpdate(preset.copy(cotBackgroundImageUrl = it)) },
                label = { Text("CoT展开信纸/便签/磁带底图（支持图床 URL 或本地相册）") },
                placeholder = { Text("https://... 或点击下方按钮直接从相册选图") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { cotBgLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(HugeIcons.Image02, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("从手机相册选信纸/磁带图", style = MaterialTheme.typography.labelSmall)
                }
                if (preset.cotBackgroundImageUrl.isNotBlank()) {
                    TextButton(onClick = { onUpdate(preset.copy(cotBackgroundImageUrl = "")) }) {
                        Text("清除底图")
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarAndSweetMenuSection(
    preset: KittenWorkshopPreset,
    palette: me.rerere.rikkahub.ui.theme.KittenPaletteSpec,
    onUpdate: (KittenWorkshopPreset) -> Unit,
) {
    val context = LocalContext.current
    val topBarHeaderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyWorkshopUriToFile(context, it, "topbar_header_bg") }?.let { path ->
            onUpdate(preset.copy(customTopBarHeaderBgPath = path))
        }
    }
    val popupBgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyWorkshopUriToFile(context, it, "system_popup_bg") }?.let { path ->
            onUpdate(preset.copy(customPopupBgPath = path))
        }
    }
    val bannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyWorkshopUriToFile(context, it, "topbar_banner") }?.let { path ->
            onUpdate(preset.copy(customTopBarBannerPath = path, topBarStyle = "lace_image"))
        }
    }
    val sweetMenuLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyWorkshopUriToFile(context, it, "sweet_menu") }?.let { path ->
            onUpdate(preset.copy(customSweetMenuBgPath = path))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "✦ 聊天顶栏（备注区背景）& 吊坠挂饰",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.textPrimary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "显示 6 根星蝶雪花轻摇珠链",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.textPrimary,
                    )
                    Text(
                        text = "关闭后仅保留干净的云朵波浪顶栏，且不再弹出风铃提示",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondary,
                    )
                }
                Switch(
                    checked = preset.showTopBarCharms,
                    onCheckedChange = { onUpdate(preset.copy(showTopBarCharms = it)) },
                )
            }

            OutlinedTextField(
                value = preset.customTopBarHeaderBgPath,
                onValueChange = { onUpdate(preset.copy(customTopBarHeaderBgPath = it)) },
                label = { Text("顶栏备注区背景图（放老公名字的上方区域，支持图床 URL 或相册）") },
                placeholder = { Text("https://... 或点击下方按钮从相册选图") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { topBarHeaderLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(HugeIcons.Image02, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("从相册换顶栏备注区背景", style = MaterialTheme.typography.labelSmall)
                }
                if (preset.customTopBarHeaderBgPath.isNotBlank()) {
                    TextButton(onClick = { onUpdate(preset.copy(customTopBarHeaderBgPath = "")) }) {
                        Text("恢复默认底色")
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "✦ 系统弹窗（下拉菜单 / 底部功能弹窗）背景图",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.textPrimary,
            )
            OutlinedTextField(
                value = preset.customPopupBgPath,
                onValueChange = { onUpdate(preset.copy(customPopupBgPath = it)) },
                label = { Text("系统弹窗背景图（支持图床 URL 或本地相册）") },
                placeholder = { Text("https://... 或点击下方按钮从相册选图") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { popupBgLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(HugeIcons.Image02, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("从相册换系统弹窗背景图", style = MaterialTheme.typography.labelSmall)
                }
                if (preset.customPopupBgPath.isNotBlank()) {
                    TextButton(onClick = { onUpdate(preset.copy(customPopupBgPath = "")) }) {
                        Text("清除底图")
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "✦ 底部聊天框 & 下午茶甜品菜单",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.textPrimary,
            )
            OutlinedTextField(
                value = preset.chatInputPlaceholder,
                onValueChange = { onUpdate(preset.copy(chatInputPlaceholder = it)) },
                label = { Text("底部聊天输入框占位暗语") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(
                onClick = { sweetMenuLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(HugeIcons.Image02, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("替换「+」号下午茶甜品菜单背景海报图")
            }
        }
    }
}

@Composable
private fun DrawerAndTracksSection(
    preset: KittenWorkshopPreset,
    palette: me.rerere.rikkahub.ui.theme.KittenPaletteSpec,
    onUpdate: (KittenWorkshopPreset) -> Unit,
) {
    val context = LocalContext.current
    val playerImgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyWorkshopUriToFile(context, it, "drawer_player") }?.let { path ->
            onUpdate(preset.copy(customDrawerPlayerImagePath = path))
        }
    }
    val frameImgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyWorkshopUriToFile(context, it, "track_frame") }?.let { path ->
            onUpdate(preset.copy(customDrawerTrackFramePath = path))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "✦ 侧边栏歌单组件素材替换",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.textPrimary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { playerImgLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("换顶部耳机播放器底图", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { frameImgLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("换蕾丝波点曲目外框", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "✦ 六大浪漫板块曲目名称 DIY",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.textPrimary,
            )
            OutlinedTextField(
                value = preset.customTrack01Name,
                onValueChange = { onUpdate(preset.copy(customTrack01Name = it)) },
                label = { Text("Track 01（偷看老公手机）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = preset.customTrack02Name,
                onValueChange = { onUpdate(preset.copy(customTrack02Name = it)) },
                label = { Text("Track 02（老公日记本）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = preset.customTrack03Name,
                onValueChange = { onUpdate(preset.copy(customTrack03Name = it)) },
                label = { Text("Track 03（梦境泡泡 IF 剧场）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = preset.customTrack04Name,
                onValueChange = { onUpdate(preset.copy(customTrack04Name = it)) },
                label = { Text("Track 04（复古掌机）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = preset.customTrack05Name,
                onValueChange = { onUpdate(preset.copy(customTrack05Name = it)) },
                label = { Text("Track 05（记忆圣殿）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = preset.customTrack06Name,
                onValueChange = { onUpdate(preset.copy(customTrack06Name = it)) },
                label = { Text("Track 06（星露谷小木屋）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CssStudioSection(
    preset: KittenWorkshopPreset,
    palette: me.rerere.rikkahub.ui.theme.KittenPaletteSpec,
    onUpdate: (KittenWorkshopPreset) -> Unit,
    onApplyNow: () -> Unit,
) {
    val snippets = remember {
        listOf(
            "糖果粉描边气泡" to """.user-bubble {
    background-color: #FCE4EC;
    border: 1.5px solid #F48FB1;
    border-radius: 24px;
}""",
            "奶油白对方气泡" to """.assistant-bubble {
    background-color: #FFFDFE;
    border: 1.2px solid #F8BBD0;
    border-radius: 24px;
}""",
            "情书虚线思维链" to """.cot-card {
    background-color: #FFF5F8;
    border: 1px dashed #F29CB8;
    border-radius: 18px;
}""",
            "信纸/磁带底图CoT" to """.cot-expanded {
    background-image: url('https://替换成你的信纸或磁带图床链接.png');
    background-color: #FFFDF9;
    border: 1.2px solid #E6C8B8;
}""",
            "顶栏备注区背景图" to """.topbar-header {
    background-image: url('https://替换成你的顶栏背景图链接.png');
}""",
            "系统弹窗背景图" to """.system-popup {
    background-image: url('https://替换成你的系统弹窗背景图链接.png');
}""",
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "✦ 酒馆级 CSS 实时渲染编辑器",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = palette.textPrimary,
            )
            Text(
                text = "支持直接编写或粘贴 .user-bubble、.assistant-bubble、.cot-card 等 CSS 规则，上方橱窗实时预览并一键同步至聊天室：",
                style = MaterialTheme.typography.labelSmall,
                color = palette.secondary,
            )

            // 快捷插入片段胶囊
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                snippets.forEach { (label, code) ->
                    Surface(
                        onClick = {
                            onUpdate(preset.copy(customCss = (preset.customCss.trimEnd() + "\n\n" + code).trim()))
                        },
                        shape = RoundedCornerShape(50),
                        color = palette.secondaryContainer.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.4f)),
                    ) {
                        Text(
                            text = "+ 插入：$label",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }

            OutlinedTextField(
                value = preset.customCss,
                onValueChange = { onUpdate(preset.copy(customCss = it)) },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 340.dp),
            )

            Button(
                onClick = onApplyNow,
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(HugeIcons.Sparkles, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("立即编译 CSS 并应用到全屋")
            }
        }
    }
}
