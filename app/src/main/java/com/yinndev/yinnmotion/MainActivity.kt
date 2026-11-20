package com.yinndev.yinnmotion

import android.net.Uri
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
        uris.forEach { assets.add("Media" to it) }
        if (uris.isNotEmpty()) notice = "${uris.size} media file(s) imported"
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { assets.add("Audio" to uri); notice = "Audio imported" }
    }

    MaterialTheme(colorScheme = if (appearance == "light") lightColorScheme(primary = blue, background = bg, surface = panel) else darkColorScheme(primary = blue, background = bg, surface = panel)) {
        Scaffold(containerColor = bg, bottomBar = {
            NavigationBar(containerColor = panel) {
                listOf("Home" to Icons.Default.Home, "Editor" to Icons.Default.Movie, "Code" to Icons.Default.Code, "Settings" to Icons.Default.Settings, "About" to Icons.Default.Info).forEach { (label, icon) ->
                    NavigationBarItem(selected = page == label, onClick = { page = label }, icon = { Icon(icon, null) }, label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = blue, selectedTextColor = blue, indicatorColor = Color(0xFF20364D), unselectedIconColor = muted, unselectedTextColor = muted))
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("YINN", fontSize = 25.sp, fontWeight = FontWeight.Black, color = foreground)
                        Text("MOTION STUDIO", fontSize = 10.sp, color = blue, letterSpacing = 3.sp)
                    }
                    Text("v1.1.0", color = muted, fontSize = 12.sp)
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
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Studio editor", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Color.White) }
        item {
            Box(Modifier.fillMaxWidth().height(260.dp).background(Color.Black, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PlayCircle, null, tint = blue, modifier = Modifier.size(54.dp))
                    Text("Preview canvas", color = Color.White)
                    Text("Portrait · 1080 × 1920", color = muted, fontSize = 12.sp)
                }
            }
        }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = media, modifier = Modifier.weight(1f)) { Icon(Icons.Default.VideoLibrary, null); Text("Media") }
            OutlinedButton(onClick = audio, modifier = Modifier.weight(1f)) { Icon(Icons.Default.MusicNote, null); Text("Audio") }
        } }
        item { Button(onClick = code, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Code, null); Text("Open Code Motion") } }
        item { Text("TIMELINE · ${assets.size} ASSETS", color = muted, fontSize = 11.sp, letterSpacing = 2.sp) }
        if (assets.isEmpty()) item { Text("Import video, image or audio to begin.", color = muted) }
        items(assets) { asset ->
            Surface(color = panel, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (asset.first == "Audio") Icons.Default.AudioFile else Icons.Default.Movie, null, tint = blue)
                    Spacer(Modifier.width(10.dp))
                    Column { Text(asset.first, color = Color.White); Text(asset.second.lastPathSegment ?: "Imported file", color = muted, fontSize = 11.sp) }
                }
            }
        }
        item { Text("MP4 rendering pipeline is being integrated.", color = muted, fontSize = 12.sp) }
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("About", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Surface(color = panel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Box(Modifier.size(65.dp).background(Color(0xFF203B59), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                    Text("Y", color = blue, fontSize = 40.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(14.dp))
                Text("YinnMotion", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Code-first Video Editor", color = blue)
                Spacer(Modifier.height(16.dp))
                AboutLine("Developer", "Yinndev / Ilham")
                AboutLine("Version", "1.1.0")
                AboutLine("Platform", "Android")
                AboutLine("Engine", "Kotlin · Jetpack Compose · Media3")
                AboutLine("Motion", "HTML · CSS · JavaScript")
            }
        }
        Text("Built for creators who love code.", color = muted)
    }
}

@Composable
private fun AboutLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = muted, fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}