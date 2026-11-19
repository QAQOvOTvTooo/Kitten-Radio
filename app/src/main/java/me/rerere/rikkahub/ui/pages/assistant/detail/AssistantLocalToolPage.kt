/*
 * 小猫电台 · AI本地工具设置（JavaScript、时间信息、询问、2D星露谷小家自主行动、工作流、语音朗读/通话、剪贴板、网页抓取等；闹钟已移至「设置 -> 系统工具」）
 */

package me.rerere.rikkahub.ui.pages.assistant.detail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.rikkahub.R
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.ai.tools.LocalToolOption
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.service.RikkaAccessibilityService
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.pages.setting.settingsScaffoldContainerColor
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.ui.theme.LargeFlexibleTopAppBar
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AssistantLocalToolPage(id: String) {
    val vm: AssistantDetailVM = koinViewModel(
        parameters = {
            parametersOf(id)
        }
    )
    val assistant by vm.assistant.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(stringResource(R.string.assistant_page_tab_local_tools))
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
    ) { innerPadding ->
        AssistantLocalToolContent(
            modifier = Modifier.padding(innerPadding),
            assistant = assistant,
            onUpdate = { vm.update(it) }
        )
    }
}

@Composable
private fun AssistantLocalToolContent(
    modifier: Modifier = Modifier,
    assistant: Assistant,
    onUpdate: (Assistant) -> Unit
) {
    val context = LocalContext.current
    val navController = LocalNavController.current

    fun toggleLocalTool(option: LocalToolOption, enabled: Boolean) {
        val newLocalTools = if (enabled) {
            assistant.localTools + option
        } else {
            assistant.localTools - option
        }
        onUpdate(assistant.copy(localTools = newLocalTools))
    }

    val calendarPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_CALENDAR] == true &&
            permissions[Manifest.permission.WRITE_CALENDAR] == true
        if (granted) {
            toggleLocalTool(LocalToolOption.Calendar, true)
        }
    }

    // Track whether the accessibility service is currently enabled in system settings
    var a11yEnabled by remember {
        mutableStateOf(RikkaAccessibilityService.isEnabledInSettings(context))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                a11yEnabled = RikkaAccessibilityService.isEnabledInSettings(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CardGroup(
            title = { Text("核心本地工具（JavaScript / 时间 / 询问 / 2D小家 / 工作流）") }
        ) {
            // 1. JavaScript 引擎
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_javascript_engine_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_javascript_engine_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.JavascriptEngine),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.JavascriptEngine, it) }
                    )
                }
            )
            // 2. 时间信息
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_time_info_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_time_info_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.TimeInfo),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.TimeInfo, it) }
                    )
                }
            )
            // 3. 询问 (ask_user)
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_ask_user_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_ask_user_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.AskUser),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.AskUser, it) }
                    )
                }
            )
            // 4. 2D星露谷小家自主行动工具 (control_home_avatar)
            item(
                headlineContent = { Text("2D星露谷小家行动 (control_home_avatar)") },
                supportingContent = { Text("允许老公自主控制自己在『家』2D像素网格里的位置坐标与头顶状态气泡") },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.HomeAvatar),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.HomeAvatar, it) }
                    )
                }
            )
            // 5. 工作流自动化工具 (workflow_*)
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_workflows_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_workflows_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.Workflows),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.Workflows, it) }
                    )
                }
            )
        }

        CardGroup(
            title = { Text("语音、剪贴板、网页与自动化扩展工具") }
        ) {
            // TTS 语音朗读
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_tts_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_tts_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.Tts),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.Tts, it) }
                    )
                }
            )
            // AI 主动发起语音通话
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_request_voice_call_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_request_voice_call_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.RequestVoiceCall),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.RequestVoiceCall, it) }
                    )
                }
            )
            // 剪贴板
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_clipboard_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_clipboard_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.Clipboard),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.Clipboard, it) }
                    )
                }
            )
            // 网页抓取
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_web_fetch_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_web_fetch_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.WebFetch),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.WebFetch, it) }
                    )
                }
            )
            // 压缩包查看
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_list_zip_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_list_zip_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.ListZipContents),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.ListZipContents, it) }
                    )
                }
            )
            // 日历
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_calendar_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_calendar_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.Calendar),
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                val hasRead = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.READ_CALENDAR
                                ) == PackageManager.PERMISSION_GRANTED
                                val hasWrite = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.WRITE_CALENDAR
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasRead && hasWrite) {
                                    toggleLocalTool(LocalToolOption.Calendar, true)
                                } else {
                                    calendarPermissionsLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_CALENDAR,
                                            Manifest.permission.WRITE_CALENDAR
                                        )
                                    )
                                }
                            } else {
                                toggleLocalTool(LocalToolOption.Calendar, false)
                            }
                        }
                    )
                }
            )
            // 允许跳过回复
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_allow_skip_reply_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_allow_skip_reply_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.AllowSkipReply),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.AllowSkipReply, it) }
                    )
                }
            )
            // 屏幕自动化
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_screen_automation_title))
                },
                supportingContent = {
                    Column {
                        Text(stringResource(R.string.assistant_page_local_tools_screen_automation_desc))
                        if (assistant.localTools.contains(LocalToolOption.ScreenAutomation) && !a11yEnabled) {
                            TextButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.assistant_page_local_tools_screen_automation_open_settings))
                            }
                        }
                    }
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.ScreenAutomation),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.ScreenAutomation, it) }
                    )
                }
            )
            // SSH 远程连接
            item(
                headlineContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_ssh_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.assistant_page_local_tools_ssh_desc))
                },
                trailingContent = {
                    Switch(
                        checked = assistant.localTools.contains(LocalToolOption.Ssh),
                        onCheckedChange = { toggleLocalTool(LocalToolOption.Ssh, it) }
                    )
                }
            )
            // 快捷跳转至「设置 -> 系统工具（含闹钟）」
            item(
                onClick = { navController.navigate(Screen.SettingSystemTools) },
                headlineContent = { Text("前往「系统工具」（闹钟与倒计时已移至此处）") },
                supportingContent = { Text("在『设置 -> 系统工具』中统一管理闹钟、倒计时、位置、通知、相机、音乐等系统权限工具") },
            )
        }
    }
}
