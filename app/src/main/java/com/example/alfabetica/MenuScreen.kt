@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.alfabetica
import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import kotlin.random.Random

// =================== GAME STATE MANAGEMENT ===================

object GameSettings {
    var selectedLetters by mutableStateOf(
        setOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
    )
    var soundEnabled by mutableStateOf(true)
    var vibrationEnabled by mutableStateOf(true)
    var timerDuration by mutableStateOf(10)
    
    fun getAvailableLetters(): List<Char> {
        return selectedLetters.sorted()
    }
    
    fun getRandomLetters(count: Int): List<Char> {
        val available = getAvailableLetters()
        if (available.size < count) {
            // Se não há letras suficientes, adicionar letras padrão
            val defaultLetters = ('A'..'Z').toList()
            val combined = (available + defaultLetters).distinct().sorted().take(count)
            return combined
        }
        return available.shuffled().take(count)
    }
    
    fun getOrderedLetters(count: Int): List<Char> {
        val available = getAvailableLetters()
        if (available.size >= count) {
            return available.take(count)
        }
        
        // Se não há letras suficientes, adicionar letras padrão em ordem alfabética
        val defaultLetters = ('A'..'Z').toList()
        val combined = (available + defaultLetters).distinct().sorted()
        return combined.take(count)
    }
    
    fun hasMinimumLetters(): Boolean {
        return selectedLetters.size >= 5
    }
}

private fun pickColor(index: Int): Color {
    val palette = listOf(
        Color(0xFF00C2FF),
        Color(0xFF2E7BFF),
        Color(0xFF00D7FF),
        Color(0xFF18D7C7),
        Color(0xFF3D59FF),
        Color(0xFF7A56FF)
    )
    return palette[index % palette.size]
}

// =================== GAME SCREEN ===================

@Composable
fun GameScreen(
    category: String = "FILME",
    onBack: () -> Unit = {},
    onLetter: (Char) -> Unit = {},
    onPlay: () -> Unit = {}
) {
    // Reage às letras selecionadas nos ajustes
    val selectedLetters = GameSettings.selectedLetters
    // Timer central
    var secondsLeft by remember { mutableStateOf(GameSettings.timerDuration) }
    var isTimerRunning by remember { mutableStateOf(false) }
    LaunchedEffect(isTimerRunning, secondsLeft) {
        if (isTimerRunning && secondsLeft > 0) {
            while (isTimerRunning && secondsLeft > 0) {
                kotlinx.coroutines.delay(1000)
                secondsLeft -= 1
            }
        }
    }
    val conf = LocalConfiguration.current
    val w = conf.screenWidthDp.dp
    val h = conf.screenHeightDp.dp
    
    // Padding responsivo baseado no tamanho da tela
    val sidePad = (w.value * 0.03f).coerceIn(16f, 32f).dp
    val topPad = (h.value * 0.03f).coerceIn(12f, 24f).dp
    val gutter = (w.value * 0.012f).coerceIn(8f, 16f).dp

    // Header responsivo
    val headerHeight = (h.value * 0.12f).coerceIn(48f, 80f).dp
    val headerShape = RoundedCornerShape(percent = 50)
    
    // Tamanho mínimo de toque para acessibilidade
    val minTouchTarget = 48.dp

    val palette = listOf(
        Color(0xFF00C2FF),
        Color(0xFF2E7BFF),
        Color(0xFF00D7FF),
        Color(0xFF18D7C7),
        Color(0xFF3D59FF),
        Color(0xFF7A56FF)
    )

    Box(Modifier.fillMaxSize()) {
        // Fundo igual ao do menu
        AnimatedPartyBackground()

        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = sidePad, vertical = topPad)
        ) {
            // Header cápsula com gradiente
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .clip(headerShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6D76FF), Color(0xFF22C0FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (h.value * 0.08f).coerceIn(18f, 28f).sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(gutter * 1.4f))

            // Layout adaptativo baseado nas letras selecionadas
            @Suppress("UnusedBoxWithConstraintsScope")
            BoxWithConstraints(Modifier.weight(1f)) {
                val innerPad = gutter
                val availWidth = maxWidth - innerPad * 2
                val availHeight = maxHeight - innerPad * 2

                // Letras vindas do menu Ajustes (em ordem alfabética)
                val displayLetters = remember(selectedLetters) { selectedLetters.sorted() }
                val totalLetters = displayLetters.size
                
                // Calcular distribuição adaptativa das letras
                val layoutInfo = remember(totalLetters) { calculateAdaptiveLayout(totalLetters) }
                
                val rows = 3
                val rowHeight = ((availHeight - gutter * (rows - 1)) / rows).coerceAtLeast(minTouchTarget)
                val minRowHeight = (h.value * 0.08f).coerceIn(40f, 60f).dp
                val finalRowHeight = maxOf(rowHeight, minRowHeight)

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(all = innerPad),
                    verticalArrangement = Arrangement.spacedBy(gutter)
                ) {
                    // Primeira linha
                    AdaptiveRow(
                        letters = layoutInfo.firstRowLetters,
                        displayLetters = displayLetters,
                        availWidth = availWidth,
                        gutter = gutter,
                        rowHeight = finalRowHeight,
                        onLetter = onLetter
                    )
                    
                    // Segunda linha (com timer no centro)
                    AdaptiveRowWithTimer(
                        letters = layoutInfo.secondRowLetters,
                        displayLetters = displayLetters,
                        availWidth = availWidth,
                        gutter = gutter,
                        rowHeight = finalRowHeight,
                        timerDuration = GameSettings.timerDuration,
                        secondsLeft = secondsLeft,
                        isTimerRunning = isTimerRunning,
                        onLetter = onLetter,
                        onTimerToggle = {
                            if (!isTimerRunning || secondsLeft == 0) {
                                secondsLeft = GameSettings.timerDuration
                            }
                            isTimerRunning = !isTimerRunning
                        }
                    )
                    
                    // Terceira linha
                    AdaptiveRow(
                        letters = layoutInfo.thirdRowLetters,
                        displayLetters = displayLetters,
                        availWidth = availWidth,
                        gutter = gutter,
                        rowHeight = finalRowHeight,
                        onLetter = onLetter
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterTile(
    ch: Char,
    color: Color,
    height: Dp,
    width: Dp,
    onClick: () -> Unit
) {
    val bg = if (color == Color.Unspecified) Color(0xFF1EA8FF) else color
    
    // Tamanho da fonte responsivo com limites mínimos e máximos
    val base = minOf(height.value, width.value)
    val fontSize = (base * 0.38f).coerceIn(14f, 34f).sp
    val radius = (base * 0.22f).coerceIn(10f, 22f).dp
    
    Box(
        Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(radius))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ch.toString(),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlayTile(onClick: () -> Unit, targetSize: Dp) {
    Box(
        Modifier
            .size(targetSize)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val outer = targetSize * 0.86f
        val ring = targetSize * 0.74f
        val inner = targetSize * 0.62f
        
        // Tamanho do ícone responsivo
        val iconSize = (targetSize.value * 0.3f).coerceIn(16f, 32f).dp
        
        Box(Modifier.size(outer).clip(CircleShape).background(Color(0xFF4BC8FF)))
        Box(Modifier.size(ring).clip(CircleShape).background(Color(0xFF86E0FF)))
        Box(
            Modifier.size(inner).clip(CircleShape).background(Color(0xFF132033)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

// =================== TIMER TILE ===================

@Composable
private fun TimerTile(
    totalSeconds: Int,
    secondsLeft: Int,
    running: Boolean,
    size: Dp,
    onToggle: () -> Unit
) {
    Box(
        Modifier
            .size(size)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        val outer = size * 0.92f
        val ring = size * 0.80f
        val inner = size * 0.68f
        // camadas do botão
        Box(Modifier.size(outer).clip(CircleShape).background(Color(0xFF4BC8FF)))
        Box(Modifier.size(ring).clip(CircleShape).background(Color(0xFF86E0FF)))
        Box(Modifier.size(inner).clip(CircleShape).background(Color(0xFF132033)))

        // progresso circular
        val progress = if (totalSeconds <= 0) 0f else secondsLeft.coerceAtMost(totalSeconds) / totalSeconds.toFloat()
        Canvas(modifier = Modifier.size(ring)) {
            val stroke = size.toPx() * 0.06f
            drawArc(
                color = Color(0x33132033),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )
            drawArc(
                color = Color(0xFF22C0FF),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )
        }

        val txtSize = (size.value * 0.36f).coerceIn(12f, 32f).sp
        Text(
            text = secondsLeft.coerceAtLeast(0).toString(),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = txtSize
        )
    }
}

@Composable
fun MenuScreen(
    onStart: () -> Unit = {},
    onQuickParty: () -> Unit = {},
    onClassic: () -> Unit = {},
    onCustom: () -> Unit = {},
    onPlay: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme

    // Landscape hard — se cair em portrait por algum motivo, mostra aviso amigável.
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (!isLandscape) {
        RotateToLandscape()
        return
    }

    // Estado do diálogo de categorias e dados iniciais
    var showCategoriesDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val categories = remember {
        mutableStateListOf(
            "Nome", "Animal", "Fruta", "País", "Cor", "Objeto", "Profissão"
        )
    }

    // Navegação simples: abre a tela de jogo ao clicar em "Começar"
    var showGame by remember { mutableStateOf(false) }

    if (showGame) {
        GameScreen(category = "FILME", onBack = { showGame = false })
        return
    }

    // Dimensões para tamanhos proporcionais
    val screenW = configuration.screenWidthDp.dp
    val screenH = configuration.screenHeightDp.dp

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedPartyBackground()

        // Layout 100% proporcional (landscape)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = (screenW.value * 0.06f).dp, vertical = (screenH.value * 0.07f).dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header (logo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo maior e adaptativo: baseado na altura e limitado pela largura
                val logoSize = minOf((screenH.value * 0.30f).dp, (screenW.value * 0.10f).dp)
                Image(
                    painter = painterResource(id = R.drawable.logo_alf),
                    contentDescription = "Logo Alfabetica",
                    modifier = Modifier
                        .size(logoSize)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                val titleSize = (screenH.value * 0.09f).sp
                Text(
                    text = "ALFABÉTICA",
                    color = Color.White,
                    fontSize = titleSize,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Cards em 3 colunas: altura proporcional à tela e à largura
            val cardsHeight = minOf(screenH.value * 0.34f, screenW.value * 0.22f).dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardsHeight),
                horizontalArrangement = Arrangement.spacedBy((screenW.value * 0.02f).dp)
            ) {
                ModeCard(
                    title = "Começar",
                    emoji = "▶️",
                    accent = cs.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { showGame = true; onPlay() },
                    height = cardsHeight
                )
                // Abrir diálogo de Categorias ao clicar
                ModeCard(
                    title = "Categorias",
                    emoji = "🗂️",
                    accent = cs.secondary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        showCategoriesDialog = true
                    },
                    height = cardsHeight
                )
                ModeCard(
                    title = "Ajustes",
                    emoji = "⚙️",
                    accent = cs.tertiary,
                    modifier = Modifier.weight(1f),
                    onClick = { showSettingsDialog = true },
                    height = cardsHeight
                )
            }

            // Empurra conteúdo para manter espaçamento inferior
            Spacer(Modifier.height((screenH.value * 0.04f).dp))
        }
        // Diálogo de Categorias
        CategoriesDialog(
            show = showCategoriesDialog,
            onDismiss = { showCategoriesDialog = false },
            categories = categories,
            onUpdate = { i, v -> if (i in categories.indices) categories[i] = v.trim() },
            onRemove = { i -> if (i in categories.indices) categories.removeAt(i) },
            onAdd = { v -> val t = v.trim(); if (t.isNotEmpty()) categories.add(t) }
        )
        
        // Diálogo de Ajustes
        SettingsDialog(
            show = showSettingsDialog,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

// Diálogo de categorias - Nova implementação limpa e eficiente
@Composable
private fun CategoriesDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    categories: List<String>,
    onUpdate: (Int, String) -> Unit,
    onRemove: (Int) -> Unit,
    onAdd: (String) -> Unit
) {
    if (!show) return
    
    val conf = LocalConfiguration.current
    val cs = MaterialTheme.colorScheme
    val h = conf.screenHeightDp.dp
    val w = conf.screenWidthDp.dp
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Int?>(null) }
    var categoryToEdit by remember { mutableStateOf<Int?>(null) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height((h.value * 0.7f).dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (h.value * 0.1f).dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header com botão de adicionar e fechar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Categorias",
                            style = MaterialTheme.typography.titleLarge,
                            color = cs.onSurface
                        )
                        
                        Row {
                            // Botão de adicionar categoria
                            IconButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_input_add),
                                    contentDescription = "Adicionar categoria",
                                    tint = cs.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Botão de fechar
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                    contentDescription = "Fechar",
                                    tint = cs.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    // Lista de categorias
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories.size) { index ->
                            val item = categories[index]
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = cs.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = cs.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row {
                                        IconButton(
                                            onClick = { 
                                                categoryToEdit = index
                                                showEditDialog = true
                                            }
                                        ) { 
                                            Icon(
                                                Icons.Filled.Edit,
                                                contentDescription = "Editar",
                                                tint = cs.primary
                                            ) 
                                        }
                                        IconButton(
                                            onClick = { 
                                                categoryToDelete = index
                                                showDeleteDialog = true 
                                            }
                                        ) { 
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Remover",
                                                tint = cs.error
                                            ) 
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Popup separado para adicionar categoria
            if (showAddDialog) {
                AddCategoryDialog(
                    onDismiss = { showAddDialog = false },
                    onAdd = onAdd
                )
            }
            
            // Popup separado para editar categoria
            if (showEditDialog && categoryToEdit != null) {
                EditCategoryDialog(
                    onDismiss = { 
                        showEditDialog = false
                        categoryToEdit = null
                    },
                    onUpdate = { newName ->
                        onUpdate(categoryToEdit!!, newName)
                        showEditDialog = false
                        categoryToEdit = null
                    },
                    currentName = categories[categoryToEdit!!]
                )
            }
            
            // Dialog de confirmação de exclusão
            if (showDeleteDialog && categoryToDelete != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showDeleteDialog = false
                        categoryToDelete = null
                    },
                    title = {
                        Text("Confirmar Exclusão")
                    },
                    text = {
                        Text("Tem certeza que deseja excluir a categoria \"${categories[categoryToDelete!!]}\"?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onRemove(categoryToDelete!!)
                                showDeleteDialog = false
                                categoryToDelete = null
                            }
                        ) {
                            Text("Excluir", color = cs.error)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                categoryToDelete = null
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

// Popup separado para adicionar categoria
@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var newText by remember { mutableStateOf("") }
    val newFr = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(200.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Nova Categoria",
                            style = MaterialTheme.typography.titleLarge,
                            color = cs.onSurface
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                contentDescription = "Fechar",
                                tint = cs.onSurface
                            )
                        }
                    }
                    
                    // Campo de texto
                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        singleLine = true,
                        label = { Text("Nome da categoria") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(newFr)
                    )
                    
                    // Botões
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onAdd(newText.trim())
                                newText = ""
                                onDismiss()
                            }
                        ) {
                            Text("Adicionar")
                        }
                    }
                }
            }
        }
    }
    
    // Focar no campo quando o popup abrir
    LaunchedEffect(Unit) {
        scope.launch {
            kotlinx.coroutines.delay(100)
            newFr.requestFocus()
            keyboardController?.show()
        }
    }
}

// Popup separado para editar categoria
@Composable
private fun EditCategoryDialog(
    onDismiss: () -> Unit,
    onUpdate: (String) -> Unit,
    currentName: String
) {
    var newText by remember { mutableStateOf(currentName) }
    val newFr = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(200.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Editar Categoria",
                            style = MaterialTheme.typography.titleLarge,
                            color = cs.onSurface
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                contentDescription = "Fechar",
                                tint = cs.onSurface
                            )
                        }
                    }
                    
                    // Campo de texto
                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        singleLine = true,
                        label = { Text("Nome da categoria") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(newFr)
                    )
                    
                    // Botões
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newText.trim().isNotEmpty()) {
                                    onUpdate(newText.trim())
                                }
                            },
                            enabled = newText.trim().isNotEmpty()
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
    
    // Focar no campo quando o popup abrir
    LaunchedEffect(Unit) {
        scope.launch {
            kotlinx.coroutines.delay(100)
            newFr.requestFocus()
            keyboardController?.show()
        }
    }
}

// Diálogo de Ajustes
@Composable
private fun SettingsDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return
    
    val conf = LocalConfiguration.current
    val cs = MaterialTheme.colorScheme
    val h = conf.screenHeightDp.dp
    val w = conf.screenWidthDp.dp
    
    // Usar o estado compartilhado das configurações
    val selectedLetters = GameSettings.selectedLetters
    val soundEnabled = GameSettings.soundEnabled
    val vibrationEnabled = GameSettings.vibrationEnabled
    val timerDuration = GameSettings.timerDuration
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Surface(
                color = cs.surface,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height((h.value * 0.7f).dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (h.value * 0.1f).dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ajustes",
                            style = MaterialTheme.typography.titleLarge,
                            color = cs.onSurface
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                contentDescription = "Fechar",
                                tint = cs.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Conteúdo com scroll
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Seção de Letras
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = cs.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "Letras do Jogo",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = cs.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Selecione as letras que aparecerão no jogo:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    // Aviso se poucas letras estão selecionadas
                                    if (!GameSettings.hasMinimumLetters()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "⚠️ Selecione pelo menos 5 letras para o jogo funcionar bem",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = cs.error
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Teclado de letras
                                    val alphabet = ('A'..'Z').toList()
                                    val rows = alphabet.chunked(13)
                                    
                                    rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            row.forEach { letter ->
                                                val isSelected = selectedLetters.contains(letter)
                                                Card(
                                                    modifier = Modifier
                                                        .width(28.dp)
                                                        .height(32.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSelected) cs.primary else cs.surfaceVariant
                                                    ),
                                                    elevation = CardDefaults.cardElevation(
                                                        defaultElevation = if (isSelected) 2.dp else 0.dp
                                                    ),
                                                    onClick = {
                                                        GameSettings.selectedLetters = if (isSelected) {
                                                            GameSettings.selectedLetters - letter
                                                        } else {
                                                            GameSettings.selectedLetters + letter
                                                        }
                                                    }
                                                ) {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = letter.toString(),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = if (isSelected) cs.onPrimary else cs.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                        
                        // Seção de Som
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = cs.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Som",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = cs.onSurface
                                        )
                                        Text(
                                            "Efeitos sonoros do jogo",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = cs.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    Switch(
                                        checked = soundEnabled,
                                        onCheckedChange = { GameSettings.soundEnabled = it }
                                    )
                                }
                            }
                        }
                        
                        // Seção de Vibração
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = cs.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Vibração",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = cs.onSurface
                                        )
                                        Text(
                                            "Feedback tátil do jogo",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = cs.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    Switch(
                                        checked = vibrationEnabled,
                                        onCheckedChange = { GameSettings.vibrationEnabled = it }
                                    )
                                }
                            }
                        }
                        
                        // Seção de Timer
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = cs.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "Duração do Timer",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = cs.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Tempo para cada rodada: ${timerDuration}s",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    val timerOptions = listOf(5, 10, 15, 20, 30, 60)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        timerOptions.forEach { option ->
                                            FilterChip(
                                                onClick = { GameSettings.timerDuration = option },
                                                label = { Text("${option}s") },
                                                selected = timerDuration == option,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RotateToLandscape() {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Por favor, gire o dispositivo",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Este jogo funciona somente em modo paisagem",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =================== COMPONENTES ===================

@Composable
private fun BigCta(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    height: Dp,
    radius: Dp
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "cta-scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(16.dp, RoundedCornerShape(radius), clip = false)
            .clip(RoundedCornerShape(radius))
            .background(gradient)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                contentDescription = "Começar agora"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
    }
}

@Composable
private fun ModeCard(
    title: String,
    emoji: String,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit,
    height: Dp
) {
    // Proporções derivadas da altura fornecida
    val radiusPercent = 24
    val elevation = (height.value * 0.08f).dp
    val borderW = (height.value * 0.025f).dp
    val pad = (height.value * 0.12f).dp
    val iconBox = (height.value * 0.44f).dp
    val emojiFont = (height.value * 0.28f).sp
    val titleFont = (height.value * 0.18f).sp

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "card-press")

    val shape = RoundedCornerShape(percent = radiusPercent)
    val borderBrush = Brush.horizontalGradient(listOf(accent.copy(alpha = 0.8f), accent, accent.copy(alpha = 0.8f)))
    val fillBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.16f),
            Color.White.copy(alpha = 0.10f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(fillBrush)
            .border(borderW, borderBrush, shape)
            .clickable(interactionSource = interaction, indication = LocalIndication.current, onClick = onClick)
            .padding(pad)
            .semantics { role = Role.Button; contentDescription = title }
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícone/emoji centralizado com cápsula sutil de acento
            Box(
                modifier = Modifier
                    .size(iconBox)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = emojiFont, textAlign = TextAlign.Center)
            }
            Text(
                title,
                color = Color.White,
                fontSize = titleFont,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChipToggle(label: String, on: Boolean, onToggle: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "chip-scale")
    val conf = LocalConfiguration.current
    val h = conf.screenHeightDp.dp

    val radius = (h.value * 0.04f).dp
    val padH = (h.value * 0.02f).dp
    val padW = (h.value * 0.035f).dp
    val minH = (h.value * 0.07f).dp
    val font = (h.value * 0.045f).sp

    val bg = if (on) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.10f)
    Box(
        Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .heightIn(min = minH)
            .clip(RoundedCornerShape(radius))
            .background(bg)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onToggle
            )
            .padding(horizontal = padW, vertical = padH)
            .semantics {
                role = Role.Button
                contentDescription = label
            }
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = font)
    }
}

// =================== FUNDO ANIMADO ===================

@Composable
private fun AnimatedPartyBackground() {
    val cs = MaterialTheme.colorScheme
    val t by rememberInfiniteTransition(label = "bg")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "anim"
        )

    val gradient = Brush.linearGradient(
        colors = listOf(cs.secondary, cs.primary, cs.secondary),
        start = Offset(0f, 0f),
        end = Offset(1000f * (0.6f + t), 1400f * (1.2f - t))
    )

    Box(Modifier.fillMaxSize().background(cs.background)) {
        Box(Modifier.matchParentSize().background(gradient).alpha(0.85f))
        PartySparkles(count = 80, speed = 0.12f)
        // scrim um pouco mais forte para contraste
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.16f)))
    }
}

private data class Particle(
    val start: Offset,
    val velocity: Offset,
    val radius: Float,
    val color: Color,
    val phase: Float
)

@Composable
private fun PartySparkles(count: Int, speed: Float) {
    val cs = MaterialTheme.colorScheme
    val particles = remember {
        List(count) { i ->
            val r = Random(i + 1337)
            Particle(
                start = Offset(r.nextFloat(), r.nextFloat()),
                velocity = Offset(r.nextFloat() - 0.5f, r.nextFloat() - 0.5f),
                radius = 2f + r.nextFloat() * 3f,
                color = listOf(cs.primary, cs.secondary, cs.tertiary, Color.White.copy(alpha = 0.8f))[r.nextInt(4)],
                phase = r.nextFloat()
            )
        }
    }
    val t by rememberInfiniteTransition(label = "spark")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 9000, easing = LinearEasing)
            ),
            label = "sparkAnim"
        )

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        particles.forEach { p ->
            val px = (p.start.x + (p.velocity.x * (t + p.phase) * speed)).mod(1f)
            val py = (p.start.y + (p.velocity.y * (t + p.phase) * speed)).mod(1f)
            val x = px * w; val y = py * h
            drawLine(
                p.color.copy(alpha = 0.25f),
                start = Offset(x, y),
                end = Offset(x - p.velocity.x * 30f, y - p.velocity.y * 30f),
                strokeWidth = p.radius * 0.6f,
                cap = StrokeCap.Round
            )
            drawCircle(p.color.copy(alpha = 0.9f), radius = p.radius, center = Offset(x, y))
        }
    }
}

// =================== ADAPTIVE LAYOUT ===================

data class LayoutInfo(
    val firstRowLetters: Int,
    val secondRowLetters: Int,
    val thirdRowLetters: Int
)

/**
 * Calcula a distribuição adaptativa das letras em 3 linhas
 * Garante que a linha do meio tenha número par de letras para o timer ficar no centro
 */
private fun calculateAdaptiveLayout(totalLetters: Int): LayoutInfo {
    if (totalLetters <= 0) {
        return LayoutInfo(0, 0, 0)
    }
    
    // Se temos poucas letras, distribuir de forma equilibrada
    if (totalLetters <= 6) {
        val perRow = totalLetters / 3
        val remainder = totalLetters % 3
        return when (remainder) {
            0 -> LayoutInfo(perRow, perRow, perRow)
            1 -> LayoutInfo(perRow + 1, perRow, perRow)
            else -> LayoutInfo(perRow + 1, perRow + 1, perRow)
        }
    }
    
    // Para mais letras, priorizar a linha do meio com número par
    val targetMiddleRow = if (totalLetters % 2 == 0) {
        // Se total é par, linha do meio pode ter mais letras
        (totalLetters * 0.4f).toInt().let { if (it % 2 == 0) it else it + 1 }
    } else {
        // Se total é ímpar, linha do meio deve ter número par
        ((totalLetters - 1) * 0.4f).toInt().let { if (it % 2 == 0) it else it + 1 }
    }
    
    val remainingLetters = totalLetters - targetMiddleRow
    val firstRow = remainingLetters / 2
    val thirdRow = remainingLetters - firstRow
    
    return LayoutInfo(firstRow, targetMiddleRow, thirdRow)
}

@Composable
private fun AdaptiveRow(
    letters: Int,
    displayLetters: List<Char>,
    availWidth: Dp,
    gutter: Dp,
    rowHeight: Dp,
    onLetter: (Char) -> Unit
) {
    if (letters <= 0) {
        Spacer(modifier = Modifier.height(rowHeight))
        return
    }
    
    val cellWidth = (availWidth - gutter * (letters - 1)) / letters
    
    Row(
        Modifier
            .fillMaxWidth()
            .height(rowHeight),
        horizontalArrangement = Arrangement.spacedBy(gutter),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(letters) { index ->
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (index < displayLetters.size) {
                    val ch = displayLetters[index]
                    LetterTile(
                        ch,
                        pickColor(index),
                        height = rowHeight,
                        width = cellWidth
                    ) { onLetter(ch) }
                } else {
                    Spacer(modifier = Modifier.height(rowHeight))
                }
            }
        }
    }
}

@Composable
private fun AdaptiveRowWithTimer(
    letters: Int,
    displayLetters: List<Char>,
    availWidth: Dp,
    gutter: Dp,
    rowHeight: Dp,
    timerDuration: Int,
    secondsLeft: Int,
    isTimerRunning: Boolean,
    onLetter: (Char) -> Unit,
    onTimerToggle: () -> Unit
) {
    if (letters <= 0) {
        // Se não há letras, mostrar apenas o timer centralizado
        Row(
            Modifier
                .fillMaxWidth()
                .height(rowHeight),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val timerSize = minOf(rowHeight, availWidth * 0.15f)
            TimerTile(
                totalSeconds = timerDuration,
                secondsLeft = secondsLeft,
                running = isTimerRunning,
                size = timerSize,
                onToggle = onTimerToggle
            )
        }
        return
    }
    
    val cellWidth = (availWidth - gutter * (letters - 1)) / letters
    val timerSize = minOf(rowHeight, cellWidth) * 0.92f
    
    Row(
        Modifier
            .fillMaxWidth()
            .height(rowHeight),
        horizontalArrangement = Arrangement.spacedBy(gutter),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Letras antes do timer
        val lettersBeforeTimer = letters / 2
        repeat(lettersBeforeTimer) { index ->
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (index < displayLetters.size) {
                    val ch = displayLetters[index]
                    LetterTile(
                        ch,
                        pickColor(index),
                        height = rowHeight,
                        width = cellWidth
                    ) { onLetter(ch) }
                } else {
                    Spacer(modifier = Modifier.height(rowHeight))
                }
            }
        }
        
        // Timer no centro
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            TimerTile(
                totalSeconds = timerDuration,
                secondsLeft = secondsLeft,
                running = isTimerRunning,
                size = timerSize,
                onToggle = onTimerToggle
            )
        }
        
        // Letras depois do timer
        val lettersAfterTimer = letters - lettersBeforeTimer
        repeat(lettersAfterTimer) { index ->
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                val letterIndex = lettersBeforeTimer + index
                if (letterIndex < displayLetters.size) {
                    val ch = displayLetters[letterIndex]
                    LetterTile(
                        ch,
                        pickColor(letterIndex),
                        height = rowHeight,
                        width = cellWidth
                    ) { onLetter(ch) }
                } else {
                    Spacer(modifier = Modifier.height(rowHeight))
                }
            }
        }
    }
}
