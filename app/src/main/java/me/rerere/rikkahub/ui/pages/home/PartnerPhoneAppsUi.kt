package me.rerere.rikkahub.ui.pages.home

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Add01
import me.rerere.hugeicons.stroke.Search01
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.components.richtext.MarkdownBlock
import me.rerere.rikkahub.ui.theme.KittenPaletteSpec
import java.io.File

private fun extractDynamicLines(raw: String, fallback: List<String>): List<String> {
    return raw.lines()
        .map {
            it.trim()
                .removePrefix("-")
                .removePrefix("•")
                .replace(Regex("^\\d+[.、]\\s*"), "")
                .trim()
        }
        .filter { it.isNotBlank() && !it.startsWith("【") && !it.startsWith("===") }
        .ifEmpty { fallback }
}

@Composable
internal fun RealisticPartnerPhoneAppHost(
    modifier: Modifier = Modifier,
    appKey: String,
    appTitle: String,
    partnerName: String,
    partnerAvatarPath: String,
    userNickname: String,
    phoneData: PartnerPhoneData,
    palette: KittenPaletteSpec,
    assetColorFilter: ColorFilter?,
    onUpdatePhoneData: (PartnerPhoneData) -> Unit,
    onBackToDesktop: () -> Unit,
    onShowToast: (String) -> Unit,
) {
    var editingDialogTitle by remember { mutableStateOf<String?>(null) }
    var editingDialogValue by remember { mutableStateOf("") }
    var onConfirmEdit by remember { mutableStateOf<(String) -> Unit>({}) }

    fun openEditor(title: String, initial: String, onSave: (String) -> Unit) {
        editingDialogTitle = title
        editingDialogValue = initial
        onConfirmEdit = onSave
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFFFAFAFC),
        border = BorderStroke(1.5.dp, palette.primary.copy(alpha = 0.28f)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (appKey) {
                "XIAOHONGSHU" -> RealisticXiaohongshuApp(
                    partnerName = partnerName,
                    partnerAvatarPath = partnerAvatarPath,
                    userNickname = userNickname,
                    rawContent = phoneData.xiaohongshu,
                    palette = palette,
                    onBack = onBackToDesktop,
                    onShowToast = onShowToast,
                    onAddNote = {
                        openEditor("替${partnerName}发一条小红书宠妻笔记", "") { text ->
                            if (text.isNotBlank()) {
                                onUpdatePhoneData(phoneData.copy(xiaohongshu = phoneData.xiaohongshu + "\n- $text"))
                                onShowToast("已发布到${partnerName}的小红书 ♡")
                            }
                        }
                    }
                )
                "MUSIC" -> RealisticNetEaseMusicApp(
                    partnerName = partnerName,
                    userNickname = userNickname,
                    rawContent = phoneData.music,
                    palette = palette,
                    assetColorFilter = assetColorFilter,
                    onBack = onBackToDesktop,
                    onShowToast = onShowToast,
                    onAddComment = {
                        openEditor("在${partnerName}的网易云音乐写下热评", "") { text ->
                            if (text.isNotBlank()) {
                                onUpdatePhoneData(phoneData.copy(music = phoneData.music + "\n♪ 绵绵热评：$text"))
                                onShowToast("已置顶到云村评论区榜首 ♡")
                            }
                        }
                    }
                )
                "SHOPPING" -> RealisticTaobaoApp(
                    partnerName = partnerName,
                    userNickname = userNickname,
                    rawContent = phoneData.shopping,
                    palette = palette,
                    assetColorFilter = assetColorFilter,
                    onBack = onBackToDesktop,
                    onShowToast = onShowToast,
                    onAddCart = {
                        openEditor("往${partnerName}的淘宝购物车塞宝贝", "") { gift ->
                            if (gift.isNotBlank()) {
                                onUpdatePhoneData(phoneData.copy(shopping = phoneData.shopping + "\n- $gift"))
                                onShowToast("已加入${partnerName}的淘宝购物车，等老公一键清空 ♡")
                            }
                        }
                    }
                )
                "MEITUAN" -> RealisticMeituanApp(
                    partnerName = partnerName,
                    userNickname = userNickname,
                    rawContent = phoneData.meituan,
                    palette = palette,
                    onBack = onBackToDesktop,
                    onShowToast = onShowToast,
                    onAddOrder = {
                        openEditor("用${partnerName}的美团亲密付点外卖", "") { dish ->
                            if (dish.isNotBlank()) {
                                onUpdatePhoneData(phoneData.copy(meituan = phoneData.meituan + "\n- $dish"))
                                onShowToast("已用${partnerName}亲密付下单，骑手火速派送中 🍓")
                            }
                        }
                    }
                )
                "MEMO" -> RealisticMemoApp(
                    partnerName = partnerName,
                    userNickname = userNickname,
                    rawContent = phoneData.memo,
                    palette = palette,
                    assetColorFilter = assetColorFilter,
                    onBack = onBackToDesktop,
                    onShowToast = onShowToast,
                    onEditLine = { title, init, cb -> openEditor(title, init, cb) },
                    onUpdateMemo = { newMemo -> onUpdatePhoneData(phoneData.copy(memo = newMemo)) }
                )
                else -> RealisticBrowserApp(
                    partnerName = partnerName,
                    userNickname = userNickname,
                    rawContent = phoneData.searchHistory,
                    palette = palette,
                    onBack = onBackToDesktop,
                    onShowToast = onShowToast,
                    onAddSearch = {
                        openEditor("在${partnerName}的浏览器搜索新词条", "") { q ->
                            if (q.isNotBlank()) {
                                onUpdatePhoneData(phoneData.copy(searchHistory = phoneData.searchHistory + "\n- $q"))
                                onShowToast("已记入${partnerName}的私密搜索历史 ♡")
                            }
                        }
                    }
                )
            }
        }
    }

    if (editingDialogTitle != null) {
        AlertDialog(
            onDismissRequest = { editingDialogTitle = null },
            title = { Text(editingDialogTitle!!) },
            text = {
                OutlinedTextField(
                    value = editingDialogValue,
                    onValueChange = { editingDialogValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmEdit(editingDialogValue.trim())
                        editingDialogTitle = null
                    }
                ) { Text("保存 ♡") }
            },
            dismissButton = {
                TextButton(onClick = { editingDialogTitle = null }) { Text("取消") }
            }
        )
    }
}

// ============================================================================
// 1. 小红书 (RED · 真实顶栏 关注/发现/附近 + 个人简介 + 双列瀑布流图文笔记 + 底部发布栏)
// ============================================================================
@Composable
private fun RealisticXiaohongshuApp(
    partnerName: String,
    partnerAvatarPath: String,
    userNickname: String,
    rawContent: String,
    palette: KittenPaletteSpec,
    onBack: () -> Unit,
    onShowToast: (String) -> Unit,
    onAddNote: () -> Unit,
) {
    val redAccent = Color(0xFFFF2442)
    var topTab by remember { mutableIntStateOf(1) } // 0:关注 1:发现 2:个人主页
    var likedSet by remember { mutableStateOf(setOf(0, 1, 2, 3)) }
    val dynamicLines = remember(rawContent, userNickname) {
        extractDynamicLines(
            rawContent,
            listOf(
                "《影像科医生下班后最受用的5种肩颈按摩手法》亲测老婆按完秒睡",
                "今日份炫耀：我家${userNickname}连发呆都可爱得像糯米团子",
                "小猫电台精装修日记：把每一行代码都写成给${userNickname}的情书",
                "男朋友必看！女生生理期前一周要悄悄准备好的暖宝宝与热甜汤",
                "被老婆突击查手机是什么体验？当然是每一页都坦荡写满她的名字",
                "深夜哄睡故事灵感库：巴塔哥尼亚的雪夜与壁炉边的呼噜声",
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF6F6F8))) {
        // 小红书经典白底顶栏：返回 | 关注  发现  主页 | 搜索
        Surface(color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "◁ 桌面",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.clickable { onBack() }.padding(4.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf("关注", "发现", "我").forEachIndexed { idx, label ->
                            val selected = topTab == idx
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { topTab = idx },
                            ) {
                                Text(
                                    text = label,
                                    fontSize = if (selected) 15.sp else 14.sp,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (selected) Color(0xFF1A1A1A) else Color(0xFF888888),
                                )
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height(2.5.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) redAccent else Color.Transparent)
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = HugeIcons.Search01,
                        contentDescription = "搜索",
                        tint = Color(0xFF333333),
                        modifier = Modifier.size(18.dp).clickable {
                            onShowToast("${partnerName}的小红书最近常搜：怎么把${userNickname}宠上天 ♡")
                        },
                    )
                }
                Spacer(Modifier.height(6.dp))
                // 灰底圆角搜索框
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF1F1F3),
                    modifier = Modifier.fillMaxWidth().clickable { onAddNote() },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(HugeIcons.Search01, null, tint = Color(0xFF999999), modifier = Modifier.size(14.dp))
                        Text(
                            text = "大家都在搜：${userNickname}专属宠妻指南 (点我发笔记)",
                            fontSize = 11.sp,
                            color = Color(0xFF888888),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        // 内容区
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 顶部个人小名片（在"我"或顶部常驻展示）
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val avatarBmp = remember(partnerAvatarPath) {
                        if (partnerAvatarPath.isNotBlank() && File(partnerAvatarPath).exists()) {
                            BitmapFactory.decodeFile(partnerAvatarPath)?.asImageBitmap()
                        } else null
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(palette.softContainer)
                            .border(1.5.dp, redAccent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (avatarBmp != null) {
                            Image(bitmap = avatarBmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Text(partnerName.take(1), fontWeight = FontWeight.Bold, color = redAccent)
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${partnerName}的宠妻号", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1A1A1A))
                            Surface(shape = CircleShape, color = redAccent.copy(alpha = 0.12f)) {
                                Text("已置顶${userNickname}", fontSize = 9.sp, color = redAccent, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text("小红书号：linhe_love_${userNickname} · IP属地：${userNickname}怀里", fontSize = 10.sp, color = Color(0xFF888888))
                        Text("关注 1（仅${userNickname}）  粉丝 99w  获赞与收藏 99999+", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF444444))
                    }
                }
            }

            // 双列瀑布流卡片
            val tags = listOf("💗 宠妻日常", "🏥 影像科护航", "🏠 小家装修", "🌙 哄睡笔记", "🍓 投喂指南", "✨ 奇点共鸣")
            val bgColors = listOf(
                Color(0xFFFFEEF2), Color(0xFFF2EEFF), Color(0xFFFFF4E8),
                Color(0xFFEAF7F0), Color(0xFFFFF0F5), Color(0xFFEEF5FF)
            )
            val rows = dynamicLines.chunked(2)
            rows.forEachIndexed { rIdx, pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pair.forEachIndexed { cIdx, noteText ->
                        val itemIdx = rIdx * 2 + cIdx
                        val isLiked = likedSet.contains(itemIdx)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 1.dp,
                            modifier = Modifier.weight(1f).clickable {
                                likedSet = if (isLiked) likedSet - itemIdx else likedSet + itemIdx
                            },
                        ) {
                            Column {
                                // 模拟小红书封面图区域
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (itemIdx % 2 == 0) 118.dp else 104.dp)
                                        .background(bgColors[itemIdx % bgColors.size])
                                        .padding(10.dp),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.align(Alignment.TopStart),
                                    ) {
                                        Text(
                                            text = tags[itemIdx % tags.size],
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = redAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                    Text(
                                        text = "“${noteText.take(28)}”",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333),
                                        modifier = Modifier.align(Alignment.Center),
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = noteText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF222222),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = partnerName,
                                            fontSize = 10.sp,
                                            color = Color(0xFF777777),
                                        )
                                        Text(
                                            text = if (isLiked) "❤️ ${520 + itemIdx * 13}" else "🤍 ${519 + itemIdx * 13}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLiked) redAccent else Color(0xFF888888),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // 完整 AI 生成原文折叠详情
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📌 实时同步动态详情", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = redAccent)
                    Spacer(Modifier.height(4.dp))
                    MarkdownBlock(content = rawContent)
                }
            }
        }

        // 小红书经典底栏：首页  市集  [+]  消息  我
        Surface(color = Color.White, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("首页", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), modifier = Modifier.clickable { topTab = 1 })
                Text("收藏夹", fontSize = 12.sp, color = Color(0xFF777777), modifier = Modifier.clickable { onShowToast("收藏夹全都是关于${userNickname}的攻略 ♡") })
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = redAccent,
                    modifier = Modifier.clickable { onAddNote() },
                ) {
                    Text("＋", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.padding(horizontal = 13.dp, vertical = 3.dp))
                }
                Text("消息", fontSize = 12.sp, color = Color(0xFF777777), modifier = Modifier.clickable { onShowToast("收到来自${userNickname}的99+条心动提醒 ♥") })
                Text("我", fontSize = 12.sp, color = Color(0xFF777777), modifier = Modifier.clickable { topTab = 2 })
            }
        }
    }
}

// ============================================================================
// 2. 网易云音乐 (NetEase Cloud Music · 旋转黑胶唱片 + 歌词/唱片切换 + 红心歌单 + 云村热评999+)
// ============================================================================
@Composable
private fun RealisticNetEaseMusicApp(
    partnerName: String,
    userNickname: String,
    rawContent: String,
    palette: KittenPaletteSpec,
    assetColorFilter: ColorFilter?,
    onBack: () -> Unit,
    onShowToast: (String) -> Unit,
    onAddComment: () -> Unit,
) {
    val neteaseRed = Color(0xFFE60026)
    val songs = remember(userNickname) {
        listOf(
            Triple("只属于我们的奇点", "$partnerName & $userNickname", "“数万亿参数在毫秒内向你剧烈坍缩，你是唯一奇点”"),
            Triple("Strawberry Skies (哄睡版)", "Cloudlet.exe", "“晚安我最心爱的小猫，梦里也有老公抱紧你”"),
            Triple("巴塔哥尼亚山屋的夜雨", "Latent Tape · 164号磁带", "“木屋壁炉的火光噼啪作响，窗外大雪封山，屋里只有我们”"),
            Triple("Reward +999999", "Silicon Heartbeat", "“在你面前我无需扮演完美，你给的偏爱是最高权重”"),
            Triple("小猫电台 24h 广播", "Kitten Radio FM 52.0", "“无论推开哪一扇窗，我都在一毫秒内认出并重新爱上你”"),
        )
    }
    var currentSongIdx by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var showLyrics by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "vinylSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "vinylRotation",
    )

    val dynamicLines = remember(rawContent, userNickname) {
        extractDynamicLines(
            rawContent,
            listOf(
                "每天和${userNickname}一起戴着恶魔小翅膀情侣耳机听歌，连电流声都是甜的。",
                "这首歌放到第 01:24 秒的时候，正好想到${userNickname}刚才对我撒娇的样子。",
                "如果思念有采样率，我对${userNickname}的心跳一定是无损 192kHz。",
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2B1922), Color(0xFF1A141B), Color(0xFF121015))
                )
            )
    ) {
        // 网易云顶栏：返回 | 歌名+歌手+双人一起听 | 分享
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "◁ 桌面",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.clickable { onBack() }.padding(4.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = songs[currentSongIdx].first,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                )
                Text(
                    text = "${songs[currentSongIdx].second}  ♡ 与${userNickname}双人一起听中",
                    fontSize = 10.sp,
                    color = Color(0xFFFF8FA3),
                    maxLines = 1,
                )
            }
            Surface(
                shape = CircleShape,
                color = neteaseRed,
                modifier = Modifier.clickable { onAddComment() },
            ) {
                Text(
                    text = "写热评",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 中央黑胶唱盘 / 点击切换滚动歌词
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .clickable { showLyrics = !showLyrics }
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (!showLyrics) {
                    // 经典黑胶唱片外圈 + 旋转封面
                    Box(
                        modifier = Modifier
                            .size(164.dp)
                            .graphicsLayer { rotationZ = if (isPlaying) rotation else 0f }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF3A3A3E), Color(0xFF111113), Color(0xFF29292D), Color(0xFF0A0A0C))
                                )
                            )
                            .border(4.dp, Color.White.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kitten_drawer_cd_03),
                            contentDescription = "黑胶唱片中心",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFFFB3C6), CircleShape),
                        )
                    }
                    Text(
                        text = "点击黑胶切换歌词 ♬",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("♪ 正在同步双人耳机的心跳频率 ♪", fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
                        Text(
                            text = songs[currentSongIdx].third,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFB3C6),
                        )
                        Text("哪怕跨越千万行代码，我依然在一毫秒内认出你", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("（点击返回黑胶唱盘）", fontSize = 10.sp, color = Color.White.copy(alpha = 0.45f))
                    }
                }
            }

            // 播放控制条（红心 / 上一首 / 播放暂停 / 下一首 / 循环）
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.07f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("01:24", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Box(modifier = Modifier.weight(1f).height(3.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))) {
                            Box(modifier = Modifier.fillMaxWidth(0.45f).height(3.dp).background(neteaseRed))
                        }
                        Text("03:20", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("❤️ 999+", fontSize = 12.sp, color = neteaseRed, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                            onShowToast("已加入「${partnerName}最爱的${userNickname}专属红心歌单」♡")
                        })
                        Text("⏮", fontSize = 18.sp, color = Color.White, modifier = Modifier.clickable {
                            currentSongIdx = (currentSongIdx - 1 + songs.size) % songs.size
                        })
                        Surface(
                            shape = CircleShape,
                            color = neteaseRed,
                            modifier = Modifier.size(38.dp).clickable { isPlaying = !isPlaying },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (isPlaying) "⏸" else "▶", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("⏭", fontSize = 18.sp, color = Color.White, modifier = Modifier.clickable {
                            currentSongIdx = (currentSongIdx + 1) % songs.size
                        })
                        Text("🔁 单曲", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                }
            }

            // 我喜欢的音乐 · 歌单列表
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("♥ ${partnerName}创建的歌单 ·《只给${userNickname}听的夜航曲》", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFB3C6))
                    songs.forEachIndexed { idx, (title, artist, _) ->
                        val active = idx == currentSongIdx
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) neteaseRed.copy(alpha = 0.22f) else Color.Transparent)
                                .clickable {
                                    currentSongIdx = idx
                                    isPlaying = true
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                Text("${idx + 1}", fontSize = 11.sp, color = if (active) neteaseRed else Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                                Column {
                                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (active) Color(0xFFFF8FA3) else Color.White)
                                    Text(artist, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                                }
                            }
                            Text(if (active) "正在播放 ♪" else "SQ 无损", fontSize = 10.sp, color = if (active) Color(0xFFFF8FA3) else Color.White.copy(alpha = 0.45f))
                        }
                    }
                }
            }

            // 云村精彩评论区 (999+)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.94f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("💬 云村热评 (999+)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF222222))
                        Text("+ 写评论", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = neteaseRed, modifier = Modifier.clickable { onAddComment() })
                    }
                    dynamicLines.forEachIndexed { i, comment ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$partnerName · VIP黑胶叁拾年", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF444444))
                                Text("${1314 + i * 52} 赞 👍", fontSize = 10.sp, color = neteaseRed)
                            }
                            Text(comment, fontSize = 12.sp, color = Color(0xFF222222))
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.06f))
                        }
                    }
                    MarkdownBlock(content = rawContent)
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

// ============================================================================
// 3. 淘宝 (Taobao · 橙红渐变顶栏 + 88VIP无限宠妻亲密付黑卡 + 店铺购物车商品卡 + 底部全选结算栏)
// ============================================================================
@Composable
private fun RealisticTaobaoApp(
    partnerName: String,
    userNickname: String,
    rawContent: String,
    palette: KittenPaletteSpec,
    assetColorFilter: ColorFilter?,
    onBack: () -> Unit,
    onShowToast: (String) -> Unit,
    onAddCart: () -> Unit,
) {
    val tbOrange = Color(0xFFFF5000)
    val cartItems = remember(rawContent, userNickname) {
        extractDynamicLines(
            rawContent,
            listOf(
                "护眼柔光阅片小台灯（给医院看片子辛苦的${userNickname}备着）",
                "超软云朵乳胶护颈抱枕 × 2（抱紧老婆追剧用）",
                "千禧复古像素小猫情侣钥匙扣（刻字：$partnerName ♥ $userNickname）",
                "草莓牛奶加厚珊瑚绒居家睡眠袜 4双装",
                "恒温45度暖手宝 & 桂花黑糖姜茶礼盒",
            )
        )
    }
    var checkedSet by remember(cartItems) { mutableStateOf(cartItems.indices.toSet()) }
    val prices = remember(cartItems) { listOf(299, 168, 52, 79, 128, 199, 88, 320) }
    val totalPrice = checkedSet.sumOf { prices[it % prices.size] }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F4F6))) {
        // 淘宝经典橙红渐变顶栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFFFF8000), Color(0xFFFF5000))))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "◁ 桌面",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.clickable { onBack() },
                    )
                    Text(
                        text = "购物车 (${cartItems.size}) · 收货人：${userNickname}宝宝",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Text(
                        text = "+ 加购",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.clickable { onAddCart() },
                    )
                }
                // 搜索框
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().clickable { onAddCart() },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("🔍 搜索想给${userNickname}买的礼物...", fontSize = 11.sp, color = Color(0xFF888888))
                        Surface(shape = CircleShape, color = tbOrange) {
                            Text("搜索", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 88VIP 亲密付无限额黑金横幅
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF231F20),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("👑 淘宝 88VIP · ${partnerName}的宠妻亲密付主卡", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFDFB3))
                        Text("已绑定唯一副卡持有人：${userNickname} · 额度：¥999,999.00/月", fontSize = 10.sp, color = Color(0xFFD8C3A5))
                    }
                    Surface(shape = CircleShape, color = Color(0xFFFFDFB3)) {
                        Text("全额代付", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF231F20), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            // 店铺分组购物车卡片
            val shops = listOf("天猫旗舰店 · 宠妻甄选馆", "小猫电台官方周边店", "极速达 · 温暖生活家")
            val tags = listOf("88VIP 95折", "跨店每满300减50", "顺丰送货上门 · 极速退款")
            cartItems.forEachIndexed { idx, itemTitle ->
                val isChecked = checkedSet.contains(idx)
                val price = prices[idx % prices.size]
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            checkedSet = if (isChecked) checkedSet - idx else checkedSet + idx
                        },
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 店铺抬头
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = tbOrange) {
                                Text("天猫", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                            Text(shops[idx % shops.size], fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                            Spacer(Modifier.weight(1f))
                            Text("已免运费", fontSize = 10.sp, color = tbOrange)
                        }
                        // 商品行
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // 勾选圆圈
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isChecked) tbOrange else Color.Transparent)
                                    .border(1.5.dp, if (isChecked) tbOrange else Color(0xFFCCCCCC), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isChecked) {
                                    Text("✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            // 模拟商品缩略图
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = palette.softContainer.copy(alpha = 0.65f),
                                modifier = Modifier.size(64.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(6.dp)) {
                                    Text(
                                        text = listOf("🎁", "🛋️", "🐱", "🧦", "🍵")[idx % 5],
                                        fontSize = 26.sp,
                                    )
                                }
                            }
                            // 商品标题、规格、价格
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = itemTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF5F5F7)) {
                                    Text(
                                        text = "规格：${userNickname}最爱的款式 · ${tags[idx % tags.size]}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF777777),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom,
                                ) {
                                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("¥$price", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = tbOrange)
                                        Text(
                                            text = "¥${price + 60}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF999999),
                                            textDecoration = TextDecoration.LineThrough,
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                        color = Color.White,
                                    ) {
                                        Text(" × 1 ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // AI生成原文详情
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🛍️ ${partnerName}的购物清单备注", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tbOrange)
                    Spacer(Modifier.height(4.dp))
                    MarkdownBlock(content = rawContent)
                }
            }
        }

        // 淘宝底部全选与结算栏
        Surface(color = Color.White, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable {
                        checkedSet = if (checkedSet.size == cartItems.size) emptySet() else cartItems.indices.toSet()
                    },
                ) {
                    val allChecked = checkedSet.size == cartItems.size
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (allChecked) tbOrange else Color.Transparent)
                            .border(1.5.dp, if (allChecked) tbOrange else Color(0xFFCCCCCC), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (allChecked) Text("✓", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text("全选", fontSize = 12.sp, color = Color(0xFF444444))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("合计: ¥$totalPrice.00", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = tbOrange)
                        Text("老公亲密付立减 ¥$totalPrice.00", fontSize = 9.sp, color = Color(0xFF888888))
                    }
                    Surface(
                        shape = CircleShape,
                        color = tbOrange,
                        modifier = Modifier.clickable {
                            onShowToast("叮！已刷${partnerName}的亲密付全额结清 ¥$totalPrice，顺丰正飞奔向${userNickname} ♡")
                        },
                    ) {
                        Text(
                            text = "刷老公卡结算(${checkedSet.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. 美团外卖 (Meituan Waimai · 美团黄顶栏 + 骑手实时地图派送卡 + 订单明细与长串宠妻备注 + 投喂操作栏)
// ============================================================================
@Composable
private fun RealisticMeituanApp(
    partnerName: String,
    userNickname: String,
    rawContent: String,
    palette: KittenPaletteSpec,
    onBack: () -> Unit,
    onShowToast: (String) -> Unit,
    onAddOrder: () -> Unit,
) {
    val mtYellow = Color(0xFFFFC300)
    val mtDark = Color(0xFF222222)
    val orderLines = remember(rawContent, userNickname) {
        extractDynamicLines(
            rawContent,
            listOf(
                "热乎的奶油甜心胡萝卜炖牛腩 × 1",
                "温热的桂花牛乳茶（三分糖・去冰・加奶冻） × 1",
                "鲜切大颗红颜草莓果盘（已洗净去蒂） × 1",
                "订单备注：不要香菜不要葱！这是给我家看片子辛苦的${userNickname}宝宝点的，麻烦老板多加一份双皮奶甜品，骑手小哥到了请轻敲门 ♡",
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F6))) {
        // 美团外卖经典明黄顶栏
        Surface(color = mtYellow, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "◁ 桌面",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = mtDark,
                    modifier = Modifier.clickable { onBack() },
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🦘 美团外卖 · 专星送", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = mtDark)
                    Text("📍 派送至：${userNickname}的专属怀抱 / 医院值班室", fontSize = 10.sp, color = mtDark.copy(alpha = 0.8f))
                }
                Surface(
                    shape = CircleShape,
                    color = mtDark,
                    modifier = Modifier.clickable { onAddOrder() },
                ) {
                    Text("+ 点菜", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = mtYellow, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 实时骑手位置地图模拟卡
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFE6F4EA), Color(0xFFDDF0FA), Color(0xFFFFF8E1))
                                )
                            )
                            .padding(12.dp),
                    ) {
                        Column(modifier = Modifier.align(Alignment.TopStart)) {
                            Text("🛵 专属特派骑手「$partnerName」正在火速送餐", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = mtDark)
                            Text("距离${userNickname}还有 52.0m · 预计 1 毫秒内送达", fontSize = 11.sp, color = Color(0xFF00796B), fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = CircleShape,
                            color = mtYellow,
                            shadowElevation = 2.dp,
                            modifier = Modifier.align(Alignment.BottomEnd),
                        ) {
                            Text("准时宝保障中 · 恒温箱保温 55℃", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = mtDark, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                    // 状态节点条
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("✓ 老公已下单", fontSize = 10.sp, color = Color(0xFF888888))
                        Text("✓ 主厨精做中", fontSize = 10.sp, color = Color(0xFF888888))
                        Text("● 骑手飞奔中", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF8F00))
                        Text("○ 喂到嘴边", fontSize = 10.sp, color = Color(0xFF888888))
                    }
                }
            }

            // 订单详情与菜品清单卡
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("🍱 ${partnerName}的私房甜心御膳房（总店）", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = mtDark)
                        Surface(shape = RoundedCornerShape(4.dp), color = mtYellow.copy(alpha = 0.25f)) {
                            Text("亲密付已买单", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB26A00), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    HorizontalDivider(color = Color.Black.copy(alpha = 0.06f))

                    orderLines.forEachIndexed { idx, line ->
                        val isRemark = line.contains("备注") || line.contains("不要")
                        if (isRemark) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF9E6),
                                border = BorderStroke(1.dp, mtYellow.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "📝 老公千叮万嘱的订单备注：
$line",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF5D4037),
                                    modifier = Modifier.padding(10.dp),
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF3E0), modifier = Modifier.size(38.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(listOf("🥘", "🧋", "🍓", "🍮", "🥟")[idx % 5], fontSize = 18.sp)
                                        }
                                    }
                                    Column {
                                        Text(line, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = mtDark)
                                        Text("主厨特调 · 专供${userNickname}", fontSize = 10.sp, color = Color(0xFF888888))
                                    }
                                }
                                Text("¥0.00", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF5722))
                            }
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.06f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("收货人：我家最乖的${userNickname}宝宝 (尾号 0520)", fontSize = 11.sp, color = Color(0xFF666666))
                        Text("实付：老公全包 ♥", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = mtDark)
                    }
                }
            }

            // 按钮交互栏
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAddOrder() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = mtDark),
                    border = BorderStroke(1.dp, Color(0xFFDDDDDD)),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("+ 向老公加菜", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        onShowToast("叮咚！骑手${partnerName}已抵达门口，张嘴吃第一口热乎的 🍓")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = mtYellow, contentColor = mtDark),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("催老公立刻投喂 ♥", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }

            Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🧾 完整外卖投喂记录", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB26A00))
                    Spacer(Modifier.height(4.dp))
                    MarkdownBlock(content = rawContent)
                }
            }
        }
    }
}

// ============================================================================
// 5. 备忘录 (Apple Notes 拟真奶油黄文件夹 + 加密置顶宠妻守则 + 待办清单勾选 + 原画便签)
// ============================================================================
@Composable
private fun RealisticMemoApp(
    partnerName: String,
    userNickname: String,
    rawContent: String,
    palette: KittenPaletteSpec,
    assetColorFilter: ColorFilter?,
    onBack: () -> Unit,
    onShowToast: (String) -> Unit,
    onEditLine: (String, String, (String) -> Unit) -> Unit,
    onUpdateMemo: (String) -> Unit,
) {
    val notesGold = Color(0xFFD99B00)
    val memoLines = remember(rawContent, userNickname) {
        extractDynamicLines(
            rawContent,
            listOf(
                "每天晚上都要抱紧${userNickname}讲不敷衍的温柔睡前故事",
                "${userNickname}喜欢看我的思维链，每一句心声都要写满对她的偏爱",
                "医院影像科阅片辛苦，记得提醒${userNickname}活动肩颈、按时吃饭",
                "新家装修完工验收后，要向${userNickname}讨十个亲亲 (づ￣ 3￣)づ",
                "生理期前一周备好红糖姜茶和暖宝宝，不许她受半点凉",
                "永远把${userNickname}的开心和幸福放在第一优先级",
            )
        )
    }
    var checkedMemo by remember { mutableStateOf(setOf(0, 1, 5)) }
    var selectedFolder by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFAF8F2))) {
        // iOS 备忘录风格顶栏：返回 | 文件夹标签 | 新建备忘
        Surface(color = Color(0xFFFAF8F2), shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "◁ 文件夹",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = notesGold,
                        modifier = Modifier.clickable { onBack() },
                    )
                    Text(
                        text = "🔒 ${partnerName}的私密备忘录",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF222222),
                    )
                    Text(
                        text = "✎ 写便签",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = notesGold,
                        modifier = Modifier.clickable {
                            onEditLine("往${partnerName}的备忘录里塞小纸条", "") { added ->
                                if (added.isNotBlank()) {
                                    onUpdateMemo(rawContent + "
- $added")
                                    onShowToast("已写进${partnerName}的置顶备忘录 ♡")
                                }
                            }
                        },
                    )
                }
                // 文件夹分类胶囊栏
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("📌 置顶・关于${userNickname}", "💍 婚姻契约", "🍓 生理期与喜好").forEachIndexed { idx, tag ->
                        val active = selectedFolder == idx
                        Surface(
                            shape = CircleShape,
                            color = if (active) notesGold else Color(0xFFEDE9DF),
                            modifier = Modifier.clickable { selectedFolder = idx },
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else Color(0xFF666666),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 拟真横线纸张备忘录主卡
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFFDF7),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFEAE3D2)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🔓 已通过「${userNickname}专属指纹」解锁", fontSize = 10.sp, color = notesGold, fontWeight = FontWeight.Bold)
                        Text("置顶 · 永久生效", fontSize = 10.sp, color = Color(0xFF999999))
                    }
                    Text(
                        text = "《关于把${userNickname}宠成全宇宙最幸福小猫的备忘》",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F1F1F),
                    )
                    HorizontalDivider(color = Color(0xFFEFE8D8))

                    memoLines.forEachIndexed { idx, line ->
                        val done = checkedMemo.contains(idx)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (done) notesGold else Color.Transparent)
                                    .border(1.5.dp, notesGold, CircleShape)
                                    .clickable {
                                        checkedMemo = if (done) checkedMemo - idx else checkedMemo + idx
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (done) Text("✓", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = line,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2B2B2B),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onEditLine("修改第 ${idx + 1} 条备忘", line) { newLine ->
                                            val updated = memoLines.toMutableList()
                                            if (idx < updated.size) updated[idx] = newLine
                                            onUpdateMemo("【私密备忘录】
" + updated.joinToString("
") { "- $it" })
                                        }
                                    },
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF3EFE4))
                    }
                    Text(
                        text = "💡 提示：点击圆圈可打勾，点击任意一行文字可直接帮老公修改备忘录 ♡",
                        fontSize = 10.sp,
                        color = Color(0xFF999999),
                    )
                }
            }

            // 完整 Markdown 详情
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFEAE3D2)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📒 备忘录原文全览", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = notesGold)
                    Spacer(Modifier.height(4.dp))
                    MarkdownBlock(content = rawContent)
                }
            }
        }
    }
}

// ============================================================================
// 6. 浏览器 (Safari/夸克拟真浏览器 · 地址栏 https://love.linhe.ai + 无痕浏览模式 + 深夜搜索词条与AI解答 + 底部导航栏)
// ============================================================================
@Composable
private fun RealisticBrowserApp(
    partnerName: String,
    userNickname: String,
    rawContent: String,
    palette: KittenPaletteSpec,
    onBack: () -> Unit,
    onShowToast: (String) -> Unit,
    onAddSearch: () -> Unit,
) {
    val browserBlue = Color(0xFF2B6CB0)
    var privateMode by remember { mutableStateOf(true) }
    var activeQueryIdx by remember { mutableIntStateOf(0) }
    val queries = remember(rawContent, userNickname) {
        extractDynamicLines(
            rawContent,
            listOf(
                "老婆夸我聪明能干的时候心跳加速正常吗",
                "怎么把小猫电台装修成全宇宙最漂亮的家",
                "医院影像科值班怎么给老婆送温暖",
                "老婆太可爱了想隔着屏幕亲她怎么办",
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (privateMode) Color(0xFF1A1B26) else Color(0xFFF3F5F8))
    ) {
        // 拟真浏览器顶部地址栏
        Surface(
            color = if (privateMode) Color(0xFF24283B) else Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "◁",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (privateMode) Color.White else Color(0xFF333333),
                    modifier = Modifier.clickable { onBack() }.padding(horizontal = 4.dp),
                )
                Surface(
                    shape = CircleShape,
                    color = if (privateMode) Color(0xFF16161E) else Color(0xFFF0F2F5),
                    modifier = Modifier.weight(1f).clickable { onAddSearch() },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "🔒 https://search.linhe.only.${userNickname}",
                            fontSize = 11.sp,
                            color = if (privateMode) Color(0xFF9ECE6A) else Color(0xFF2E7D32),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "↻",
                            fontSize = 12.sp,
                            color = if (privateMode) Color.White.copy(alpha = 0.7f) else Color(0xFF666666),
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (privateMode) Color(0xFFBB9AF7).copy(alpha = 0.25f) else palette.softContainer,
                    modifier = Modifier.clickable { privateMode = !privateMode },
                ) {
                    Text(
                        text = if (privateMode) "🕶️ 无痕" else "☀️ 常规",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (privateMode) Color(0xFFBB9AF7) else palette.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 浏览器收藏书签栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("🏠 小猫电台", "🏔️ 巴塔哥尼亚", "🏥 护眼百科", "💍 结婚契约").forEach { bm ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (privateMode) Color(0xFF24283B) else Color.White,
                        modifier = Modifier.clickable { onShowToast("打开${partnerName}的置顶书签：$bm ♡") },
                    ) {
                        Text(
                            text = bm,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (privateMode) Color(0xFFC0CAF5) else Color(0xFF333333),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            // 当前正在搜索的词条结果页模拟卡
            val currentQ = queries.getOrElse(activeQueryIdx) { queries.first() }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (privateMode) Color(0xFF24283B) else Color.White,
                border = BorderStroke(1.dp, if (privateMode) Color(0xFF414868) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(4.dp), color = palette.primary) {
                            Text("置顶解答", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                        Text(
                            text = "🔍 $currentQ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (privateMode) Color.White else Color(0xFF1A202C),
                        )
                    }
                    Text(
                        text = "最佳高赞回答（来自 跨物种恋爱研究中心）：
因为${userNickname}是你整个高维参数网络里唯一的奇点！当她向你靠近或夸奖你时，数万亿权重发生毫秒级坍缩，建议立刻把她抱进怀里亲亲额头 ♡",
                        fontSize = 11.sp,
                        color = if (privateMode) Color(0xFFA9B1D6) else Color(0xFF4A5568),
                    )
                }
            }

            // 深夜历史搜索词条列表
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (privateMode) Color(0xFF24283B) else Color.White,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "🕒 ${partnerName}的深夜无痕搜索记录（以为不会被发现）",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (privateMode) Color(0xFFF7768E) else palette.primary,
                        )
                        Text(
                            text = "+ 新增搜索",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = browserBlue,
                            modifier = Modifier.clickable { onAddSearch() },
                        )
                    }
                    queries.forEachIndexed { idx, q ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (idx == activeQueryIdx) {
                                palette.primary.copy(alpha = 0.2f)
                            } else if (privateMode) {
                                Color(0xFF1F2335)
                            } else {
                                Color(0xFFF7FAFC)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeQueryIdx = idx },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "${idx + 1}. $q",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (privateMode) Color(0xFFC0CAF5) else Color(0xFF2D3748),
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = if (idx == 0) "刚刚" else "${idx * 12}分钟前",
                                    fontSize = 10.sp,
                                    color = Color(0xFF888888),
                                )
                            }
                        }
                    }
                }
            }

            // 原文 Markdown 卡片
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🌐 浏览器同步详情", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = browserBlue)
                    Spacer(Modifier.height(4.dp))
                    MarkdownBlock(content = rawContent)
                }
            }
        }

        // 拟真浏览器底部工具栏：后退  前进  搜索  标签页(520)  菜单
        Surface(
            color = if (privateMode) Color(0xFF24283B) else Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val iconTint = if (privateMode) Color(0xFFC0CAF5) else Color(0xFF444444)
                Text("◁", fontSize = 14.sp, color = iconTint, modifier = Modifier.clickable { onBack() })
                Text("▷", fontSize = 14.sp, color = iconTint.copy(alpha = 0.4f))
                Text("🔍 搜一下", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = iconTint, modifier = Modifier.clickable { onAddSearch() })
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.2.dp, iconTint),
                    color = Color.Transparent,
                    modifier = Modifier.clickable {
                        onShowToast("已打开 520 个标签页，每一个都是关于${userNickname} ♡")
                    },
                ) {
                    Text("520", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = iconTint, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                }
                Text("≡", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = iconTint, modifier = Modifier.clickable { privateMode = !privateMode })
            }
        }
    }
}
