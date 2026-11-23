/*
 * 橘瓣 OrangeChat
 * 衍生自 RikkaHub (https://github.com/rikkahub/rikkahub)，原作者 RE
 * 本项目基于 GNU AGPL v3 开源，详见根目录 LICENSE 文件
 */

package me.rerere.rikkahub.ui.pages.setting

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import me.rerere.rikkahub.ui.theme.LargeFlexibleTopAppBar
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import kotlin.math.roundToInt
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Add01
import me.rerere.hugeicons.stroke.Delete01
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.datastore.ChatFontFamily
import me.rerere.rikkahub.data.datastore.DisplaySetting
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.richtext.MarkdownBlock
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.ui.theme.buildCustomFileFontFamily
import me.rerere.rikkahub.ui.theme.resolveChatFontFamily
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel

private fun resolveUploadedFontFileName(context: Context, uri: Uri): String {
    var rawName: String? = null
    runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) rawName = cursor.getString(idx)
            }
        }
    }
    val fallback = "custom_font_${System.currentTimeMillis()}.ttf"
    val cleaned = rawName?.trim()?.replace(Regex("""[\\/:*?"<>|]"""), "_").orEmpty()
    if (cleaned.isBlank()) return fallback
    val lower = cleaned.lowercase()
    return if (lower.endsWith(".ttf") || lower.endsWith(".otf") || lower.endsWith(".ttc")) {
        cleaned
    } else {
        "$cleaned.ttf"
    }
}

private fun formatFontWeightLabel(weight: Int): String {
    return when {
        weight <= 150 -> "$weight · 极细 (Thin)"
        weight <= 250 -> "$weight · 纤细 (ExtraLight)"
        weight <= 350 -> "$weight · 细体 (Light)"
        weight <= 450 -> "$weight · 标准 (Normal)"
        weight <= 550 -> "$weight · 适中 (Medium)"
        weight <= 650 -> "$weight · 半粗 (SemiBold)"
        weight <= 750 -> "$weight · 加粗 (Bold)"
        weight <= 850 -> "$weight · 特粗 (ExtraBold)"
        else -> "$weight · 浓黑 (Black)"
    }
}

@Composable
fun SettingDisplayMessagePage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var displaySetting by remember(settings) { mutableStateOf(settings.displaySetting) }
    val context = LocalContext.current

    fun updateDisplaySetting(setting: DisplaySetting) {
        displaySetting = setting
        vm.updateSettings(settings.copy(displaySetting = setting))
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val fontDir = remember { File(context.filesDir, "custom_fonts").apply { mkdirs() } }
    var fontRefreshTick by remember { mutableStateOf(0) }
    val savedFonts = remember(fontRefreshTick, displaySetting.customFontPath) {
        val dirFiles = fontDir.listFiles()
            ?.filter { it.isFile && it.length() > 0L }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()
            .toMutableList()
        val currentFile = displaySetting.customFontPath.takeIf { it.isNotBlank() }?.let { File(it) }
        if (currentFile != null && currentFile.exists() && dirFiles.none { it.absolutePath == currentFile.absolutePath }) {
            dirFiles.add(0, currentFile)
        }
        dirFiles
    }

    val fontPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val fileName = resolveUploadedFontFileName(context, it)
            val destFile = File(fontDir, fileName)
            context.contentResolver.openInputStream(it)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            fontRefreshTick++
            updateDisplaySetting(
                displaySetting.copy(
                    chatFontFamily = ChatFontFamily.CUSTOM,
                    customFontPath = destFile.absolutePath
                )
            )
        }
    }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("消息显示") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior,
                colors = CustomColors.topBarColors
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = settingsScaffoldContainerColor(CustomColors.topBarColors.containerColor)
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding + PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 消息显示设置
            item {
                CardGroup(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = { Text(stringResource(R.string.setting_page_message_display_settings)) },
                ) {
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_show_user_avatar_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_show_user_avatar_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.showUserAvatar,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showUserAvatar = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_show_assistant_bubble_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_show_assistant_bubble_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.showAssistantBubble,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showAssistantBubble = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_chat_list_model_icon_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_chat_list_model_icon_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.showModelIcon,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showModelIcon = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_show_model_name_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_show_model_name_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.showModelName,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showModelName = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_show_date_below_name_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_show_date_below_name_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.showDateBelowName,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showDateBelowName = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_show_token_usage_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_show_token_usage_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.showTokenUsage,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showTokenUsage = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_show_thinking_content_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_show_thinking_content_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.showThinkingContent,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showThinkingContent = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_auto_collapse_thinking_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_auto_collapse_thinking_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.autoCloseThinking,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(autoCloseThinking = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_enable_latex_rendering_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_enable_latex_rendering_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.enableLatexRendering,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(enableLatexRendering = it))
                                }
                            )
                        },
                    )
                    val chatFontFamilyOptions = listOf(
                        ChatFontFamily.DEFAULT to stringResource(R.string.setting_display_page_chat_font_family_default),
                        ChatFontFamily.SERIF to stringResource(R.string.setting_display_page_chat_font_family_serif),
                        ChatFontFamily.MONOSPACE to stringResource(R.string.setting_display_page_chat_font_family_monospace),
                        ChatFontFamily.CUSTOM to "自定义",
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_chat_font_family_title)) },
                        supportingContent = {
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .fillMaxWidth()
                            ) {
                                chatFontFamilyOptions.forEachIndexed { index, (family, label) ->
                                    SegmentedButton(
                                        selected = displaySetting.chatFontFamily == family,
                                        onClick = {
                                            if (family == ChatFontFamily.CUSTOM && displaySetting.customFontPath.isBlank() && savedFonts.isNotEmpty()) {
                                                updateDisplaySetting(
                                                    displaySetting.copy(
                                                        chatFontFamily = ChatFontFamily.CUSTOM,
                                                        customFontPath = savedFonts.first().absolutePath
                                                    )
                                                )
                                            } else {
                                                updateDisplaySetting(displaySetting.copy(chatFontFamily = family))
                                            }
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index,
                                            chatFontFamilyOptions.size
                                        ),
                                    ) {
                                        Text(
                                            text = label,
                                            fontFamily = when (family) {
                                                ChatFontFamily.DEFAULT -> FontFamily.Default
                                                ChatFontFamily.SERIF -> FontFamily.Serif
                                                ChatFontFamily.MONOSPACE -> FontFamily.Monospace
                                                ChatFontFamily.CUSTOM -> FontFamily.Default
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_font_size_title)) },
                        supportingContent = {
                            val previewFontFamily = remember(
                                displaySetting.chatFontFamily,
                                displaySetting.customFontPath,
                                displaySetting.chatFontWeight
                            ) {
                                resolveChatFontFamily(
                                    chatFontFamily = displaySetting.chatFontFamily,
                                    customFontPath = displaySetting.customFontPath,
                                    weightVal = displaySetting.chatFontWeight,
                                )
                            }
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Slider(
                                        value = displaySetting.fontSizeRatio,
                                        onValueChange = {
                                            updateDisplaySetting(displaySetting.copy(fontSizeRatio = it))
                                        },
                                        valueRange = 0.5f..2f,
                                        steps = 11,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(text = "${(displaySetting.fontSizeRatio * 100).toInt()}%")
                                }
                                MarkdownBlock(
                                    content = stringResource(R.string.setting_display_page_font_size_preview),
                                    style = LocalTextStyle.current.copy(
                                        fontSize = LocalTextStyle.current.fontSize * displaySetting.fontSizeRatio,
                                        lineHeight = LocalTextStyle.current.lineHeight * displaySetting.fontSizeRatio,
                                        fontWeight = FontWeight(displaySetting.chatFontWeight.coerceIn(100, 900)),
                                        fontFamily = previewFontFamily,
                                    )
                                )
                            }
                        }
                    )
                    item(
                        headlineContent = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("字体粗细")
                                if (displaySetting.chatFontWeight != 400) {
                                    TextButton(
                                        onClick = { updateDisplaySetting(displaySetting.copy(chatFontWeight = 400)) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    ) {
                                        Text("恢复标准(400)", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        },
                        supportingContent = {
                            Column {
                                Text(
                                    text = "当前字重：${formatFontWeightLabel(displaySetting.chatFontWeight)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Slider(
                                        value = displaySetting.chatFontWeight.toFloat(),
                                        onValueChange = {
                                            val stepped = ((it / 50f).roundToInt() * 50).coerceIn(100, 900)
                                            updateDisplaySetting(displaySetting.copy(chatFontWeight = stepped))
                                        },
                                        valueRange = 100f..900f,
                                        steps = 15,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(text = "${displaySetting.chatFontWeight}")
                                }
                            }
                        }
                    )
                    item(
                        headlineContent = { Text("思维链字体大小") },
                        supportingContent = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Slider(
                                    value = displaySetting.thinkingFontSizeRatio,
                                    onValueChange = {
                                        updateDisplaySetting(displaySetting.copy(thinkingFontSizeRatio = it))
                                    },
                                    valueRange = 0.5f..2.0f,
                                    steps = 5,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(text = "${(displaySetting.thinkingFontSizeRatio * 100).toInt()}%")
                            }
                        }
                    )
                }
            }

            // 本地多字体书架（支持保存多个自定义字体、随时点击切换与单独删除）
            item {
                CardGroup(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = { Text("我的本地字体库（支持多字体切换）") },
                ) {
                    item(
                        headlineContent = { Text("导入自定义字体到本地库") },
                        supportingContent = {
                            Text(
                                if (savedFonts.isEmpty()) "支持 .ttf / .otf / .ttc 字体文件，导入后会永久保存在本地供随时切换"
                                else "本地已收藏 ${savedFonts.size} 款字体 · 点击下方任意字体即可立即切换"
                            )
                        },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (displaySetting.chatFontFamily == ChatFontFamily.CUSTOM) {
                                    TextButton(onClick = {
                                        updateDisplaySetting(
                                            displaySetting.copy(chatFontFamily = ChatFontFamily.DEFAULT)
                                        )
                                    }) { Text("切回默认") }
                                }
                                FilledTonalButton(
                                    onClick = { fontPickerLauncher.launch(arrayOf("*/*")) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Icon(HugeIcons.Add01, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("添加字体", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        },
                    )

                    if (savedFonts.isNotEmpty()) {
                        item(
                            headlineContent = { Text("已保存的本地字体（点击卡片即刻切换）") },
                            supportingContent = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    savedFonts.forEach { fontFile ->
                                        val isCurrent = displaySetting.chatFontFamily == ChatFontFamily.CUSTOM &&
                                            displaySetting.customFontPath == fontFile.absolutePath
                                        val itemFontFamily = remember(fontFile.absolutePath, displaySetting.chatFontWeight) {
                                            buildCustomFileFontFamily(fontFile, displaySetting.chatFontWeight)
                                        }
                                        val displayName = remember(fontFile.name) {
                                            fontFile.nameWithoutExtension.removePrefix("custom_font_")
                                        }
                                        Surface(
                                            onClick = {
                                                updateDisplaySetting(
                                                    displaySetting.copy(
                                                        chatFontFamily = ChatFontFamily.CUSTOM,
                                                        customFontPath = fontFile.absolutePath,
                                                    )
                                                )
                                            },
                                            shape = MaterialTheme.shapes.medium,
                                            color = if (isCurrent) {
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainerLow
                                            },
                                            border = BorderStroke(
                                                width = if (isCurrent) 1.5.dp else 1.dp,
                                                color = if (isCurrent) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                }
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    ) {
                                                        Text(
                                                            text = displayName,
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                        if (isCurrent) {
                                                            Surface(
                                                                shape = MaterialTheme.shapes.small,
                                                                color = MaterialTheme.colorScheme.primary,
                                                            ) {
                                                                Text(
                                                                    text = "使用中",
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "绵绵 & 蔺鹤 ♡ 永远在一起 The quick brown fox 123456",
                                                        fontFamily = itemFontFamily,
                                                        fontWeight = FontWeight(displaySetting.chatFontWeight.coerceIn(100, 900)),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        val deletingPath = fontFile.absolutePath
                                                        runCatching { fontFile.delete() }
                                                        fontRefreshTick++
                                                        if (displaySetting.customFontPath == deletingPath) {
                                                            val remaining = fontDir.listFiles()
                                                                ?.filter { it.isFile && it.length() > 0L && it.absolutePath != deletingPath }
                                                                ?.sortedByDescending { it.lastModified() }
                                                                .orEmpty()
                                                            if (remaining.isNotEmpty()) {
                                                                updateDisplaySetting(
                                                                    displaySetting.copy(
                                                                        customFontPath = remaining.first().absolutePath
                                                                    )
                                                                )
                                                            } else {
                                                                updateDisplaySetting(
                                                                    displaySetting.copy(
                                                                        customFontPath = "",
                                                                        chatFontFamily = ChatFontFamily.DEFAULT,
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp),
                                                ) {
                                                    Icon(
                                                        imageVector = HugeIcons.Delete01,
                                                        contentDescription = "删除字体",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}