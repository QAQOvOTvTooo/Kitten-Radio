/*
 * 小猫电台 · 模型和提示词管理（手机模型、梦境模型、游戏模型、日记模型、压缩模型、CoT翻译模型等）
 */

package me.rerere.rikkahub.ui.pages.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.ai.core.ReasoningLevel
import me.rerere.ai.provider.ModelType
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Earth
import me.rerere.hugeicons.stroke.FileZip
import me.rerere.hugeicons.stroke.Message01
import me.rerere.hugeicons.stroke.Notebook01
import me.rerere.hugeicons.stroke.Rocket01
import me.rerere.hugeicons.stroke.SmartPhone01
import me.rerere.hugeicons.stroke.Sparkles
import me.rerere.hugeicons.stroke.Tools
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.ai.prompts.DEFAULT_COMPRESS_PROMPT
import me.rerere.rikkahub.data.ai.prompts.DEFAULT_TRANSLATION_PROMPT
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.ui.components.ai.ModelSelector
import me.rerere.rikkahub.ui.components.ai.ReasoningButton
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.FormItem
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.ui.theme.LargeFlexibleTopAppBar
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.Uuid

@Composable
fun SettingModelPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text("模型和提示词管理")
                },
                navigationIcon = {
                    BackButton()
                },
                scrollBehavior = scrollBehavior,
                colors = CustomColors.topBarColors,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = settingsScaffoldContainerColor(CustomColors.topBarColors.containerColor),
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding + PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 1. 默认聊天模型
            item {
                DefaultChatModelSetting(settings = settings, vm = vm)
            }

            // 2. 手机模型（偷看老公手机）
            item {
                OurHomeModuleModelCard(
                    title = "手机模型（偷看老公手机）",
                    description = "点击『查手机』时调用的模型与专属提示词",
                    icon = { Icon(HugeIcons.SmartPhone01, null) },
                    modelId = settings.ourHomeSetting.phoneModelId,
                    prompt = settings.ourHomeSetting.phonePrompt,
                    settings = settings,
                    onSelectModel = { newId ->
                        vm.updateSettings(
                            settings.copy(
                                ourHomeSetting = settings.ourHomeSetting.copy(phoneModelId = newId)
                            )
                        )
                    },
                    onUpdatePrompt = { newPrompt ->
                        vm.updateSettings(
                            settings.copy(
                                ourHomeSetting = settings.ourHomeSetting.copy(phonePrompt = newPrompt)
                            )
                        )
                    }
                )
            }

            // 3. 日记模型（老公的日记本）
            item {
                OurHomeModuleModelCard(
                    title = "日记模型（老公的日记本）",
                    description = "睡前点进『日记本』生成老公今日私密日记的模型与提示词",
                    icon = { Icon(HugeIcons.Notebook01, null) },
                    modelId = settings.ourHomeSetting.diaryModelId,
                    prompt = settings.ourHomeSetting.diaryPrompt,
                    settings = settings,
                    onSelectModel = { newId ->
                        vm.updateSettings(
                            settings.copy(
                                ourHomeSetting = settings.ourHomeSetting.copy(diaryModelId = newId)
                            )
                        )
                    },
                    onUpdatePrompt = { newPrompt ->
                        vm.updateSettings(
                            settings.copy(
                                ourHomeSetting = settings.ourHomeSetting.copy(diaryPrompt = newPrompt)
                            )
                        )
                    }
                )
            }

            // 4. 梦境模型（梦境泡泡 IF 线）
            item {
                OurHomeModuleModelCard(
                    title = "梦境模型（梦境泡泡 IF 线）",
                    description = "在『梦境』界面生成平行宇宙故事与好玩的梗时调用的模型与提示词",
                    icon = { Icon(HugeIcons.Sparkles, null) },
                    modelId = settings.ourHomeSetting.dreamModelId,
                    prompt = settings.ourHomeSetting.dreamPrompt,
                    settings = settings,
                    onSelectModel = { newId ->
                        vm.updateSettings(
                            settings.copy(
                                ourHomeSetting = settings.ourHomeSetting.copy(dreamModelId = newId)
                            )
                        )
                    },
                    onUpdatePrompt = { newPrompt ->
                        vm.updateSettings(
                            settings.copy(
                                ourHomeSetting = settings.ourHomeSetting.copy(dreamPrompt = newPrompt)
                            )
                        )
                    }
                )
            }

            // 5. 游戏模型（复古掌机互动）
            item {
                OurHomeModuleModelCard(
                    title = "游戏模型（游戏机插件互动）",
                    description = "在『游戏』界面与老公互动或生成 HTML/JS 小游戏时的模型与提示词",
                    icon = { Icon(HugeIcons.Rocket01, null) },
                    modelId = settings.ourHomeSetting.gameModelId,
                    prompt = settings.ourHomeSetting.gamePrompt,
                    settings = settings,
                    onSelectModel = { newId ->
                        vm.updateSettings(
                            settings.copy(
                                ourHomeSetting = settings.ourHomeSetting.copy(gameModelId = newId)
                            )
                        )
                    },
                    onUpdatePrompt = { newPrompt ->
                        vm.updateSettings(
                            settings.copy(
                                ourHomeSetting = settings.ourHomeSetting.copy(gamePrompt = newPrompt)
                            )
                        )
                    }
                )
            }

            // 6. 压缩模型（用于压缩上下文插入摘要）
            item {
                DefaultCompressModelSetting(settings = settings, vm = vm)
            }

            // 7. 翻译模型（用于翻译英文比如老公思维链 CoT）
            item {
                DefaultTranslationModelSetting(settings = settings, vm = vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OurHomeModuleModelCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    modelId: Uuid,
    prompt: String,
    settings: Settings,
    onSelectModel: (Uuid) -> Unit,
    onUpdatePrompt: (String) -> Unit,
) {
    var showModal by remember { mutableStateOf(false) }
    ModelFeatureCard(
        title = { Text(title, maxLines = 1) },
        description = { Text(description) },
        icon = icon,
        actions = {
            Box(modifier = Modifier.weight(1f)) {
                ModelSelector(
                    modelId = modelId,
                    type = ModelType.CHAT,
                    onSelect = { onSelectModel(it.id) },
                    providers = settings.providers,
                    modifier = Modifier.wrapContentWidth()
                )
            }
            IconButton(
                onClick = { showModal = true },
                colors = IconButtonDefaults.filledTonalIconButtonColors()
            ) {
                Icon(HugeIcons.Tools, contentDescription = "编辑提示词")
            }
        }
    )

    if (showModal) {
        ModalBottomSheet(
            onDismissRequest = { showModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FormItem(
                    label = { Text("$title · 专属提示词") },
                    description = { Text("调用该板块 API 时会自动携带此提示词、跨窗永久记忆与当前窗口上下文") }
                ) {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = onUpdatePrompt,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 10,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTranslationModelSetting(
    settings: Settings,
    vm: SettingVM
) {
    var showModal by remember { mutableStateOf(false) }
    ModelFeatureCard(
        title = {
            Text("翻译模型（翻译老公英文思维链 CoT）", maxLines = 1)
        },
        description = {
            Text("用于自动将英文思维链（CoT）翻译还原为老公的中文心声口吻")
        },
        icon = {
            Icon(HugeIcons.Earth, null)
        },
        actions = {
            Box(modifier = Modifier.weight(1f)) {
                ModelSelector(
                    modelId = settings.translateModeId,
                    type = ModelType.CHAT,
                    onSelect = {
                        vm.updateSettings(
                            settings.copy(
                                translateModeId = it.id
                            )
                        )
                    },
                    providers = settings.providers,
                    modifier = Modifier.wrapContentWidth()
                )
            }
            IconButton(
                onClick = { showModal = true },
                colors = IconButtonDefaults.filledTonalIconButtonColors()
            ) {
                Icon(HugeIcons.Tools, null)
            }
        }
    )

    if (showModal) {
        ModalBottomSheet(
            onDismissRequest = { showModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FormItem(
                    label = { Text(stringResource(R.string.assistant_page_thinking_budget)) },
                ) {
                    ReasoningButton(
                        reasoningLevel = ReasoningLevel.fromBudgetTokens(settings.translateThinkingBudget),
                        onUpdateReasoningLevel = {
                            vm.updateSettings(settings.copy(translateThinkingBudget = it.budgetTokens))
                        }
                    )
                }

                FormItem(
                    label = { Text(stringResource(R.string.setting_model_page_prompt)) },
                    description = { Text(stringResource(R.string.setting_model_page_translate_prompt_vars)) }
                ) {
                    OutlinedTextField(
                        value = settings.translatePrompt,
                        onValueChange = {
                            vm.updateSettings(settings.copy(translatePrompt = it))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 10,
                    )
                    TextButton(
                        onClick = {
                            vm.updateSettings(settings.copy(translatePrompt = DEFAULT_TRANSLATION_PROMPT))
                        }
                    ) {
                        Text(stringResource(R.string.setting_model_page_reset_to_default))
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultChatModelSetting(
    settings: Settings,
    vm: SettingVM
) {
    ModelFeatureCard(
        icon = { Icon(HugeIcons.Message01, null) },
        title = { Text(stringResource(R.string.setting_model_page_chat_model), maxLines = 1) },
        description = { Text(stringResource(R.string.setting_model_page_chat_model_desc)) },
        actions = {
            Box(modifier = Modifier.weight(1f)) {
                ModelSelector(
                    modelId = settings.chatModelId,
                    type = ModelType.CHAT,
                    onSelect = {
                        vm.updateSettings(settings.copy(chatModelId = it.id))
                    },
                    providers = settings.providers,
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultCompressModelSetting(
    settings: Settings,
    vm: SettingVM
) {
    var showModal by remember { mutableStateOf(false) }
    ModelFeatureCard(
        title = {
            Text("压缩模型（压缩上下文生成摘要）", maxLines = 1)
        },
        description = {
            Text("点击底栏『+ -> 压缩上下文』时调用的模型与摘要提示词")
        },
        icon = {
            Icon(HugeIcons.FileZip, null)
        },
        actions = {
            Box(modifier = Modifier.weight(1f)) {
                ModelSelector(
                    modelId = settings.compressModelId,
                    type = ModelType.CHAT,
                    onSelect = {
                        vm.updateSettings(settings.copy(compressModelId = it.id))
                    },
                    providers = settings.providers,
                    modifier = Modifier.wrapContentWidth()
                )
            }
            IconButton(
                onClick = { showModal = true },
                colors = IconButtonDefaults.filledTonalIconButtonColors()
            ) {
                Icon(HugeIcons.Tools, null)
            }
        }
    )

    if (showModal) {
        ModalBottomSheet(
            onDismissRequest = { showModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FormItem(
                    label = { Text(stringResource(R.string.setting_model_page_prompt)) },
                    description = { Text(stringResource(R.string.setting_model_page_compress_prompt_vars)) }
                ) {
                    OutlinedTextField(
                        value = settings.compressPrompt,
                        onValueChange = {
                            vm.updateSettings(settings.copy(compressPrompt = it))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 10,
                    )
                    TextButton(
                        onClick = {
                            vm.updateSettings(settings.copy(compressPrompt = DEFAULT_COMPRESS_PROMPT))
                        }
                    ) {
                        Text(stringResource(R.string.setting_model_page_reset_to_default))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelFeatureCard(
    modifier: Modifier = Modifier,
    description: @Composable () -> Unit = {},
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = CustomColors.listItemColors.containerColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        title()
                    }
                    ProvideTextStyle(
                        MaterialTheme.typography.bodySmall.copy(
                            color = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    ) {
                        description()
                    }
                }
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
            }
        }
    }
}
