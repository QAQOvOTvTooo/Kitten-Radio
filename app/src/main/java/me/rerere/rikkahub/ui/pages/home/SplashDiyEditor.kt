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
import me.rerere.rikkahub.data.datastore.SettingsStore
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
 * 开屏手帐画布底层背景：优先显示自定义全屏背景图，未设置时显示5色主题手帐网格底板
 */
@Composable
fun SplashCanvasBackground(
    homeSetting: OurHomeSetting,
    palette: KittenPaletteSpec,
) {
    val bgPath = homeSetting.splashBackgroundPath
    val bgBitmap = remember(bgPath) {
        if (bgPath.isNotBlank() && File(bgPath).exists()) {
            BitmapFactory.decodeFile(bgPath)?.asImageBitmap()
        } else null
    }
    if (bgBitmap != null) {
        Image(
            bitmap = bgBitmap,
            contentDescription = "开屏自定义背景图",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Box(
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
                }
        )
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
    val prefStore: SettingsStore = koinInject()
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

    // "__PLAYER__" 代表当前选中黑胶播放器小组件，默认打开即可看到播放器缩放与拖动提示
    var selectedItemId by remember { mutableStateOf<String?>("__PLAYER__") }
    var isPanelCollapsed by remember { mutableStateOf(false) }
    var isTopBarCollapsed by remember { mutableStateOf(false) }
    var showAddTextDialog by remember { mutableStateOf(false) }
    var showStickerPickerDialog by remember { mutableStateOf(false) }
    var showSongDialog by remember { mutableStateOf(false) }

    val bgImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            copyUriToInternal(uri, "splash_bg")?.let { path ->
                updateHome { it.copy(splashBackgroundPath = path) }
            }
        }
    }

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
    val isPlayerSelected = selectedItemId == "__PLAYER__"

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val parentWidthPx = with(density) { maxWidth.toPx() }
        val parentHeightPx = with(density) { maxHeight.toPx() }

        // 0. 底层背景图或手帐网格
        SplashCanvasBackground(
            homeSetting = homeSetting,
            palette = palette,
        )

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
                                            xFraction = (cur.xFraction + dx).coerceIn(-0.35f, 0.96f),
                                            yFraction = (cur.yFraction + dy).coerceIn(-0.08f, 0.96f),
                                        )
                                    } else cur
                                }
                            )
                        }
                    }
                )
            }
        }

        // 2. 可像小组件一样自由拖动位置与调整大小的图2 Night Drive 黑胶播放器
        val playerOffsetX = (homeSetting.splashPlayerXFraction * parentWidthPx).roundToInt()
        val playerOffsetY = (homeSetting.splashPlayerYFraction * parentHeightPx).roundToInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(playerOffsetX, playerOffsetY) }
                .graphicsLayer {
                    scaleX = homeSetting.splashPlayerScale
                    scaleY = homeSetting.splashPlayerScale
                }
                .then(
                    if (isPlayerSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = palette.primary,
                            shape = RoundedCornerShape(28.dp),
                        )
                    } else Modifier
                )
                .pointerInput(parentWidthPx, parentHeightPx, homeSetting.splashPlayerScale) {
                    detectDragGestures(
                        onDragStart = { selectedItemId = "__PLAYER__" },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (parentWidthPx > 0f && parentHeightPx > 0f) {
                                val dx = (dragAmount.x * homeSetting.splashPlayerScale) / parentWidthPx
                                val dy = (dragAmount.y * homeSetting.splashPlayerScale) / parentHeightPx
                                updateHome { old ->
                                    old.copy(
                                        splashPlayerXFraction = (old.splashPlayerXFraction + dx).coerceIn(-0.45f, 0.45f),
                                        splashPlayerYFraction = (old.splashPlayerYFraction + dy).coerceIn(-0.42f, 0.42f),
                                    )
                                }
                            }
                        }
                    )
                }
                .clickable { selectedItemId = "__PLAYER__" },
            contentAlignment = Alignment.Center,
        ) {
            KittenNightDriveVinylCard(
                homeSetting = homeSetting,
                palette = palette,
                onPickCenterImage = { centerImageLauncher.launch("image/*") },
                onEditSongInfo = { showSongDialog = true },
                onToggleSpin = { updateHome { it.copy(splashSpinRecord = !it.splashSpinRecord) } },
            )
        }

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
                                            xFraction = (cur.xFraction + dx).coerceIn(-0.35f, 0.96f),
                                            yFraction = (cur.yFraction + dy).coerceIn(-0.08f, 0.96f),
                                        )
                                    } else cur
                                }
                            )
                        }
                    }
                )
            }
        }

        // 顶部操作栏：支持一键收起成右上角小胶囊，彻底不挡顶部手帐区域
        if (isTopBarCollapsed) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 12.dp),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 6.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "▾ 顶栏",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.primary,
                        modifier = Modifier.clickable { isTopBarCollapsed = false },
                    )
                    Text(
                        text = "完成",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                        modifier = Modifier.clickable { onDismiss() },
                    )
                }
            }
        } else {
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
                            text = "▴ 手帐DIY",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                            modifier = Modifier.clickable { isTopBarCollapsed = true },
                        )
                        me.rerere.rikkahub.ui.theme.allKittenPalettes().forEach { p ->
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
        }

        // 底部手帐工具台：支持一键收起成右下角迷你悬浮按钮，完全露出下半区供绵绵自由排版
        if (isPanelCollapsed) {
            Surface(
                onClick = { isPanelCollapsed = false },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(50),
                color = palette.primary,
                shadowElevation = 8.dp,
                border = BorderStroke(1.5.dp, Color.White),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "▴ 展开工具箱",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
            }
        } else {
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
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 顶部折叠把手栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "✨ 手帐装修工具箱",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.textSecondary,
                        )
                        Surface(
                            onClick = { isPanelCollapsed = true },
                            shape = RoundedCornerShape(50),
                            color = palette.softContainer,
                        ) {
                            Text(
                                text = "▾ 收起面板 (编辑下半区)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                // 如果选中了黑胶播放器小组件，显示播放器专属的大小缩放滑块与一键归位控制条
                if (isPlayerSelected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "已选中：黑胶播放器（可手指直接拖动位置）",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = palette.primary,
                        )
                        TextButton(
                            onClick = {
                                updateHome {
                                    it.copy(
                                        splashPlayerXFraction = 0f,
                                        splashPlayerYFraction = 0f,
                                        splashPlayerScale = 1.0f,
                                    )
                                }
                            }
                        ) {
                            Text("归位重置", color = palette.primary)
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("唱机大小", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = homeSetting.splashPlayerScale,
                            onValueChange = { v ->
                                updateHome { it.copy(splashPlayerScale = v.coerceIn(0.45f, 1.35f)) }
                            },
                            valueRange = 0.45f..1.35f,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${(homeSetting.splashPlayerScale * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary,
                        )
                    }
                }

                // 如果选中了某个贴纸/文字元素，显示缩放/旋转/前后层级/删除控制条
                if (selectedItem != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "已选中贴纸（手指直接拖动位置）",
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
                                    selectedItemId = "__PLAYER__"
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

                // 第一排：设置开屏背景图 + 调整唱机小组件 + 换唱机圆图与歌名
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = homeSetting.splashBackgroundPath.isNotBlank(),
                        onClick = { bgImageLauncher.launch("image/*") },
                        label = { Text(if (homeSetting.splashBackgroundPath.isNotBlank()) "更换背景图" else "+ 设置背景图") },
                    )
                    if (homeSetting.splashBackgroundPath.isNotBlank()) {
                        FilterChip(
                            selected = false,
                            onClick = { updateHome { it.copy(splashBackgroundPath = "") } },
                            label = { Text("恢复网格底") },
                        )
                    }
                    FilterChip(
                        selected = isPlayerSelected,
                        onClick = { selectedItemId = "__PLAYER__" },
                        label = { Text("调整播放器") },
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

                    // 第二排：添加手帐贴纸与文字
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
                            label = { Text("+ 相册贴纸") },
                        )
                        FilterChip(
                            selected = false,
                            onClick = { showAddTextDialog = true },
                            label = { Text("+ 手帐文字") },
                        )
                    }
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
