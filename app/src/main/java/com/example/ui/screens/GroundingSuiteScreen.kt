package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.BreathingPhase
import com.example.viewmodel.NeuroGroundViewModel

@Composable
fun GroundingSuiteScreen(
    viewModel: NeuroGroundViewModel,
    onLogSession: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("5-4-3-2-1 Sensory", "4-4-4-4 Breathing", "Somatic Anchors")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("grounding_suite_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("grounding_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> SensoryGroundingTab(viewModel, onLogSession)
            1 -> BoxBreathingTab(viewModel, onLogSession)
            2 -> SomaticAnchorsTab(onLogSession)
        }
    }
}

@Composable
fun SensoryGroundingTab(
    viewModel: NeuroGroundViewModel,
    onLogSession: (String) -> Unit
) {
    val step by viewModel.sensoryStep.collectAsState()
    val logs by viewModel.sensoryLogs.collectAsState()
    val scrollState = rememberScrollState()
    var currentInput by remember { mutableStateOf("") }

    val stepMeta = listOf(
        Triple("5 Things You See", "Visual Cortex Activation", Icons.Default.Visibility),
        Triple("4 Things You Feel", "Somatosensory Cortex Activation", Icons.Default.PanTool),
        Triple("3 Things You Hear", "Auditory Cortex Focus", Icons.Default.Hearing),
        Triple("2 Things You Smell", "Olfactory Bulb Integration", Icons.Default.Air),
        Triple("1 Thing You Taste", "Gustatory Perception", Icons.Default.Restaurant)
    )

    val currentMeta = stepMeta[step]
    val stepPrompt = when (step) {
        0 -> "Notice 5 distinct visual details in your immediate field of vision (a shadow, a color gradient, a surface texture, an edge)."
        1 -> "Notice 4 physical textures you are touching right now (the fabric of your pants, the solid desk, the cool phone glass, air temperature)."
        2 -> "Listen closely for 3 distinct ambient sounds (the hum of air conditioning, birds outside, your own steady breath)."
        3 -> "Identify 2 distinct scents around you (fresh air, hand lotion, coffee, book paper, or clothes)."
        4 -> "Notice 1 physical taste inside your mouth (lingering drink, mint, water, or the neutral warmth of your tongue)."
        else -> ""
    }

    val currentLogs = logs[step] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step progress header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step ${step + 1} of 5",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = currentMeta.second,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0..4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (i <= step) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sensory Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentMeta.third,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = currentMeta.first,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stepPrompt,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input to record item
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { currentInput = it },
                        placeholder = { Text("Log an observation...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (currentInput.isNotBlank()) {
                                viewModel.addSensoryItem(step, currentInput.trim())
                                currentInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = currentInput.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }

                if (currentLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentLogs.forEach { logItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = logItem,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { viewModel.previousSensoryStep() },
                enabled = step > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Previous")
            }

            if (step < 4) {
                Button(
                    onClick = { viewModel.nextSensoryStep() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Next Step")
                }
            } else {
                Button(
                    onClick = {
                        onLogSession("5-4-3-2-1 Sensory Grounding")
                        viewModel.resetSensoryTool()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Complete & Log")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Neurobiology Mechanism Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "The Neuroscience of 5-4-3-2-1",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sensory identification shifts metabolic glucose demand from the hyperactive amygdala (threat/fear hub) back into the prefrontal cortex and primary sensory cortices, restoring reality appraisal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BoxBreathingTab(
    viewModel: NeuroGroundViewModel,
    onLogSession: (String) -> Unit
) {
    val isActive by viewModel.breathingActive.collectAsState()
    val phase by viewModel.breathingPhase.collectAsState()
    val progress by viewModel.phaseProgress.collectAsState()
    val secondsLeft by viewModel.secondsRemainingInPhase.collectAsState()
    val cycleCount by viewModel.cycleCount.collectAsState()
    val context = LocalContext.current

    // Trigger gentle haptic vibration on phase transition if enabled
    LaunchedEffect(phase) {
        if (isActive) {
            try {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(40)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    val animatedRadiusFactor by animateFloatAsState(
        targetValue = when (phase) {
            BreathingPhase.INHALE -> 0.45f + (0.45f * progress)
            BreathingPhase.HOLD_IN -> 0.90f
            BreathingPhase.EXHALE -> 0.90f - (0.45f * progress)
            BreathingPhase.HOLD_OUT -> 0.45f
        },
        label = "radiusAnim"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "4-4-4-4 Box Breathing Visualizer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Vagus Nerve Autonomic Stabilization",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Visual Breathing Pacing Canvas
        Box(
            modifier = Modifier
                .size(240.dp)
                .testTag("breathing_canvas_box"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.minDimension / 2 * 0.95f
                val currentRadius = maxRadius * animatedRadiusFactor

                // Outer guide circle
                drawCircle(
                    color = primaryColor.copy(alpha = 0.15f),
                    radius = maxRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Expanding breathing pulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            secondaryColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = currentRadius
                    ),
                    radius = currentRadius,
                    center = center
                )

                // Dynamic progress perimeter arc
                drawCircle(
                    color = primaryColor,
                    radius = currentRadius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            // Phase and Seconds Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isActive) phase.label.uppercase() else "READY",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isActive) "${secondsLeft}s" else "4s",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isActive) phase.instruction else "Tap Start to Begin",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats row
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Completed Cycles: $cycleCount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Control Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.toggleBreathing() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .height(52.dp)
                    .width(160.dp)
                    .testTag("toggle_breathing_btn")
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isActive) "Pause" else "Start Cycle",
                    fontWeight = FontWeight.Bold
                )
            }

            if (cycleCount > 0) {
                OutlinedButton(
                    onClick = {
                        onLogSession("4-4-4-4 Box Breathing ($cycleCount cycles)")
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log In Journal")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Vagal Rationale
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Vagus Nerve Autonomic Mechanism",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rhythmic 4-second box cycles stabilize arterial CO2 and activate the ventral vagal complex, releasing acetylcholine onto cardiac pacemaker cells to slow heart rate and dampen the fight-or-flight cascade.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SomaticAnchorsTab(
    onLogSession: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Physical Somatosensory Anchors",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Butterfly Hug Anchor
        AnchorCard(
            title = "Bilateral Butterfly Hug",
            mechanism = "Bilateral tactile stimulation balances interhemispheric brain activity and soothes amygdala arousal.",
            steps = listOf(
                "Cross your arms across your chest so your hands rest on your clavicles or upper arms.",
                "Interlock your thumbs to form the body of a butterfly.",
                "Alternate tapping your left hand, then your right hand, in a slow, rhythmic cadence (1 tap per second).",
                "Take 6 to 8 deep, natural breaths while continuing the alternating bilateral taps."
            ),
            icon = Icons.Default.Spa,
            onLog = { onLogSession("Bilateral Butterfly Hug") }
        )

        // Mammalian Dive Reflex
        AnchorCard(
            title = "Mammalian Dive Reflex (Cold Water Reset)",
            mechanism = "Cold sensory stimulus on the ophthalmic branch of the trigeminal nerve triggers immediate parasympathetic vagal bradycardia.",
            steps = listOf(
                "Dip a paper towel or washcloth in cold water or hold an ice cube wrapped in cloth.",
                "Gently press the cool surface over your cheekbones, bridge of the nose, and temples for 15-30 seconds.",
                "Notice your heart rate reflexively decelerating as your autonomic system detects water-submersion signals."
            ),
            icon = Icons.Default.WaterDrop,
            onLog = { onLogSession("Mammalian Dive Reflex (Cold Reset)") }
        )

        // Graviceptive Feet-on-Floor Anchor
        AnchorCard(
            title = "Graviceptive Grounding (Feet to Earth)",
            mechanism = "Stimulates plantar mechanoreceptors, sending upward kinesthetic feedback to the parietal cortex to dissolve feelings of unreality or dissociation.",
            steps = listOf(
                "Remove your shoes if possible, or place both soles flat against the floor.",
                "Push downward through your heels, the balls of your feet, and your toes.",
                "Notice the firm, unmoving resistance of the floor pushing back up to hold your full weight.",
                "Say mentally: 'My body is physically safe right here on the ground.'"
            ),
            icon = Icons.Default.SelfImprovement,
            onLog = { onLogSession("Graviceptive Grounding") }
        )
    }
}

@Composable
fun AnchorCard(
    title: String,
    mechanism: String,
    steps: List<String>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onLog: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = mechanism,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEachIndexed { idx, stepText ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${idx + 1}.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(22.dp)
                        )
                        Text(
                            text = stepText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onLog,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log This Exercise")
            }
        }
    }
}
