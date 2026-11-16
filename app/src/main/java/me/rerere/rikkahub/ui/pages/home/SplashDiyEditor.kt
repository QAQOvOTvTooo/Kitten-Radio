package me.rerere.rikkahub.ui.pages.home

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.datastore.OurHomeSetting
import me.rerere.rikkahub.data.datastore.PreferencesStore
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.datastore.SplashDiyItem
import me.rerere.rikkahub.ui.theme.KittenPaletteSpec
import me.rerere.rikkahub.ui.theme.KittenPalettes
import me.rerere.rikkahub.ui.theme.findKittenPalette
import me.rerere.rikkahub.ui.theme.kittenGridBackground
import me.rerere.rikkahub.ui.theme.rememberKittenAssetColorFilter
import org.koin.compose.koinInject
import java.io.File
import kotlin.math.roundToInt

data class BuiltinStickerOption(
    val id: String,
    val label: String,
    val resId: Int,
    val tintable: Boolean = true,
)

val BuiltinSplashStickers = listOf(
    BuiltinStickerOption("plush_auto", "五色小猫玩偶", R.drawable.kitten_plush_pink, tintable = false),
    BuiltinStickerOption("polaroid", "草莓拍立得", R.drawable.kitten_asset_polaroid_frame, tintable = true),
    BuiltinStickerOption("lace_tag", "恶魔翅膀吊牌", R.drawable.kitten_asset_lace_tag, tintable = true),
    BuiltinStickerOption("wavy_todo", "波浪待办卡", R.drawable.kitten_asset_wavy_todo, tintable = true),
    BuiltinStickerOption("spiral_note", "线圈便签本", R.drawable.kitten_asset_spiral_note, tintable = true),
    BuiltinStickerOption("clip_memo", "回形针便签", R.drawable.kitten_asset_clip_memo, tintable = true),
    BuiltinStickerOption("make_amazing", "蝴蝶结日签", R.drawable.kitten_asset_make_amazing, tintable = true),
)

/**
 * 图2同款：Night Drive 黑胶播放器核心卡片
 * - 整个卡片随全屋5色智能染色
 * - 唱片中心圆区域可点击从相册换图，并缓慢旋转
 * - 左下方歌名与歌手名可点击修改
 */
@Composable
fun KittenNightDriveVinylCard(
    homeSetting: OurHomeSetting,
    palette: KittenPaletteSpec,
    onPickCenterImage: () -> Unit,
    onEditSongInfo: () -> Unit,
    onToggleSpin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val assetColorFilter = rememberKittenAssetColorFilter(
        paletteId = palette.id,
        enabled = homeSetting.enableAssetTint,
    )

    val centerPath = homeSetting.splashCenterImagePath
    val centerBitmap = remember(centerPath) {
        if (centerPath.isNotBlank() && File(centerPath).exists()) {
            BitmapFactory.decodeFile(centerPath)?.asImageBitmap()
        } else null
    }

    val infiniteTransition = rememberInfiniteTransition(label = "nightDriveSpin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )
    val effectiveAngle = if (homeSetting.splashSpinRecord) spinAngle else 0f

    // 原图尺寸比例 1748 : 2411 (约 0.725f)
    // 黑胶圆心位于 (x = 0.4983f, y = 0.3339f)，内圆直径占卡片宽度 0.3518f
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth(0.84f)
            .aspectRatio(1748f / 2411f),
    ) {
        val cardW = maxWidth
        val cardH = maxHeight
        val innerDiameter = cardW * 0.355f
        val centerOffsetX = cardW * 0.4983f - innerDiameter / 2
        val centerOffsetY = cardH * 0.3339f - innerDiameter / 2

        // 1. 播放器高清底图（已擦除原图固定的英文歌名，支持5色智能染色）
        Image(
            painter = painterResource(id = R.drawable.kitten_splash_vinyl_clean),
            contentDescription = "Night Drive Vinyl Player",
            colorFilter = assetColorFilter,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )

        // 2. 唱片中心圆形可旋转区域（点击可换自己的圆形图！）
        Box(
            modifier = Modifier
                .offset(x = centerOffsetX, y = centerOffsetY)
                .size(innerDiameter)
                .graphicsLayer { rotationZ = effectiveAngle }
                .clip(CircleShape)
                .clickable { onPickCenterImage() },
            contentAlignment = Alignment.Center,
        ) {
            if (centerBitmap != null) {
                Image(
                    bitmap = centerBitmap,
                    contentDescription = "自定义唱片圆心图",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(3.dp, Color.White, CircleShape),
                )
            }
        }

        // 右上角唱针热区（点击可暂停/恢复旋转）
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = cardH * 0.12f, end = cardW * 0.04f)
                .size(width = cardW * 0.18f, height = cardH * 0.35f)
                .clickable { onToggleSpin() }
        )

        // 3. 左下方可自定义歌名与歌手名（原图 y = 0.645f..0.755f 处已擦白）
        Column(
            modifier = Modifier
                .offset(x = cardW * 0.068f, y = cardH * 0.650f)
                .clickable { onEditSongInfo() },
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = homeSetting.splashSongTitle.ifBlank { "Night Drive" },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                ),
                color = palette.textPrimary,
            )
            Text(
                text = homeSetting.splashArtistName.ifBlank { "Luvbyte" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
                color = palette.textSecondary,
            )
        }
    }
}

/**
 * 渲染单个手帐 DIY 元素（支持拖拽、缩放、旋转、横竖排文字、内置贴纸与相册图片）
 */
@Composable
fun SplashDiyElementView(
    item: SplashDiyItem,
    palette: KittenPaletteSpec,
    enableAssetTint: Boolean,
    parentWidthPx: Float,
    parentHeightPx: Float,
    isEditing: Boolean = false,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onDragDelta: (dxFraction: Float, dyFraction: Float) -> Unit = { _, _ -> },
) {
    val assetColorFilter = rememberKittenAssetColorFilter(
        paletteId = palette.id,
        enabled = enableAssetTint,
    )
    val offsetX = (item.xFraction * parentWidthPx).roundToInt()
    val offsetY = (item.yFraction * parentHeightPx).roundToInt()

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .graphicsLayer {
                scaleX = item.scale
                scaleY = item.scale
                rotationZ = item.rotation
            }
            .let { mod ->
                if (isEditing) {
                    mod
                        .then(
                            if (isSelected) {
                                Modifier.border(1.5.dp, palette.primary, RoundedCornerShape(10.dp))
                            } else Modifier
                        )
                        .pointerInput(item.id, parentWidthPx, parentHeightPx) {
                            detectDragGestures(
                                onDragStart = { onSelect() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (parentWidthPx > 0f && parentHeightPx > 0f) {
                                        onDragDelta(
                                            dragAmount.x / parentWidthPx,
                                            dragAmount.y / parentHeightPx,
                                        )
                                    }
                                }
                            )
                        }
                        .clickable { onSelect() }
                } else mod
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (item.type) {
            "text" -> {
                val textContent = item.content.ifBlank { "欢迎收听小猫电台 ♡" }
                Surface(
                    color = if (item.hasPillBg) palette.primary.copy(alpha = 0.78f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    if (item.isVerticalText) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            textContent.forEach { ch ->
                                Text(
                                    text = ch.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (item.hasPillBg) Color.White else palette.textPrimary,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = textContent,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (item.hasPillBg) Color.White else palette.textPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
            "image" -> {
                val bmp = remember(item.content) {
                    if (item.content.isNotBlank() && File(item.content).exists()) {
                        BitmapFactory.decodeFile(item.content)?.asImageBitmap()
                    } else null
                }
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = "DIY贴纸",
                        modifier = Modifier.size(130.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            else -> {
                // builtin_sticker
                val opt = BuiltinSplashStickers.find { it.id == item.content }
                val resId = when {
                    item.content == "plush_auto" -> palette.plushRes
                    opt != null -> opt.resId
                    else -> palette.plushRes
                }
                val useTint = opt?.tintable == true
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = opt?.label ?: "内置贴纸",
                    colorFilter = if (useTint) assetColorFilter else null,
                    modifier = Modifier.size(136.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

/**
 * 「设置 -> 显示设置 -> 开屏界面自定义」全屏手帐 DIY 画布编辑器
 */
@Composable
fun SplashDiyFullEditorModal(
    settings: Settings,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefStore: PreferencesStore = koinInject()
    val homeSetting = settings.ourHomeSetting
    val palette = findKittenPalette(homeSetting.kittenPaletteId)
    val density = LocalDensity.current

    fun updateHome(block: (OurHomeSetting) -> OurHomeSetting) {
        scope.launch {
            prefStore.update { old -> old.copy(ourHomeSetting = block(old.ourHomeSetting)) }
        }
    }

    fun copyUriToInternal(uri: Uri, prefix: String): String? {
        return runCatching {
            val dir = File(context.filesDir, "kitten_splash").apply { mkdirs() }
            val dst = File(dir, "${prefix}_${System.currentTimeMillis()}.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                dst.outputStream().use { output -> input.copyTo(output) }
            }
            dst.absolutePath
        }.getOrNull()
    }

    var selectedItemId by remember { mutableStateOf<String?>(homeSetting.splashDiyItems.lastOrNull()?.id) }
    var showAddTextDialog by remember { mutableStateOf(false) }
    var showStickerPickerDialog by remember { mutableStateOf(false) }
    var showSongDialog by remember { mutableStateOf(false) }

    val centerImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            copyUriToInternal(uri, "center")?.let { path ->
                updateHome { it.copy(splashCenterImagePath = path) }
            }
        }
    }

    val diyImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            copyUriToInternal(uri, "diy_sticker")?.let { path ->
                val newItem = SplashDiyItem(
                    id = "diy_${System.currentTimeMillis()}",
                    type = "image",
                    content = path,
                    xFraction = 0.08f,
                    yFraction = 0.62f,
                    scale = 1.0f,
                    abovePlayer = true,
                )
                selectedItemId = newItem.id
                updateHome { it.copy(splashDiyItems = it.splashDiyItems + newItem) }
            }
        }
    }

    val selectedItem = homeSetting.splashDiyItems.find { it.id == selectedItemId }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .let { mod ->
                if (homeSetting.enableGridBackground) {
                    mod.kittenGridBackground(
                        backgroundColor = palette.background,
                        gridColor = palette.gridLineColor,
                        cellSize = 22.dp,
                    )
                } else {
                    mod.background(palette.background)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val parentWidthPx = with(density) { maxWidth.toPx() }
        val parentHeightPx = with(density) { maxHeight.toPx() }

        // 1. 播放器下方的 DIY 元素
        homeSetting.splashDiyItems.filter { !it.abovePlayer }.forEach { item ->
            Box(modifier = Modifier.fillMaxSize()) {
                SplashDiyElementView(
                    item = item,
                    palette = palette,
                    enableAssetTint = homeSetting.enableAssetTint,
                    parentWidthPx = parentWidthPx,
                    parentHeightPx = parentHeightPx,
                    isEditing = true,
                    isSelected = item.id == selectedItemId,
                    onSelect = { selectedItemId = item.id },
                    onDragDelta = { dx, dy ->
                        updateHome { old ->
                            old.copy(
                                splashDiyItems = old.splashDiyItems.map { cur ->
                                    if (cur.id == item.id) {
                                        cur.copy(
                                            xFraction = (cur.xFraction + dx).coerceIn(-0.2f, 0.92f),
                                            yFraction = (cur.yFraction + dy).coerceIn(0.02f, 0.90f),
                                        )
                                    } else cur
                                }
                            )
                        }
                    }
                )
            }
        }

        // 2. 居中的图2 Night Drive 黑胶播放器
        KittenNightDriveVinylCard(
            homeSetting = homeSetting,
            palette = palette,
            onPickCenterImage = { centerImageLauncher.launch("image/*") },
            onEditSongInfo = { showSongDialog = true },
            onToggleSpin = { updateHome { it.copy(splashSpinRecord = !it.splashSpinRecord) } },
        )

        // 3. 播放器上方的 DIY 元素
        homeSetting.splashDiyItems.filter { it.abovePlayer }.forEach { item ->
            Box(modifier = Modifier.fillMaxSize()) {
                SplashDiyElementView(
                    item = item,
                    palette = palette,
                    enableAssetTint = homeSetting.enableAssetTint,
                    parentWidthPx = parentWidthPx,
                    parentHeightPx = parentHeightPx,
                    isEditing = true,
                    isSelected = item.id == selectedItemId,
                    onSelect = { selectedItemId = item.id },
                    onDragDelta = { dx, dy ->
                        updateHome { old ->
                            old.copy(
                                splashDiyItems = old.splashDiyItems.map { cur ->
                                    if (cur.id == item.id) {
                                        cur.copy(
                                            xFraction = (cur.xFraction + dx).coerceIn(-0.2f, 0.92f),
                                            yFraction = (cur.yFraction + dy).coerceIn(0.02f, 0.90f),
                                        )
                                    } else cur
                                }
                            )
                        }
                    }
                )
            }
        }

        // 顶部操作栏：5色切换 + 完成保存
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp, start = 12.dp, end = 12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.94f),
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "手帐DIY",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                    )
                    KittenPalettes.forEach { p ->
                        val sel = p.id == palette.id
                        Box(
                            modifier = Modifier
                                .size(if (sel) 22.dp else 16.dp)
                                .background(p.primary, CircleShape)
                                .let { m -> if (sel) m.border(2.dp, p.secondary, CircleShape) else m }
                                .clickable { updateHome { it.copy(kittenPaletteId = p.id) } }
                        )
                    }
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                ) {
                    Text("完成", color = Color.White)
                }
            }
        }

        // 底部手帐工具台
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.96f),
            shadowElevation = 10.dp,
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 如果选中了某个元素，显示缩放/旋转/前后层级/删除控制条
                if (selectedItem != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "已选中元素（手指直接拖动位置）",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.primary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = selectedItem.abovePlayer,
                                onClick = {
                                    updateHome { old ->
                                        old.copy(
                                            splashDiyItems = old.splashDiyItems.map {
                                                if (it.id == selectedItem.id) it.copy(abovePlayer = !it.abovePlayer) else it
                                            }
                                        )
                                    }
                                },
                                label = { Text(if (selectedItem.abovePlayer) "置于唱机前" else "置于唱机后") }
                            )
                            TextButton(
                                onClick = {
                                    updateHome { old ->
                                        old.copy(splashDiyItems = old.splashDiyItems.filterNot { it.id == selectedItem.id })
                                    }
                                    selectedItemId = null
                                }
                            ) {
                                Text("删除", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("大小", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = selectedItem.scale,
                            onValueChange = { v ->
                                updateHome { old ->
                                    old.copy(
                                        splashDiyItems = old.splashDiyItems.map {
                                            if (it.id == selectedItem.id) it.copy(scale = v) else it
                                        }
                                    )
                                }
                            },
                            valueRange = 0.4f..2.2f,
                            modifier = Modifier.weight(1f),
                        )
                        Text("旋转", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = selectedItem.rotation,
                            onValueChange = { v ->
                                updateHome { old ->
                                    old.copy(
                                        splashDiyItems = old.splashDiyItems.map {
                                            if (it.id == selectedItem.id) it.copy(rotation = v) else it
                                        }
                                    )
                                }
                            },
                            valueRange = -90f..90f,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // 快捷添加按钮栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { showStickerPickerDialog = true },
                        label = { Text("+ 内置贴纸") },
                    )
                    FilterChip(
                        selected = false,
                        onClick = { diyImageLauncher.launch("image/*") },
                        label = { Text("+ 相册图片") },
                    )
                    FilterChip(
                        selected = false,
                        onClick = { showAddTextDialog = true },
                        label = { Text("+ 手帐文字") },
                    )
                    FilterChip(
                        selected = false,
                        onClick = { centerImageLauncher.launch("image/*") },
                        label = { Text("换唱机圆图") },
                    )
                    FilterChip(
                        selected = false,
                        onClick = { showSongDialog = true },
                        label = { Text("改歌名") },
                    )
                }
            }
        }
    }

    if (showStickerPickerDialog) {
        AlertDialog(
            onDismissRequest = { showStickerPickerDialog = false },
            title = { Text("选择内置手帐贴纸") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BuiltinSplashStickers.forEach { st ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.softContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newItem = SplashDiyItem(
                                        id = "diy_${System.currentTimeMillis()}",
                                        type = "builtin",
                                        content = st.id,
                                        xFraction = 0.06f,
                                        yFraction = 0.62f,
                                        scale = 1.0f,
                                        abovePlayer = true,
                                    )
                                    selectedItemId = newItem.id
                                    updateHome { it.copy(splashDiyItems = it.splashDiyItems + newItem) }
                                    showStickerPickerDialog = false
                                }
                        ) {
                            Text(
                                text = "✨ ${st.label}",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStickerPickerDialog = false }) { Text("关闭") }
            }
        )
    }

    if (showAddTextDialog) {
        var textVal by remember { mutableStateOf("欢迎收听小猫の音乐电台！") }
        var isVert by remember { mutableStateOf(true) }
        var hasPill by remember { mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = { showAddTextDialog = false },
            title = { Text("添加手帐文字贴纸") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = textVal,
                        onValueChange = { textVal = it },
                        label = { Text("文字内容") },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("竖排文字样式")
                        Switch(checked = isVert, onCheckedChange = { isVert = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("带主题色胶带底框")
                        Switch(checked = hasPill, onCheckedChange = { hasPill = it })
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newItem = SplashDiyItem(
                            id = "diy_${System.currentTimeMillis()}",
                            type = "text",
                            content = textVal,
                            xFraction = 0.06f,
                            yFraction = 0.14f,
                            scale = 1.0f,
                            isVerticalText = isVert,
                            hasPillBg = hasPill,
                            abovePlayer = true,
                        )
                        selectedItemId = newItem.id
                        updateHome { it.copy(splashDiyItems = it.splashDiyItems + newItem) }
                        showAddTextDialog = false
                    }
                ) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextDialog = false }) { Text("取消") }
            }
        )
    }

    if (showSongDialog) {
        var songInput by remember { mutableStateOf(homeSetting.splashSongTitle) }
        var artistInput by remember { mutableStateOf(homeSetting.splashArtistName) }
        var btnInput by remember { mutableStateOf(homeSetting.splashButtonText) }
        AlertDialog(
            onDismissRequest = { showSongDialog = false },
            title = { Text("修改播放器歌名与信息") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = songInput,
                        onValueChange = { songInput = it },
                        label = { Text("歌曲名称（如 Night Drive）") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = artistInput,
                        onValueChange = { artistInput = it },
                        label = { Text("歌手 / 副标题（如 Luvbyte）") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = btnInput,
                        onValueChange = { btnInput = it },
                        label = { Text("底部回家按钮文字") },
                        singleLine = true,
                    )
                    if (homeSetting.splashCenterImagePath.isNotBlank()) {
                        TextButton(
                            onClick = {
                                updateHome { it.copy(splashCenterImagePath = "") }
                                showSongDialog = false
                            }
                        ) {
                            Text("恢复原图恶魔爱心圆心")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        updateHome {
                            it.copy(
                                splashSongTitle = songInput,
                                splashArtistName = artistInput,
                                splashButtonText = btnInput,
                            )
                        }
                        showSongDialog = false
                    }
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showSongDialog = false }) { Text("取消") }
            }
        )
    }
}
