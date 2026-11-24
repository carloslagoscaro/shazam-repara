package cl.shazamrepara.app.ui

import cl.shazamrepara.app.data.DiagnosisResult
import cl.shazamrepara.app.data.EquipmentType
import cl.shazamrepara.app.data.SafetyLevel
import cl.shazamrepara.app.data.VisualSign
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.layout.Justify
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AppScreen(viewModel: ViewModel) {
    val state = remember { viewModel.state }

    MaterialTheme { // Theme inner composable for colors/typography inside this file if desired
        // No hace falta envolver aquí; el tema ya viene del padre
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                elevation = 8.dp,
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                trailingIcon = {
                    IconButton(onClick = { /* compartir / más acciones */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más acciones")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Shazam Repara",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            // Escaneo rápido
            OutlinedTextButton(
                onClick = { state.scanAction.invoke() },
                enabled = state.scanEnabled
            ) {
                Text("Diagnosticar por audio y vibración")
            }

            // Resultado si ya hay diagnóstico
            if (state.result != null) {
                Card(
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Seguridad en rojo/naranja/verde
                        val safetyColor = when (state.result?.safety) {
                            SafetyLevel.PELIGRO -> Color.Red
                            SafetyLevel.PRECAUCION -> Color.Orange
                            else -> Color.Green
                        }
                        val safetyText = when (state.result?.safety) {
                            SafetyLevel.PELIGRO -> "⚠️ PELIGRO"
                            SafetyLevel.PRECAUCION -> "⚠️ Precaución"
                            else -> "✅ Seguro"
                        }

                        Text(
                            text = safetyText,
                            style = MaterialTheme.typography.headlineSmall?.copy(
                                color = safetyColor,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Causa principal: ${state.result?.top?.fault?.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = state.result?.safetyReason ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            style = MaterialTheme.typography.body1
                        )
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Probabilidad: ${state.result?.top?.probability}%",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W500
                        )
                        Spacer(Modifier.height(8.dp))

                        // Costos
                        val min = state.result?.totalMinClp ?: 0
                        val max = state.result?.totalMaxClp ?: 0
                        Text(
                            text = "Costo estimado: $${min} - $${max}",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))

                        // Reparación simple vs técnico
                        val diff = state.result?.top?.difficulty?.label ?: "-"
                        Text(
                            text = "Reparación: $diff",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))

                        // Botones de acción
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.center,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    state.addToHistory()
                                    // podría compartir o guardar
                                }
                            ) {
                                Text("Guardar en historial")
                            }
                            OutlinedButton(
                                onClick = {
                                    // volver al escaneo
                                }
                            ) {
                                Text("Diagnosticar otra vez")
                            }
                        }
                    }
                }
            } else {
                // Pantalla de bienvenida / lista de equipos
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selecciona tu equipo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.centerWrap,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        equipmentButton(EquipmentType.AUTO, state)
                        equipmentButton(EquipmentType.LAVADORA, state)
                        equipmentButton(EquipmentType.REFRIGERADOR, state)
                    }
                    Spacer(Modifier.height(8.dp))
                    equipmentButton(EquipmentType.AIRE, state)
                    Spacer(Modifier.height(8.dp))
                    equipmentButton(EquipmentType.MICROONDAS, state)
                    Spacer(Modifier.height(8.dp))
                    equipmentButton(EquipmentType.CALEFON, state)
                    Spacer(Modifier.height(8.dp))
                    equipmentButton(EquipmentType.LAVAVAJILLAS, state)
                }
            }
        }
    }
}

@Composable
fun equipmentButton(
    type: EquipmentType,
    state: AppState
) {
    OutlinedButton(
        onClick = { state.selectEquipment(type) },
        modifier = Modifier.width(140.dp)
    ) {
        Text(type.label)
    }
}

@Composable
fun TopBar(
    onBack: () -> Unit,
    title: String,
    scanAction: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onBack() }) {
            Icon(Icons.Back, contentDescription = "Atrás")
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Spacer(Modifier.weight(1f)))
        OutlinedButton(
            onClick = { scanAction() },
            enabled = enabled
        ) {
            Text("Escaneo")
        }
    }
}

data class AppState(
    val equipment: EquipmentType? = null,
    val result: DiagnosisResult? = null,
    val isScanning: Boolean = false
) {
    fun selectEquipment(type: EquipmentType) = copy(equipment = type)
    fun scanAction() = copy(isScanning = true)
    fun scanComplete(result: DiagnosisResult) = copy(result = result, isScanning = false)
    fun addToHistory() = // placeholder; se conecta con HistoryStore en el ViewModel
        Unit
}

class ViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: State<AppState> = _state.asState()

    fun selectEquipment(type: EquipmentType) {
        _state.value = _state.value.copy(equipment = type)
    }

    fun startScan(type: EquipmentType) {
        // Aquí el UI llamaría a los sensores y al ViewModel actualizaría el estado
        _state.value = _state.value.copy(equipment = type)
    }

    fun finishScan(result: DiagnosisResult) {
        _state.value = _state.value.copy(result = result, isScanning = false)
        // Opcional: guardar en historial
    }
}