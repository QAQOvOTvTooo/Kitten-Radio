package me.rerere.rikkahub.ui.pages.backup.tabs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.ToastType
import kotlinx.coroutines.launch
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.File01
import me.rerere.hugeicons.stroke.FileImport
import me.rerere.rikkahub.data.sync.OurHomeBackupModule
import me.rerere.rikkahub.data.sync.OurHomeBackupSummary
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.pages.backup.BackupVM
import me.rerere.rikkahub.ui.theme.findKittenPalette
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun OurHomeModulesBackupTab(
    vm: BackupVM,
    onShowRestartDialog: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val palette = findKittenPalette(settings.ourHomeSetting.kittenPaletteId)
    val summary by vm.ourHomeBackupSummary.collectAsStateWithLifecycle()

    var selectedModules by remember {
        mutableStateOf(OurHomeBackupModule.entries.toSet())
    }
    // 导入恢复时：true = 恢复备份包内的全部板块；false = 仅恢复下方勾选的板块
    var restoreAllInPackage by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.refreshOurHomeBackupSummary()
    }

    val createModuleZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { targetUri ->
            scope.launch {
                isExporting = true
                runCatching {
                    val zipFile = vm.exportOurHomeModulesToFile(selectedModules)
                    context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                        FileInputStream(zipFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    zipFile.delete()
                    toaster.show(
                        "新家板块备份导出成功（共 ${selectedModules.size} 个板块及配图）♡",
                        type = ToastType.Success
                    )
                }.onFailure { err ->
                    err.printStackTrace()
                    toaster.show(
                        "导出新家板块备份失败：${err.message ?: "未知错误"}",
                        type = ToastType.Error
                    )
                }
                isExporting = false
            }
        }
    }

    val openModuleZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { sourceUri ->
            scope.launch {
                isRestoring = true
                runCatching {
                    val tempFile = File(
                        context.cacheDir,
                        "temp_our_home_modules_${System.currentTimeMillis()}.zip"
                    )
                    context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                        FileOutputStream(tempFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    val filter = if (restoreAllInPackage) null else selectedModules
                    val result = vm.restoreOurHomeModulesFromLocalFile(tempFile, filter)
                    tempFile.delete()
                    vm.refreshOurHomeBackupSummary()

                    toaster.show(
                        "已恢复 ${result.restoredModules.size} 个新家板块、${result.restoredFilesCount} 个素材文件 ♡",
                        type = ToastType.Success
                    )
                    onShowRestartDialog()
                }.onFailure { err ->
                    err.printStackTrace()
                    toaster.show(
                        "恢复新家板块失败：${err.message ?: "请确认选择的是新家板块备份包"}",
                        type = ToastType.Error
                    )
                }
                isRestoring = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. 顶部说明与实时数据统计横幅
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = palette.softContainer,
                border = BorderStroke(1.5.dp, palette.primary.copy(alpha = 0.35f)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "🏡 小猫电台 · 新家板块独立备份通道",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            color = palette.textPrimary,
                        )
                    }
                    Text(
                        text = "与传统基础备份完全解耦！原有的「本地 / WebDAV」继续保持纯净通用格式，随时可跨客户端搬家；而这里专门打包我们在小猫电台里的 7 大新板块数据与全部相册装扮图片。",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary,
                        lineHeight = 18.sp,
                    )

                    if (summary != null) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = palette.primary.copy(alpha = 0.2f),
                        )
                        SummaryStatsRow(summary = summary!!, palette = palette)
                    }
                }
            }
        }

        // 2. 一键导出 & 单独导入操作区
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.28f)),
                shadowElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "✨ 导出与导入通道",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                    )

                    // 导出按钮
                    Button(
                        onClick = {
                            if (!isExporting && selectedModules.isNotEmpty()) {
                                val timestamp = LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                val suffix = if (selectedModules.size == OurHomeBackupModule.entries.size) {
                                    "all"
                                } else {
                                    "${selectedModules.size}mods"
                                }
                                createModuleZipLauncher.launch("kitten_home_modules_${suffix}_$timestamp.zip")
                            }
                        },
                        enabled = !isExporting && !isRestoring && selectedModules.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                    ) {
                        if (isExporting) {
                            CircularWavyProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("正在打包新家板块与本地图片...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(HugeIcons.File01, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = if (selectedModules.size == OurHomeBackupModule.entries.size) {
                                    "一键导出全部新家板块备份 (.zip)"
                                } else {
                                    "导出已勾选的 ${selectedModules.size} 个板块备份 (.zip)"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    // 导入模式切换与导入按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = restoreAllInPackage,
                            onClick = { restoreAllInPackage = true },
                            label = { Text("导入包内全部板块", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.softContainer,
                                selectedLabelColor = palette.primary,
                            ),
                        )
                        FilterChip(
                            selected = !restoreAllInPackage,
                            onClick = { restoreAllInPackage = false },
                            label = { Text("仅导入下方勾选板块", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.softContainer,
                                selectedLabelColor = palette.primary,
                            ),
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isRestoring && !isExporting) {
                                openModuleZipLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                            }
                        },
                        enabled = !isRestoring && !isExporting && (restoreAllInPackage || selectedModules.isNotEmpty()),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, palette.primary),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                    ) {
                        if (isRestoring) {
                            CircularWavyProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = palette.primary,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("正在恢复新家板块数据与配图...", color = palette.primary, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(HugeIcons.FileImport, contentDescription = null, tint = palette.primary)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = if (restoreAllInPackage) {
                                    "从备份文件导入恢复新家板块 (.zip)"
                                } else {
                                    "从备份文件仅导入所选的 ${selectedModules.size} 个板块 (.zip)"
                                },
                                color = palette.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        // 3. 7 大新家板块自由勾选清单
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "📦 选择要包含的新家板块 (${selectedModules.size}/${OurHomeBackupModule.entries.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.textPrimary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { selectedModules = OurHomeBackupModule.entries.toSet() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("全选", color = palette.primary, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = {
                            selectedModules = if (selectedModules.size == OurHomeBackupModule.entries.size) {
                                setOf(OurHomeBackupModule.DIARY, OurHomeBackupModule.DREAM, OurHomeBackupModule.WORKSHOP)
                            } else {
                                OurHomeBackupModule.entries.toSet()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text("常用精选", color = palette.textSecondary)
                    }
                }
            }
        }

        items(OurHomeBackupModule.entries) { module ->
            val checked = module in selectedModules
            val countTag = remember(module, summary) {
                summary?.let { s ->
                    when (module) {
                        OurHomeBackupModule.PHONE -> "${s.phoneNoteCount} 个内置App"
                        OurHomeBackupModule.DIARY -> "${s.diaryCount} 篇手帐日记"
                        OurHomeBackupModule.DREAM -> "${s.dreamBubbleCount} 个吧唧 · ${s.dreamWallCount} 面墙"
                        OurHomeBackupModule.GAME -> "${s.gameCartridgeCount} 张游戏卡带"
                        OurHomeBackupModule.SANCTUARY -> "${s.memoryCount} 条记忆 & 显影库"
                        OurHomeBackupModule.STARDEW -> "${s.stardewRoomCount} 个房间与衣帽间"
                        OurHomeBackupModule.WORKSHOP -> "${s.workshopPresetCount} 套方案 & 全屋素材"
                    }
                }.orEmpty()
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedModules = if (checked) {
                            if (selectedModules.size > 1) selectedModules - module else selectedModules
                        } else {
                            selectedModules + module
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                color = if (checked) palette.cardSurface else palette.cardSurface.copy(alpha = 0.6f),
                border = BorderStroke(
                    width = if (checked) 1.5.dp else 1.dp,
                    color = if (checked) palette.primary.copy(alpha = 0.55f) else palette.primary.copy(alpha = 0.18f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { isNowChecked ->
                            selectedModules = if (isNowChecked) {
                                selectedModules + module
                            } else {
                                if (selectedModules.size > 1) selectedModules - module else selectedModules
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = palette.primary,
                            checkmarkColor = Color.White,
                        ),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "${module.badge} ${module.title}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = palette.textPrimary,
                            )
                        }
                        if (countTag.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = palette.softContainer,
                            ) {
                                Text(
                                    text = countTag,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = palette.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Text(
                            text = module.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SummaryStatsRow(
    summary: OurHomeBackupSummary,
    palette: me.rerere.rikkahub.ui.theme.KittenPaletteSpec,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StatPill("日记", "${summary.diaryCount}篇", palette)
        StatPill("梦境吧唧", "${summary.dreamBubbleCount}个", palette)
        StatPill("小木屋", "${summary.stardewRoomCount}间", palette)
        StatPill("工坊方案", "${summary.workshopPresetCount}套", palette)
        StatPill("永久记忆", "${summary.memoryCount}条", palette)
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    palette: me.rerere.rikkahub.ui.theme.KittenPaletteSpec,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = palette.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textSecondary,
        )
    }
}
