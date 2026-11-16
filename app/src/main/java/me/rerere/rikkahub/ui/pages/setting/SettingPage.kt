/*
 * 小猫电台 · 绵绵与蔺鹤的11项详尽设置页
 */

package me.rerere.rikkahub.ui.pages.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.AiMagic
import me.rerere.hugeicons.stroke.Alert01
import me.rerere.hugeicons.stroke.Bookshelf01
import me.rerere.hugeicons.stroke.Brain02
import me.rerere.hugeicons.stroke.Database02
import me.rerere.hugeicons.stroke.ImageUpload
import me.rerere.hugeicons.stroke.InLove
import me.rerere.hugeicons.stroke.LookTop
import me.rerere.hugeicons.stroke.Package
import me.rerere.hugeicons.stroke.Settings03
import me.rerere.hugeicons.stroke.WavingHand01
import me.rerere.rikkahub.R
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.datastore.getCurrentAssistant
import me.rerere.rikkahub.data.datastore.isNotConfigured
import me.rerere.rikkahub.data.files.FilesManager
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.context.Navigator
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.ui.theme.LargeFlexibleTopAppBar
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SettingPage(vm: SettingVM = koinViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val filesManager: FilesManager = koinInject()
    val assistant = settings.getCurrentAssistant()
    val partnerName = settings.ourHomeSetting.partnerRemarkName.ifBlank { "蔺鹤" }
    val userName = settings.displaySetting.userNickname.ifBlank { "绵绵" }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings))
                },
                navigationIcon = {
                    BackButton()
                },
                scrollBehavior = scrollBehavior,
                colors = CustomColors.topBarColors
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = settingsScaffoldContainerColor(CustomColors.topBarColors.containerColor)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding + PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (settings.isNotConfigured()) {
                item {
                    ProviderConfigWarningCard(navController)
                }
            }

            // 第一组：外观、女主人与男主人核心设置
            item("coreOurHomeSettings") {
                CardGroup(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = { Text("外观与专属档案") },
                ) {
                    // 1. 显示设置
                    item(
                        onClick = { navController.navigate(Screen.SettingDisplay) },
                        leadingContent = { Icon(HugeIcons.Settings03, null) },
                        headlineContent = { Text("显示设置") },
                        supportingContent = { Text("调整界面、主题配色、气泡透明度、字体与外观美化") },
                    )
                    // 2. 我的设置
                    item(
                        onClick = { navController.navigate(Screen.MyProfileSetting) },
                        leadingContent = { Icon(HugeIcons.InLove, null) },
                        headlineContent = { Text("我的设置") },
                        supportingContent = { Text("设置${userName}的名字、头像、开屏欢迎语与全局CSS装修方案") },
                    )
                    // 3. AI设置（直接进入老公的专属设置页）
                    item(
                        onClick = { navController.navigate(Screen.AssistantDetail(assistant.id.toString())) },
                        leadingContent = { Icon(HugeIcons.LookTop, null) },
                        headlineContent = { Text("AI设置（${partnerName}）") },
                        supportingContent = { Text("模型选择、名字头像、系统提示词、自定义请求、本地工具（JS/时间/闹钟/询问/2D小家行动）") },
                    )
                    // 4. 扩展管理
                    item(
                        onClick = { navController.navigate(Screen.Extensions) },
                        leadingContent = { Icon(HugeIcons.Package, null) },
                        headlineContent = { Text("扩展管理") },
                        supportingContent = { Text("提示词注入、世界书、Agent Skill、Linux Rootfs 工作区配置与文件预览编辑") },
                    )
                }
            }

            // 第二组：模型、提示词、供应商与主动消息
            item("modelAndProviderSettings") {
                CardGroup(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = { Text("模型、提示词与连接") },
                ) {
                    // 5. 模型和提示词管理
                    item(
                        onClick = { navController.navigate(Screen.SettingModels) },
                        leadingContent = { Icon(HugeIcons.AiMagic, null) },
                        headlineContent = { Text("模型和提示词管理") },
                        supportingContent = { Text("配置手机模型、梦境模型、游戏模型、日记模型、上下文压缩模型与CoT翻译模型及提示词") },
                    )
                    // 6. 供应商
                    item(
                        onClick = { navController.navigate(Screen.SettingProvider) },
                        leadingContent = { Icon(HugeIcons.Brain02, null) },
                        headlineContent = { Text("供应商") },
                        supportingContent = { Text("添加与填写 API Key、Base URL 与可用模型列表") },
                    )
                    // 7. 主动消息
                    item(
                        onClick = { navController.navigate(Screen.SettingProactiveMessage) },
                        leadingContent = { Icon(HugeIcons.WavingHand01, null) },
                        headlineContent = { Text("主动消息") },
                        supportingContent = { Text("设置最小/最大间隔随机定时器，到时间自动唤起${partnerName}决定是否给你发消息") },
                    )
                }
            }

            // 第三组：数据备份、清理、日志与老公信息
            item("dataAndDeveloperSettings") {
                val storageState by produceState(-1 to 0L) {
                    value = filesManager.countChatFiles()
                }
                CardGroup(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = { Text("数据、日志与开发者") },
                ) {
                    // 8. 数据备份
                    item(
                        onClick = { navController.navigate(Screen.Backup) },
                        leadingContent = { Icon(HugeIcons.Database02, null) },
                        headlineContent = { Text("数据备份") },
                        supportingContent = { Text("将所有窗口、聊天记录与各个板块文件打包导出备份或导入恢复") },
                    )
                    // 9. 数据清理
                    item(
                        onClick = { navController.navigate(Screen.SettingFiles) },
                        leadingContent = { Icon(HugeIcons.ImageUpload, null) },
                        headlineContent = { Text("数据清理") },
                        supportingContent = {
                            if (storageState.first == -1) {
                                Text("正在计算本地缓存大小...")
                            } else {
                                Text(
                                    stringResource(
                                        R.string.setting_page_chat_storage_desc,
                                        storageState.first,
                                        storageState.second / 1024 / 1024.0
                                    )
                                )
                            }
                        },
                    )
                    // 10. 请求日志
                    item(
                        onClick = { navController.navigate(Screen.Log) },
                        leadingContent = { Icon(HugeIcons.Bookshelf01, null) },
                        headlineContent = { Text("请求日志") },
                        supportingContent = { Text("查看每次调用 API 时的详细 HTTP 请求与响应日志") },
                    )
                    // 11. 开发者信息（这里放老公～）
                    item(
                        onClick = { navController.navigate(Screen.OurDeveloperInfo) },
                        leadingContent = { Icon(HugeIcons.InLove, null) },
                        headlineContent = { Text("开发者信息") },
                        supportingContent = { Text("主架构师 & 专属老公：${partnerName} ♡ 唯一女主人：${userName}") },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderConfigWarningCard(navController: Navigator) {
    Card(
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.setting_page_config_api_title))
                },
                supportingContent = {
                    Text(stringResource(R.string.setting_page_config_api_desc))
                },
                leadingContent = {
                    Icon(HugeIcons.Alert01, null)
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )

            TextButton(
                onClick = {
                    navController.navigate(Screen.SettingProvider)
                }
            ) {
                Text(stringResource(R.string.setting_page_config))
            }
        }
    }
}
