/*
 * 小猫电台 · 扩展管理（提示词注入、世界书、Agent Skill、Linux Rootfs工作区）
 */

package me.rerere.rikkahub.ui.pages.extensions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book01
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.Folder01
import me.rerere.hugeicons.stroke.Puzzle
import me.rerere.rikkahub.R
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.pages.setting.settingsScaffoldContainerColor
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.ui.theme.LargeFlexibleTopAppBar
import me.rerere.rikkahub.utils.plus

@Composable
fun ExtensionsPage() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.extensions_page_title)) },
                navigationIcon = { BackButton() },
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
            item {
                CardGroup(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = { Text("扩展管理四大核心模块") },
                ) {
                    // 1. 提示词注入
                    item(
                        onClick = { navController.navigate(Screen.Prompts) },
                        leadingContent = { Icon(HugeIcons.Book03, null) },
                        headlineContent = { Text("提示词注入") },
                        supportingContent = { Text("管理全局或深度的模式提示词注入规则，支持导入酒馆预设") },
                    )
                    // 2. 世界书 (Lorebook)
                    item(
                        onClick = { navController.navigate(Screen.Prompts) },
                        leadingContent = { Icon(HugeIcons.Book01, null) },
                        headlineContent = { Text("世界书 (Lorebook)") },
                        supportingContent = { Text("管理关键词触发或常驻的世界书设定集") },
                    )
                    // 3. Agent Skill
                    item(
                        onClick = { navController.navigate(Screen.Skills) },
                        leadingContent = { Icon(HugeIcons.Puzzle, null) },
                        headlineContent = { Text("Agent Skill") },
                        supportingContent = { Text("管理挂载在 /skills 下的专属技能包") },
                    )
                    // 4. 工作区（安装 Linux Rootfs、查看文件夹、直接预览和编辑）
                    item(
                        onClick = { navController.navigate(Screen.Workspaces) },
                        leadingContent = { Icon(HugeIcons.Folder01, null) },
                        headlineContent = { Text("工作区 (Linux Rootfs)") },
                        supportingContent = { Text("安装配置 Linux Rootfs 沙盒，查看 /workspace 文件夹并直接预览和编辑文件") },
                    )
                }
            }
        }
    }
}
