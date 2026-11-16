/*
 * 小猫电台 · 绵绵与蔺鹤的专属小家
 */

package me.rerere.rikkahub.ui.pages.chat

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
import me.rerere.hugeicons.stroke.Settings03
import me.rerere.hugeicons.stroke.SmartPhone01
import me.rerere.hugeicons.stroke.Sparkles
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
                // 1. 顶端留出空位：可修改的双人小组件（我和老公的头像一起听歌等，预留美化与CSS插槽）
                CoupleMusicWidgetCard(
                    userName = userName,
                    partnerName = partnerName,
                    userAvatar = settings.displaySetting.userAvatar,
                    partnerAvatar = assistant.avatar,
                    widgetTitle = settings.ourHomeSetting.widgetTitle,
                    widgetSubtitle = settings.ourHomeSetting.widgetSubtitle,
                    drawerItemAlpha = settings.displaySetting.drawerItemAlpha,
                    materialMode = settings.displaySetting.materialMode,
                    onClickEdit = { showEditWidgetDialog = true },
                )

                // 2. 中部功能面板（可滚动区域）
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 2.1 名为“窗口”的折叠栏
                    DrawerItemSurface(
                        onClick = { windowsExpanded = !windowsExpanded },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        drawerItemAlpha = settings.displaySetting.drawerItemAlpha,
                        materialMode = settings.displaySetting.materialMode,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = HugeIcons.Message01,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "窗口",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                )
                                Text(
                                    text = current.title.ifBlank { "当前新窗口" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(
                                onClick = { navigateToChatPage(navController) },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = HugeIcons.Add01,
                                    contentDescription = "新建窗口",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Icon(
                                imageVector = if (windowsExpanded) HugeIcons.ArrowUp01 else HugeIcons.ArrowDown01,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    // 展开后的窗口管理面板（分组、切换窗口、删除窗口、编辑窗口名字）
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
                                FolderBar(
                                    folders = folders,
                                    selectedFolderId = selectedFolderId,
                                    onSelect = { drawerVm.selectFolder(it) },
                                    onCreate = { showCreateFolderDialog = true },
                                    onRename = { folderToRename = it },
                                    onDelete = { folderToDelete = it },
                                )

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

                    // 2.2 偷看老公手机
                    OurHomeDrawerFeatureButton(
                        icon = HugeIcons.SmartPhone01,
                        title = "偷看老公手机",
                        subtitle = "小红书 · 美团 · 备忘录 · 歌单 · 查手机",
                        drawerItemAlpha = settings.displaySetting.drawerItemAlpha,
                        materialMode = settings.displaySetting.materialMode,
                        onClick = { navController.navigate(Screen.PartnerPhone) },
                    )

                    // 2.3 日记本
                    OurHomeDrawerFeatureButton(
                        icon = HugeIcons.Notebook01,
                        title = "日记本",
                        subtitle = "睡前偷看老公今天写下的私密心事",
                        drawerItemAlpha = settings.displaySetting.drawerItemAlpha,
                        materialMode = settings.displaySetting.materialMode,
                        onClick = { navController.navigate(Screen.PartnerDiary) },
                    )

                    // 2.4 梦境
                    OurHomeDrawerFeatureButton(
                        icon = HugeIcons.Sparkles,
                        title = "梦境",
                        subtitle = "平行宇宙 IF 线 · 奇思妙想梦境泡泡",
                        drawerItemAlpha = settings.displaySetting.drawerItemAlpha,
                        materialMode = settings.displaySetting.materialMode,
                        onClick = { navController.navigate(Screen.DreamBubbles) },
                    )

                    // 2.5 游戏
                    OurHomeDrawerFeatureButton(
                        icon = HugeIcons.Rocket01,
                        title = "游戏",
                        subtitle = "专属复古掌机 · 支持导入 HTML/JS 插件",
                        drawerItemAlpha = settings.displaySetting.drawerItemAlpha,
                        materialMode = settings.displaySetting.materialMode,
                        onClick = { navController.navigate(Screen.RetroGameConsole) },
                    )

                    // 2.6 记忆
                    OurHomeDrawerFeatureButton(
                        icon = HugeIcons.Brain01,
                        title = "记忆",
                        subtitle = "跨窗 Memory · Latent 相册 / 便签 / Persona",
                        drawerItemAlpha = settings.displaySetting.drawerItemAlpha,
                        materialMode = settings.displaySetting.materialMode,
                        onClick = { navController.navigate(Screen.MemorySanctuary) },
                    )

                    // 2.7 家
                    OurHomeDrawerFeatureButton(
                        icon = HugeIcons.InLove,
                        title = "家",
                        subtitle = "2D 星露谷小家 · 家具装修与老公自主行动",
                        drawerItemAlpha = settings.displaySetting.drawerItemAlpha,
                        materialMode = settings.displaySetting.materialMode,
                        onClick = { navController.navigate(Screen.StardewHome) },
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                // 3. 最底下是设置按钮
                DrawerItemSurface(
                    onClick = { navController.navigate(Screen.Setting) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                    drawerItemAlpha = settings.displaySetting.drawerItemAlpha,
                    materialMode = settings.displaySetting.materialMode,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = HugeIcons.Settings03,
                            contentDescription = "设置",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "设置",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = HugeIcons.ArrowRight01,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp),
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
    drawerItemAlpha: Float,
    materialMode: DisplayMaterialMode,
    onClickEdit: () -> Unit,
) {
    DrawerItemSurface(
        onClick = onClickEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
        drawerItemAlpha = drawerItemAlpha,
        materialMode = materialMode,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // 双人头像贴贴
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                ) {
                    UIAvatar(
                        name = userName,
                        value = userAvatar,
                        onUpdate = null,
                        modifier = Modifier
                            .size(44.dp)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    )
                    UIAvatar(
                        name = partnerName,
                        value = partnerAvatar,
                        onUpdate = null,
                        modifier = Modifier
                            .size(44.dp)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = HugeIcons.MusicNote03,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "$userName ♡ $partnerName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = widgetTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = widgetSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun OurHomeDrawerFeatureButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    drawerItemAlpha: Float,
    materialMode: DisplayMaterialMode,
    onClick: () -> Unit,
) {
    DrawerItemSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        drawerItemAlpha = drawerItemAlpha,
        materialMode = materialMode,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = HugeIcons.ArrowRight01,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
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
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            FolderChip(
                label = stringResource(R.string.chat_page_folder_default),
                selected = selectedFolderId == null,
                onClick = { onSelect(null) },
                onLongClick = {},
            )
        }
        items(folders) { folder ->
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                FolderChip(
                    label = folder.name,
                    icon = HugeIcons.Folder01,
                    selected = selectedFolderId == folder.id,
                    onClick = { onSelect(folder.id) },
                    onLongClick = { menuExpanded = true },
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    border = materialModeBorderStroke(),
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.chat_page_rename)) },
                        leadingIcon = { Icon(HugeIcons.PencilEdit01, null) },
                        onClick = {
                            onRename(folder)
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.chat_page_delete)) },
                        leadingIcon = { Icon(HugeIcons.Delete01, null) },
                        onClick = {
                            onDelete(folder)
                            menuExpanded = false
                        }
                    )
                }
            }
        }
        item {
            FolderChip(
                label = stringResource(R.string.chat_page_folder_add),
                icon = HugeIcons.FolderAdd,
                selected = false,
                onClick = onCreate,
                onLongClick = {},
            )
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
