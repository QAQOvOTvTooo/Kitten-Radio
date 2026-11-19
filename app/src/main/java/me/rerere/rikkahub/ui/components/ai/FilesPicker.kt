/*
 * 小猫电台 · 绵绵与蔺鹤的专属小家
 * 输入框「+」号专属下午茶甜品菜单 (Menu · SWEET MOMENTS)
 */

package me.rerere.rikkahub.ui.components.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.rerere.ai.core.ReasoningLevel
import me.rerere.ai.provider.Model
import me.rerere.ai.provider.ModelType
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Camera01
import me.rerere.hugeicons.stroke.ComputerTerminal01
import me.rerere.hugeicons.stroke.Files02
import me.rerere.hugeicons.stroke.Folder01
import me.rerere.hugeicons.stroke.Image02
import me.rerere.hugeicons.stroke.MusicNote03
import me.rerere.hugeicons.stroke.Settings02
import me.rerere.hugeicons.stroke.Video01
import me.rerere.rikkahub.R
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.ai.mcp.McpManager
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.datastore.findProvider
import me.rerere.rikkahub.data.db.entity.WorkspaceEntity
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.data.model.Conversation
import me.rerere.rikkahub.data.model.lastContextTokens
import me.rerere.rikkahub.data.repository.WorkspaceRepository
import me.rerere.rikkahub.ui.components.ui.ExtensionSelector
import me.rerere.rikkahub.ui.components.ui.permission.PermissionCamera
import me.rerere.rikkahub.ui.components.ui.permission.PermissionManager
import me.rerere.rikkahub.ui.components.ui.permission.rememberPermissionState
import me.rerere.rikkahub.ui.context.LocalCurrentChatModel
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.context.LocalProviders
import me.rerere.rikkahub.ui.context.LocalSettings
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.hooks.ChatInputState
import me.rerere.rikkahub.ui.theme.findKittenPalette
import me.rerere.rikkahub.ui.theme.popupContainerColor
import me.rerere.rikkahub.ui.theme.rememberKittenAssetColorFilter
import me.rerere.rikkahub.utils.formatNumber
import me.rerere.workspace.WorkspaceShellStatus
import org.koin.compose.koinInject
import kotlin.uuid.Uuid

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun FilesPicker(
    conversation: Conversation,
    assistant: Assistant,
    state: ChatInputState,
    mcpManager: McpManager,
    enableSearch: Boolean,
    onToggleSearch: (Boolean) -> Unit,
    onUpdateSearchService: (Int) -> Unit,
    onUpdateChatModel: (Model) -> Unit,
    onCompressContext: (additionalPrompt: String, targetTokens: Int, keepRecentMessages: Int) -> Job,
    onUpdateAssistant: (Assistant) -> Unit,
    onUpdateWorkspaceCwd: (String?) -> Unit = {},
    showInjectionSheet: Boolean,
    onShowInjectionSheetChange: (Boolean) -> Unit,
    showCompressDialog: Boolean,
    onShowCompressDialogChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onTakePic: () -> Unit,
    onPickImage: () -> Unit,
    onPickVideo: () -> Unit,
    onPickAudio: () -> Unit,
    onPickFile: () -> Unit,
) {
    val settings = LocalSettings.current
    val currentChatModel = LocalCurrentChatModel.current
    val providers = LocalProviders.current
    val provider = currentChatModel?.findProvider(providers = providers)
    val navController = LocalNavController.current
    val toaster = LocalToaster.current
    val workspaceRepository: WorkspaceRepository = koinInject()
    val workspaces by workspaceRepository.listFlow().collectAsState(initial = emptyList())

    val boundWorkspace = remember(workspaces, assistant.workspaceId) {
        workspaces.find { it.id == assistant.workspaceId?.toString() }
    }
    var showCwdSheet by remember { mutableStateOf(false) }
    var showModelSheet by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showReasoningSheet by remember { mutableStateOf(false) }
    var showWorkspaceBakerySheet by remember { mutableStateOf(false) }

    val palette = findKittenPalette(settings.ourHomeSetting.kittenPaletteId)
    val assetColorFilter = rememberKittenAssetColorFilter(
        paletteId = settings.ourHomeSetting.kittenPaletteId,
        enabled = settings.ourHomeSetting.enableAssetTint,
    )

    val reasoningLabel = when (assistant.reasoningLevel) {
        ReasoningLevel.OFF -> stringResource(R.string.reasoning_off)
        ReasoningLevel.AUTO -> stringResource(R.string.reasoning_auto)
        ReasoningLevel.LOW -> stringResource(R.string.reasoning_light)
        ReasoningLevel.MEDIUM -> stringResource(R.string.reasoning_medium)
        ReasoningLevel.HIGH -> stringResource(R.string.reasoning_heavy)
        ReasoningLevel.XHIGH -> stringResource(R.string.reasoning_xhigh)
    }
    val enabledExtensionsCount = assistant.quickMessageIds.size +
        assistant.modeInjectionIds.size +
        assistant.lorebookIds.size +
        assistant.enabledSkills.size

    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val maxMenuHeight = (screenHeightDp * 0.62f).coerceIn(360.dp, 540.dp)

    // 绵绵亲自设计的下午茶甜品菜单卡 (Menu · SWEET MOMENTS · 1024x1536)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .heightIn(max = maxMenuHeight)
                .aspectRatio(1024f / 1536f)
                .clip(RoundedCornerShape(22.dp))
        ) {
            val mw = maxWidth
            val mh = maxHeight

            Image(
                painter = painterResource(id = R.drawable.kitten_chat_sweet_menu),
                contentDescription = "Sweet Moments Menu",
                colorFilter = assetColorFilter,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )

            // 通用透明菜名热区 + 虚线上方精致当前状态小副标
            @Composable
            fun MenuHotspot(
                xFrac: Float,
                yFrac: Float,
                wFrac: Float,
                hFrac: Float,
                badgeText: String = "",
                onClick: () -> Unit,
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = mw * xFrac, y = mh * yFrac)
                        .size(width = mw * wFrac, height = mh * hFrac)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClick() },
                ) {
                    if (badgeText.isNotBlank()) {
                        Text(
                            text = badgeText,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.primary.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = mw * 0.03f, bottom = mh * 0.004f),
                        )
                    }
                }
            }

            // ================= 左上 Coffee 区域 =================
            // 1. Chef's Model Blend（模型）
            MenuHotspot(
                xFrac = 0.08f,
                yFrac = 0.355f,
                wFrac = 0.40f,
                hFrac = 0.072f,
                badgeText = currentChatModel?.displayName.orEmpty(),
                onClick = { showModelSheet = true },
            )
            // 2. Deep-Brew Reasoning（推理）
            MenuHotspot(
                xFrac = 0.08f,
                yFrac = 0.440f,
                wFrac = 0.40f,
                hFrac = 0.072f,
                badgeText = reasoningLabel,
                onClick = { showReasoningSheet = true },
            )

            // ================= 右上 Tea 区域 =================
            // 3. Web-Hunt Berry Tea（搜索）
            MenuHotspot(
                xFrac = 0.52f,
                yFrac = 0.355f,
                wFrac = 0.39f,
                hFrac = 0.072f,
                badgeText = if (enableSearch) "ON" else "OFF",
                onClick = { showSearchSheet = true },
            )
            // 4. History Syrup（压缩历史）
            MenuHotspot(
                xFrac = 0.52f,
                yFrac = 0.440f,
                wFrac = 0.39f,
                hFrac = 0.072f,
                badgeText = if (conversation.messageNodes.isNotEmpty()) "${conversation.messageNodes.size}条" else "",
                onClick = { onShowCompressDialogChange(true) },
            )

            // ================= 左下 Desserts 区域 =================
            // 5. Mille-Feuille Extensions（扩展）
            MenuHotspot(
                xFrac = 0.14f,
                yFrac = 0.625f,
                wFrac = 0.35f,
                hFrac = 0.058f,
                badgeText = if (enabledExtensionsCount > 0) "${enabledExtensionsCount}项" else "",
                onClick = { onShowInjectionSheetChange(true) },
            )
            // 6. Sandbox Bakery（工作区：弹出包含切换工作区、设置齿轮、打开终端的专属菜单）
            MenuHotspot(
                xFrac = 0.14f,
                yFrac = 0.690f,
                wFrac = 0.35f,
                hFrac = 0.058f,
                badgeText = boundWorkspace?.name.orEmpty(),
                onClick = { showWorkspaceBakerySheet = true },
            )
            // 7. Home Pantry（/workspace 文件夹）
            MenuHotspot(
                xFrac = 0.14f,
                yFrac = 0.752f,
                wFrac = 0.35f,
                hFrac = 0.058f,
                badgeText = conversation.workspaceCwd ?: "/workspace",
                onClick = {
                    if (boundWorkspace != null) {
                        showCwdSheet = true
                    } else {
                        showWorkspaceBakerySheet = true
                    }
                },
            )

            // ================= 右下 Refreshers 区域 =================
            // 8. Photo Parfait（图片）
            MenuHotspot(
                xFrac = 0.52f,
                yFrac = 0.625f,
                wFrac = 0.35f,
                hFrac = 0.058f,
                onClick = { onPickImage() },
            )
            // 9. File Mille-Crêpe（上传文件）
            MenuHotspot(
                xFrac = 0.52f,
                yFrac = 0.690f,
                wFrac = 0.35f,
                hFrac = 0.058f,
                onClick = { onPickFile() },
            )
        }
    }

    // 7. Home Pantry (/workspace 文件夹选择弹窗)
    if (showCwdSheet && boundWorkspace != null) {
        WorkspaceCwdPickerSheet(
            workspaceId = boundWorkspace.id,
            workspaceDir = boundWorkspace.workspaceDir(),
            currentCwd = conversation.workspaceCwd,
            onSelectCwd = { newCwd ->
                onUpdateWorkspaceCwd(newCwd)
            },
            onDismiss = { showCwdSheet = false },
        )
    }

    // 5. Mille-Feuille Extensions (扩展管理弹窗)
    if (showInjectionSheet) {
        InjectionQuickConfigSheet(
            assistant = assistant,
            settings = settings,
            onUpdateAssistant = onUpdateAssistant,
            onDismiss = { onShowInjectionSheetChange(false) },
        )
    }

    // 4. History Syrup (压缩历史弹窗，顶部附带当前上下文 Token 用量环)
    if (showCompressDialog) {
        CompressContextDialog(
            onDismiss = {
                onShowCompressDialogChange(false)
                onDismiss()
            },
            onConfirm = { additionalPrompt, targetTokens, keepRecentMessages ->
                onCompressContext(additionalPrompt, targetTokens, keepRecentMessages)
            }
        )
    }

    // 1. Chef's Model Blend (模型选择弹窗)
    if (showModelSheet) {
        val modelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        ModalBottomSheet(
            onDismissRequest = { showModelSheet = false },
            sheetState = modelSheetState,
            containerColor = popupContainerColor(BottomSheetDefaults.ContainerColor),
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight(0.8f)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val filteredProviderSettings = providers.fastFilter {
                    it.enabled && it.models.fastAny { m -> m.type == ModelType.CHAT }
                }
                ModelList(
                    currentModel = assistant.chatModelId ?: settings.chatModelId,
                    providers = filteredProviderSettings,
                    modelType = ModelType.CHAT,
                    onSelect = {
                        onUpdateChatModel(it)
                        scope.launch {
                            modelSheetState.hide()
                            showModelSheet = false
                        }
                    },
                    onDismiss = {
                        scope.launch {
                            modelSheetState.hide()
                            showModelSheet = false
                        }
                    }
                )
            }
        }
    }

    // 3. Web-Hunt Berry Tea (搜索开关 + 搜索服务选择弹窗)
    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSearchSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = popupContainerColor(BottomSheetDefaults.ContainerColor),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🍓 Web-Hunt Berry Tea · 搜索设置",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )

                // 将原本在外层的网页搜索总开关收进点击菜名后的弹窗顶部
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.search_picker_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            )
                            Text(
                                text = if (enableSearch) stringResource(R.string.web_search_enabled) else stringResource(R.string.web_search_disabled),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = enableSearch,
                            onCheckedChange = { onToggleSearch(it) },
                        )
                    }
                }

                SearchPicker(
                    enableSearch = enableSearch,
                    settings = settings,
                    onToggleSearch = onToggleSearch,
                    onUpdateSearchService = onUpdateSearchService,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    model = currentChatModel,
                    onDismiss = {
                        showSearchSheet = false
                    }
                )
            }
        }
    }

    // 2. Deep-Brew Reasoning (思考深度调节弹窗)
    if (showReasoningSheet) {
        ReasoningPicker(
            reasoningLevel = assistant.reasoningLevel,
            onDismissRequest = { showReasoningSheet = false },
            onUpdateReasoningLevel = {
                onUpdateAssistant(assistant.copy(reasoningLevel = it))
            }
        )
    }

    // 6. Sandbox Bakery (工作区切换 + 工作区设置齿轮 + 打开终端弹窗)
    if (showWorkspaceBakerySheet) {
        var showSelectSubSheet by remember { mutableStateOf(false) }
        ModalBottomSheet(
            onDismissRequest = { showWorkspaceBakerySheet = false },
            containerColor = popupContainerColor(BottomSheetDefaults.ContainerColor),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "🍰 Sandbox Bakery · 工作区管理",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "当前绑定工作区：${boundWorkspace?.name ?: stringResource(R.string.assistant_page_workspace_unbound)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (boundWorkspace != null) {
                                FilledTonalButton(
                                    onClick = {
                                        showWorkspaceBakerySheet = false
                                        onDismiss()
                                        navController.navigate(Screen.WorkspaceDetail(boundWorkspace.id, initialTab = 1))
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(HugeIcons.Settings02, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("工作区设置")
                                }
                                if (boundWorkspace.supportsTerminal()) {
                                    FilledTonalButton(
                                        onClick = {
                                            showWorkspaceBakerySheet = false
                                            onDismiss()
                                            navController.navigate(Screen.WorkspaceTerminal(boundWorkspace.id))
                                        },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(HugeIcons.ComputerTerminal01, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("打开终端")
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilledTonalButton(
                                onClick = { showSelectSubSheet = true },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("切换绑定工作区")
                            }
                            if (boundWorkspace != null) {
                                FilledTonalButton(
                                    onClick = {
                                        showWorkspaceBakerySheet = false
                                        showCwdSheet = true
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(HugeIcons.Folder01, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("/workspace 目录")
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        if (showSelectSubSheet) {
            WorkspaceSelectSheet(
                assistant = assistant,
                workspaces = workspaces,
                onSelect = { workspaceId ->
                    val newId = workspaceId?.let { Uuid.parse(it) }
                    if (newId != assistant.workspaceId) {
                        onUpdateWorkspaceCwd(null)
                        onUpdateAssistant(assistant.copy(workspaceId = newId))
                    }
                    showSelectSubSheet = false
                },
                onManage = {
                    showSelectSubSheet = false
                    showWorkspaceBakerySheet = false
                    onDismiss()
                    navController.navigate(Screen.Workspaces)
                },
                onDismiss = { showSelectSubSheet = false },
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun InjectionQuickConfigSheet(
    assistant: Assistant, settings: Settings, onUpdateAssistant: (Assistant) -> Unit, onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp),
        ) {
            ExtensionSelector(
                assistant = assistant,
                settings = settings,
                onUpdate = onUpdateAssistant,
                modifier = Modifier.weight(1f),
                onNavigateToQuickMessages = {
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                        navController.navigate(Screen.QuickMessages)
                    }
                },
                onNavigateToPrompts = {
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                        navController.navigate(Screen.Prompts)
                    }
                },
                onNavigateToSkills = {
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                        navController.navigate(Screen.Skills)
                    }
                })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ImagePickButton(onClick: () -> Unit = {}) {
    BigIconTextButton(icon = {
        Icon(HugeIcons.Image02, null)
    }, text = {
        Text(stringResource(R.string.photo))
    }) {
        onClick()
    }
}

@Composable
fun TakePicButton(onLaunchCamera: () -> Unit = {}) {
    val cameraPermission = rememberPermissionState(PermissionCamera)

    PermissionManager(
        permissionState = cameraPermission
    ) {
        BigIconTextButton(icon = {
            Icon(HugeIcons.Camera01, null)
        }, text = {
            Text(stringResource(R.string.take_picture))
        }) {
            if (cameraPermission.allRequiredPermissionsGranted) {
                onLaunchCamera()
            } else {
                cameraPermission.requestPermissions()
            }
        }
    }
}

@Composable
fun VideoPickButton(onClick: () -> Unit = {}) {
    BigIconTextButton(icon = {
        Icon(HugeIcons.Video01, null)
    }, text = {
        Text(stringResource(R.string.video))
    }) {
        onClick()
    }
}

@Composable
fun AudioPickButton(onClick: () -> Unit = {}) {
    BigIconTextButton(icon = {
        Icon(HugeIcons.MusicNote03, null)
    }, text = {
        Text(stringResource(R.string.audio))
    }) {
        onClick()
    }
}

@Composable
fun FilePickButton(onClick: () -> Unit = {}) {
    BigIconTextButton(icon = {
        Icon(HugeIcons.Files02, null)
    }, text = {
        Text(stringResource(R.string.upload_file))
    }) {
        onClick()
    }
}

@Composable
private fun BigIconTextButton(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick
            )
            .semantics {
                role = Role.Button
            }
            .wrapContentWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Surface(
            tonalElevation = 2.dp, shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                icon()
            }
        }
        ProvideTextStyle(MaterialTheme.typography.bodySmall) {
            text()
        }
    }
}

// 上下文使用量进度环，接近上限时变色提醒压缩
@Composable
private fun ContextUsageRing(fraction: Float) {
    val color = when {
        fraction >= 0.9f -> MaterialTheme.colorScheme.error
        fraction >= 0.7f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    CircularProgressIndicator(
        progress = { fraction.coerceIn(0f, 1f) },
        modifier = Modifier.size(24.dp),
        color = color,
        strokeWidth = 3.dp,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
}

@Preview(showBackground = true)
@Composable
private fun BigIconTextButtonPreview() {
    Row(
        modifier = Modifier.padding(16.dp)
    ) {
        BigIconTextButton(icon = {
            Icon(HugeIcons.Image02, null)
        }, text = {
            Text(stringResource(R.string.photo))
        }) {}
    }
}
