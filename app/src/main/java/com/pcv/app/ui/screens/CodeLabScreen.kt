package com.pcv.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pcv.app.codelab.AnalysisResult
import com.pcv.app.codelab.CodeImprovementEngine
import com.pcv.app.codelab.PCV_SOURCE_FILES
import com.pcv.app.data.*
import com.pcv.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeLabScreen(engine: CodeImprovementEngine) {
    val scope     = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    var apiKey         by remember { mutableStateOf(engine.apiKey) }
    var showKeyDialog  by remember { mutableStateOf(!engine.hasApiKey) }
    var selectedFile   by remember { mutableStateOf(PCV_SOURCE_FILES.first()) }
    var sourceCode     by remember { mutableStateOf("") }
    var isAnalyzing    by remember { mutableStateOf(false) }
    var result         by remember { mutableStateOf<AnalysisResult?>(null) }
    var errorMsg       by remember { mutableStateOf<String?>(null) }
    var expandedCards  by remember { mutableStateOf(setOf<Int>()) }
    var showSource     by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFile) {
        sourceCode = engine.loadSourceCode(selectedFile)
        result = null; errorMsg = null
    }

    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { if (engine.hasApiKey) showKeyDialog = false },
            icon    = { Icon(Icons.Filled.VpnKey, null, tint = CyanPrimary) },
            title   = { Text("Anthropic API Key") },
            text    = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your key from console.anthropic.com to enable AI code analysis.",
                        style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(apiKey, { apiKey = it }, label = { Text("sk-ant-…") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = { engine.apiKey = apiKey; showKeyDialog = false },
                    enabled = apiKey.isNotBlank()) { Text("Save") }
            },
            dismissButton = {
                if (engine.hasApiKey) TextButton(onClick = { showKeyDialog = false }) { Text("Cancel") }
            }
        )
    }

    PCVScreen(title = "Code Lab") {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(if (engine.hasApiKey) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    null, tint = if (engine.hasApiKey) GreenActive else AmberAccent,
                    modifier = Modifier.size(16.dp))
                Text(if (engine.hasApiKey) "API key configured" else "API key required",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (engine.hasApiKey) GreenActive else AmberAccent)
            }
            TextButton(onClick = { showKeyDialog = true }) {
                Icon(Icons.Filled.Edit, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Change", style = MaterialTheme.typography.labelSmall)
            }
        }

        PCVCard {
            Text("Source File", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
            var dropdownOpen by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = dropdownOpen, onExpandedChange = { dropdownOpen = !dropdownOpen }) {
                OutlinedTextField(
                    value         = "${selectedFile.module}/${selectedFile.name}",
                    onValueChange = {},
                    readOnly      = true,
                    modifier      = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownOpen) },
                    label         = { Text("Select file to analyze") }
                )
                ExposedDropdownMenu(expanded = dropdownOpen, onDismissRequest = { dropdownOpen = false }) {
                    PCV_SOURCE_FILES.groupBy { it.module }.forEach { (module, files) ->
                        DropdownMenuItem(text = { Text(module.uppercase(),
                            style = MaterialTheme.typography.labelSmall, color = CyanPrimary) },
                            onClick = {}, enabled = false)
                        files.forEach { file ->
                            DropdownMenuItem(
                                text    = { Text("  ${file.name}",
                                    color = if (file == selectedFile) CyanPrimary else TextPrimary) },
                                onClick = { selectedFile = file; dropdownOpen = false }
                            )
                        }
                    }
                }
            }

            Button(onClick = {
                scope.launch {
                    isAnalyzing = true; result = null; errorMsg = null; expandedCards = setOf()
                    result      = engine.analyzeFile(selectedFile, sourceCode)
                    isAnalyzing = false
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = engine.hasApiKey && !isAnalyzing) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp)); Text("Analyzing…")
                } else {
                    Icon(Icons.Filled.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("Analyze with AI")
                }
            }

            TextButton(onClick = { showSource = !showSource }, modifier = Modifier.align(Alignment.End)) {
                Icon(if (showSource) Icons.Filled.VisibilityOff else Icons.Filled.Code,
                    null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (showSource) "Hide Source" else "View Source", style = MaterialTheme.typography.labelSmall)
            }
            AnimatedVisibility(showSource) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("${sourceCode.lines().size} lines",
                            style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        IconButton(onClick = { clipboard.setText(AnnotatedString(sourceCode)) }) {
                            Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(8.dp)).background(NavyDeep)
                        .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
                        .verticalScroll(rememberScrollState()).padding(12.dp)) {
                        Text(sourceCode, fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                            color = TextPrimary, lineHeight = 16.sp)
                    }
                }
            }
        }

        errorMsg?.let { msg ->
            Surface(color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Error, null, tint = RedAlert)
                    Text(msg, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        when (val res = result) {
            is AnalysisResult.Error   -> { errorMsg = res.message }
            is AnalysisResult.Success -> {
                Text("${res.improvements.size} improvements found in ${selectedFile.name}",
                    style = MaterialTheme.typography.titleSmall, color = CyanPrimary)
                res.improvements.forEachIndexed { idx, imp ->
                    ImprovementCard(
                        improvement    = imp,
                        expanded       = idx in expandedCards,
                        onToggle       = { expandedCards = if (idx in expandedCards) expandedCards - idx else expandedCards + idx },
                        onCopyImproved = { clipboard.setText(AnnotatedString(imp.improvedCode)) }
                    )
                }
            }
            null -> {}
        }
    }
}

@Composable
private fun ImprovementCard(
    improvement   : CodeImprovement,
    expanded      : Boolean,
    onToggle      : () -> Unit,
    onCopyImproved: () -> Unit
) {
    val (catColor, catLabel) = when (improvement.category) {
        ImprovementCategory.Performance  -> CyanPrimary  to "Performance"
        ImprovementCategory.Architecture -> AmberAccent  to "Architecture"
        ImprovementCategory.Memory       -> Color(0xFF9C27B0) to "Memory"
        ImprovementCategory.Security     -> RedAlert     to "Security"
        ImprovementCategory.BestPractice -> GreenActive  to "Best Practice"
    }
    val (sevColor, sevLabel) = when (improvement.severity) {
        ImprovementSeverity.Critical   -> RedAlert   to "Critical"
        ImprovementSeverity.Warning    -> AmberAccent to "Warning"
        ImprovementSeverity.Suggestion -> GreenActive to "Suggestion"
    }
    Surface(shape = RoundedCornerShape(12.dp), color = NavyCard,
        modifier = Modifier.fillMaxWidth().border(1.dp, NavyBorder, RoundedCornerShape(12.dp))) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge(containerColor = catColor.copy(alpha = 0.15f)) {
                            Text(catLabel, color = catColor, style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                        Badge(containerColor = sevColor.copy(alpha = 0.15f)) {
                            Text(sevLabel, color = sevColor, style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(improvement.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = TextSecondary)
            }
            AnimatedVisibility(expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(improvement.description, style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary, lineHeight = 18.sp)
                    if (improvement.originalCode.isNotBlank()) CodeBlock("Before", improvement.originalCode, RedAlert)
                    if (improvement.improvedCode.isNotBlank()) {
                        CodeBlock("After", improvement.improvedCode, GreenActive)
                        OutlinedButton(onClick = onCopyImproved, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp)); Text("Copy improved code", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(label: String, code: String, labelColor: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = labelColor)
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
            .background(NavyDeep).border(1.dp, NavyBorder, RoundedCornerShape(6.dp)).padding(10.dp)) {
            Text(code, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimary, lineHeight = 16.sp)
        }
    }
}
