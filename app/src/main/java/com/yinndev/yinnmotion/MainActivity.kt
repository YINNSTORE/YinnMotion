package com.yinndev.yinnmotion

import android.net.Uri
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ExportException
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private var bg = Color(0xFF090D14)
private var panel = Color(0xFF151E2C)
private var blue = Color(0xFF79BEFF)
private var muted = Color(0xFF91A0B4)
private var foreground = Color.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { YinnMotionApp(this) }
    }
}

@Composable
private fun YinnMotionApp(activity: ComponentActivity) {
    val prefs = remember { activity.getSharedPreferences("yinnmotion_settings", Context.MODE_PRIVATE) }
    var appearance by remember { mutableStateOf(prefs.getString("appearance", "dark") ?: "dark") }
    var language by remember { mutableStateOf(prefs.getString("language", "id") ?: "id") }
    var highFps by remember { mutableStateOf(prefs.getBoolean("high_fps", false)) }
    var settingsNotice by remember { mutableStateOf("") }
    var page by remember { mutableStateOf("Home") }

    LaunchedEffect(appearance, language, highFps) {
        prefs.edit().putString("appearance", appearance).putString("language", language).putBoolean("high_fps", highFps).apply()
        when (appearance) {
            "light" -> { bg = Color(0xFFF3F6FB); panel = Color.White; foreground = Color(0xFF17212E); blue = Color(0xFF1769AA); muted = Color(0xFF5E6B7A) }
            "glass" -> { bg = Color(0xFF101B2B); panel = Color(0xAA263D58); blue = Color(0xFF9BD5FF); muted = Color(0xFFB1C4D8) }
            else -> { bg = Color(0xFF090D14); panel = Color(0xFF151E2C); foreground = Color.White; blue = Color(0xFF79BEFF); muted = Color(0xFF91A0B4) }
        }
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            val attrs = activity.window.attributes
            attrs.preferredRefreshRate = if (highFps) 120f else 0f
            activity.window.attributes = attrs
            if (highFps) {
                val supported = activity.windowManager.defaultDisplay.supportedModes.any { it.refreshRate >= 119f }
                settingsNotice = if (supported) "120Hz requested; actual FPS depends on device and workload." else "This display does not expose a 120Hz mode."
            } else settingsNotice = "Standard refresh rate enabled."
        }
    }
    var projectName by remember { mutableStateOf("Untitled Project") }
    var tab by remember { mutableStateOf("HTML") }
    var html by remember { mutableStateOf("<div class=\"scene\">\\n  <h1>Hello YinnMotion</h1>\\n</div>") }
    var css by remember { mutableStateOf(".scene {\\n  display: flex;\\n  color: white;\\n  background: #080d16;\\n}") }
    var js by remember { mutableStateOf("// Add animation logic here\\nconst title = document.querySelector('h1');") }
    var notice by remember { mutableStateOf("Ready to create") }
    val assets = remember { mutableStateListOf<Pair<String, Uri>>() }

    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            val mime = activity.contentResolver.getType(uri).orEmpty()
            val kind = if (mime.startsWith("video/")) "Video" else "Image"
            assets.add(kind to uri)
            runCatching { activity.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        }
        if (uris.isNotEmpty()) notice = "${uris.size} media file(s) imported"
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            assets.add("Audio" to uri)
            runCatching { activity.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            notice = "Audio imported"
        }
    }

    MaterialTheme(colorScheme = if (appearance == "light") lightColorScheme(primary = blue, background = bg, surface = panel) else darkColorScheme(primary = blue, background = bg, surface = panel)) {
        Box(Modifier.fillMaxSize()) {
        Scaffold(containerColor = bg, bottomBar = {
            Box(Modifier.fillMaxWidth().padding(bottom = 12.dp), contentAlignment = Alignment.Center) {
                Surface(color = Color(0xDD182333), shape = RoundedCornerShape(28.dp), border = BorderStroke(1.dp, Color(0x557CCBFF)), shadowElevation = 18.dp, modifier = Modifier.fillMaxWidth(0.94f).height(76.dp)) {
                    Row(Modifier.fillMaxSize().padding(6.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        listOf("Home" to Icons.Default.Home, "Editor" to Icons.Default.Movie, "Code" to Icons.Default.Code, "Settings" to Icons.Default.Settings, "About" to Icons.Default.Info).forEach { (label, icon) ->
                            val selected = page == label
                            Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(21.dp)).background(if (selected) Color(0x553D8DCE) else Color.Transparent).clickable { page = label }.padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(icon, null, tint = if (selected) Color(0xFF9BD5FF) else Color(0xFF9AAABD), modifier = Modifier.size(23.dp))
                                Text(label, color = if (selected) Color(0xFFB9E1FF) else Color(0xFF9AAABD), fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("YINN", fontSize = 25.sp, fontWeight = FontWeight.Black, color = foreground)
                        Text("MOTION STUDIO", fontSize = 10.sp, color = blue, letterSpacing = 3.sp)
                    }
                    Text("v1.2.0", color = muted, fontSize = 12.sp)
                }
                when (page) {
                    "Home" -> HomeScreen(projectName, { projectName = it }, assets.size, notice, { page = "Editor" }, { mediaPicker.launch(arrayOf("video/*", "image/*")) }, { audioPicker.launch(arrayOf("audio/*")) })
                    "Editor" -> EditorScreen(assets.toList(), { mediaPicker.launch(arrayOf("video/*", "image/*")) }, { audioPicker.launch(arrayOf("audio/*")) }, { page = "Code" })
                    "Code" -> CodeScreen(tab, { tab = it }, html, { html = it }, css, { css = it }, js, { js = it })
                    "Settings" -> SettingsScreen(appearance, { appearance = it }, language, { language = it }, highFps, { highFps = it }, settingsNotice)
                    else -> AboutScreen()
                }
            }
        }
        }
    }
}

@Composable
private fun BrandMark(size: Int) {
    Box(Modifier.size(size.dp).background(Color(0xFF102B4B), RoundedCornerShape((size * 0.28f).dp)), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.PlayArrow, null, tint = Color(0xFFBDEAFF), modifier = Modifier.size((size * 0.68f).dp))
    }
}

@Composable
private fun HomeScreen(name: String, setName: (String) -> Unit, count: Int, notice: String, edit: () -> Unit, media: () -> Unit, audio: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Create something\nthat moves.", fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold, color = Color.White); Text("A lightweight, code-first motion studio.", color = muted) }
        item {
            Surface(color = panel, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().clickable { edit() }) {
                Column(Modifier.padding(20.dp)) {
                    Text("START CREATING", color = blue, fontSize = 11.sp, letterSpacing = 2.sp)
                    Text("New project", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Code, compose, preview and export.", color = muted)
                    Spacer(Modifier.height(15.dp))
                    Button(onClick = edit, modifier = Modifier.fillMaxWidth()) { Text("Open editor") }
                }
            }
        }
        item { Text("QUICK IMPORT", color = muted, fontSize = 11.sp, letterSpacing = 2.sp) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickTile("Video / Image", Icons.Default.AddPhotoAlternate, media, Modifier.weight(1f))
            QuickTile("Audio", Icons.Default.AudioFile, audio, Modifier.weight(1f))
        } }
        item {
            Surface(color = panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("CURRENT PROJECT", color = muted, fontSize = 10.sp, letterSpacing = 2.sp)
                    OutlinedTextField(value = name, onValueChange = setName, label = { Text("Project name") }, singleLine = true)
                    Text("$count imported assets", color = blue)
                    Text(notice, color = muted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickTile(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit, modifier: Modifier = Modifier) {
    Surface(color = panel, shape = RoundedCornerShape(16.dp), modifier = modifier.clickable { action() }) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = blue, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.White, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EditorScreen(assets: List<Pair<String, Uri>>, media: () -> Unit, audio: () -> Unit, code: () -> Unit) {
    val context = LocalContext.current
    val clips = remember { mutableStateListOf<Pair<String, Uri>>().apply { addAll(assets) } }
    LaunchedEffect(assets.size) { assets.forEach { if (it !in clips) clips.add(it) } }
    var selected by remember { mutableIntStateOf(0) }
    var exportState by remember { mutableStateOf("") }
    var exporting by remember { mutableStateOf(false) }
    val selectedClip = clips.getOrNull(selected)
    val playable = selectedClip?.takeIf { it.first == "Video" || it.first == "Audio" }
    val player = remember(playable?.second) {
        ExoPlayer.Builder(context).build().apply { if (playable != null) { setMediaItem(MediaItem.fromUri(playable.second)); prepare() } }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    fun exportSelectedVideo() {
        val clip = selectedClip
        if (clip == null || clip.first != "Video") { exportState = "Pilih klip video terlebih dahulu."; return }
        val outputDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)
        if (outputDir == null) { exportState = "Folder output tidak tersedia."; return }
        val output = java.io.File(outputDir, "YinnMotion_${System.currentTimeMillis()}.mp4")
        exporting = true
        exportState = "Menyiapkan export MP4…"
        val listener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, result: ExportResult) {
                exporting = false
                exportState = "Export selesai: ${output.absolutePath}"
            }
            override fun onError(composition: Composition, result: ExportResult, exception: ExportException) {
                exporting = false
                exportState = "Export gagal: ${exception.message ?: "format tidak didukung"}"
            }
        }
        try {
            val edited = EditedMediaItem.Builder(MediaItem.fromUri(clip.second)).build()
            Transformer.Builder(context).addListener(listener).build().start(edited, output.absolutePath)
        } catch (error: Exception) {
            exporting = false
            exportState = "Export gagal: ${error.message ?: "kesalahan tidak diketahui"}"
        }
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Studio", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = foreground)
                    Text("Editor workspace • ${clips.size} clips", color = muted, fontSize = 13.sp)
                }
                Text("9:16", color = blue, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Surface(color = Color.Black, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(250.dp)) {
                if (playable != null) {
                    AndroidView(factory = { viewContext -> PlayerView(viewContext).apply { this.player = player; useController = true } }, modifier = Modifier.fillMaxSize(), update = { it.player = player })
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Movie, null, tint = Color(0xFF9BD5FF), modifier = Modifier.size(52.dp))
                            Text(if (selectedClip?.first == "Image") "Image layer selected" else "Preview canvas", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(selectedClip?.second?.lastPathSegment ?: "Import a video to preview it here", color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = media, modifier = Modifier.weight(1f)) { Icon(Icons.Default.VideoLibrary, null); Spacer(Modifier.width(5.dp)); Text("Import media") }
                OutlinedButton(onClick = audio, modifier = Modifier.weight(1f)) { Icon(Icons.Default.MusicNote, null); Spacer(Modifier.width(5.dp)); Text("Audio") }
            }
        }
        item {
            Surface(color = panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("TIMELINE", color = blue, fontSize = 11.sp, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
                        Text("${clips.size} clips", color = muted, fontSize = 11.sp)
                    }
                    if (clips.isEmpty()) Text("Import media to create your first track.", color = muted, fontSize = 12.sp)
                    clips.forEachIndexed { index, asset ->
                        Surface(color = if (selected == index) Color(0xFF25496A) else Color(0xFF101722), shape = RoundedCornerShape(11.dp), modifier = Modifier.fillMaxWidth().clickable { selected = index }) {
                            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}", color = blue, fontSize = 11.sp)
                                Spacer(Modifier.width(8.dp))
                                Icon(when(asset.first) { "Audio" -> Icons.Default.AudioFile; "Image" -> Icons.Default.Image; else -> Icons.Default.Movie }, null, tint = blue)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(asset.first, color = foreground, fontSize = 13.sp)
                                    Text(asset.second.lastPathSegment ?: "Imported file", color = muted, fontSize = 10.sp, maxLines = 1)
                                }
                                IconButton(onClick = { if (index > 0) { val item = clips.removeAt(index); clips.add(index - 1, item); selected = index - 1 } }, enabled = index > 0) { Icon(Icons.Default.KeyboardArrowUp, "Move up", tint = foreground) }
                                IconButton(onClick = { clips.removeAt(index); selected = (selected - 1).coerceAtLeast(0) }) { Icon(Icons.Default.DeleteOutline, "Remove", tint = Color(0xFFFF8C8C)) }
                            }
                        }
                    }
                }
            }
        }
        item {
            Button(onClick = ::exportSelectedVideo, enabled = !exporting && selectedClip?.first == "Video", modifier = Modifier.fillMaxWidth()) {
                if (exporting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Icon(Icons.Default.FileUpload, null)
                Spacer(Modifier.width(8.dp))
                Text(if (exporting) "Exporting MP4…" else "Export selected video to MP4")
            }
            if (exportState.isNotBlank()) Text(exportState, color = muted, fontSize = 11.sp)
        }
        item { OutlinedButton(onClick = code, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Code, null); Text("Open Code Motion") } }
        item { Text("Current export renders the selected source video to MP4. Multi-layer compositing, trimming, effects and animated text are not yet part of this renderer.", color = muted, fontSize = 11.sp) }
    }
}
@Composable
private fun CodeScreen(tab: String, setTab: (String) -> Unit, html: String, setHtml: (String) -> Unit, css: String, setCss: (String) -> Unit, js: String, setJs: (String) -> Unit) {
    val code = when (tab) { "CSS" -> css; "JavaScript" -> js; else -> html }
    val update = when (tab) { "CSS" -> setCss; "JavaScript" -> setJs; else -> setHtml }
    Column(Modifier.fillMaxSize()) {
        Text("Code Motion", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Edit HTML, CSS and JavaScript.", color = muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("HTML", "CSS", "JavaScript").forEach { FilterChip(selected = tab == it, onClick = { setTab(it) }, label = { Text(it) }) }
        }
        Spacer(Modifier.height(10.dp))
        Surface(color = Color(0xFF101722), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxSize()) {
            BasicTextField(value = code, onValueChange = update, modifier = Modifier.fillMaxSize().padding(14.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFD8E6F6), fontSize = 13.sp, lineHeight = 20.sp),
                decorationBox = { inner -> Box { if (code.isEmpty()) Text("Write code here…", color = muted); inner() } })
        }
    }
}


@Composable
private fun SettingsScreen(
    appearance: String, setAppearance: (String) -> Unit,
    language: String, setLanguage: (String) -> Unit,
    highFps: Boolean, setHighFps: (Boolean) -> Unit,
    notice: String
) {
    val id = language == "id"
    val titleColor = foreground
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(if (id) "Pengaturan" else "Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Text(if (id) "Atur tampilan dan performa aplikasi." else "Customize appearance and performance.", color = muted)
        }
        item {
            Surface(color = panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (id) "TAMPILAN" else "APPEARANCE", color = blue, fontSize = 11.sp, letterSpacing = 2.sp)
                    listOf("dark" to (if (id) "Mode malam" else "Dark mode"), "light" to (if (id) "Mode siang" else "Light mode"), "glass" to "Liquid Glass").forEach { (value, label) ->
                        Row(Modifier.fillMaxWidth().clickable { setAppearance(value) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = appearance == value, onClick = { setAppearance(value) })
                            Text(label, color = titleColor)
                        }
                    }
                }
            }
        }
        item {
            Surface(color = panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(if (id) "BAHASA" else "LANGUAGE", color = blue, fontSize = 11.sp, letterSpacing = 2.sp)
                    listOf("id" to "Bahasa Indonesia", "en" to "English").forEach { (value, label) ->
                        Row(Modifier.fillMaxWidth().clickable { setLanguage(value) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = language == value, onClick = { setLanguage(value) })
                            Text(label, color = titleColor)
                        }
                    }
                }
            }
        }
        item {
            Surface(color = panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(if (id) "PERFORMA EKSPERIMENTAL" else "EXPERIMENTAL PERFORMANCE", color = blue, fontSize = 11.sp, letterSpacing = 2.sp)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("120 FPS / 120Hz", color = titleColor, fontWeight = FontWeight.SemiBold)
                            Text(if (id) "Meminta refresh rate tinggi jika layar mendukung. Tidak menjamin 120 FPS." else "Requests a high refresh rate when supported. Does not guarantee 120 FPS.", color = muted, fontSize = 12.sp)
                        }
                        Switch(checked = highFps, onCheckedChange = setHighFps)
                    }
                    if (notice.isNotBlank()) Text(notice, color = muted, fontSize = 11.sp)
                }
            }
        }
        item { Text(if (id) "Pengaturan tersimpan otomatis. Fitur 120Hz bergantung pada perangkat." else "Settings save automatically. 120Hz depends on device support.", color = muted, fontSize = 12.sp) }
    }
}

@Composable
private fun AboutScreen() {
    val context = LocalContext.current
    val links = listOf(
        Triple("Telegram", "@yinnprovpn", "https://t.me/yinnprovpn"),
        Triple("TikTok", "@yinnzzz7", "https://www.tiktok.com/@yinnzzz7"),
        Triple("Instagram", "@amm0.skizzo__", "https://www.instagram.com/amm0.skizzo__/")
    )
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(245.dp).background(Brush.verticalGradient(listOf(Color(0xFF163B65), Color(0xFF080E19))), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BrandMark(88)
                    Spacer(Modifier.height(14.dp))
                    Text("YINNMOTION", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Text("CREATE • MOVE • INSPIRE", color = Color(0xFF9BD5FF), fontSize = 10.sp, letterSpacing = 2.sp)
                }
            }
        }
        item {
            Text("YINNDEV", color = foreground, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Pengembang aplikasi", color = muted, fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            Text("YinnMotion adalah studio kreatif Android untuk mengolah media dan membuat motion dengan workflow berbasis kode.", color = foreground, fontSize = 14.sp)
        }
        item { Text("MEDIA SOSIAL", color = muted, fontSize = 12.sp, letterSpacing = 2.sp) }
        item {
            Surface(color = panel, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 18.dp)) {
                    links.forEachIndexed { index, item ->
                        Row(Modifier.fillMaxWidth().clickable {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(item.third))
                            context.startActivity(intent)
                        }.padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(when(item.first) { "Telegram" -> Icons.Default.Send; "TikTok" -> Icons.Default.MusicVideo; else -> Icons.Default.CameraAlt }, null, tint = foreground, modifier = Modifier.size(25.dp))
                            Spacer(Modifier.width(18.dp))
                            Text(item.first, color = foreground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(item.second, color = muted, fontSize = 13.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ChevronRight, null, tint = muted)
                        }
                        if (index != links.lastIndex) Divider(color = muted.copy(alpha = 0.22f))
                    }
                }
            }
        }
        item {
            Surface(color = Color(0xFFB8D4F6), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().clickable {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/yinnprovpn"))
                context.startActivity(intent)
            }) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = Color(0xFF203A5C), modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Dukung developer", color = Color(0xFF203A5C), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Terhubung dan dukung pengembangan YinnMotion", color = Color(0xFF304D70), fontSize = 11.sp)
                    }
                }
            }
        }
        item { Text("YinnMotion v1.2.0 • Crafted by Yinndev", color = muted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 14.dp)) }
    }
}
@Composable
private fun AboutLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = muted, fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}