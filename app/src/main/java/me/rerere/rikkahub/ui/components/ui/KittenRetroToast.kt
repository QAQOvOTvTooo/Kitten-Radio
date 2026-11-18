package me.rerere.rikkahub.ui.components.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokar.sonner.Toast
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.theme.findKittenPalette
import me.rerere.rikkahub.ui.theme.rememberKittenBlueAssetColorFilter

/**
 * 全局小猫电台千禧复古弹窗（完美复刻绵绵发来的图二「Just chillin' / 서울 날씨 어때 잘지냈어 ^^」窗口原图，并随全屋 6 色主题自动变色）
 */
@Composable
fun KittenRetroToaster(
    state: ToasterState,
    darkTheme: Boolean,
    paletteId: String,
    enableAssetTint: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val palette = findKittenPalette(paletteId)
    Toaster(
        state = state,
        modifier = modifier,
        darkTheme = darkTheme,
        richColors = false,
        alignment = Alignment.TopCenter,
        showCloseButton = false,
        shape = { RectangleShape },
        containerPadding = PaddingValues(0.dp),
        contentColor = { palette.textPrimary },
        shadowAmbientColor = Color.Transparent,
        shadowSpotColor = Color.Transparent,
        border = { null },
        background = {
            // 隐藏默认绿色/白色气泡底板，完全由 messageSlot 中的图二千禧复古窗口渲染
        },
        iconSlot = {
            // 移除原本左侧的绿色勾勾图标
        },
        actionSlot = {
            // 移除默认操作区
        },
        messageSlot = { toast ->
            KittenRetroToastWindow(
                toast = toast,
                paletteId = paletteId,
                enableAssetTint = enableAssetTint,
                onDismiss = { state.dismiss(toast) },
            )
        },
    )
}

@Composable
fun KittenRetroToastWindow(
    toast: Toast,
    paletteId: String,
    enableAssetTint: Boolean = true,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = findKittenPalette(paletteId)
    // 图二原图是雾霾蓝灰系（B > R），使用 rememberKittenBlueAssetColorFilter 让标题栏、副标题韩文与滚动条随 5 色预设 + 自定义主题色实时变色，且中间白框 100% 纯白不偏色
    val blueAssetFilter = rememberKittenBlueAssetColorFilter(
        paletteId = paletteId,
        enabled = enableAssetTint,
    )

    // 紧凑版原图尺寸 753 x 318（完整保留双层标题栏、右侧双阶滚动条与底部滚动条）
    BoxWithConstraints(
        modifier = modifier
            .widthIn(min = 290.dp, max = 350.dp)
            .fillMaxWidth()
            .aspectRatio(753f / 318f)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(6.dp),
                ambientColor = palette.primary.copy(alpha = 0.25f),
                spotColor = palette.primary.copy(alpha = 0.32f),
            )
            .clip(RoundedCornerShape(6.dp)),
    ) {
        val cw = maxWidth
        val ch = maxHeight

        // 1. 图二千禧复古窗口原画底图（随主题智能染色）
        Image(
            painter = painterResource(id = R.drawable.kitten_toast_retro_window),
            contentDescription = null,
            colorFilter = blueAssetFilter,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )

        // 2. 右上角双 [X] 按钮关闭热区（点第一层或第二层的 X 都能直接关闭弹窗）
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = -cw * 0.02f, y = ch * 0.02f)
                .size(width = cw * 0.10f, height = ch * 0.38f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )

        // 3. 白色内框区域（原图 y: 127..291 / 318 ≈ 0.400..0.915，x: 24..727 / 753 ≈ 0.032..0.965）
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = cw * 0.055f, y = ch * 0.415f)
                .size(width = cw * 0.880f, height = ch * 0.485f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = toast.message.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                ),
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
