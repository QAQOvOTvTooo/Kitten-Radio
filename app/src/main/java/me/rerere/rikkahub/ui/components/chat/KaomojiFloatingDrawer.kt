package me.rerere.rikkahub.ui.components.chat

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.theme.KittenPaletteSpec
import me.rerere.rikkahub.ui.theme.findKittenPalette
import me.rerere.rikkahub.ui.theme.rememberKittenAssetColorFilter
import me.rerere.rikkahub.utils.JsonInstant
import kotlin.math.roundToInt

@Serializable
data class KaomojiCategoryItem(
    val name: String,
    val items: List<String>,
)

private const val KAOMOJI_PREFS_NAME = "kaomoji_drawer_kit_store"
private const val KAOMOJI_LIBRARY_KEY = "KaomojiDrawerKit.library.v1"
private const val KAOMOJI_FLOAT_X_KEY = "kaomoji_float_x_ratio"
private const val KAOMOJI_FLOAT_Y_KEY = "kaomoji_float_y_ratio"

private fun loadBundledKaomojiCategories(context: Context): List<KaomojiCategoryItem> {
    return runCatching {
        val jsonStr = context.assets.open("default_kaomoji.json").bufferedReader().use { it.readText() }
        JsonInstant.decodeFromString<List<KaomojiCategoryItem>>(jsonStr)
    }.getOrElse {
        listOf(
            KaomojiCategoryItem(
                name = "绵绵最爱 ♡",
                items = listOf("૮₍ᵔ⤙ᵔ₎ა", "₍^˶ ╸𖥦 ╸˵^₎⟆", "˃ 𖥦 ˂", "₍ᐢ.ˬ.ᐢ₎", "૮₍ ´• ˕ •` ₎ა", "≡^ˇ^≡", "૮₍°□°₎ა", "(づ￣ ³￣)づ～")
            )
        )
    }
}

private fun loadSavedKaomojiCategories(context: Context): List<KaomojiCategoryItem> {
    val prefs = context.getSharedPreferences(KAOMOJI_PREFS_NAME, Context.MODE_PRIVATE)
    val savedRaw = prefs.getString(KAOMOJI_LIBRARY_KEY, null)
    if (!savedRaw.isNullOrBlank()) {
        val parsed = runCatching {
            JsonInstant.decodeFromString<List<KaomojiCategoryItem>>(savedRaw)
        }.getOrNull()
        if (!parsed.isNullOrEmpty()) return parsed
    }
    return loadBundledKaomojiCategories(context)
}

private fun saveKaomojiCategories(context: Context, list: List<KaomojiCategoryItem>) {
    val prefs = context.getSharedPreferences(KAOMOJI_PREFS_NAME, Context.MODE_PRIVATE)
    runCatching {
        prefs.edit().putString(KAOMOJI_LIBRARY_KEY, JsonInstant.encodeToString(list)).apply()
    }
}

/**
 * 聊天界面可自由拖动的像素情书颜文字悬浮球 + KaomojiDrawerKit 抽屉
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaomojiFloatingLetterOverlay(
    paletteId: String,
    enableAssetTint: Boolean,
    onSelectKaomoji: (String) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val palette = findKittenPalette(paletteId)
    val assetColorFilter = rememberKittenAssetColorFilter(
        paletteId = paletteId,
        enabled = enableAssetTint,
    )

    val prefs = remember { context.getSharedPreferences(KAOMOJI_PREFS_NAME, Context.MODE_PRIVATE) }
    var xRatio by remember { mutableFloatStateOf(prefs.getFloat(KAOMOJI_FLOAT_X_KEY, 0.84f)) }
    var yRatio by remember { mutableFloatStateOf(prefs.getFloat(KAOMOJI_FLOAT_Y_KEY, 0.72f)) }
    var showDrawer by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val maxH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val btnW = with(density) { 48.dp.toPx() }
        val btnH = with(density) { 38.dp.toPx() }

        val currentX = (xRatio * (maxW - btnW)).coerceIn(0f, (maxW - btnW).coerceAtLeast(0f))
        val currentY = (yRatio * (maxH - btnH)).coerceIn(0f, (maxH - btnH).coerceAtLeast(0f))

        // 可拖动像素情书悬浮图标（随 6 色主题自动染色）
        Box(
            modifier = Modifier
                .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
                .size(width = 48.dp, height = 38.dp)
                .pointerInput(maxW, maxH) {
                    detectDragGestures(
                        onDragEnd = {
                            prefs.edit()
                                .putFloat(KAOMOJI_FLOAT_X_KEY, xRatio)
                                .putFloat(KAOMOJI_FLOAT_Y_KEY, yRatio)
                                .apply()
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        val nextX = (xRatio * (maxW - btnW) + dragAmount.x).coerceIn(0f, (maxW - btnW).coerceAtLeast(1f))
                        val nextY = (yRatio * (maxH - btnH) + dragAmount.y).coerceIn(0f, (maxH - btnH).coerceAtLeast(1f))
                        xRatio = nextX / (maxW - btnW).coerceAtLeast(1f)
                        yRatio = nextY / (maxH - btnH).coerceAtLeast(1f)
                    }
                }
                .clickable { showDrawer = true },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.kitten_kaomoji_love_letter),
                contentDescription = "打开颜文字小抽屉",
                colorFilter = assetColorFilter,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (showDrawer) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { showDrawer = false },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color.Transparent,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            KaomojiDrawerKitSheetContent(
                palette = palette,
                onSelect = { face ->
                    onSelectKaomoji(face)
                    showDrawer = false
                },
                onClose = { showDrawer = false },
            )
        }
    }
}

/**
 * 1:1 复刻 GitHub Pyruslili/KaomojiDrawerKit 的颜文字抽屉界面（自动融合小猫电台 6 色主题）
 */
@Composable
private fun KaomojiDrawerKitSheetContent(
    palette: KittenPaletteSpec,
    onSelect: (String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var categories by remember { mutableStateOf(loadSavedKaomojiCategories(context)) }
    var activeCategory by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var addCategory by remember { mutableStateOf("绵绵最爱 ♡") }
    var newKaomoji by remember { mutableStateOf("") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // 融合 KaomojiDrawerStyle.rose 与当前 6 色主题色板
    val bgTop = palette.background
    val bgBottom = palette.softContainer.copy(alpha = 0.85f)
    val glowColor = palette.primary
    val cardColor = Color.White.copy(alpha = 0.76f)
    val accentColor = palette.primary
    val textColor = palette.textPrimary
    val secondaryTextColor = palette.textSecondary

    val visibleCategories = remember(categories, activeCategory) {
        if (activeCategory == null) categories
        else categories.filter { it.name == activeCategory }
    }
    val totalCount = remember(categories) { categories.sumOf { it.items.size } }

    fun addFace(raw: String, targetCategory: String) {
        val value = raw.trim()
        if (value.isEmpty()) return
        val catName = targetCategory.trim().ifEmpty { "绵绵最爱 ♡" }
        val mutable = categories.toMutableList()
        val idx = mutable.indexOfFirst { it.name == catName }
        if (idx >= 0) {
            if (!mutable[idx].items.contains(value)) {
                mutable[idx] = mutable[idx].copy(items = listOf(value) + mutable[idx].items)
            }
        } else {
            mutable.add(0, KaomojiCategoryItem(name = catName, items = listOf(value)))
        }
        categories = mutable
        saveKaomojiCategories(context, mutable)
    }

    fun deleteFace(face: String, fromCategory: String) {
        val mutable = categories.toMutableList()
        val idx = mutable.indexOfFirst { it.name == fromCategory }
        if (idx >= 0) {
            val updatedItems = mutable[idx].items.filterNot { it == face }
            if (updatedItems.isEmpty()) {
                mutable.removeAt(idx)
                if (activeCategory == fromCategory) activeCategory = null
            } else {
                mutable[idx] = mutable[idx].copy(items = updatedItems)
            }
            categories = mutable
            saveKaomojiCategories(context, mutable)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 420.dp, max = 640.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
    ) {
        // 右上角 Radial Glow 柔光晕染（还原 KaomojiDrawerKit drawerBackground）
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.06f),
                    radius = size.width * 0.65f,
                ),
                center = Offset(size.width * 0.85f, size.height * 0.06f),
                radius = size.width * 0.65f,
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. 顶部 Header：左侧 KaomojiTidy 编辑按钮 | 中间标题与数量 | 右侧 KaomojiClose 猫咪关闭按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                KaomojiCircleIconButton(
                    active = isEditing,
                    accent = accentColor,
                    card = cardColor,
                    onClick = { isEditing = !isEditing },
                ) {
                    KaomojiTidyVectorIcon(tint = accentColor)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "绵绵的颜文字抽屉",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                    )
                    Text(
                        text = "$totalCount faces · 点击直接填入输入框",
                        fontSize = 10.sp,
                        color = secondaryTextColor,
                    )
                }

                KaomojiCircleIconButton(
                    active = false,
                    accent = accentColor,
                    card = cardColor,
                    onClick = onClose,
                ) {
                    KaomojiCloseCatVectorIcon(tint = accentColor)
                }
            }

            // 2. 横向分类胶囊栏 (All + 各个分类)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KaomojiCategoryPill(
                    title = "全部",
                    selected = activeCategory == null,
                    accent = accentColor,
                    card = cardColor,
                    secondaryText = secondaryTextColor,
                    onClick = { activeCategory = null },
                )
                categories.forEach { cat ->
                    KaomojiCategoryPill(
                        title = cat.name,
                        selected = activeCategory == cat.name,
                        accent = accentColor,
                        card = cardColor,
                        secondaryText = secondaryTextColor,
                        onClick = { activeCategory = cat.name },
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // 3. 分类颜文字网格列表 + 底部新增颜文字面板
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                visibleCategories.forEach { category ->
                    item(key = "header_${category.name}") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = category.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                )
                                Text(
                                    text = "${category.items.size}",
                                    fontSize = 11.sp,
                                    color = secondaryTextColor,
                                )
                            }

                            // 双列自适应网格展示颜文字胶囊
                            val rows = category.items.chunked(2)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                rows.forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        pair.forEach { kaomoji ->
                                            KaomojiCellCard(
                                                modifier = Modifier.weight(1f),
                                                kaomoji = kaomoji,
                                                isEditing = isEditing,
                                                cardColor = cardColor,
                                                accentColor = accentColor,
                                                textColor = textColor,
                                                onSelect = { onSelect(kaomoji) },
                                                onDelete = { deleteFace(kaomoji, category.name) },
                                            )
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 底部 Add a new face 面板
                item(key = "add_panel") {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = cardColor,
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.14f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 18.dp),
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
                                Text(
                                    text = "添加新颜文字 ♡",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                )
                                Box {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { categoryMenuExpanded = true }
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = addCategory,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = accentColor,
                                        )
                                        Text("▾", fontSize = 11.sp, color = accentColor)
                                    }
                                    DropdownMenu(
                                        expanded = categoryMenuExpanded,
                                        onDismissRequest = { categoryMenuExpanded = false },
                                        shape = RoundedCornerShape(14.dp),
                                    ) {
                                        val allCatNames = (listOf("绵绵最爱 ♡") + categories.map { it.name }).distinct()
                                        allCatNames.forEach { cName ->
                                            DropdownMenuItem(
                                                text = { Text(cName, fontSize = 12.sp) },
                                                onClick = {
                                                    addCategory = cName
                                                    categoryMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.85f))
                                        .border(0.8.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    if (newKaomoji.isEmpty()) {
                                        Text(
                                            text = "粘贴或输入喜欢的颜文字...",
                                            fontSize = 13.sp,
                                            color = secondaryTextColor.copy(alpha = 0.6f),
                                        )
                                    }
                                    BasicTextField(
                                        value = newKaomoji,
                                        onValueChange = { newKaomoji = it },
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            color = textColor,
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }

                                val canAdd = newKaomoji.trim().isNotEmpty()
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (canAdd) accentColor else accentColor.copy(alpha = 0.4f))
                                        .clickable(enabled = canAdd) {
                                            addFace(newKaomoji, addCategory)
                                            newKaomoji = ""
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "＋",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }

                            if (isEditing) {
                                Text(
                                    text = "↺ 恢复默认颜文字库",
                                    fontSize = 11.sp,
                                    color = secondaryTextColor,
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .clickable {
                                            val bundled = loadBundledKaomojiCategories(context)
                                            categories = bundled
                                            saveKaomojiCategories(context, bundled)
                                        }
                                        .padding(4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KaomojiCircleIconButton(
    active: Boolean,
    accent: Color,
    card: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (active) accent.copy(alpha = 0.16f) else card)
            .border(
                width = 0.8.dp,
                color = accent.copy(alpha = if (active) 0.28f else 0.14f),
                shape = CircleShape,
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun KaomojiCategoryPill(
    title: String,
    selected: Boolean,
    accent: Color,
    card: Color,
    secondaryText: Color,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(if (selected) accent else card, label = "pillBg")
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = 0.8.dp,
                color = if (selected) Color.Transparent else accent.copy(alpha = 0.14f),
                shape = CircleShape,
            )
            .clickable { onClick() }
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) Color.White else secondaryText,
        )
    }
}

@Composable
private fun KaomojiCellCard(
    modifier: Modifier = Modifier,
    kaomoji: String,
    isEditing: Boolean,
    cardColor: Color,
    accentColor: Color,
    textColor: Color,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor)
            .border(0.8.dp, accentColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .clickable(enabled = !isEditing) { onSelect() }
            .padding(start = 11.dp, end = if (isEditing) 30.dp else 11.dp, top = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = kaomoji,
            fontSize = 13.5.sp,
            color = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (isEditing) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935).copy(alpha = 0.85f))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center,
            ) {
                Text("－", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * 原创矢量图标复刻 1：kaomoji-tidy.svg（双胶囊标签 + 右上加号 + 左下四角星）
 */
@Composable
private fun KaomojiTidyVectorIcon(tint: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val s = size.width / 24f
        val stroke = Stroke(width = 1.3f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = tint,
            topLeft = Offset(4f * s, 6f * s),
            size = Size(13.5f * s, 4f * s),
            cornerRadius = CornerRadius(2f * s, 2f * s),
            style = stroke,
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(6.5f * s, 12.5f * s),
            size = Size(13.5f * s, 4f * s),
            cornerRadius = CornerRadius(2f * s, 2f * s),
            style = stroke,
        )
        drawLine(tint, Offset(8f * s, 8f * s), Offset(13.5f * s, 8f * s), strokeWidth = 1.15f * s, cap = StrokeCap.Round)
        drawLine(tint, Offset(10.5f * s, 14.5f * s), Offset(16f * s, 14.5f * s), strokeWidth = 1.15f * s, cap = StrokeCap.Round)
        // 右上小加号
        drawLine(tint, Offset(19.2f * s, 3.4f * s), Offset(19.2f * s, 6.6f * s), strokeWidth = 1.15f * s, cap = StrokeCap.Round)
        drawLine(tint, Offset(17.6f * s, 5f * s), Offset(20.8f * s, 5f * s), strokeWidth = 1.15f * s, cap = StrokeCap.Round)
        // 左下四角星
        val star = Path().apply {
            moveTo(4.8f * s, 18.0f * s)
            lineTo(5.2f * s, 19.2f * s)
            lineTo(6.4f * s, 19.6f * s)
            lineTo(5.2f * s, 20.0f * s)
            lineTo(4.8f * s, 21.2f * s)
            lineTo(4.4f * s, 20.0f * s)
            lineTo(3.2f * s, 19.6f * s)
            lineTo(4.4f * s, 19.2f * s)
            close()
        }
        drawPath(star, color = tint)
    }
}

/**
 * 原创矢量图标复刻 2：kaomoji-close.svg（小猫耳朵脑袋轮廓 + 中间 X）
 */
@Composable
private fun KaomojiCloseCatVectorIcon(tint: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val s = size.width / 24f
        val catPath = Path().apply {
            moveTo(6.1f * s, 8.3f * s)
            lineTo(5.5f * s, 4.4f * s)
            lineTo(9.2f * s, 6.45f * s)
            quadraticBezierTo(12f * s, 5.8f * s, 14.8f * s, 6.45f * s)
            lineTo(18.5f * s, 4.4f * s)
            lineTo(17.9f * s, 8.3f * s)
            cubicTo(19.5f * s, 9.6f * s, 20f * s, 11.2f * s, 20f * s, 12.85f * s)
            cubicTo(20f * s, 16.8f * s, 16.8f * s, 19.5f * s, 12f * s, 19.5f * s)
            cubicTo(7.2f * s, 19.5f * s, 4f * s, 16.8f * s, 4f * s, 12.85f * s)
            cubicTo(4f * s, 11.2f * s, 4.5f * s, 9.6f * s, 6.1f * s, 8.3f * s)
            close()
        }
        drawPath(
            path = catPath,
            color = tint,
            style = Stroke(width = 1.25f * s, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawLine(tint, Offset(9.4f * s, 10.6f * s), Offset(14.6f * s, 15.5f * s), strokeWidth = 1.25f * s, cap = StrokeCap.Round)
        drawLine(tint, Offset(14.6f * s, 10.6f * s), Offset(9.4f * s, 15.5f * s), strokeWidth = 1.25f * s, cap = StrokeCap.Round)
    }
}
