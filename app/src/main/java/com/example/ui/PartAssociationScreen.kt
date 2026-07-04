package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.PartAssociation
import com.example.data.ScannedPart
import com.example.data.Vehicle
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartAssociationScreen(viewModel: PartAssociationViewModel, onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsState()
    val showToast by viewModel.showToastMessage.collectAsState()

    // Trigger android toast when message changes
    LaunchedEffect(showToast) {
        showToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = "Industrial Logo",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "FINDER PARTS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu de Navegação",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSyncMode() }) {
                        val isOnline by viewModel.isOnline.collectAsState()
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudSync else Icons.Default.CloudOff,
                            contentDescription = "Sincronização",
                            tint = if (isOnline) IndustrialTertiaryFixed else Color.Gray
                        )
                    }
                    IconButton(onClick = { /* Configurações */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndustrialPrimary
                )
            )
        },
        bottomBar = {
            Column {
                // Bottom Bar for Mobile Layout
                NavigationBar(
                    containerColor = IndustrialPrimary,
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        Triple("BUSCAR", Icons.Default.Search, "Buscar"),
                        Triple("MINHAS PEÇAS", Icons.Default.Inventory2, "Minhas Peças"),
                        Triple("ESCANEAR", Icons.Default.QrCodeScanner, "Escanear"),
                        Triple("PERFIL", Icons.Default.Person, "Perfil")
                    )

                    tabs.forEach { (tabId, icon, label) ->
                        val isSelected = activeTab == tabId
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tabId) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) IndustrialTertiaryFixed else Color.White.copy(alpha = 0.6f)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    color = if (isSelected) IndustrialTertiaryFixed else Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = IndustrialPrimary.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (activeTab == "ESCANEAR") {
                FloatingActionButton(
                    onClick = { viewModel.saveAssociation() },
                    containerColor = IndustrialPrimary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .testTag("save_fab_mobile")
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Salvar Registro",
                        tint = IndustrialTertiaryFixed
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                "ESCANEAR" -> EscanearTab(viewModel)
                "MINHAS PEÇAS" -> MinhasPecasTab(viewModel)
                "BUSCAR" -> BuscarTab(viewModel)
                "PERFIL" -> PerfilTab(viewModel, onLogout)
            }
        }
    }
}

@Composable
fun EscanearTab(viewModel: PartAssociationViewModel) {
    val scannedPart by viewModel.scannedPart.collectAsState()
    val primaryVehicle by viewModel.primaryVehicle.collectAsState()
    val compatibleVehicles by viewModel.compatibleVehicles.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()

    var showPrimarySelectDialog by remember { mutableStateOf(false) }
    var showCompatibilityAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Info
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "FINDER PARTS • REGISTRO MASTER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Precisão de Componentes",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = IndustrialPrimary
                    )
                )
                Text(
                    text = "Sincronização.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Section C: Scanner Preview Box
        item {
            ScannerBox(scannedPart = scannedPart, onSimulateScan = { viewModel.simulateScan() })
        }

        // Section Original Reference Card
        item {
            OriginalReferenceCard(part = scannedPart)
        }

        // Section Target Vehicle Association (Dark card)
        item {
            TargetVehicleCard(
                vehicle = primaryVehicle,
                onChangeVehicle = { showPrimarySelectDialog = true },
                onConfirmPrimaryLink = {
                    viewModel.saveAssociation()
                }
            )
        }

        // Section Cross Compatibility Panel
        item {
            CrossCompatibilityPanel(
                compatibleVehicles = compatibleVehicles,
                onAddTagClicked = { showCompatibilityAddDialog = true },
                onRemoveTag = { viewModel.removeCompatibleVehicle(it) }
            )
        }

        // Status Bar
        item {
            val statusColor = if (isOnline) StatusGreen else Color.Gray
            val statusText = if (isOnline) "API CONECTADA - SINCRONIZANDO DADOS..." else "MODO OFFLINE - VÍNCULOS SALVOS LOCALMENTE"
            val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor)
                    .alpha(pulseAlpha)
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.CloudSync else Icons.Default.CloudOff,
                        contentDescription = "Sync icon",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Desktop action bar (visual integration)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SESSÃO DE REGISTRO",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Operador: Sagacitas",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = { viewModel.saveAssociation() },
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialPrimary),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.testTag("save_desktop_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Salvar Associação", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Salvar",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogue for choosing the target primary vehicle
    if (showPrimarySelectDialog) {
        VehicleSelectionDialog(
            title = "Selecione o Veículo Alvo Principal",
            vehicles = allVehicles,
            onVehicleSelected = {
                viewModel.selectPrimaryVehicle(it)
                showPrimarySelectDialog = false
            },
            onDismiss = { showPrimarySelectDialog = false }
        )
    }

    // Dialogue for choosing cross-compatibility vehicle
    if (showCompatibilityAddDialog) {
        VehicleSelectionDialog(
            title = "Adicionar Compatibilidade Cruzada",
            vehicles = allVehicles,
            onVehicleSelected = {
                viewModel.addCompatibleVehicle(it)
                showCompatibilityAddDialog = false
            },
            onDismiss = { showCompatibilityAddDialog = false }
        )
    }
}

@Composable
fun ScannerBox(scannedPart: ScannedPart?, onSimulateScan: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.77f)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black)
            .border(2.dp, IndustrialPrimary, RoundedCornerShape(4.dp))
    ) {
        if (scannedPart != null) {
            // Coil load the image
            AsyncImage(
                model = scannedPart.imageUrl,
                contentDescription = "Peça Escaneada",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.85f)
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera placeholder",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhuma peça escaneada", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Decorative laser scan line
        val infiniteTransition = rememberInfiniteTransition(label = "scanner")
        val translateY by infiniteTransition.animateFloat(
            initialValue = 0.05f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "laser"
        )

        // Draw laser scan line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height * translateY
            drawLine(
                color = IndustrialTertiaryFixed,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Active Scanner Status Chip Overlay
        Box(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
                .background(IndustrialPrimary.copy(alpha = 0.85f), RoundedCornerShape(2.dp))
                .border(1.dp, IndustrialTertiaryFixed, RoundedCornerShape(2.dp))
                .padding(vertical = 4.dp, horizontal = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(IndustrialTertiaryFixed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SCANNER ATIVO",
                    color = IndustrialTertiaryFixed,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                )
            }
        }

        // Scanner Trigger Action button
        IconButton(
            onClick = onSimulateScan,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Cached,
                contentDescription = "Alternar Peça",
                tint = Color.White
            )
        }

        // Barcode Overlay at Bottom
        if (scannedPart != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Barcode Icon",
                        tint = IndustrialTertiaryFixed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = scannedPart.barcode,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun OriginalReferenceCard(part: ScannedPart?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REFERÊNCIA ORIGINAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = IndustrialPrimary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = part?.originalReference ?: "N/A",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = IndustrialPrimary
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .background(IndustrialTertiaryFixed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verificado",
                        tint = IndustrialPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "NOME DA PEÇA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                )
                Text(
                    text = part?.name ?: "NENHUMA PEÇA DETECTADA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = IndustrialPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "ESPECIFICAÇÃO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                )
                Text(
                    text = part?.specification ?: "N/A",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = IndustrialPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun TargetVehicleCard(
    vehicle: Vehicle?,
    onChangeVehicle: () -> Unit,
    onConfirmPrimaryLink: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = IndustrialPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Semi-transparent background icon
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.05f),
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 24.dp, y = 24.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IndustrialTertiaryFixed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ASSOCIAÇÃO DE VEÍCULO ALVO",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    TextButton(onClick = onChangeVehicle) {
                        Text(
                            "ALTERAR",
                            color = IndustrialTertiaryFixed,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (vehicle != null) {
                    GridRow(
                        label1 = "MARCA",
                        val1 = vehicle.brand,
                        icon1 = Icons.Default.MinorCrash,
                        label2 = "MODELO",
                        val2 = vehicle.model
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GridRow(
                        label1 = "MODIFICAÇÃO",
                        val1 = vehicle.modification,
                        icon1 = null,
                        label2 = "ANO",
                        val2 = vehicle.year.toString()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum veículo selecionado",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onConfirmPrimaryLink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_primary_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialTertiaryFixed),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Confirm Link Icon",
                            tint = IndustrialOnPrimaryFixed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONFIRMAR VÍNCULO PRIMÁRIO",
                            color = IndustrialOnPrimaryFixed,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridRow(
    label1: String,
    val1: String,
    icon1: androidx.compose.ui.graphics.vector.ImageVector?,
    label2: String,
    val2: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label1,
                color = IndustrialTertiaryFixed.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon1 != null) {
                    Icon(
                        imageVector = icon1,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = val1,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label2,
                color = IndustrialTertiaryFixed.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = val2,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrossCompatibilityPanel(
    compatibleVehicles: List<Vehicle>,
    onAddTagClicked: () -> Unit,
    onRemoveTag: (Vehicle) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PAINEL DE COMPATIBILIDADE CRUZADA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = IndustrialPrimary
                    )
                )

                Box(
                    modifier = Modifier
                        .background(IndustrialPrimary, RoundedCornerShape(2.dp))
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                ) {
                    Text(
                        text = "${compatibleVehicles.size} ATIVOS",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fake Search Bar matching HTML
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .clickable { onAddTagClicked() }
                    .padding(vertical = 12.dp, horizontal = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = IndustrialPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BUSCAR COMPATIBILIDADE DE VEÍCULO...",
                        color = Color.Gray.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Compatibility chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                compatibleVehicles.forEach { vehicle ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(2.dp))
                            .border(1.dp, IndustrialPrimary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                            .padding(start = 10.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = "${vehicle.brand} ${vehicle.model} ${vehicle.year}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = IndustrialPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onRemoveTag(vehicle) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove tag",
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Plus add tag button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Transparent, RoundedCornerShape(2.dp))
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(Color.Gray, Color.LightGray)),
                            RoundedCornerShape(2.dp)
                        )
                        .clickable { onAddTagClicked() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Compatibility",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ ADD TAG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleSelectionDialog(
    title: String,
    vehicles: List<Vehicle>,
    onVehicleSelected: (Vehicle) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vehicles) { vehicle ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                .clickable { onVehicleSelected(vehicle) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = IndustrialPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${vehicle.brand} ${vehicle.model} (${vehicle.year})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                                )
                                Text(
                                    text = vehicle.modification,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MinhasPecasTab(viewModel: PartAssociationViewModel) {
    val associations by viewModel.allAssociations.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()
    val allParts by viewModel.allScannedParts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "MINHAS ASSOCIAÇÕES SALVAS",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = IndustrialPrimary)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Lista de compatibilidades registradas no banco de dados local da Sagacitas.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (associations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Empty list",
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nenhuma associação registrada ainda.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                    )
                    Text(
                        text = "Vá para a guia ESCANEAR para salvar seu primeiro link.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(associations) { assoc ->
                    val part = allParts.find { it.barcode == assoc.barcode }
                    val primaryVehicle = allVehicles.find { it.id == assoc.primaryVehicleId }
                    val compatibleList = allVehicles.filter { assoc.compatibleVehicleIds.contains(it.id) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Part",
                                        tint = IndustrialPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = part?.name ?: "Peça Desconhecida",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteAssociation(assoc.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir",
                                        tint = StatusRed
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "VÍNCULO PRINCIPAL:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                            )
                            if (primaryVehicle != null) {
                                Text(
                                    text = "${primaryVehicle.brand} ${primaryVehicle.model} (${primaryVehicle.year})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                                )
                                Text(
                                    text = primaryVehicle.modification,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            } else {
                                Text("Veículo Desconhecido", style = MaterialTheme.typography.bodyMedium)
                            }

                            if (compatibleList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "COMPATIBILIDADE CRUZADA:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    compatibleList.forEach { v ->
                                        Box(
                                            modifier = Modifier
                                                .background(Color.White, RoundedCornerShape(2.dp))
                                                .border(1.dp, IndustrialPrimary.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                        ) {
                                            Text(
                                                text = "${v.brand} ${v.model}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IndustrialPrimary)
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
fun BuscarTab(viewModel: PartAssociationViewModel) {
    val allParts by viewModel.allScannedParts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val filteredParts = if (searchQuery.isEmpty()) {
        allParts
    } else {
        allParts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.originalReference.contains(searchQuery, ignoreCase = true) ||
                    it.barcode.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "BUSCAR CATÁLOGO (REGIONAL: BR)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = IndustrialPrimary)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Filtro estrito para o mercado brasileiro ativo de acordo com as especificações do Finder Parts.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Pesquise por nome, referência original ou código...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndustrialPrimary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredParts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma peça encontrada com essa pesquisa.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredParts) { part ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.simulateScan(part.barcode) },
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = part.imageUrl,
                                contentDescription = part.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = part.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                                )
                                Text(
                                    text = "Ref: ${part.originalReference}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                                Text(
                                    text = "Espec: ${part.specification}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = IndustrialPrimary, fontWeight = FontWeight.Bold)
                                )
                            }
                            IconButton(onClick = { viewModel.simulateScan(part.barcode) }) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Carregar para Escanear",
                                    tint = IndustrialPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerfilTab(viewModel: PartAssociationViewModel, onLogout: () -> Unit = {}) {
    val associations by viewModel.allAssociations.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()
    val allParts by viewModel.allScannedParts.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    var supabaseUrl by remember { mutableStateOf("https://uynvshykmsl.supabase.co") }
    var supabaseKey by remember { mutableStateOf("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.anon-key-placeholder") }
    var isSyncing by remember { mutableStateOf(false) }
    var syncLog by remember { mutableStateOf<List<String>>(emptyList()) }
    var showApiPayload by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val jsonPayload = remember(associations, allVehicles, allParts) {
        if (associations.isEmpty()) {
            "[]"
        } else {
            val list = associations.map { assoc ->
                val part = allParts.find { it.barcode == assoc.barcode }
                val primary = allVehicles.find { it.id == assoc.primaryVehicleId }
                val compatible = allVehicles.filter { assoc.compatibleVehicleIds.contains(it.id) }
                val compatibleString = compatible.joinToString(",\n                ") { v ->
                    "{\"id\": ${v.id}, \"brand\": \"${v.brand}\", \"model\": \"${v.model}\", \"year\": ${v.year}}"
                }
                """{
              "barcode": "${assoc.barcode}",
              "original_part_number": "${part?.originalReference ?: ""}",
              "part_name": "${part?.name ?: ""}",
              "primary_vehicle": {
                "id": ${primary?.id ?: 0},
                "brand": "${primary?.brand ?: ""}",
                "model": "${primary?.model ?: ""}",
                "year": ${primary?.year ?: 0}
              },
              "compatible_vehicles": [
                $compatibleString
              ]
            }"""
            }
            "[\n" + list.joinToString(",\n") { it.prependIndent("  ") } + "\n]"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Info
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "FINDER PARTS MASTER • PAINEL DE INTEGRAÇÃO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Base de Dados Proprietária",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = IndustrialPrimary
                    )
                )
                Text(
                    text = "Distribuição via API & Supabase",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                )
            }
        }

        // Section A: Master DB Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Database",
                            tint = IndustrialPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TABELAS ATIVAS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "4 Locais (Room)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Associations",
                            tint = IndustrialPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "VÍNCULOS SALVOS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${associations.size} Registros",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                        )
                    }
                }
            }
        }

        // Section B: Supabase Sync Settings Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = IndustrialPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(IndustrialTertiaryFixed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CANAL DE INTEGRAÇÃO SUPABASE",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(if (isOnline) StatusGreen else Color.Gray, RoundedCornerShape(2.dp))
                                .padding(vertical = 2.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = if (isOnline) "API ATIVA" else "MODO LOCAL",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Configurações de rede para alimentar o ecossistema SIMU-MES Oficina e outras aplicações parceiras.",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Connection fields
                    OutlinedTextField(
                        value = supabaseUrl,
                        onValueChange = { supabaseUrl = it },
                        label = { Text("SUPABASE_URL", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndustrialTertiaryFixed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = supabaseKey,
                        onValueChange = { supabaseKey = it },
                        label = { Text("SUPABASE_KEY (SERVICE ROLE / SERVICE API)", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndustrialTertiaryFixed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                isSyncing = true
                                syncLog = listOf(
                                    "Iniciando handshake com Supabase em $supabaseUrl",
                                    "Autenticando com chaves anon/service do Finder Parts...",
                                    "Mapeando base de dados proprietária local (Room)",
                                    "Sincronizando tabela 'veiculos' (Master)...",
                                    "Sincronizando tabela 'pecas_originais' (Master)...",
                                    "Sincronizando tabela 'produtos_fisicos' (Master)...",
                                    "Sincronizando tabela 'compatibilidade_pecas' (Master)...",
                                    "Sucesso! Sincronização em lote finalizada. 4 tabelas integradas com sucesso."
                                )
                                isSyncing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialTertiaryFixed),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(color = IndustrialOnPrimaryFixed, modifier = Modifier.size(16.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync", tint = IndustrialOnPrimaryFixed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SINCRONIZAR AGORA", color = IndustrialOnPrimaryFixed, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.toggleSyncMode() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isOnline) StatusRed else StatusGreen),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isOnline) "DESATIVAR API" else "ATIVAR API",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Section C: Real-Time API JSON response exporter
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EXPOR CANAL DE COMUNICAÇÃO (API)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = IndustrialPrimary)
                            )
                            Text(
                                text = "Dados consumidos por outras aplicações parceiras",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }

                        IconButton(onClick = { showApiPayload = !showApiPayload }) {
                            Icon(
                                imageVector = if (showApiPayload) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Alternar Visualização",
                                tint = IndustrialPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (showApiPayload) {
                        Text(
                            text = "GET /api/v1/associations?region=BR",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = IndustrialPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text(
                                        text = jsonPayload,
                                        color = IndustrialTertiaryFixed,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Consumidores autorizados podem capturar este payload JSON atualizado em tempo real da base local proprietária do Finder Parts.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            lineHeight = 16.sp
                        )
                    } else {
                        Button(
                            onClick = { showApiPayload = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialPrimary),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = "JSON", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("VISUALIZAR SCHEMA JSON EM TEMPO REAL", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // Section D: Log / Console Area
        if (syncLog.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "LOGS DE SINCRONIZAÇÃO EM TEMPO REAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        syncLog.forEach { logLine ->
                            Text(
                                text = "• $logLine",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, lineHeight = 16.sp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }

        // Section E: Diagnostic / Metadata System Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REGISTRO DE DIAGNÓSTICO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Sincronizando dados locais com endpoint /api/v1/associations\n" +
                                "• Canal ativo: Supabase (SIMU-MES Oficina compatível)\n" +
                                "• Filtro regional de catálogo: BR (Estrito)\n" +
                                "• Servindo dados via API proprietária para outros subsistemas",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, lineHeight = 18.sp)
                    )
                }
            }
        }

        // Logout button
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(StatusRed.copy(alpha = 0.5f), StatusRed.copy(alpha = 0.3f)))),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.Default.Logout, "Sair", Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("SAIR DA CONTA", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
