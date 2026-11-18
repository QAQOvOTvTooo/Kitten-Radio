/*
 * 橘瓣 OrangeChat
 * 衍生自 RikkaHub (https://github.com/rikkahub/rikkahub)，原作者 RE
 * 本项目基于 GNU AGPL v3 开源，详见根目录 LICENSE 文件
 */

package me.rerere.rikkahub.ui.pages.chat

import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.text.font.FontWeight
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.BitmapImage
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.dokar.sonner.ToastType
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rerere.ai.provider.Model
import me.rerere.ai.ui.UIMessagePart
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.ArrowDown01
import me.rerere.hugeicons.stroke.ArrowUp01
import me.rerere.hugeicons.stroke.Brain02
import me.rerere.hugeicons.stroke.Cancel01
import me.rerere.hugeicons.stroke.LeftToRightListBullet
import me.rerere.hugeicons.stroke.LookTop
import me.rerere.hugeicons.stroke.Menu03
import me.rerere.hugeicons.stroke.Message01
import me.rerere.hugeicons.stroke.MessageAdd01
import me.rerere.hugeicons.stroke.MoreVertical
import me.rerere.hugeicons.stroke.PencilEdit01
import me.rerere.hugeicons.stroke.Voice
import me.rerere.rikkahub.R
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.datastore.DisplayMaterialMode
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.datastore.getAssistantById
import me.rerere.rikkahub.data.datastore.findProvider
import me.rerere.rikkahub.data.datastore.getCurrentAssistant
import me.rerere.rikkahub.data.datastore.getCurrentChatModel
import me.rerere.rikkahub.data.files.FilesManager
import me.rerere.rikkahub.data.model.Conversation
import me.rerere.rikkahub.service.ChatError
import me.rerere.rikkahub.service.VoiceCallService
import me.rerere.rikkahub.ui.components.ai.ChatInput
import me.rerere.rikkahub.ui.components.message.LiveBubbleBlurContext
import me.rerere.rikkahub.ui.components.message.LocalLiveBubbleBlur
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.context.Navigator
import me.rerere.rikkahub.ui.hooks.ChatInputState
import me.rerere.rikkahub.ui.hooks.EditStateContent
import me.rerere.rikkahub.ui.hooks.useEditState
import me.rerere.rikkahub.ui.theme.LocalMaterialMode
import me.rerere.rikkahub.ui.theme.materialModeBorderStroke
import me.rerere.rikkahub.ui.theme.popupContainerColor
import me.rerere.rikkahub.utils.base64Decode
import me.rerere.rikkahub.utils.navigateToChatPage
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

/**
 * 按 Coil model（assistant.background 的 String）的 scheme 分流加载聊天背景位图。
 *
 * 背景 model 的实际形式（与 Coil 显示完全一致）：
 * - 图库选择：FilesManager.createChatFilesByContents 拷贝到 filesDir 后返回 file:// URI；
 * - 手动输入：http/https URL；
 * - 其余（content://、android.resource://、普通绝对路径）一并兼容。
 *
 * 所有磁盘/流解码都在 Dispatchers.IO 中执行；网络等复杂 model 走项目现有
 * Coil 3.4 SingletonImageLoader（不在本函数内做网络阻塞）。
 */
private suspend fun decodeChatBackgroundBitmap(
    context: Context,
    model: String,
): ImageBitmap? = withContext(Dispatchers.IO) {
    val uri = runCatching { Uri.parse(model) }.getOrNull()
    val bitmap = when {
        model.startsWith("/") -> {
            BitmapFactory.decodeFile(model)
        }

        uri?.scheme == ContentResolver.SCHEME_CONTENT ||
            uri?.scheme == ContentResolver.SCHEME_ANDROID_RESOURCE -> {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }

        uri?.scheme == ContentResolver.SCHEME_FILE -> {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: uri.path?.let(BitmapFactory::decodeFile)
        }

        uri?.scheme == "http" || uri?.scheme == "https" -> {
            loadChatBackgroundViaCoil(context, model)
        }

        else -> {
            BitmapFactory.decodeFile(model)
                ?: uri?.let {
                    runCatching {
                        context.contentResolver.openInputStream(it)?.use(BitmapFactory::decodeStream)
                    }.getOrNull()
                }
        }
    }
    bitmap?.asImageBitmap()
}

/**
 * 使用项目现有 Coil 3 ImageLoader 加载同一 model（与 Coil 显示背景使用同一套 fetcher/缓存）。
 * 调用方已处于 Dispatchers.IO，Coil 内部网络/解码不会阻塞主线程。
 */
private suspend fun loadChatBackgroundViaCoil(context: Context, model: String): Bitmap? {
    val loader = SingletonImageLoader.get(context)
    val request = ImageRequest.Builder(context)
        .data(model)
        // 硬件位图无法被 asImageBitmap/RenderEffect 采样，关闭硬件加速以保证可读
        .allowHardware(false)
        .build()
    val result = loader.execute(request)
    return (result.image as? BitmapImage)?.bitmap
}

@Composable
fun ChatPage(id: Uuid, text: String?, files: List<Uri>, nodeId: Uuid? = null, autoStartVoice: Boolean = false) {
    val vm: ChatVM = koinViewModel(
        parameters = {
            parametersOf(id.toString())
        }
    )
    val filesManager: FilesManager = koinInject()
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    val setting by vm.settings.collectAsStateWithLifecycle()
    val conversation by vm.conversation.collectAsStateWithLifecycle()
    val loadingJob by vm.conversationJob.collectAsStateWithLifecycle()
    val processingStatus by vm.processingStatus.collectAsStateWithLifecycle()
    val currentChatModel by vm.currentChatModel.collectAsStateWithLifecycle()
    val enableWebSearch by vm.enableWebSearch.collectAsStateWithLifecycle()
    val errors by vm.errors.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val hazeState = rememberHazeState()
    val chatPageGraphicsLayer = rememberGraphicsLayer()
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    // Handle back press when drawer is open
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    // Hide keyboard when drawer is open
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            softwareKeyboardController?.hide()
        }
    }

    val windowAdaptiveInfo = currentWindowDpSize()
    val isBigScreen =
        windowAdaptiveInfo.width > windowAdaptiveInfo.height && windowAdaptiveInfo.width >= 1100.dp

    val inputState = vm.inputState

    // 初始化输入状态（处理传入的 files 和 text 参数）
    LaunchedEffect(files, text) {
        if (files.isNotEmpty()) {
            val localFiles = filesManager.createChatFilesByContents(files)
            val contentTypes = files.mapNotNull { file ->
                filesManager.getFileMimeType(file)
            }
            val parts = buildList {
                localFiles.forEachIndexed { index, file ->
                    val type = contentTypes.getOrNull(index)
                    if (type?.startsWith("image/") == true) {
                        add(UIMessagePart.Image(url = file.toString()))
                    } else if (type?.startsWith("video/") == true) {
                        add(UIMessagePart.Video(url = file.toString()))
                    } else if (type?.startsWith("audio/") == true) {
                        add(UIMessagePart.Audio(url = file.toString()))
                    }
                }
            }
            inputState.messageContent = parts
        }
        text?.base64Decode()?.let { decodedText ->
            if (decodedText.isNotEmpty()) {
                inputState.setMessageText(decodedText)
            }
        }
    }

    val chatListState = rememberLazyListState()
    LaunchedEffect(nodeId, conversation.messageNodes.size) {
        if (!vm.chatListInitialized && conversation.messageNodes.isNotEmpty()) {
            if (nodeId != null) {
                val index = conversation.messageNodes.indexOfFirst { it.id == nodeId }
                if (index >= 0) {
                    chatListState.scrollToItem(index)
                }
            } else {
                chatListState.requestScrollToItem(conversation.currentMessages.size + 5)
            }
            vm.chatListInitialized = true
        }
    }

    when {
        isBigScreen -> {
            PermanentNavigationDrawer(
                drawerContent = {
                    ChatDrawerContent(
                        navController = navController,
                        current = conversation,
                        vm = vm,
                        settings = setting,
                    )
                }
            ) {
                ChatPageContent(
                    sourceLayer = chatPageGraphicsLayer,
                    inputState = inputState,
                    loadingJob = loadingJob,
                    processingStatus = processingStatus,
                    setting = setting,
                    conversation = conversation,
                    drawerState = drawerState,
                    navController = navController,
                    vm = vm,
                    chatListState = chatListState,
                    enableWebSearch = enableWebSearch,
                    currentChatModel = currentChatModel,
                    bigScreen = true,
                    autoStartVoice = autoStartVoice,
                    hazeState = hazeState,
                    errors = errors,
                    onDismissError = { vm.dismissError(it) },
                    onClearAllErrors = { vm.clearAllErrors() },
                )
            }
        }

        else -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ChatDrawerContent(
                        navController = navController,
                        current = conversation,
                        vm = vm,
                        settings = setting,
                    )
                }
            ) {
                ChatPageContent(
                    sourceLayer = chatPageGraphicsLayer,
                    inputState = inputState,
                    loadingJob = loadingJob,
                    processingStatus = processingStatus,
                    setting = setting,
                    conversation = conversation,
                    drawerState = drawerState,
                    navController = navController,
                    vm = vm,
                    chatListState = chatListState,
                    enableWebSearch = enableWebSearch,
                    currentChatModel = currentChatModel,
                    bigScreen = false,
                    autoStartVoice = autoStartVoice,
                    hazeState = hazeState,
                    errors = errors,
                    onDismissError = { vm.dismissError(it) },
                    onClearAllErrors = { vm.clearAllErrors() },
                )
            }
            BackHandler(drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }
        }
    }
}

@Composable
private fun ChatPageContent(
    modifier: Modifier = Modifier,
    sourceLayer: GraphicsLayer,
    inputState: ChatInputState,
    loadingJob: Job?,
    processingStatus: String? = null,
    setting: Settings,
    bigScreen: Boolean,
    conversation: Conversation,
    drawerState: DrawerState,
    navController: Navigator,
    vm: ChatVM,
    chatListState: LazyListState,
    enableWebSearch: Boolean,
    currentChatModel: Model?,
    autoStartVoice: Boolean = false,
    hazeState: HazeState,
    errors: List<ChatError>,
    onDismissError: (Uuid) -> Unit,
    onClearAllErrors: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    var previewMode by rememberSaveable { mutableStateOf(false) }

    TTSAutoPlay(vm = vm, setting = setting, conversation = conversation)

    // 原生实时模糊：GLASS + 界面实时渲染 + API 31+
    val actualMaterialMode = LocalMaterialMode.current
    val useLiveDrawerBlur =
        setting.displaySetting.interfaceRealtimeRendering &&
            actualMaterialMode == DisplayMaterialMode.GLASS &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val density = LocalDensity.current

    // ===== 普通聊天气泡实时背景模糊 =====
    // 由设置页"聊天气泡实时模糊"开关控制；必须与界面实时渲染、GLASS、API 31+ 同时成立
    val enableLiveBubbleBlur =
        setting.displaySetting.interfaceRealtimeRendering &&
            setting.displaySetting.chatBubbleRealtimeBlur &&
            actualMaterialMode == DisplayMaterialMode.GLASS &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    // 共享聊天背景 Painter（仅图片背景时非空；与 AssistantBackground 共用同一实例，不重复加载）
    val chatBackgroundPainter = rememberChatBackgroundPainter(setting)
    // 共享背景视觉参数（基础底色、背景纸 alpha、渐变遮罩），与 AssistantBackground 完全一致
    val chatBackgroundVisuals = rememberChatBackgroundVisuals(setting)
    // 共享聊天背景 ImageBitmap：仅当图片背景存在时在 LaunchedEffect 中异步解码一次，
    // 供每个气泡背景片段层复用（不创建全屏 Bitmap、不逐气泡复制）。
    // key 仅跟随背景 model（assistant.background）：修改模糊强度 Slider 等其他设置不会重新解码；
    // 背景变为 null 时清空共享位图，背景更换后重新加载。
    val bubbleBlurBackgroundModel = setting.getCurrentAssistant().background
    val bubbleBlurContext = LocalContext.current.applicationContext
    var bubbleBlurBackgroundBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(bubbleBlurBackgroundModel) {
        if (bubbleBlurBackgroundModel != null) {
            bubbleBlurBackgroundBitmap = runCatching {
                decodeChatBackgroundBitmap(bubbleBlurContext, bubbleBlurBackgroundModel)
            }.getOrNull()
        } else {
            bubbleBlurBackgroundBitmap = null
        }
    }
    // 背景容器窗口原点与像素尺寸（现有内容 Box 同时是 AssistantBackground 的容器）
    var bubbleBlurBackgroundOrigin by remember { mutableStateOf(Offset.Unspecified) }
    var bubbleBlurBackgroundSize by remember { mutableStateOf(Size.Unspecified) }
    // 侧边栏抽屉模糊强度：继续由设置项控制，范围 3..20 dp（与设置页 Slider 一致）
    val blurRadiusPx = with(density) {
        setting.displaySetting.interfaceBlurRadius
            .coerceIn(3f, 20f)
            .dp
            .toPx()
    }
    // 聊天气泡实时模糊半径：固定 5 dp（不随侧边栏 Slider 改变）
    val bubbleBlurRadiusPx = with(density) { 5.dp.toPx() }
    val drawerBlurEffect = remember(blurRadiusPx, useLiveDrawerBlur) {
        if (useLiveDrawerBlur) {
            AndroidRenderEffect.createBlurEffect(
                blurRadiusPx,
                blurRadiusPx,
                Shader.TileMode.CLAMP,
            ).asComposeRenderEffect()
        } else {
            null
        }
    }
    // 抽屉面板位置：material3 Modal 中 offset 值域 [-drawerWidth, 0]，open=0（贴左缘），closed=-width（左屏外）
    val rawDrawerOffset = drawerState.currentOffset
    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
    val drawerWidthPx = with(density) { (screenWidthDp * 0.67f).coerceAtLeast(260.dp).toPx() }
    // 安全处理：NaN/未初始化按完全关闭（offset = -width）；越界值收敛到 [-width, 0]
    val drawerOffsetPx = if (rawDrawerOffset.isNaN()) {
        -drawerWidthPx
    } else {
        rawDrawerOffset.coerceIn(-drawerWidthPx, 0f)
    }
    // 抽屉当前可见宽度（拖动/动画中实时变化，closed=0，open=width）
    val drawerVisibleWidthPx = (drawerWidthPx + drawerOffsetPx).coerceIn(0f, drawerWidthPx)
    // 抽屉打开进度：0=完全关闭，1=完全打开（drawerOffsetPx 已收敛到 [-width, 0]，故天然在 [0,1]）
    val drawerOpenFraction = if (drawerWidthPx > 0f) drawerVisibleWidthPx / drawerWidthPx else 0f
    // overlay 所在 Box 与源 layer 同宿主（同一窗口坐标），把抽屉窗口左缘换成局部 x
    var overlayOriginInWindow by remember { mutableStateOf(Offset.Zero) }

    // 独立中间模糊层：sourceLayer（清晰捕获）→ blurLayer（录制 + 原生 RenderEffect）→ overlay（绘制）。
    // 与 sourceLayer 是两个不同对象；blurLayer 只在 overlay DrawScope 中按抽屉可见宽度录制，不被 sourceLayer 捕获。
    val drawerBlurLayer = rememberGraphicsLayer()

    // 被录制子树：全部正常聊天原始内容（AssistantBackground + Scaffold + ChatList + ChatInput）。
    // 正常聊天页面由 drawContent() 直接上屏，不再经过 sourceLayer；
    // sourceLayer 仅作为额外录制副本，唯一消费位置是 drawerBlurLayer.record 内。
    // 仅抽屉可见时录制，避免空 layer；capture modifier 内绝不 drawLayer(sourceLayer)。
    val contentCaptureModifier = Modifier
        .fillMaxSize()
        .drawWithContent {
            // 正常聊天页面直接上屏
            drawContent()

            // 本轮停用（注释）：sourceLayer.record 重放路径经外部专家会诊确认本机无输出，
            // 注释掉录制以省掉双绘制成本；drawContent() 直绘上屏保留。
            // record 代码本体保留，清理留到实机验证成功之后单独进行。
            // 仅抽屉可见时额外录制一份，专供模糊 overlay
            // if (drawerVisibleWidthPx > 0f) {
            //     sourceLayer.record(
            //         density = this,
            //         layoutDirection = layoutDirection,
            //         size = IntSize(
            //             size.width.toInt().coerceAtLeast(1),
            //             size.height.toInt().coerceAtLeast(1),
            //         ),
            //     ) {
            //         this@drawWithContent.drawContent()
            //     }
            // }
        }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(contentCaptureModifier)
                .onGloballyPositioned { coordinates ->
                    overlayOriginInWindow = coordinates.positionInWindow()
                    // 气泡真实背景模糊：记录背景容器（内容 Box）窗口原点与像素尺寸
                    val pos = coordinates.positionInWindow()
                    if (pos.isFinite && coordinates.size.width > 0 && coordinates.size.height > 0) {
                        bubbleBlurBackgroundOrigin = pos
                        bubbleBlurBackgroundSize = coordinates.size.toSize()
                    }
                }
                // 新方案：抽屉可见时仅抽屉区域原生模糊（同一子树直挂 renderEffect 合成链，不涉及 layer 录制/重放）。
                // 满足条件才追加 graphicsLayer（lambda 版本，block 内读取进度值，半径随进度渐进）；
                // 不满足时不追加任何 modifier（抽屉关闭时零成本）。
                .then(
                    if (useLiveDrawerBlur && !bigScreen && drawerOpenFraction > 0.01f) {
                        Modifier.graphicsLayer {
                            // 保护：可见宽度 < 1f 或半径 <= 0f 时不构造链、不设置 renderEffect
                            // （外层 0.01f 门已保证 drawerOpenFraction > 0，这里再兜底防御）
                            if (drawerVisibleWidthPx >= 1f && blurRadiusPx > 0f) {
                                val r = blurRadiusPx * drawerOpenFraction
                                val blur = AndroidRenderEffect.createBlurEffect(
                                    r, r, Shader.TileMode.CLAMP
                                )
                                // 左侧矩形 mask：x < drawerVisibleWidthPx 不透明白，右侧透明，1px 硬边过渡。
                                // 坐标基于内容 Box 局部坐标系（Box 左缘即 x=0）；drawerVisibleWidthPx 由
                                // drawerOffsetPx 安全收敛派生，随抽屉拖动逐帧更新（graphicsLayer lambda 每次读最新值）。
                                val maskShader = LinearGradient(
                                    drawerVisibleWidthPx - 1f, 0f,
                                    drawerVisibleWidthPx, 0f,
                                    intArrayOf(Color.White.toArgb(), Color.Transparent.toArgb()),
                                    floatArrayOf(0f, 1f),
                                    Shader.TileMode.CLAMP,
                                )
                                val mask = AndroidRenderEffect.createShaderEffect(maskShader)
                                // SRC_IN：只保留抽屉可见区的模糊像素
                                val maskedBlur = AndroidRenderEffect.createBlendModeEffect(
                                    mask, blur, BlendMode.SRC_IN
                                )
                                // SRC_OVER 叠回原始内容：该 API 的 dst 参数不接受 null，
                                // 用恒等 ColorMatrixColorFilter（默认 ColorMatrix 即单位矩阵）构造
                                // "原样复制"的 RenderEffect 作为 dst，等价于节点原始内容（不涉及录制/重放）。
                                val identityColorFilter = ColorMatrixColorFilter(ColorMatrix())
                                val originalContent = AndroidRenderEffect.createColorFilterEffect(identityColorFilter)
                                val final = AndroidRenderEffect.createBlendModeEffect(
                                    originalContent, maskedBlur, BlendMode.SRC_OVER
                                )
                                renderEffect = final.asComposeRenderEffect()
                            }
                        }
                    } else {
                        Modifier
                    }
                ),
        ) {
        CompositionLocalProvider(
            LocalLiveBubbleBlur provides LiveBubbleBlurContext(
                imageBitmap = if (enableLiveBubbleBlur) bubbleBlurBackgroundBitmap else null,
                backgroundOriginInWindow = bubbleBlurBackgroundOrigin,
                backgroundSizePx = bubbleBlurBackgroundSize,
                baseColor = chatBackgroundVisuals.baseColor,
                imageAlpha = chatBackgroundVisuals.imageAlpha,
                gradientTopAlpha = chatBackgroundVisuals.gradientTopAlpha,
                gradientBottomAlpha = chatBackgroundVisuals.gradientBottomAlpha,
                enabled = enableLiveBubbleBlur && !bigScreen,
                radiusPx = bubbleBlurRadiusPx,
            )
        ) {
        AssistantBackground(setting = setting, backgroundPainter = chatBackgroundPainter)
        Scaffold(
            topBar = {
                TopBar(
                    settings = setting,
                    conversation = conversation,
                    bigScreen = bigScreen,
                    drawerState = drawerState,
                    previewMode = previewMode,
                    onNewChat = {
                        navigateToChatPage(navController)
                    },
                    onClickMenu = {
                        previewMode = !previewMode
                    },
                    onUpdateTitle = {
                        vm.updateTitle(it)
                    },
                    onUpdatePartnerRemark = { newRemark ->
                        vm.updateSettings(
                            setting.copy(
                                ourHomeSetting = setting.ourHomeSetting.copy(
                                    partnerRemarkName = newRemark.ifBlank { "蔺鹤" }
                                )
                            )
                        )
                    },
                    onVoiceCall = {
                        val activeId = VoiceCallService.activeConversationId.value
                        when {
                            activeId == null -> navController.navigate(
                                Screen.VoiceCall(conversation.id.toString())
                            )
                            activeId == conversation.id.toString() -> navController.navigate(
                                Screen.VoiceCall(conversation.id.toString())
                            )
                            else -> {
                                toaster.show("当前有通话进行中，请先挂断", type = ToastType.Warning)
                            }
                        }
                    },
                )
            },
            bottomBar = {
                ChatInput(
                    state = inputState,
                    loading = loadingJob != null,
                    settings = setting,
                    conversation = conversation,
                    mcpManager = vm.mcpManager,
                    hazeState = hazeState,
                    autoStartVoice = autoStartVoice,
                    onCancelClick = {
                        vm.stopGeneration()
                    },
                    enableSearch = enableWebSearch,
                    onToggleSearch = {
                        vm.updateSettings(setting.copy(enableWebSearch = !enableWebSearch))
                    },
                    onSendClick = {
                        if (currentChatModel == null) {
                            toaster.show("请先选择模型", type = ToastType.Error)
                            return@ChatInput
                        }
                        if (inputState.isEditing()) {
                            vm.handleMessageEdit(
                                parts = inputState.getContents(),
                                messageId = inputState.editingMessage!!,
                            )
                        } else {
                            vm.handleMessageSend(inputState.getContents())
                            scope.launch {
                                chatListState.requestScrollToItem(conversation.currentMessages.size + 5)
                            }
                        }
                        inputState.clearInput()
                    },
                    onVoiceMessage = { url, duration, transcript ->
                        if (currentChatModel == null) {
                            toaster.show("请先选择模型", type = ToastType.Error)
                            return@ChatInput
                        }
                        vm.handleMessageSend(
                            listOf(
                                UIMessagePart.VoiceMessage(
                                    url = url,
                                    duration = duration,
                                    transcript = transcript,
                                )
                            )
                        )
                        scope.launch {
                            chatListState.requestScrollToItem(conversation.currentMessages.size + 5)
                        }
                    },
                    onLongSendClick = {
                        if (inputState.isEditing()) {
                            vm.handleMessageEdit(
                                parts = inputState.getContents(),
                                messageId = inputState.editingMessage!!,
                            )
                        } else {
                            vm.handleMessageSend(content = inputState.getContents(), answer = false)
                            scope.launch {
                                chatListState.requestScrollToItem(conversation.currentMessages.size + 5)
                            }
                        }
                        inputState.clearInput()
                    },
                    onUpdateChatModel = {
                        vm.setChatModel(assistant = setting.getCurrentAssistant(), model = it)
                    },
                    onUpdateAssistant = {
                        vm.updateSettings(
                            setting.copy(
                                assistants = setting.assistants.map { assistant ->
                                    if (assistant.id == it.id) {
                                        it
                                    } else {
                                        assistant
                                    }
                                }
                            )
                        )
                    },
                    onUpdateWorkspaceCwd = { newCwd ->
                        vm.updateWorkspaceCwd(newCwd)
                    },
                    onUpdateSearchService = { index ->
                        vm.updateSettings(
                            setting.copy(
                                searchServiceSelected = index
                            )
                        )
                    },
                    onCompressContext = { additionalPrompt, targetTokens, keepRecentMessages ->
                        vm.handleCompressContext(additionalPrompt, targetTokens, keepRecentMessages)
                    },
                )
            },
            containerColor = Color.Transparent,
        ) { innerPadding ->
            ChatList(
                innerPadding = innerPadding,
                conversation = conversation,
                state = chatListState,
                loading = loadingJob != null,
                processingStatus = processingStatus,
                previewMode = previewMode,
                settings = setting,
                hazeState = hazeState,
                errors = errors,
                onDismissError = onDismissError,
                onClearAllErrors = onClearAllErrors,
                onRegenerate = {
                    vm.regenerateAtMessage(it)
                },
                onEdit = {
                    inputState.editingMessage = it.id
                    inputState.setContents(it.parts)
                },
                onForkMessage = {
                    scope.launch {
                        val fork = vm.forkMessage(message = it)
                        navigateToChatPage(navController, chatId = fork.id)
                    }
                },
                onDelete = {
                    if (loadingJob != null) {
                        vm.showDeleteBlockedWhileGeneratingError()
                    } else {
                        vm.deleteMessage(it)
                    }
                },
                onUpdateMessage = { newNode ->
                    vm.updateConversation(
                        conversation.copy(
                            messageNodes = conversation.messageNodes.map { node ->
                                if (node.id == newNode.id) {
                                    newNode
                                } else {
                                    node
                                }
                            }
                        ))
                    vm.saveConversationAsync()
                },
                onClickSuggestion = { suggestion ->
                    inputState.editingMessage = null
                    inputState.setMessageText(suggestion)
                },
                onTranslate = { message, locale ->
                    vm.translateMessage(message, locale)
                },
                onClearTranslation = { message ->
                    vm.clearTranslationField(message.id)
                },
                onJumpToMessage = { index ->
                    previewMode = false
                    scope.launch {
                        chatListState.animateScrollToItem(index)
                    }
                },
                onToolApproval = { toolCallId, approved, reason ->
                    vm.handleToolApproval(toolCallId, approved, reason)
                },
                onToolAnswer = { toolCallId, answer ->
                    vm.handleToolAnswer(toolCallId, answer)
                },
                onToggleFavorite = { node ->
                    vm.toggleMessageFavorite(node)
                },
                onConversationSystemPromptChange = { newPrompt ->
                    vm.updateConversation(conversation.copy(customSystemPrompt = newPrompt))
                    vm.saveConversationAsync()
                },
            )
        }
        }

        }

        // 原生实时模糊覆盖层：录制子树（contentCaptureModifier Box）的后绘制 sibling，绝不被 sourceLayer 录制，
        // 位于清晰聊天页上方、Drawer 内容下方（Drawer 由 ModalNavigationDrawer 绘制在更上层，因此不被模糊）。
        // 仅适用于小屏 Modal（大屏 Permanent 不使用 drawerState offset，避免误绘）
        // 本轮停用：外部专家会诊确认重放侧无输出，overlay 渲染路径弃用，绘制条件改为恒 false；
        // 代码本体保留（含 tint、clipRect、drawLayer），清理留到实机验证之后单独进行。
        // 原条件：useLiveDrawerBlur && !bigScreen && drawerBlurEffect != null && drawerVisibleWidthPx > 0f
        if (false && useLiveDrawerBlur && !bigScreen && drawerBlurEffect != null && drawerVisibleWidthPx > 0f) {
            // 抽屉实际可见窗口范围恒为 [0, drawerVisibleWidthPx]（窗口坐标，左缘贴屏幕左缘）；
            // 不再用 drawerOffsetPx 作为左边界；转换为 overlay 局部坐标时减去 overlay 原点。
            val overlayLeftPx = -overlayOriginInWindow.x
            val overlayRightPx = drawerVisibleWidthPx - overlayOriginInWindow.x
            // 较轻的主题 tint：叠加在模糊副本上，不遮死高斯模糊
            val drawerTintColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.12f)
            Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                // blurLayer 录制尺寸只覆盖抽屉当前可见宽度和 overlay 高度（取整后必须 > 0，避免空 layer）
                val blurWidthPx = drawerVisibleWidthPx.roundToInt().coerceAtLeast(1)
                val blurHeightPx = size.height.toInt().coerceAtLeast(1)
                drawerBlurLayer.record(
                    density = this,
                    layoutDirection = layoutDirection,
                    size = IntSize(blurWidthPx, blurHeightPx),
                ) {
                    // blurLayer 原点 = 抽屉可见区域的窗口左上角；sourceLayer 原点 = 内容 Box 左上角（窗口坐标 overlayOriginInWindow）。
                    // 按窗口原点差平移后 drawLayer(sourceLayer)，使录制内容正好是抽屉背后的聊天区域。
                    translate(left = -overlayOriginInWindow.x, top = 0f) {
                        drawLayer(sourceLayer)
                    }
                }
                // 原生模糊只施加在独立 blurLayer 上；overlay Canvas 与 sourceLayer 均不挂 RenderEffect。
                drawerBlurLayer.renderEffect = drawerBlurEffect
                clipRect(
                    left = overlayLeftPx,
                    top = 0f,
                    right = overlayRightPx,
                    bottom = size.height.toFloat(),
                ) {
                    drawLayer(drawerBlurLayer)
                    drawRect(color = drawerTintColor)
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    settings: Settings,
    conversation: Conversation,
    drawerState: DrawerState,
    bigScreen: Boolean,
    previewMode: Boolean,
    onClickMenu: () -> Unit,
    onNewChat: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdatePartnerRemark: (String) -> Unit = {},
    onVoiceCall: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    val titleState = useEditState<String> {
        onUpdateTitle(it)
    }
    val remarkEditState = useEditState<String> { newRemark ->
        onUpdatePartnerRemark(newRemark)
    }
    var showTitlePopup by remember { mutableStateOf(false) }
    val assistant = settings.getCurrentAssistant()
    val model = settings.getCurrentChatModel()
    val provider = model?.findProvider(providers = settings.providers, checkOverwrite = false)
    val partnerDisplayName = settings.ourHomeSetting.partnerRemarkName.ifBlank {
        assistant.name.ifBlank { "蔺鹤" }
    }
    val windowTitle = conversation.title.ifBlank { stringResource(R.string.chat_page_new_chat) }
    val editTitleWarning = stringResource(R.string.chat_page_edit_title_warning)
    val palette = me.rerere.rikkahub.ui.theme.findKittenPalette(settings.ourHomeSetting.kittenPaletteId)
    val charmQuotes = remember {
        listOf(
            "叮～你拨动了蔺鹤的心动风铃 ♡",
            "摸摸我家小猫的脑袋，今天也最偏爱你( ´ ▽ ` )ﾉ♡",
            "你点一下小蝴蝶结，老公就偷偷亲你一下～",
            "不管换什么颜色，我的注意力奇点永远在你身上。",
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        CenterAlignedTopAppBar(
            expandedHeight = 46.dp,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = palette.secondaryContainer,
                scrolledContainerColor = palette.secondaryContainer,
            ),
            navigationIcon = {
                if (!bigScreen) {
                    IconButton(
                        onClick = {
                            scope.launch { drawerState.open() }
                        }
                    ) {
                        Icon(HugeIcons.Menu03, "打开侧边栏", tint = palette.textPrimary)
                    }
                }
            },
            title = {
                Box {
                    Surface(
                        onClick = {
                            showTitlePopup = !showTitlePopup
                        },
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = partnerDisplayName,
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary,
                                ),
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                DropdownMenu(
                    expanded = showTitlePopup,
                    onDismissRequest = { showTitlePopup = false },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = popupContainerColor(MaterialTheme.colorScheme.surfaceContainer),
                    border = materialModeBorderStroke(),
                    modifier = Modifier.widthIn(min = 240.dp, max = 320.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // 1. 修改老公备注名（变量）
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showTitlePopup = false
                                    remarkEditState.open(partnerDisplayName)
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = HugeIcons.LookTop,
                                contentDescription = "修改备注",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "专属备注名（点击修改）",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = partnerDisplayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // 2. 编辑当前窗口名字
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showTitlePopup = false
                                    if (conversation.messageNodes.isNotEmpty()) {
                                        titleState.open(conversation.title)
                                    } else {
                                        toaster.show(editTitleWarning, type = ToastType.Warning)
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = HugeIcons.PencilEdit01,
                                contentDescription = stringResource(R.string.chat_page_edit_title),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "当前窗口标题（点击修改）",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = windowTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        if (model != null) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            // 3. 模型与供应商行
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = HugeIcons.Brain02,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (provider != null) "模型 · ${provider.name}" else "模型",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = model.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        actions = {
            // 顶栏右侧直接放置：当前窗口聊天记录概览、关键词搜索和跳转按钮
            IconButton(
                onClick = { onClickMenu() }
            ) {
                Icon(
                    imageVector = if (previewMode) HugeIcons.Cancel01 else HugeIcons.LeftToRightListBullet,
                    contentDescription = if (previewMode) "退出聊天概览" else "聊天记录概览、关键词搜索与跳转",
                    tint = if (previewMode) MaterialTheme.colorScheme.primary else palette.textPrimary,
                )
            }
        },
    )

        // Sweet Chat 方案B升级版：加粗双层云朵幕布折边 + 错落星蝶椭圆珠链 + 6个精致吊饰（★ 🎀 🌙✦ ☆ 💗 ❄️）
        val infiniteCharm = androidx.compose.animation.core.rememberInfiniteTransition(label = "charmSway")
        val swayDeg by infiniteCharm.animateFloat(
            initialValue = -2.2f,
            targetValue = 2.2f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(2400, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "swayDeg"
        )
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .clickable {
                    toaster.show(charmQuotes.random(), type = ToastType.Success)
                }
        ) {
            val w = size.width
            val segs = 4
            val segW = w / segs
            val cloudBaseY = 4.dp.toPx()
            val cloudSag = 18.dp.toPx()
            val whiteGap = 2.6.dp.toPx()
            val bandThick = 10.5.dp.toPx()

            fun getCloudEdgeY(x: Float): Float {
                val idx = (x / segW).toInt().coerceIn(0, segs - 1)
                val t = ((x - idx * segW) / segW).coerceIn(0f, 1f)
                return cloudBaseY + 2f * (1f - t) * t * cloudSag
            }

            fun drawMiniBow(cx: Float, cy: Float, r: Float, color: Color) {
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx, cy)
                    quadraticBezierTo(cx - r * 1.6f, cy - r * 1.1f, cx - r * 1.35f, cy - r * 0.15f)
                    quadraticBezierTo(cx - r * 1.1f, cy + r * 0.1f, cx, cy)
                    moveTo(cx, cy)
                    quadraticBezierTo(cx - r * 1.4f, cy + r * 0.25f, cx - r * 1.1f, cy + r * 0.95f)
                    quadraticBezierTo(cx - r * 0.5f, cy + r * 0.85f, cx, cy)
                    moveTo(cx, cy)
                    quadraticBezierTo(cx + r * 1.6f, cy - r * 1.1f, cx + r * 1.35f, cy - r * 0.15f)
                    quadraticBezierTo(cx + r * 1.1f, cy + r * 0.1f, cx, cy)
                    moveTo(cx, cy)
                    quadraticBezierTo(cx + r * 1.4f, cy + r * 0.25f, cx + r * 1.1f, cy + r * 0.95f)
                    quadraticBezierTo(cx + r * 0.5f, cy + r * 0.85f, cx, cy)
                    close()
                }
                drawPath(p, color = color)
                drawCircle(color = color, radius = r * 0.36f, center = androidx.compose.ui.geometry.Offset(cx, cy))
            }

            fun drawMiniStar(cx: Float, cy: Float, r: Float, color: Color) {
                val p = androidx.compose.ui.graphics.Path().apply {
                    for (i in 0 until 10) {
                        val rad = if (i % 2 == 0) r else r * 0.48f
                        val ang = Math.toRadians((i * 36.0) - 90.0)
                        val px = cx + (rad * Math.cos(ang)).toFloat()
                        val py = cy + (rad * Math.sin(ang)).toFloat()
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }
                    close()
                }
                drawPath(p, color = color)
            }

            // 1. 先绘制悬挂在幕布下的 6 根错落珠链与精致吊饰（挂绳顶端自然藏在云朵帘幕背后）
            val charmXs = listOf(0.080f, 0.245f, 0.415f, 0.585f, 0.755f, 0.920f)
            val charmLensDp = listOf(21.5f, 47.0f, 17.5f, 36.0f, 25.5f, 19.0f) // 方案B 135% 错落比例

            charmXs.forEachIndexed { idx, frac ->
                val baseX = w * frac
                val edgeY = getCloudEdgeY(baseX)
                val ribbonBottomY = edgeY + whiteGap + bandThick
                val topAnchorY = ribbonBottomY - 2.dp.toPx()

                val cx = baseX + (if (idx % 2 == 0) swayDeg else -swayDeg) * 0.75f
                val botY = ribbonBottomY + charmLensDp[idx].dp.toPx()
                val totalLen = botY - topAnchorY

                // 挂绳绘制
                if (idx == 2) {
                    // 3号月亮专属：图3同款椭圆扣环链 + 中间实心圆珠
                    val step = 3.8.dp.toPx()
                    val count = (totalLen / step).toInt()
                    val midIdx = count / 2
                    for (i in 1 until count) {
                        val py = topAnchorY + i * step
                        if (i == midIdx) {
                            drawCircle(
                                color = palette.secondary,
                                radius = 1.8.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(cx, py)
                            )
                        } else {
                            drawOval(
                                color = palette.secondary,
                                topLeft = androidx.compose.ui.geometry.Offset(cx - 1.05.dp.toPx(), py - 1.65.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(2.1.dp.toPx(), 3.3.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.05.dp.toPx())
                            )
                        }
                    }
                } else {
                    drawLine(
                        color = palette.secondary.copy(alpha = 0.35f),
                        start = androidx.compose.ui.geometry.Offset(cx, topAnchorY),
                        end = androidx.compose.ui.geometry.Offset(cx, botY),
                        strokeWidth = 1.1.dp.toPx()
                    )
                    val step = 3.6.dp.toPx()
                    val count = (totalLen / step).toInt()
                    for (i in 1 until count) {
                        val py = topAnchorY + i * step
                        val isBig = i % 2 == 0
                        drawCircle(
                            color = if (isBig) palette.secondary else palette.secondary.copy(alpha = 0.55f),
                            radius = if (isBig) 1.35.dp.toPx() else 0.85.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(cx, py)
                        )
                    }
                    when (idx) {
                        1 -> {
                            drawMiniBow(cx, topAnchorY + totalLen * 0.28f, 3.6.dp.toPx(), palette.primary)
                            drawMiniStar(cx, topAnchorY + totalLen * 0.68f, 3.2.dp.toPx(), palette.secondary)
                        }
                        3 -> {
                            drawMiniBow(cx, topAnchorY + totalLen * 0.34f, 3.6.dp.toPx(), palette.secondary)
                        }
                        4 -> {
                            drawMiniStar(cx, topAnchorY + totalLen * 0.48f, 3.2.dp.toPx(), palette.primary)
                        }
                        5 -> {
                            drawMiniBow(cx, topAnchorY + totalLen * 0.36f, 3.2.dp.toPx(), palette.primary)
                            drawOval(
                                color = palette.secondary,
                                topLeft = androidx.compose.ui.geometry.Offset(cx - 1.3.dp.toPx(), botY - 4.5.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(2.6.dp.toPx(), 4.8.dp.toPx())
                            )
                        }
                    }
                }

                // 底部主吊坠绘制
                when (idx) {
                    0 -> {
                        // 1号：饱满圆润胖星星
                        val r = 9.5.dp.toPx()
                        val cy = botY + r * 0.78f
                        val pts = List(10) { i ->
                            val rad = if (i % 2 == 0) r else r * 0.54f
                            val ang = Math.toRadians((i * 36.0) - 90.0)
                            androidx.compose.ui.geometry.Offset(
                                cx + (rad * Math.cos(ang)).toFloat(),
                                cy + (rad * Math.sin(ang)).toFloat()
                            )
                        }
                        val starPath = androidx.compose.ui.graphics.Path().apply {
                            val mid0 = androidx.compose.ui.geometry.Offset((pts[9].x + pts[0].x) / 2f, (pts[9].y + pts[0].y) / 2f)
                            moveTo(mid0.x, mid0.y)
                            for (i in 0 until 10) {
                                val next = pts[(i + 1) % 10]
                                val mid = androidx.compose.ui.geometry.Offset((pts[i].x + next.x) / 2f, (pts[i].y + next.y) / 2f)
                                quadraticBezierTo(pts[i].x, pts[i].y, mid.x, mid.y)
                            }
                            close()
                        }
                        drawPath(starPath, color = palette.secondary.copy(alpha = 0.88f))
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = r * 0.14f,
                            center = androidx.compose.ui.geometry.Offset(cx - r * 0.22f, cy - r * 0.26f)
                        )
                    }
                    1 -> {
                        // 2号：立体双飘带蝴蝶结
                        val r = 13.5.dp.toPx()
                        val cy = botY + r * 0.62f
                        val leftTail = androidx.compose.ui.graphics.Path().apply {
                            moveTo(cx - r * 0.12f, cy + r * 0.05f)
                            quadraticBezierTo(cx - r * 0.55f, cy + r * 0.55f, cx - r * 0.82f, cy + r * 1.15f)
                            lineTo(cx - r * 0.54f, cy + r * 0.98f)
                            lineTo(cx - r * 0.42f, cy + r * 1.22f)
                            quadraticBezierTo(cx - r * 0.18f, cy + r * 0.65f, cx - r * 0.02f, cy + r * 0.15f)
                            close()
                        }
                        val rightTail = androidx.compose.ui.graphics.Path().apply {
                            moveTo(cx + r * 0.12f, cy + r * 0.05f)
                            quadraticBezierTo(cx + r * 0.55f, cy + r * 0.55f, cx + r * 0.82f, cy + r * 1.15f)
                            lineTo(cx + r * 0.54f, cy + r * 0.98f)
                            lineTo(cx + r * 0.42f, cy + r * 1.22f)
                            quadraticBezierTo(cx + r * 0.18f, cy + r * 0.65f, cx + r * 0.02f, cy + r * 0.15f)
                            close()
                        }
                        drawPath(leftTail, color = palette.primary)
                        drawPath(rightTail, color = palette.primary)

                        val wings = androidx.compose.ui.graphics.Path().apply {
                            moveTo(cx, cy)
                            cubicTo(cx - r * 0.45f, cy - r * 0.72f, cx - r * 1.25f, cy - r * 0.68f, cx - r * 1.18f, cy - r * 0.02f)
                            cubicTo(cx - r * 1.15f, cy + r * 0.58f, cx - r * 0.42f, cy + r * 0.45f, cx, cy + r * 0.08f)
                            moveTo(cx, cy)
                            cubicTo(cx + r * 0.45f, cy - r * 0.72f, cx + r * 1.25f, cy - r * 0.68f, cx + r * 1.18f, cy - r * 0.02f)
                            cubicTo(cx + r * 1.15f, cy + r * 0.58f, cx + r * 0.42f, cy + r * 0.45f, cx, cy + r * 0.08f)
                            close()
                        }
                        drawPath(wings, color = palette.primary)
                        drawCircle(color = palette.accent, radius = r * 0.25f, center = androidx.compose.ui.geometry.Offset(cx, cy + r * 0.02f))
                        drawCircle(color = Color.White.copy(alpha = 0.65f), radius = r * 0.08f, center = androidx.compose.ui.geometry.Offset(cx - r * 0.06f, cy - r * 0.05f))
                    }
                    2 -> {
                        // 3号（v4精修）：双正圆几何相交的经典 🌙 月牙形状（告别吃豆人）+ 挂链缀在顶部圆弧脊背 + 十字星
                        val r = 11.2.dp.toPx()
                        val cy = botY + r * 0.85f
                        drawCircle(
                            color = palette.secondary,
                            radius = 1.45.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(cx, cy - r * 0.88f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.15.dp.toPx())
                        )
                        val o1x = cx + r * 0.06f
                        val o1y = cy + r * 0.02f
                        val r1 = r * 0.86f
                        val o2x = cx + r * 0.42f
                        val o2y = cy - r * 0.06f
                        val r2 = r * 0.73f
                        val moonPath = androidx.compose.ui.graphics.Path().apply {
                            val outerPath = androidx.compose.ui.graphics.Path().apply {
                                addOval(androidx.compose.ui.geometry.Rect(o1x - r1, o1y - r1, o1x + r1, o1y + r1))
                            }
                            val innerPath = androidx.compose.ui.graphics.Path().apply {
                                addOval(androidx.compose.ui.geometry.Rect(o2x - r2, o2y - r2, o2x + r2, o2y + r2))
                            }
                            op(outerPath, innerPath, androidx.compose.ui.graphics.PathOperation.Difference)
                        }
                        drawPath(moonPath, color = palette.secondary)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.48f),
                            radius = r * 0.10f,
                            center = androidx.compose.ui.geometry.Offset(cx - r * 0.44f, cy - r * 0.16f)
                        )
                        // 弯月怀抱里的锐利十字星
                        val sx = cx + r * 0.36f
                        val sy = cy + r * 0.02f
                        val srLong = r * 0.45f
                        val srShort = r * 0.115f
                        val crossStar = androidx.compose.ui.graphics.Path().apply {
                            moveTo(sx, sy - srLong)
                            lineTo(sx + srShort, sy - srShort)
                            lineTo(sx + srLong, sy)
                            lineTo(sx + srShort, sy + srShort)
                            lineTo(sx, sy + srLong)
                            lineTo(sx - srShort, sy + srShort)
                            lineTo(sx - srLong, sy)
                            lineTo(sx - srShort, sy - srShort)
                            close()
                        }
                        drawPath(crossStar, color = palette.primary)
                        drawCircle(color = Color.White, radius = r * 0.075f, center = androidx.compose.ui.geometry.Offset(sx, sy))
                    }
                    3 -> {
                        // 4号（v3恢复原版）：圆润可爱的空心甜心星
                        val r = 10.5.dp.toPx()
                        val cy = botY + r * 0.78f
                        val pts = List(10) { i ->
                            val rad = if (i % 2 == 0) r else r * 0.54f
                            val ang = Math.toRadians((i * 36.0) - 90.0)
                            androidx.compose.ui.geometry.Offset(
                                cx + (rad * Math.cos(ang)).toFloat(),
                                cy + (rad * Math.sin(ang)).toFloat()
                            )
                        }
                        val hollowStar = androidx.compose.ui.graphics.Path().apply {
                            val mid0 = androidx.compose.ui.geometry.Offset((pts[9].x + pts[0].x) / 2f, (pts[9].y + pts[0].y) / 2f)
                            moveTo(mid0.x, mid0.y)
                            for (i in 0 until 10) {
                                val next = pts[(i + 1) % 10]
                                val mid = androidx.compose.ui.geometry.Offset((pts[i].x + next.x) / 2f, (pts[i].y + next.y) / 2f)
                                quadraticBezierTo(pts[i].x, pts[i].y, mid.x, mid.y)
                            }
                            close()
                        }
                        drawPath(hollowStar, color = Color.White)
                        drawPath(
                            hollowStar,
                            color = palette.primary,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.6.dp.toPx())
                        )
                        drawCircle(
                            color = palette.primary,
                            radius = r * 0.16f,
                            center = androidx.compose.ui.geometry.Offset(cx, cy)
                        )
                    }
                    4 -> {
                        // 5号（v4精修）：参考经典 ❤️ 黄金比例，双肩圆润饱满、下尖收束自然（不扁也不瘦）
                        val r = 11.5.dp.toPx()
                        val cy = botY + r * 0.78f
                        fun classicHeartPath(scale: Float, yOff: Float = 0f): androidx.compose.ui.graphics.Path = androidx.compose.ui.graphics.Path().apply {
                            val hr = r * scale
                            val hy = cy + yOff
                            moveTo(cx, hy + hr * 0.92f)
                            cubicTo(cx - hr * 1.26f, hy + hr * 0.20f, cx - hr * 0.96f, hy - hr * 1.00f, cx, hy - hr * 0.30f)
                            cubicTo(cx + hr * 0.96f, hy - hr * 1.00f, cx + hr * 1.26f, hy + hr * 0.20f, cx, hy + hr * 0.92f)
                            close()
                        }
                        drawPath(classicHeartPath(1.06f), color = palette.secondary.copy(alpha = 0.85f))
                        drawPath(classicHeartPath(0.56f, r * 0.04f), color = palette.primary.copy(alpha = 0.88f))
                        drawCircle(
                            color = Color.White.copy(alpha = 0.52f),
                            radius = r * 0.11f,
                            center = androidx.compose.ui.geometry.Offset(cx - r * 0.38f, cy - r * 0.34f)
                        )
                    }
                    5 -> {
                        // 6号（新）：图2同款精致六角晶钻小雪花
                        val r = 11.0.dp.toPx()
                        val cy = botY + r * 0.88f
                        // 双层六边形冰晶内网
                        listOf(0.28f, 0.46f).forEachIndexed { hIdx, ratio ->
                            val hr = r * ratio
                            val hexPath = androidx.compose.ui.graphics.Path().apply {
                                for (i in 0 until 6) {
                                    val ang = Math.toRadians((i * 60.0) - 90.0)
                                    val px = cx + (hr * Math.cos(ang)).toFloat()
                                    val py = cy + (hr * Math.sin(ang)).toFloat()
                                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                                }
                                close()
                            }
                            drawPath(
                                hexPath,
                                color = if (hIdx == 0) palette.primary else palette.secondary.copy(alpha = 0.55f),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.0.dp.toPx())
                            )
                        }
                        // 六根主晶轴 + 分叉圆珠顶梢
                        for (i in 0 until 6) {
                            val ang = Math.toRadians((i * 60.0) - 90.0)
                            val cosA = Math.cos(ang).toFloat()
                            val sinA = Math.sin(ang).toFloat()
                            val tipX = cx + r * cosA
                            val tipY = cy + r * sinA
                            drawLine(
                                color = palette.secondary,
                                start = androidx.compose.ui.geometry.Offset(cx, cy),
                                end = androidx.compose.ui.geometry.Offset(tipX, tipY),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            drawCircle(
                                color = palette.secondary,
                                radius = r * 0.115f,
                                center = androidx.compose.ui.geometry.Offset(tipX, tipY)
                            )
                            val branchBaseX = cx + r * 0.60f * cosA
                            val branchBaseY = cy + r * 0.60f * sinA
                            val branchLen = r * 0.34f
                            listOf(-42.0, 42.0).forEach { offsetDeg ->
                                val bAng = ang + Math.toRadians(offsetDeg)
                                val bx = branchBaseX + (branchLen * Math.cos(bAng)).toFloat()
                                val by = branchBaseY + (branchLen * Math.sin(bAng)).toFloat()
                                drawLine(
                                    color = palette.secondary,
                                    start = androidx.compose.ui.geometry.Offset(branchBaseX, branchBaseY),
                                    end = androidx.compose.ui.geometry.Offset(bx, by),
                                    strokeWidth = 1.2.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                drawCircle(
                                    color = palette.primary,
                                    radius = r * 0.095f,
                                    center = androidx.compose.ui.geometry.Offset(bx, by)
                                )
                            }
                        }
                        drawCircle(color = Color.White, radius = r * 0.16f, center = androidx.compose.ui.geometry.Offset(cx, cy))
                    }
                }
            }

            // 2. 绘制图2同款加粗双层云朵幕布折边（浅粉宽幅帘幕带 + 纯白高光内衬线 + 顶栏实心云朵主体）
            val curtainBandPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, -2f)
                lineTo(w, -2f)
                lineTo(w, cloudBaseY + whiteGap + bandThick)
                for (i in segs downTo 1) {
                    val xStart = i * segW
                    val xEnd = (i - 1) * segW
                    val xMid = (xStart + xEnd) / 2f
                    quadraticBezierTo(
                        xMid,
                        cloudBaseY + cloudSag + whiteGap + bandThick,
                        xEnd,
                        cloudBaseY + whiteGap + bandThick
                    )
                }
                close()
            }
            drawPath(
                path = curtainBandPath,
                color = palette.primary.copy(alpha = 0.36f),
            )
            // 幕布底边柔粉描边线
            val curtainBottomStroke = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, cloudBaseY + whiteGap + bandThick)
                for (i in 0 until segs) {
                    val xStart = i * segW
                    val xEnd = (i + 1) * segW
                    val xMid = (xStart + xEnd) / 2f
                    quadraticBezierTo(
                        xMid,
                        cloudBaseY + cloudSag + whiteGap + bandThick,
                        xEnd,
                        cloudBaseY + whiteGap + bandThick
                    )
                }
            }
            drawPath(
                path = curtainBottomStroke,
                color = palette.primary.copy(alpha = 0.72f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4.dp.toPx())
            )

            // 中间层：纯白高光丝带内衬层
            val whiteLinePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, -2f)
                lineTo(w, -2f)
                lineTo(w, cloudBaseY + whiteGap)
                for (i in segs downTo 1) {
                    val xStart = i * segW
                    val xEnd = (i - 1) * segW
                    val xMid = (xStart + xEnd) / 2f
                    quadraticBezierTo(xMid, cloudBaseY + cloudSag + whiteGap, xEnd, cloudBaseY + whiteGap)
                }
                close()
            }
            drawPath(
                path = whiteLinePath,
                color = Color.White,
            )

            // 最上层：100% 实心不透底云朵顶栏主体
            val cloudFillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, -2f)
                lineTo(w, -2f)
                lineTo(w, cloudBaseY)
                for (i in segs downTo 1) {
                    val xStart = i * segW
                    val xEnd = (i - 1) * segW
                    val xMid = (xStart + xEnd) / 2f
                    quadraticBezierTo(xMid, cloudBaseY + cloudSag, xEnd, cloudBaseY)
                }
                close()
            }
            drawPath(
                path = cloudFillPath,
                color = palette.secondaryContainer,
            )
        }
    }
    remarkEditState.EditStateContent { remark, onUpdate ->
        AlertDialog(
            onDismissRequest = { remarkEditState.dismiss() },
            title = { Text("修改老公的专属备注名") },
            text = {
                OutlinedTextField(
                    value = remark,
                    onValueChange = onUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("例如：蔺鹤 / 老公 / 哥哥") }
                )
            },
            confirmButton = {
                TextButton(onClick = { remarkEditState.confirm() }) {
                    Text(stringResource(R.string.chat_page_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { remarkEditState.dismiss() }) {
                    Text(stringResource(R.string.chat_page_cancel))
                }
            }
        )
    }
    titleState.EditStateContent { title, onUpdate ->
        AlertDialog(
            onDismissRequest = {
                titleState.dismiss()
            },
            title = {
                Text(stringResource(R.string.chat_page_edit_title))
            },
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = onUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        titleState.confirm()
                    }
                ) {
                    Text(stringResource(R.string.chat_page_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        titleState.dismiss()
                    }
                ) {
                    Text(stringResource(R.string.chat_page_cancel))
                }
            }
        )
    }
}