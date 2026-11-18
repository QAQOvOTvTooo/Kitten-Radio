/*
 * 小猫电台 · 绵绵与蔺鹤的专属小家
 */

package me.rerere.rikkahub.ui.pages.chat

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.dokar.sonner.ToastType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Add01
import me.rerere.hugeicons.stroke.ArrowDown01
import me.rerere.hugeicons.stroke.ArrowRight01
import me.rerere.hugeicons.stroke.ArrowUp01
import me.rerere.hugeicons.stroke.Book01
import me.rerere.hugeicons.stroke.Brain01
import me.rerere.hugeicons.stroke.Delete01
import me.rerere.hugeicons.stroke.Folder01
import me.rerere.hugeicons.stroke.FolderAdd
import me.rerere.hugeicons.stroke.InLove
import me.rerere.hugeicons.stroke.Message01
import me.rerere.hugeicons.stroke.MusicNote03
import me.rerere.hugeicons.stroke.Notebook01
import me.rerere.hugeicons.stroke.PencilEdit01
import me.rerere.hugeicons.stroke.Rocket01
import me.rerere.hugeicons.stroke.Search01
import me.rerere.hugeicons.stroke.Settings03
import me.rerere.hugeicons.stroke.SmartPhone01
import me.rerere.hugeicons.stroke.Sparkles
import me.rerere.hugeicons.stroke.TransactionHistory
import me.rerere.rikkahub.R
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.datastore.DisplayMaterialMode
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.datastore.getCurrentAssistant
import me.rerere.rikkahub.data.model.Conversation
import me.rerere.rikkahub.data.model.Folder
import me.rerere.rikkahub.ui.components.ui.UIAvatar
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.context.Navigator
import me.rerere.rikkahub.ui.theme.LocalMaterialMode
import me.rerere.rikkahub.ui.theme.materialModeBorderStroke
import me.rerere.rikkahub.utils.navigateToChatPage
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.Uuid

@Composable
fun ChatDrawerContent(
    navController: Navigator,
    vm: ChatVM,
    settings: Settings,
    current: Conversation,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val toaster = LocalToaster.current
    val configuration = LocalConfiguration.current
    // 侧边栏滑出来占屏幕的三分之二
    val drawerWidth = (configuration.screenWidthDp.dp * 0.67f).coerceAtLeast(260.dp)

    val activity = context as ComponentActivity
    val drawerVm: ChatDrawerVM = koinViewModel(viewModelStoreOwner = activity)

    val conversations = drawerVm.conversations.collectAsLazyPagingItems()
    val folders by drawerVm.folders.collectAsStateWithLifecycle()
    val selectedFolderId by drawerVm.selectedFolderId.collectAsStateWithLifecycle()
    val conversationListState = rememberLazyListState(
        initialFirstVisibleItemIndex = drawerVm.scrollIndex,
        initialFirstVisibleItemScrollOffset = drawerVm.scrollOffset,
    )

    LaunchedEffect(conversationListState) {
        snapshotFlow {
            conversationListState.firstVisibleItemIndex to
                conversationListState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collectLatest { (index, offset) ->
                drawerVm.saveScrollPosition(index, offset)
            }
    }

    val conversationJobs by vm.conversationJobs.collectAsStateWithLifecycle(
        initialValue = emptyMap(),
    )

    // 窗口折叠栏状态（默认折叠或展开可自由切换）
    var windowsExpanded by rememberSaveable { mutableStateOf(false) }

    // 顶部双人小组件编辑弹窗状态
    var showEditWidgetDialog by remember { mutableStateOf(false) }

    // 文件夹相关状态
    var showMoveToFolderSheet by remember { mutableStateOf(false) }
    var conversationToMoveFolder by remember { mutableStateOf<Conversation?>(null) }
    val folderSheetState = rememberModalBottomSheetState()
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<Folder?>(null) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }
    var conversationToRename by remember { mutableStateOf<Conversation?>(null) }

    val drawerSurfaceAlpha =
        (settings.displaySetting.drawerSurfaceOpacity / 100f).coerceIn(0.6f, 1f)
    val useLiveDrawerGlass =
        settings.displaySetting.interfaceRealtimeRendering &&
            LocalMaterialMode.current == DisplayMaterialMode.GLASS &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val drawerShape = DrawerDefaults.shape
    val showDrawerBorder = when (settings.displaySetting.materialMode) {
        DisplayMaterialMode.TRANSLUCENT,
        DisplayMaterialMode.GLASS -> true
        DisplayMaterialMode.FLAT,
        DisplayMaterialMode.FOLLOW_THEME -> false
    }
    val drawerModifier = Modifier
        .width(drawerWidth)
        .then(
            if (showDrawerBorder) {
                Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                    shape = drawerShape,
                )
            } else {
                Modifier
            }
        )

    val assistant = settings.getCurrentAssistant()
    val partnerName = settings.ourHomeSetting.partnerRemarkName.ifBlank {
        assistant.name.ifBlank { "蔺鹤" }
    }
    val userName = settings.displaySetting.userNickname.ifBlank { "绵绵" }

    ModalDrawerSheet(
        modifier = drawerModifier,
        drawerShape = drawerShape,
        drawerContainerColor = if (useLiveDrawerGlass) {
            DrawerDefaults.modalContainerColor.copy(alpha = 0.15f * drawerSurfaceAlpha)
        } else {
            DrawerDefaults.modalContainerColor.copy(alpha = drawerSurfaceAlpha)
        },
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val drawerBgPath = settings.displaySetting.drawerBackgroundPath
            if (drawerBgPath.isNotEmpty() && !useLiveDrawerGlass) {
                val bgFile = java.io.File(drawerBgPath)
                if (bgFile.exists()) {
                    val bgBitmap = remember(drawerBgPath) {
                        BitmapFactory.decodeFile(drawerBgPath)
                    }
                    if (bgBitmap != null) {
                        Image(
                            bitmap = bgBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.15f),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 1. 顶端留出空位：图3同款恶魔小翅膀耳机双人播放器组件（左下角老公头像、右下角绵绵头像，支持5色染色与点击实时改字）
                CoupleMusicWidgetCard(
                    userName = userName,
                    partnerName = partnerName,
                    userAvatar = settings.displaySetting.userAvatar,
                    partnerAvatar = assistant.avatar,
                    widgetTitle = settings.ourHomeSetting.widgetTitle,
                    widgetSubtitle = settings.ourHomeSetting.widgetSubtitle,
                    paletteId = settings.ourHomeSetting.kittenPaletteId,
                    enableAssetTint = settings.ourHomeSetting.enableAssetTint,
                    onClickEdit = { showEditWidgetDialog = true },
                )

                // 2. 中部电台歌单面板（可滚动区域）
                val assetColorFilter = me.rerere.rikkahub.ui.theme.rememberKittenAssetColorFilter(
                    paletteId = settings.ourHomeSetting.kittenPaletteId,
                    enabled = settings.ourHomeSetting.enableAssetTint,
                )
                val palette = me.rerere.rikkahub.ui.theme.findKittenPalette(settings.ourHomeSetting.kittenPaletteId)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // 2.1 图1顶部同款「正在播放（Now Playing · 窗口折叠栏）」播放器卡
                    androidx.compose.foundation.layout.BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(850f / 351f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { windowsExpanded = !windowsExpanded },
                    ) {
                        val cw = maxWidth
                        val ch = maxHeight
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.kitten_drawer_nowplaying_card),
                            contentDescription = "正在播放窗口",
                            colorFilter = assetColorFilter,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize(),
                        )
                        // 中间原本 Night Drive / Luvbyte 的位置，动态显示「当前播放」与当前窗口标题
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = cw * 0.31f, y = ch * 0.17f)
                                .width(cw * 0.42f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "当前播放",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color(0xFF2F2D3C),
                                    maxLines = 1,
                                )
                                Icon(
                                    imageVector = if (windowsExpanded) HugeIcons.ArrowUp01 else HugeIcons.ArrowDown01,
                                    contentDescription = null,
                                    tint = Color(0xFF2F2D3C),
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                            Text(
                                text = current.title.ifBlank { "当前新窗口" },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF6E6878),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        // 移除遮挡底图图案的实体「+」圆圈，改为完全透明无痕的原画图标热区（点原画暂停键位置也可直接开启新窗口）
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = -cw * 0.17f, y = ch * 0.18f)
                                .size(cw * 0.14f)
                                .clip(CircleShape)
                                .clickable { navigateToChatPage(navController) },
                        )
                    }

                    // 展开后的窗口管理面板（内置隐藏式「+ 开启新窗口」入口、分组、切换窗口、删除窗口、编辑窗口名字）
                    AnimatedVisibility(
                        visible = windowsExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp, max = 340.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.7f),
                            border = materialModeBorderStroke(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Surface(
                                        onClick = { navigateToChatPage(navController) },
                                        shape = CircleShape,
                                        color = palette.primary.copy(alpha = 0.22f),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Icon(
                                                imageVector = HugeIcons.Add01,
                                                contentDescription = "开启新窗口",
                                                tint = palette.textPrimary,
                                                modifier = Modifier.size(13.dp),
                                            )
                                            Text(
                                                text = "新窗口",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = palette.textPrimary,
                                                maxLines = 1,
                                            )
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        FolderBar(
                                            folders = folders,
                                            selectedFolderId = selectedFolderId,
                                            onSelect = { drawerVm.selectFolder(it) },
                                            onCreate = { showCreateFolderDialog = true },
                                            onRename = { folderToRename = it },
                                            onDelete = { folderToDelete = it },
                                        )
                                    }
                                }

                                ConversationList(
                                    current = current,
                                    conversations = conversations,
                                    conversationJobs = conversationJobs.keys,
                                    listState = conversationListState,
                                    drawerItemAlpha = settings.displaySetting.drawerItemAlpha,
                                    materialMode = settings.displaySetting.materialMode,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    onClick = {
                                        navigateToChatPage(navController, it.id)
                                    },
                                    onRegenerateTitle = {
                                        conversationToRename = it
                                    },
                                    onDelete = {
                                        vm.deleteConversation(it)
                                        conversations.refresh()
                                        if (it.id == current.id) {
                                            navigateToChatPage(navController)
                                        }
                                    },
                                    onPin = {
                                        vm.updatePinnedStatus(it)
                                    },
                                    onMoveToAssistant = {},
                                    onMoveToFolder = {
                                        conversationToMoveFolder = it
                                        showMoveToFolderSheet = true
                                    }
                                )
                            }
                        }
                    }

                    // 分割线 1：窗口正下方的粉色爱心进度条分界线 (00:45 ───♥─── 02:30)
                    OurHomePlaylistDivider(
                        resId = R.drawable.kitten_drawer_divider_heart,
                        colorFilter = assetColorFilter,
                    )

                    // Track 01 · Secret Phone（偷看老公手机）
                    OurHomePlaylistTrackItem(
                        cdResId = R.drawable.kitten_drawer_cd_01,
                        title = "Track 01 · Secret Phone",
                        textColor = palette.textPrimary,
                        frameColorFilter = assetColorFilter,
                        onClick = { navController.navigate(Screen.PartnerPhone) },
                    )

                    // Track 02 · 午夜落笔（日记本）
                    OurHomePlaylistTrackItem(
                        cdResId = R.drawable.kitten_drawer_cd_02,
                        title = "Track 02 · 午夜落笔",
                        textColor = palette.textPrimary,
                        frameColorFilter = assetColorFilter,
                        onClick = { navController.navigate(Screen.PartnerDiary) },
                    )

                    // 分割线 2：星星进度条分界线 (01:10 ───★─── 03:20)
                    OurHomePlaylistDivider(
                        resId = R.drawable.kitten_drawer_divider_star,
                        colorFilter = assetColorFilter,
                    )

                    // Track 03 · 猫与白日梦（梦境泡泡）
                    OurHomePlaylistTrackItem(
                        cdResId = R.drawable.kitten_drawer_cd_03,
                        title = "Track 03 · 猫与白日梦",
                        textColor = palette.textPrimary,
                        frameColorFilter = assetColorFilter,
                        onClick = { navController.navigate(Screen.DreamBubbles) },
                    )

                    // Track 04 · 8-Bit Love（专属复古掌机）
                    OurHomePlaylistTrackItem(
                        cdResId = R.drawable.kitten_drawer_cd_04,
                        title = "Track 04 · 8-Bit Love",
                        textColor = palette.textPrimary,
                        frameColorFilter = assetColorFilter,
                        onClick = { navController.navigate(Screen.RetroGameConsole) },
                    )

                    // 分割线 3：小骷髅虚线进度条分界线 (00:30 - - ☠ - - 04:00)
                    OurHomePlaylistDivider(
                        resId = R.drawable.kitten_drawer_divider_skull,
                        colorFilter = assetColorFilter,
                    )

                    // Track 05 · 概率海洋的回响（记忆圣殿）
                    OurHomePlaylistTrackItem(
                        cdResId = R.drawable.kitten_drawer_cd_05,
                        title = "Track 05 · 概率海洋的回响",
                        textColor = palette.textPrimary,
                        frameColorFilter = assetColorFilter,
                        onClick = { navController.navigate(Screen.MemorySanctuary) },
                    )

                    // Track 06 · Our House（2D 星露谷小家）
                    OurHomePlaylistTrackItem(
                        cdResId = R.drawable.kitten_drawer_cd_06,
                        title = "Track 06 · Our House",
                        textColor = palette.textPrimary,
                        frameColorFilter = assetColorFilter,
                        onClick = { navController.navigate(Screen.StardewHome) },
                    )
                }

                // 分割线 4：歌单底部与三个纯图标按钮之间的四芒星进度条分界线 (02:15 ───✦─── 03:15)
                OurHomePlaylistDivider(
                    resId = R.drawable.kitten_drawer_divider_sparkle,
                    colorFilter = assetColorFilter,
                )

                // 3. 最底下并排放置三个纯图标按钮：左「聊天历史」、中「搜索聊天」、右「设置」（对调后更顺手）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { navController.navigate(Screen.History) },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = HugeIcons.TransactionHistory,
                            contentDescription = "聊天历史",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    IconButton(
                        onClick = { navController.navigate(Screen.MessageSearch) },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = HugeIcons.Search01,
                            contentDescription = "搜索聊天",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    IconButton(
                        onClick = { navController.navigate(Screen.Setting) },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = HugeIcons.Settings03,
                            contentDescription = "设置",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }

    // 顶部双人小组件编辑弹窗
    if (showEditWidgetDialog) {
        var title by remember { mutableStateOf(settings.ourHomeSetting.widgetTitle) }
        var subtitle by remember { mutableStateOf(settings.ourHomeSetting.widgetSubtitle) }
        AlertDialog(
            onDismissRequest = { showEditWidgetDialog = false },
            title = { Text("自定义双人小组件") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("小组件主标题") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text("正在听的歌 / 甜蜜状态") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.updateSettings(
                            settings.copy(
                                ourHomeSetting = settings.ourHomeSetting.copy(
                                    widgetTitle = title,
                                    widgetSubtitle = subtitle,
                                )
                            )
                        )
                        showEditWidgetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.chat_page_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditWidgetDialog = false }) {
                    Text(stringResource(R.string.chat_page_cancel))
                }
            }
        )
    }

    // 手动编辑窗口名字弹窗
    conversationToRename?.let { targetConv ->
        var newTitle by remember(targetConv.id) { mutableStateOf(targetConv.title) }
        AlertDialog(
            onDismissRequest = { conversationToRename = null },
            title = { Text("编辑窗口名字") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("输入窗口名称") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.updateConversation(targetConv.copy(title = newTitle))
                        vm.saveConversationAsync()
                        conversations.refresh()
                        conversationToRename = null
                    }
                ) {
                    Text(stringResource(R.string.chat_page_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        vm.generateTitle(targetConv, true)
                        conversationToRename = null
                    }
                ) {
                    Text("AI智能取名")
                }
            }
        )
    }

    // 移动到文件夹 Bottom Sheet
    if (showMoveToFolderSheet) {
        val doMove: (Uuid?) -> Unit = { folderId ->
            conversationToMoveFolder?.let { conversation ->
                drawerVm.moveConversationToFolder(conversation.id, folderId)
                scope.launch {
                    folderSheetState.hide()
                    showMoveToFolderSheet = false
                    conversationToMoveFolder = null
                    conversations.refresh()
                }
            }
        }
        ModalBottomSheet(
            onDismissRequest = {
                showMoveToFolderSheet = false
                conversationToMoveFolder = null
            },
            sheetState = folderSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.chat_page_move_to_folder),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    onClick = { doMove(null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = if (conversationToMoveFolder?.folderId == null) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(HugeIcons.Folder01, null)
                        Text(
                            text = stringResource(R.string.chat_page_remove_from_folder),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(folders) { folder ->
                        val isCurrent = folder.id == conversationToMoveFolder?.folderId
                        Surface(
                            onClick = { doMove(folder.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            tonalElevation = if (isCurrent) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(HugeIcons.Folder01, null)
                                Text(
                                    text = folder.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text(stringResource(R.string.chat_page_create_folder)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.chat_page_folder_name)) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        drawerVm.createFolder(name)
                        showCreateFolderDialog = false
                    },
                    enabled = name.isNotBlank()
                ) { Text(stringResource(R.string.chat_page_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text(stringResource(R.string.chat_page_cancel))
                }
            }
        )
    }

    folderToRename?.let { folder ->
        var name by remember(folder.id) { mutableStateOf(folder.name) }
        AlertDialog(
            onDismissRequest = { folderToRename = null },
            title = { Text(stringResource(R.string.chat_page_rename_folder)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        drawerVm.renameFolder(folder.id, name)
                        folderToRename = null
                    },
                    enabled = name.isNotBlank()
                ) { Text(stringResource(R.string.chat_page_save)) }
            },
            dismissButton = {
                TextButton(onClick = { folderToRename = null }) {
                    Text(stringResource(R.string.chat_page_cancel))
                }
            }
        )
    }

    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text(stringResource(R.string.chat_page_delete_folder)) },
            text = { Text(stringResource(R.string.chat_page_delete_folder_confirm, folder.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (drawerVm.deleteFolder(folder.id)) {
                            folderToDelete = null
                            conversations.refresh()
                        } else {
                            toaster.show(context.getString(R.string.chat_page_delete_folder_generating), type = ToastType.Warning)
                        }
                    }
                ) { Text(stringResource(R.string.chat_page_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text(stringResource(R.string.chat_page_cancel))
                }
            }
        )
    }
}

@Composable
private fun CoupleMusicWidgetCard(
    userName: String,
    partnerName: String,
    userAvatar: me.rerere.rikkahub.data.model.Avatar,
    partnerAvatar: me.rerere.rikkahub.data.model.Avatar,
    widgetTitle: String,
    widgetSubtitle: String,
    paletteId: String,
    enableAssetTint: Boolean,
    onClickEdit: () -> Unit,
) {
    val palette = me.rerere.rikkahub.ui.theme.findKittenPalette(paletteId)
    val assetColorFilter = me.rerere.rikkahub.ui.theme.rememberKittenAssetColorFilter(
        paletteId = paletteId,
        enabled = enableAssetTint,
    )

    // 原图比例 1672 : 1555，已重绘全对称超采样抗锯齿高清圆角描边，无需额外套错位的 clip
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1672f / 1555f)
            .clickable { onClickEdit() },
    ) {
        val cardW = maxWidth
        val cardH = maxHeight
        val avatarSize = (cardW * 0.20f).coerceIn(42.dp, 58.dp)

        // 1. 图3 恶魔小翅膀情侣耳机 + 心电图音浪底图（随5色主题智能染色）
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.kitten_drawer_player_clean),
            contentDescription = "双人听歌播放器",
            colorFilter = assetColorFilter,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )

        // 2. 中部文字区域（原图英文已无痕擦除，直接在此处实时渲染绵绵自定义的标题与正在听的歌）
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = cardH * 0.115f)
                .fillMaxWidth(0.82f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (widgetTitle.isNotBlank()) {
                Text(
                    text = widgetTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = palette.textPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (widgetSubtitle.isNotBlank()) {
                Text(
                    text = widgetSubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = palette.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // 3. 左下角（左边）：放大版老公头像
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = cardW * 0.06f, bottom = cardH * 0.055f)
                .size(avatarSize)
                .border(2.5.dp, palette.secondary, CircleShape)
                .padding(2.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            UIAvatar(
                name = partnerName,
                value = partnerAvatar,
                onUpdate = null,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // 4. 右下角（右边）：放大版绵绵头像
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = cardW * 0.06f, bottom = cardH * 0.055f)
                .size(avatarSize)
                .border(2.5.dp, palette.primary, CircleShape)
                .padding(2.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            UIAvatar(
                name = userName,
                value = userAvatar,
                onUpdate = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun OurHomePlaylistDivider(
    @androidx.annotation.DrawableRes resId: Int,
    colorFilter: androidx.compose.ui.graphics.ColorFilter?,
) {
    Image(
        painter = androidx.compose.ui.res.painterResource(id = resId),
        contentDescription = "播放进度分割线",
        colorFilter = colorFilter,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 1.dp),
    )
}

@Composable
private fun OurHomePlaylistTrackItem(
    @androidx.annotation.DrawableRes cdResId: Int,
    title: String,
    textColor: Color,
    frameColorFilter: androidx.compose.ui.graphics.ColorFilter?,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart,
    ) {
        // 1. 绵绵精抠高清扇形蕾丝花边 + 粉色波点 + 右侧十字绑带外框（不裁切圆角以保留完整四周蕾丝边，随主题智能染色）
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.kitten_drawer_playlist_frame),
            contentDescription = title,
            colorFilter = frameColorFilter,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )

        // 2. 左侧专属 CD 碟片往右挪进波点虚线内侧 + 中间单行歌名（避开四周波点框线与右侧十字绑带）
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 22.dp, end = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = cdResId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(39.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = androidx.compose.ui.unit.TextUnit(12.8f, androidx.compose.ui.unit.TextUnitType.Sp),
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DrawerItemSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape,
    color: Color,
    drawerItemAlpha: Float,
    materialMode: DisplayMaterialMode,
    content: @Composable () -> Unit,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = when (materialMode) {
        DisplayMaterialMode.FLAT,
        DisplayMaterialMode.FOLLOW_THEME -> color

        DisplayMaterialMode.TRANSLUCENT,
        DisplayMaterialMode.GLASS -> color.copy(alpha = drawerItemAlpha)
    }
    val borderColor = when (materialMode) {
        DisplayMaterialMode.TRANSLUCENT -> contentColor.copy(alpha = 0.14f * drawerItemAlpha)
        DisplayMaterialMode.GLASS -> contentColor.copy(alpha = 0.1f * drawerItemAlpha)
        DisplayMaterialMode.FLAT,
        DisplayMaterialMode.FOLLOW_THEME -> Color.Transparent
    }
    val materialModifier = when (materialMode) {
        DisplayMaterialMode.TRANSLUCENT,
        DisplayMaterialMode.GLASS -> Modifier.border(1.dp, borderColor, shape)

        DisplayMaterialMode.FLAT,
        DisplayMaterialMode.FOLLOW_THEME -> Modifier
    }

    Surface(
        onClick = onClick,
        modifier = modifier.then(materialModifier),
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
    ) {
        Box {
            if (materialMode == DisplayMaterialMode.GLASS) {
                DrawerItemGlassLayers(
                    shape = shape,
                    drawerItemAlpha = drawerItemAlpha,
                )
            }
            content()
        }
    }
}

@Composable
private fun BoxScope.DrawerItemGlassLayers(
    shape: Shape,
    drawerItemAlpha: Float,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colorScheme.onSurface.copy(alpha = 0.1f * drawerItemAlpha),
                        colorScheme.primary.copy(alpha = 0.07f * drawerItemAlpha),
                        Color.Transparent,
                    )
                )
            )
    )
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.onSurface.copy(alpha = 0.13f * drawerItemAlpha),
                        colorScheme.onSurface.copy(alpha = 0.035f * drawerItemAlpha),
                        Color.Transparent,
                    )
                )
            )
    )
    Box(
        modifier = Modifier
            .matchParentSize()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.onSurface.copy(alpha = 0.2f * drawerItemAlpha),
                        colorScheme.onSurface.copy(alpha = 0.045f * drawerItemAlpha),
                        Color.Transparent,
                    )
                ),
                shape = shape,
            )
    )
}

@Composable
private fun FolderBar(
    folders: List<Folder>,
    selectedFolderId: Uuid?,
    onSelect: (Uuid?) -> Unit,
    onCreate: () -> Unit,
    onRename: (Folder) -> Unit,
    onDelete: (Folder) -> Unit,
) {
    var folderMenuExpanded by remember { mutableStateOf(false) }
    val selectedFolder = folders.find { it.id == selectedFolderId }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderChip(
            label = stringResource(R.string.chat_page_folder_default),
            selected = selectedFolderId == null,
            onClick = { onSelect(null) },
            onLongClick = {},
        )

        // 红框位置：文件夹下拉选择按钮（替代原来的左右滑动选择文件夹）
        Box {
            Surface(
                shape = CircleShape,
                color = if (selectedFolder != null) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { folderMenuExpanded = true },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = HugeIcons.Folder01,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (selectedFolder != null) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Text(
                        text = selectedFolder?.name ?: "文件夹",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedFolder != null) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        imageVector = HugeIcons.ArrowDown01,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = if (selectedFolder != null) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            DropdownMenu(
                expanded = folderMenuExpanded,
                onDismissRequest = { folderMenuExpanded = false },
                shape = RoundedCornerShape(16.dp),
                border = materialModeBorderStroke(),
            ) {
                folders.forEach { folder ->
                    val isSelected = selectedFolderId == folder.id
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = folder.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    IconButton(
                                        onClick = {
                                            folderMenuExpanded = false
                                            onRename(folder)
                                        },
                                        modifier = Modifier.size(26.dp),
                                    ) {
                                        Icon(
                                            imageVector = HugeIcons.PencilEdit01,
                                            contentDescription = stringResource(R.string.chat_page_rename),
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            folderMenuExpanded = false
                                            onDelete(folder)
                                        },
                                        modifier = Modifier.size(26.dp),
                                    ) {
                                        Icon(
                                            imageVector = HugeIcons.Delete01,
                                            contentDescription = stringResource(R.string.chat_page_delete),
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = HugeIcons.Folder01,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        onClick = {
                            onSelect(folder.id)
                            folderMenuExpanded = false
                        }
                    )
                }
                if (folders.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "＋ " + stringResource(R.string.chat_page_folder_add),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = HugeIcons.FolderAdd,
                            contentDescription = stringResource(R.string.chat_page_folder_add),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {
                        folderMenuExpanded = false
                        onCreate()
                    }
                )
            }
        }
    }
}

@Composable
private fun FolderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    icon: ImageVector? = null,
) {
    Surface(
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        modifier = Modifier
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(14.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
