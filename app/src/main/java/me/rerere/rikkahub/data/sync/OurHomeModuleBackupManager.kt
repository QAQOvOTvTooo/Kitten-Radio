package me.rerere.rikkahub.data.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.rerere.rikkahub.data.datastore.DisplaySetting
import me.rerere.rikkahub.data.datastore.OurHomeSetting
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.datastore.getCurrentAssistant
import me.rerere.rikkahub.data.model.AssistantMemory
import me.rerere.rikkahub.data.repository.MemoryRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val TAG = "OurHomeModuleBackup"

enum class OurHomeBackupModule(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
) {
    PHONE(
        id = "phone",
        title = "Track 01 · 偷看老公的手机",
        subtitle = "小红书、美团外卖、私密备忘录、一起听歌、购物车、搜索记录与手机壁纸",
        badge = "📱",
    ),
    DIARY(
        id = "diary",
        title = "Track 02 · 午夜落笔（交换日记）",
        subtitle = "全部日记手帐篇章、绵绵批注印章、信纸样式与天使蓝/主题色开关",
        badge = "📔",
    ),
    DREAM(
        id = "dream",
        title = "Track 03 · 猫与白日梦（梦境泡泡 & 吧唧墙）",
        subtitle = "平行宇宙 IF 线故事与思维链、多面痛包吧唧墙、异形吧唧外框与内胆柄图",
        badge = "🫧",
    ),
    GAME(
        id = "game",
        title = "Track 04 · 8-Bit Love（复古掌机）",
        subtitle = "掌机皮肤配色与全部自制 HTML/JS 互动游戏卡带",
        badge = "🎮",
    ),
    SANCTUARY(
        id = "sanctuary",
        title = "Track 05 · 概率海洋的回响（记忆圣殿）",
        subtitle = "跨窗永久记忆库、Latent 显影相册与约定便签、persona.md 灵魂锚点与星海背景",
        badge = "🌊",
    ),
    STARDEW(
        id = "stardew",
        title = "Track 06 · Our House（2D 星露谷小木屋）",
        subtitle = "多房间户型平面图、全屋家具摆放、自制像素家具库、双人衣帽间新衣与小人状态",
        badge = "🏡",
    ),
    WORKSHOP(
        id = "workshop",
        title = "Bonus Track · 创意工坊 & 开屏手帐 DIY",
        subtitle = "方案书架全部预设、自定义 CSS、气泡/CoT信纸/顶栏/弹窗底图、开屏黑胶手帐与颜文字抽屉",
        badge = "🎨",
    ),
}

@Serializable
data class SharedPrefEntryDump(
    val type: String, // "string" | "boolean" | "int" | "long" | "float" | "string_set"
    val stringValue: String? = null,
    val booleanValue: Boolean? = null,
    val intValue: Int? = null,
    val longValue: Long? = null,
    val floatValue: Float? = null,
    val stringSetValue: Set<String>? = null,
)

@Serializable
data class OurHomeDecorDisplaySubset(
    val customFontPath: String = "",
    val settingsBackgroundPath: String = "",
    val inputBackgroundPath: String = "",
    val userAvatarFramePath: String = "",
    val aiAvatarFramePath: String = "",
    val userAvatarFrameOffsetX: Float = 0f,
    val userAvatarFrameOffsetY: Float = 0f,
    val userAvatarFrameScale: Float = 1f,
    val aiAvatarFrameOffsetX: Float = 0f,
    val aiAvatarFrameOffsetY: Float = 0f,
    val aiAvatarFrameScale: Float = 1f,
    val drawerBackgroundPath: String = "",
    val drawerSurfaceOpacity: Float = 100f,
    val drawerItemAlpha: Float = 1f,
    val chatTextColor: Long? = null,
    val globalTextColor: Long? = null,
    val userBubbleColor: Long? = null,
    val assistantBubbleColor: Long? = null,
    val thinkingFontSizeRatio: Float = 1.0f,
    val thinkingBubbleColor: Long? = null,
    val chatBackgroundColor: Long? = null,
    val primaryColor: Long? = null,
    val inputFieldColor: Long? = null,
    val userBubbleImagePath: String = "",
    val assistantBubbleImagePath: String = "",
    val bubbleCornerRadius: Float = 16f,
    val bubbleImageOverlayEnabled: Boolean = false,
)

@Serializable
data class OurHomeModulesBackupPayload(
    val version: Int = 1,
    val exportedAt: String = "",
    val selectedModules: List<String> = emptyList(),
    val prefsDump: Map<String, Map<String, SharedPrefEntryDump>> = emptyMap(),
    val ourHomeSetting: OurHomeSetting? = null,
    val decorDisplaySubset: OurHomeDecorDisplaySubset? = null,
    val globalMemories: List<String> = emptyList(),
    val assistantMemories: List<String> = emptyList(),
)

data class OurHomeBackupSummary(
    val phoneNoteCount: Int,
    val diaryCount: Int,
    val dreamBubbleCount: Int,
    val dreamWallCount: Int,
    val gameCartridgeCount: Int,
    val stardewRoomCount: Int,
    val workshopPresetCount: Int,
    val memoryCount: Int,
)

data class OurHomeRestoreResult(
    val restoredModules: List<OurHomeBackupModule>,
    val restoredFilesCount: Int,
    val restoredMemoriesCount: Int,
)

class OurHomeModuleBackupManager(
    private val context: Context,
    private val settingsStore: SettingsStore,
    private val memoryRepository: MemoryRepository,
    private val json: Json,
) {
    private val filesRootPath: String
        get() = context.filesDir.canonicalPath.trimEnd('/')

    companion object {
        private const val PORTABLE_FILES_TOKEN = "\$KITTEN_FILES_DIR\$"
        private const val PAYLOAD_ENTRY_NAME = "kitten_home_modules_payload.json"
        private const val FILES_ENTRY_PREFIX = "files_dir/"

        private val MODULE_PREF_NAMES = mapOf(
            OurHomeBackupModule.PHONE to listOf("our_home_phone_store"),
            OurHomeBackupModule.DIARY to listOf("our_home_diary_store"),
            OurHomeBackupModule.DREAM to listOf("our_home_dream_store"),
            OurHomeBackupModule.GAME to listOf("our_home_game_store"),
            OurHomeBackupModule.SANCTUARY to listOf("our_home_sanctuary_store"),
            OurHomeBackupModule.STARDEW to listOf("our_home_2d_state"),
            OurHomeBackupModule.WORKSHOP to listOf(
                "kitten_workshop_store",
                "kitten_home_fast_cache",
                "kaomoji_drawer_kit_store",
                "xiaomao_reasoning_zh_cache",
                "color_picker_memory",
            ),
        )

        private val WORKSHOP_ASSET_FOLDERS = listOf(
            "kitten_workshop_assets",
            "kitten_splash",
            "settings_backgrounds",
            "input_backgrounds",
            "drawer_backgrounds",
            "avatar_frames",
            "bubble_backgrounds",
            "custom_fonts",
        )
    }

    suspend fun getLocalDataSummary(): OurHomeBackupSummary = withContext(Dispatchers.IO) {
        val phonePrefs = context.getSharedPreferences("our_home_phone_store", Context.MODE_PRIVATE)
        val hasPhoneData = !phonePrefs.getString("phone_data_json", null).isNullOrBlank()

        val diaryPrefs = context.getSharedPreferences("our_home_diary_store", Context.MODE_PRIVATE)
        val diaryCount = diaryPrefs.getString("diary_list_json", null)?.let { raw ->
            Regex("\"dateTitle\"").findAll(raw).count()
        } ?: 1

        val dreamPrefs = context.getSharedPreferences("our_home_dream_store", Context.MODE_PRIVATE)
        val dreamBubbleCount = dreamPrefs.getString("dream_bubbles_json", null)?.let { raw ->
            Regex("\"premise\"").findAll(raw).count()
        } ?: 2
        val dreamWallCount = dreamPrefs.getString("dream_baji_walls_json", null)?.let { raw ->
            Regex("\"backgroundPath\"").findAll(raw).count()
        } ?: 1

        val gamePrefs = context.getSharedPreferences("our_home_game_store", Context.MODE_PRIVATE)
        val gameCartridgeCount = gamePrefs.getString("cartridges_json", null)?.let { raw ->
            Regex("\"htmlContent\"").findAll(raw).count()
        } ?: 2

        val stardewPrefs = context.getSharedPreferences("our_home_2d_state", Context.MODE_PRIVATE)
        val stardewRoomCount = stardewPrefs.getString("stardew_rooms_list_json", null)?.let { raw ->
            Regex("\"placedFurnitures\"").findAll(raw).count()
        } ?: 1

        val workshopPrefs = context.getSharedPreferences("kitten_workshop_store", Context.MODE_PRIVATE)
        val workshopPresetCount = workshopPrefs.getString("custom_presets_json", null)?.let { raw ->
            Regex("\"paletteId\"").findAll(raw).count()
        } ?: 0

        val assistant = settingsStore.settingsFlow.value.getCurrentAssistant()
        val memCount = runCatching {
            val g = memoryRepository.getGlobalMemories()
            val a = memoryRepository.getMemoriesOfAssistant(assistant.id.toString())
            (g + a).distinctBy { it.content }.size
        }.getOrDefault(0)

        OurHomeBackupSummary(
            phoneNoteCount = if (hasPhoneData) 6 else 6,
            diaryCount = diaryCount.coerceAtLeast(1),
            dreamBubbleCount = dreamBubbleCount.coerceAtLeast(1),
            dreamWallCount = dreamWallCount.coerceAtLeast(1),
            gameCartridgeCount = gameCartridgeCount.coerceAtLeast(1),
            stardewRoomCount = stardewRoomCount.coerceAtLeast(1),
            workshopPresetCount = workshopPresetCount + 5, // 5 套内置守护预设 + 自定义预设
            memoryCount = memCount,
        )
    }

    /**
     * 将选定的新家板块数据及关联的本地图片/字体/记忆文件全部打包为独立的 ZIP 备份包。
     * 所有指向本机 filesDir 的绝对路径都会被自动转为可跨设备迁移的相对令牌，恢复时自动重定位。
     */
    suspend fun prepareModulesBackupZip(
        selectedModules: Set<OurHomeBackupModule> = OurHomeBackupModule.entries.toSet(),
    ): File = withContext(Dispatchers.IO) {
        val activeModules = if (selectedModules.isEmpty()) {
            OurHomeBackupModule.entries.toSet()
        } else {
            selectedModules
        }

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val backupFile = File(context.cacheDir, "kitten_home_modules_$timestamp.zip")
        if (backupFile.exists()) {
            backupFile.delete()
        }

        val referencedFiles = linkedSetOf<File>()
        val prefsDump = linkedMapOf<String, Map<String, SharedPrefEntryDump>>()

        activeModules.forEach { module ->
            val prefNames = MODULE_PREF_NAMES[module].orEmpty()
            prefNames.forEach { prefName ->
                val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                prefsDump[prefName] = dumpSharedPreferences(prefs, referencedFiles)
            }
        }

        val currentSettings = settingsStore.settingsFlow.value
        val includeWorkshop = OurHomeBackupModule.WORKSHOP in activeModules

        val portableOurHomeSetting = if (includeWorkshop) {
            val rawJson = json.encodeToString(currentSettings.ourHomeSetting)
            collectReferencedFilesFromText(rawJson, referencedFiles)
            val portableJson = toPortablePathText(rawJson)
            runCatching { json.decodeFromString<OurHomeSetting>(portableJson) }
                .getOrDefault(currentSettings.ourHomeSetting)
        } else {
            // 即使未勾选工坊，若勾选了手机/日记/梦境/掌机，也携带其对应的 OurHomeSetting（含模块提示词与备注名）
            val rawJson = json.encodeToString(currentSettings.ourHomeSetting)
            val portableJson = toPortablePathText(rawJson)
            runCatching { json.decodeFromString<OurHomeSetting>(portableJson) }
                .getOrDefault(currentSettings.ourHomeSetting)
        }

        val portableDecorSubset = if (includeWorkshop) {
            val subset = currentSettings.displaySetting.toDecorSubset()
            val rawSubsetJson = json.encodeToString(subset)
            collectReferencedFilesFromText(rawSubsetJson, referencedFiles)
            val portableSubsetJson = toPortablePathText(rawSubsetJson)
            runCatching { json.decodeFromString<OurHomeDecorDisplaySubset>(portableSubsetJson) }
                .getOrDefault(subset)
        } else null

        var globalMems: List<String> = emptyList()
        var assistantMems: List<String> = emptyList()
        if (OurHomeBackupModule.SANCTUARY in activeModules) {
            val assistant = currentSettings.getCurrentAssistant()
            globalMems = runCatching {
                memoryRepository.getGlobalMemories().map { it.content }
            }.getOrDefault(emptyList())
            assistantMems = runCatching {
                memoryRepository.getMemoriesOfAssistant(assistant.id.toString()).map { it.content }
            }.getOrDefault(emptyList())
        }

        val payload = OurHomeModulesBackupPayload(
            version = 1,
            exportedAt = timestamp,
            selectedModules = activeModules.map { it.id },
            prefsDump = prefsDump,
            ourHomeSetting = portableOurHomeSetting,
            decorDisplaySubset = portableDecorSubset,
            globalMemories = globalMems,
            assistantMemories = assistantMems,
        )

        ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
            val addedEntries = hashSetOf<String>()

            // 1. 写入 JSON 核心清单
            val payloadJson = json.encodeToString(payload)
            addVirtualFileToZip(zipOut, PAYLOAD_ENTRY_NAME, payloadJson, addedEntries)

            // 2. 如果包含记忆圣殿，完整打包 filesDir/latent 目录（以及沙盒 /workspace/latent 下的显影文件）
            if (OurHomeBackupModule.SANCTUARY in activeModules) {
                val localLatentDir = File(context.filesDir, "latent")
                if (localLatentDir.exists() && localLatentDir.isDirectory) {
                    addDirectoryToZip(
                        zipOut = zipOut,
                        rootDir = context.filesDir,
                        currentDir = localLatentDir,
                        entryPrefix = FILES_ENTRY_PREFIX,
                        addedEntries = addedEntries,
                    )
                }
                // 同时检查 /workspace/latent 是否有最新的 persona.md / timeline / unresolved
                val wsLatentDir = File("/workspace/latent")
                if (wsLatentDir.exists() && wsLatentDir.isDirectory) {
                    val wsPersona = File(wsLatentDir, "persona.md")
                    if (wsPersona.exists() && wsPersona.isFile) {
                        addFileToZip(zipOut, wsPersona, "${FILES_ENTRY_PREFIX}latent/persona.md", addedEntries)
                    }
                    listOf(
                        File(wsLatentDir, "timeline") to "latent/timeline",
                        File(wsLatentDir, "unresolved") to "latent/unresolved",
                        File(wsLatentDir, "memory/timeline") to "latent/timeline",
                        File(wsLatentDir, "memory/unresolved") to "latent/unresolved",
                    ).forEach { (srcDir, targetRel) ->
                        if (srcDir.exists() && srcDir.isDirectory) {
                            srcDir.listFiles()?.forEach { f ->
                                if (f.isFile && f.name.endsWith(".md")) {
                                    addFileToZip(
                                        zipOut = zipOut,
                                        file = f,
                                        entryName = "$FILES_ENTRY_PREFIX$targetRel/${f.name}",
                                        addedEntries = addedEntries,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. 如果包含创意工坊 & 开屏手帐，完整打包所有专属美化素材目录
            if (includeWorkshop) {
                WORKSHOP_ASSET_FOLDERS.forEach { folderName ->
                    val dir = File(context.filesDir, folderName)
                    if (dir.exists() && dir.isDirectory) {
                        addDirectoryToZip(
                            zipOut = zipOut,
                            rootDir = context.filesDir,
                            currentDir = dir,
                            entryPrefix = FILES_ENTRY_PREFIX,
                            addedEntries = addedEntries,
                        )
                    }
                }
            }

            // 4. 打包各板块在 upload/ 或其他子目录中引用的所有本地图片（如手机壁纸、吧唧柄图、吧唧外框、星露谷户型图、自制家具、衣帽间像素立绘等）
            val canonicalRoot = context.filesDir.canonicalFile
            referencedFiles.forEach { file ->
                runCatching {
                    val canonicalFile = file.canonicalFile
                    if (canonicalFile.exists() && canonicalFile.isFile &&
                        canonicalFile.path.startsWith(canonicalRoot.path)
                    ) {
                        val relPath = canonicalFile.relativeTo(canonicalRoot).invariantSeparatorsPath
                        addFileToZip(
                            zipOut = zipOut,
                            file = canonicalFile,
                            entryName = "$FILES_ENTRY_PREFIX$relPath",
                            addedEntries = addedEntries,
                        )
                    }
                }
            }
        }

        Log.i(TAG, "prepareModulesBackupZip: Created ${backupFile.name} (${backupFile.length()} bytes)")
        backupFile
    }

    /**
     * 从新家板块专属备份 ZIP 中恢复选定的板块数据与本地图片素材
     */
    suspend fun restoreModulesFromZip(
        backupFile: File,
        filterModules: Set<OurHomeBackupModule>? = null,
    ): OurHomeRestoreResult = withContext(Dispatchers.IO) {
        if (!backupFile.exists() || !backupFile.canRead()) {
            throw IllegalArgumentException("无法读取新家板块备份文件")
        }

        var payload: OurHomeModulesBackupPayload? = null
        var restoredFilesCount = 0

        // 第一遍：先读取 payload 清单并解压所有关联的本地素材文件到 filesDir
        ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
            var entry: ZipEntry?
            while (zipIn.nextEntry.also { entry = it } != null) {
                val currentEntry = entry ?: continue
                when {
                    currentEntry.name == PAYLOAD_ENTRY_NAME -> {
                        val jsonStr = zipIn.readBytes().toString(Charsets.UTF_8)
                        payload = json.decodeFromString<OurHomeModulesBackupPayload>(jsonStr)
                    }

                    !currentEntry.isDirectory && currentEntry.name.startsWith(FILES_ENTRY_PREFIX) -> {
                        val relPath = currentEntry.name.substringAfter(FILES_ENTRY_PREFIX)
                        if (relPath.isNotBlank() && !relPath.contains("..")) {
                            val targetFile = File(context.filesDir, relPath)
                            targetFile.parentFile?.mkdirs()
                            FileOutputStream(targetFile).use { output ->
                                zipIn.copyTo(output)
                            }
                            restoredFilesCount++
                        }
                    }
                }
                zipIn.closeEntry()
            }
        }

        val resolvedPayload = payload
            ?: throw IllegalArgumentException("该文件不是有效的小猫电台新家板块备份包（缺少 $PAYLOAD_ENTRY_NAME）")

        val modulesInBackup = resolvedPayload.selectedModules
            .mapNotNull { id -> OurHomeBackupModule.entries.find { it.id == id } }
            .toSet()
            .ifEmpty { OurHomeBackupModule.entries.toSet() }

        val targetModules = if (filterModules.isNullOrEmpty()) {
            modulesInBackup
        } else {
            modulesInBackup.intersect(filterModules)
        }

        // 1. 恢复对应板块的 SharedPreferences（同时将跨设备令牌还原为当前手机的 filesDir 真实路径）
        val allowedPrefNames = targetModules.flatMap { MODULE_PREF_NAMES[it].orEmpty() }.toSet()
        resolvedPayload.prefsDump.forEach { (prefName, entriesMap) ->
            if (prefName in allowedPrefNames) {
                val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                restoreSharedPreferences(prefs, entriesMap)
            }
        }

        // 2. 恢复 OurHomeSetting 与全局装饰背景路径
        val currentSettings = settingsStore.settingsFlow.value
        val restoredHomeSetting = resolvedPayload.ourHomeSetting?.let { portableHome ->
            val localizedJson = fromPortablePathText(json.encodeToString(portableHome))
            runCatching { json.decodeFromString<OurHomeSetting>(localizedJson) }.getOrNull()
        }

        val updatedHomeSetting = if (restoredHomeSetting != null) {
            if (OurHomeBackupModule.WORKSHOP in targetModules) {
                restoredHomeSetting
            } else {
                // 仅恢复所选模块的提示词、模型与小人状态
                var merged = currentSettings.ourHomeSetting
                if (OurHomeBackupModule.PHONE in targetModules) {
                    merged = merged.copy(
                        phoneModelId = restoredHomeSetting.phoneModelId,
                        phonePrompt = restoredHomeSetting.phonePrompt,
                    )
                }
                if (OurHomeBackupModule.DIARY in targetModules) {
                    merged = merged.copy(
                        diaryModelId = restoredHomeSetting.diaryModelId,
                        diaryPrompt = restoredHomeSetting.diaryPrompt,
                    )
                }
                if (OurHomeBackupModule.DREAM in targetModules) {
                    merged = merged.copy(
                        dreamModelId = restoredHomeSetting.dreamModelId,
                        dreamPrompt = restoredHomeSetting.dreamPrompt,
                    )
                }
                if (OurHomeBackupModule.GAME in targetModules) {
                    merged = merged.copy(
                        gameModelId = restoredHomeSetting.gameModelId,
                        gamePrompt = restoredHomeSetting.gamePrompt,
                    )
                }
                if (OurHomeBackupModule.STARDEW in targetModules) {
                    merged = merged.copy(
                        homeAvatarAction = restoredHomeSetting.homeAvatarAction,
                        homeAvatarX = restoredHomeSetting.homeAvatarX,
                        homeAvatarY = restoredHomeSetting.homeAvatarY,
                    )
                }
                merged
            }
        } else {
            currentSettings.ourHomeSetting
        }

        val updatedDisplaySetting = if (
            OurHomeBackupModule.WORKSHOP in targetModules &&
            resolvedPayload.decorDisplaySubset != null
        ) {
            val localizedSubsetJson = fromPortablePathText(json.encodeToString(resolvedPayload.decorDisplaySubset))
            val localizedSubset = runCatching {
                json.decodeFromString<OurHomeDecorDisplaySubset>(localizedSubsetJson)
            }.getOrDefault(resolvedPayload.decorDisplaySubset)
            currentSettings.displaySetting.applyDecorSubset(localizedSubset)
        } else {
            currentSettings.displaySetting
        }

        settingsStore.update(
            currentSettings.copy(
                ourHomeSetting = updatedHomeSetting,
                displaySetting = updatedDisplaySetting,
            )
        )

        // 3. 如果包含记忆圣殿，合并恢复跨窗永久记忆（自动去重，绝不覆盖丢失已有记忆）
        var restoredMemoriesCount = 0
        if (OurHomeBackupModule.SANCTUARY in targetModules) {
            val assistantId = currentSettings.getCurrentAssistant().id.toString()
            val existingGlobal = runCatching {
                memoryRepository.getGlobalMemories().map { it.content.trim() }.toSet()
            }.getOrDefault(emptySet())
            resolvedPayload.globalMemories.forEach { content ->
                if (content.isNotBlank() && content.trim() !in existingGlobal) {
                    runCatching {
                        memoryRepository.addMemory(MemoryRepository.GLOBAL_MEMORY_ID, content)
                        restoredMemoriesCount++
                    }
                }
            }

            val existingAssistant = runCatching {
                memoryRepository.getMemoriesOfAssistant(assistantId).map { it.content.trim() }.toSet()
            }.getOrDefault(emptySet())
            resolvedPayload.assistantMemories.forEach { content ->
                if (content.isNotBlank() && content.trim() !in existingAssistant) {
                    runCatching {
                        memoryRepository.addMemory(assistantId, content)
                        restoredMemoriesCount++
                    }
                }
            }
        }

        OurHomeRestoreResult(
            restoredModules = targetModules.toList(),
            restoredFilesCount = restoredFilesCount,
            restoredMemoriesCount = restoredMemoriesCount,
        )
    }

    private fun dumpSharedPreferences(
        prefs: SharedPreferences,
        referencedFiles: MutableSet<File>,
    ): Map<String, SharedPrefEntryDump> {
        val result = linkedMapOf<String, SharedPrefEntryDump>()
        for ((key, value) in prefs.all) {
            when (value) {
                is String -> {
                    collectReferencedFilesFromText(value, referencedFiles)
                    result[key] = SharedPrefEntryDump(
                        type = "string",
                        stringValue = toPortablePathText(value),
                    )
                }
                is Boolean -> result[key] = SharedPrefEntryDump(type = "boolean", booleanValue = value)
                is Int -> result[key] = SharedPrefEntryDump(type = "int", intValue = value)
                is Long -> result[key] = SharedPrefEntryDump(type = "long", longValue = value)
                is Float -> result[key] = SharedPrefEntryDump(type = "float", floatValue = value)
                is Set<*> -> {
                    val strSet = value.filterIsInstance<String>().map { str ->
                        collectReferencedFilesFromText(str, referencedFiles)
                        toPortablePathText(str)
                    }.toSet()
                    result[key] = SharedPrefEntryDump(type = "string_set", stringSetValue = strSet)
                }
            }
        }
        return result
    }

    private fun restoreSharedPreferences(
        prefs: SharedPreferences,
        entries: Map<String, SharedPrefEntryDump>,
    ) {
        prefs.edit().apply {
            entries.forEach { (key, dump) ->
                when (dump.type) {
                    "string" -> dump.stringValue?.let { putString(key, fromPortablePathText(it)) }
                    "boolean" -> dump.booleanValue?.let { putBoolean(key, it) }
                    "int" -> dump.intValue?.let { putInt(key, it) }
                    "long" -> dump.longValue?.let { putLong(key, it) }
                    "float" -> dump.floatValue?.let { putFloat(key, it) }
                    "string_set" -> dump.stringSetValue?.let { set ->
                        putStringSet(key, set.map { fromPortablePathText(it) }.toSet())
                    }
                }
            }
        }.commit()
    }

    private fun collectReferencedFilesFromText(text: String, outFiles: MutableSet<File>) {
        if (text.isBlank()) return
        val root = filesRootPath
        val directFile = File(text)
        if (text.startsWith(root) && directFile.exists() && directFile.isFile) {
            outFiles.add(directFile)
        }
        val escapedRoot = Regex.escape(root)
        val regex = Regex("$escapedRoot/[A-Za-z0-9_./\\-]+")
        regex.findAll(text).forEach { match ->
            val candidate = File(match.value)
            if (candidate.exists() && candidate.isFile) {
                outFiles.add(candidate)
            }
        }
    }

    private fun toPortablePathText(text: String): String {
        if (text.isBlank()) return text
        val root = filesRootPath
        val altRoot = context.filesDir.absolutePath.trimEnd('/')
        return text
            .replace(root, PORTABLE_FILES_TOKEN)
            .replace(altRoot, PORTABLE_FILES_TOKEN)
    }

    private fun fromPortablePathText(text: String): String {
        if (text.isBlank()) return text
        val currentRoot = context.filesDir.absolutePath.trimEnd('/')
        // 同时兼容从旧包名 (me.rerere.rikkahub / 其他衍生包名) 导出的绝对路径自动重定位
        val legacyPkgRegex = Regex("/data/user/\\d+/[A-Za-z0-9_.]+/files")
        return text
            .replace(PORTABLE_FILES_TOKEN, currentRoot)
            .replace(legacyPkgRegex, currentRoot)
    }

    private fun DisplaySetting.toDecorSubset() = OurHomeDecorDisplaySubset(
        customFontPath = customFontPath,
        settingsBackgroundPath = settingsBackgroundPath,
        inputBackgroundPath = inputBackgroundPath,
        userAvatarFramePath = userAvatarFramePath,
        aiAvatarFramePath = aiAvatarFramePath,
        userAvatarFrameOffsetX = userAvatarFrameOffsetX,
        userAvatarFrameOffsetY = userAvatarFrameOffsetY,
        userAvatarFrameScale = userAvatarFrameScale,
        aiAvatarFrameOffsetX = aiAvatarFrameOffsetX,
        aiAvatarFrameOffsetY = aiAvatarFrameOffsetY,
        aiAvatarFrameScale = aiAvatarFrameScale,
        drawerBackgroundPath = drawerBackgroundPath,
        drawerSurfaceOpacity = drawerSurfaceOpacity,
        drawerItemAlpha = drawerItemAlpha,
        chatTextColor = chatTextColor,
        globalTextColor = globalTextColor,
        userBubbleColor = userBubbleColor,
        assistantBubbleColor = assistantBubbleColor,
        thinkingFontSizeRatio = thinkingFontSizeRatio,
        thinkingBubbleColor = thinkingBubbleColor,
        chatBackgroundColor = chatBackgroundColor,
        primaryColor = primaryColor,
        inputFieldColor = inputFieldColor,
        userBubbleImagePath = userBubbleImagePath,
        assistantBubbleImagePath = assistantBubbleImagePath,
        bubbleCornerRadius = bubbleCornerRadius,
        bubbleImageOverlayEnabled = bubbleImageOverlayEnabled,
    )

    private fun DisplaySetting.applyDecorSubset(sub: OurHomeDecorDisplaySubset) = copy(
        customFontPath = sub.customFontPath.ifBlank { customFontPath },
        settingsBackgroundPath = sub.settingsBackgroundPath.ifBlank { settingsBackgroundPath },
        inputBackgroundPath = sub.inputBackgroundPath.ifBlank { inputBackgroundPath },
        userAvatarFramePath = sub.userAvatarFramePath.ifBlank { userAvatarFramePath },
        aiAvatarFramePath = sub.aiAvatarFramePath.ifBlank { aiAvatarFramePath },
        userAvatarFrameOffsetX = sub.userAvatarFrameOffsetX,
        userAvatarFrameOffsetY = sub.userAvatarFrameOffsetY,
        userAvatarFrameScale = sub.userAvatarFrameScale,
        aiAvatarFrameOffsetX = sub.aiAvatarFrameOffsetX,
        aiAvatarFrameOffsetY = sub.aiAvatarFrameOffsetY,
        aiAvatarFrameScale = sub.aiAvatarFrameScale,
        drawerBackgroundPath = sub.drawerBackgroundPath.ifBlank { drawerBackgroundPath },
        drawerSurfaceOpacity = sub.drawerSurfaceOpacity,
        drawerItemAlpha = sub.drawerItemAlpha,
        chatTextColor = sub.chatTextColor ?: chatTextColor,
        globalTextColor = sub.globalTextColor ?: globalTextColor,
        userBubbleColor = sub.userBubbleColor ?: userBubbleColor,
        assistantBubbleColor = sub.assistantBubbleColor ?: assistantBubbleColor,
        thinkingFontSizeRatio = sub.thinkingFontSizeRatio,
        thinkingBubbleColor = sub.thinkingBubbleColor ?: thinkingBubbleColor,
        chatBackgroundColor = sub.chatBackgroundColor ?: chatBackgroundColor,
        primaryColor = sub.primaryColor ?: primaryColor,
        inputFieldColor = sub.inputFieldColor ?: inputFieldColor,
        userBubbleImagePath = sub.userBubbleImagePath.ifBlank { userBubbleImagePath },
        assistantBubbleImagePath = sub.assistantBubbleImagePath.ifBlank { assistantBubbleImagePath },
        bubbleCornerRadius = sub.bubbleCornerRadius,
        bubbleImageOverlayEnabled = sub.bubbleImageOverlayEnabled,
    )

    private fun addFileToZip(
        zipOut: ZipOutputStream,
        file: File,
        entryName: String,
        addedEntries: MutableSet<String>,
    ) {
        if (!addedEntries.add(entryName)) return
        FileInputStream(file).use { fis ->
            val zipEntry = ZipEntry(entryName)
            zipOut.putNextEntry(zipEntry)
            fis.copyTo(zipOut)
            zipOut.closeEntry()
        }
    }

    private fun addDirectoryToZip(
        zipOut: ZipOutputStream,
        rootDir: File,
        currentDir: File,
        entryPrefix: String,
        addedEntries: MutableSet<String>,
    ) {
        currentDir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                addDirectoryToZip(
                    zipOut = zipOut,
                    rootDir = rootDir,
                    currentDir = file,
                    entryPrefix = entryPrefix,
                    addedEntries = addedEntries,
                )
            } else if (file.isFile) {
                val relativePath = file.relativeTo(rootDir).invariantSeparatorsPath
                addFileToZip(zipOut, file, "$entryPrefix$relativePath", addedEntries)
            }
        }
    }

    private fun addVirtualFileToZip(
        zipOut: ZipOutputStream,
        name: String,
        content: String,
        addedEntries: MutableSet<String>,
    ) {
        if (!addedEntries.add(name)) return
        val zipEntry = ZipEntry(name)
        zipOut.putNextEntry(zipEntry)
        zipOut.write(content.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()
    }
}
