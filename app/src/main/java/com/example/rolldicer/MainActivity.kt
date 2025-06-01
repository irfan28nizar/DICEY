package com.example.rolldicer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rolldicer.ui.theme.RollDicerTheme
import kotlinx.coroutines.delay
import kotlin.random.Random
import android.os.Build
import androidx.compose.ui.graphics.luminance

data class ThemeSettings(
    val useCustomBackground: Boolean = false,
    val backgroundImageUri: String? = null,
    val gradientStartColor: Color = Color(0xFFBDBDBD),
    val gradientEndColor: Color = Color(0xFFF5F5F5)
) {
    companion object {
        val Saver = Saver<ThemeSettings, List<Any?>>(
            save = { theme ->
                listOf(
                    theme.useCustomBackground,
                    theme.backgroundImageUri,
                    theme.gradientStartColor.toArgb(),
                    theme.gradientEndColor.toArgb()
                )
            },
            restore = { data ->
                ThemeSettings(
                    useCustomBackground = data[0] as Boolean,
                    backgroundImageUri = data[1] as? String,
                    gradientStartColor = Color(data[2] as Int),
                    gradientEndColor = Color(data[3] as Int)
                )
            }
        )
    }
}

// Predefined gradients
val gradientPresets = listOf(
    "Grey-White" to Pair(Color(0xFFBDBDBD), Color(0xFFF5F5F5)),
    "Purple-Blue" to Pair(Color(0xFF9C27B0), Color(0xFF2196F3)),
    "Sunset" to Pair(Color(0xFFFF9800), Color(0xFFFF5722)),
    "Forest" to Pair(Color(0xFF4CAF50), Color(0xFF1B5E20)),
    "Ocean" to Pair(Color(0xFF03A9F4), Color(0xFF0288D1))
)

@Composable
fun ThemeDialog(
    onDismiss: () -> Unit,
    currentTheme: ThemeSettings,
    onThemeChange: (ThemeSettings) -> Unit
) {
    var showGradientPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onThemeChange(currentTheme.copy(
                useCustomBackground = true,
                backgroundImageUri = it.toString()
            ))
        }
    }

    if (!showGradientPicker) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Theme Settings",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Background Type", fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { showGradientPicker = true },
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text("Gradient")
                        }
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Text("Custom Image")
                        }
                    }

                    if (currentTheme.backgroundImageUri != null) {
                        Text("Current Background:", fontWeight = FontWeight.Bold)
                        AsyncImage(
                            model = currentTheme.backgroundImageUri,
                            contentDescription = "Current background",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showGradientPicker) {
        AlertDialog(
            onDismissRequest = { showGradientPicker = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showGradientPicker = false }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Select Gradient",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gradientPresets.forEach { (name, colors) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(colors.first, colors.second)
                                    )
                                )
                                .clickable {
                                    onThemeChange(currentTheme.copy(
                                        useCustomBackground = false,
                                        gradientStartColor = colors.first,
                                        gradientEndColor = colors.second
                                    ))
                                    showGradientPicker = false
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    private fun playDiceSound() {
        try {
            // Release any existing MediaPlayer
            mediaPlayer?.release()
            mediaPlayer = null
            
            // Create new MediaPlayer instance
            MediaPlayer.create(this, R.raw.dice_roll)?.also { player ->
                mediaPlayer = player
                player.setVolume(1.0f, 1.0f)
                player.setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                }
                player.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Cleanup in case of error
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            setContent {
                RollDicerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        DiceRollerScreen(
                            onRollDice = { playDiceSound() }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        // Release MediaPlayer when app is paused
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}

data class RollHistory(
    val values: List<Int>,
    val timestamp: String
)

fun Color.isLight() = luminance() > 0.5f

@Composable
fun DiceRollerScreen(
    onRollDice: () -> Unit
) {
    var diceValues by rememberSaveable { mutableStateOf(listOf(1)) }
    var isRolling by rememberSaveable { mutableStateOf(false) }
    var numberOfDice by rememberSaveable { mutableStateOf(1) }
    val rotation = remember { Animatable(0f) }
    val context = LocalContext.current
    var rollHistory by rememberSaveable { mutableStateOf(listOf<RollHistory>()) }
    var showMenu by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var themeSettings by rememberSaveable(
        stateSaver = ThemeSettings.Saver
    ) { mutableStateOf(ThemeSettings()) }

    // Calculate text color based on background
    val textColor = remember(themeSettings) {
        if (themeSettings.useCustomBackground) {
            Color(0xFF424242) // Default dark text for custom background with overlay
        } else {
            // For gradient, use the average of start and end colors
            val avgColor = Color(
                (themeSettings.gradientStartColor.red + themeSettings.gradientEndColor.red) / 2f,
                (themeSettings.gradientStartColor.green + themeSettings.gradientEndColor.green) / 2f,
                (themeSettings.gradientStartColor.blue + themeSettings.gradientEndColor.blue) / 2f
            )
            if (avgColor.isLight()) Color(0xFF424242) else Color.White
        }
    }

    if (showThemeDialog) {
        ThemeDialog(
            onDismiss = { showThemeDialog = false },
            currentTheme = themeSettings,
            onThemeChange = { themeSettings = it }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (themeSettings.useCustomBackground && themeSettings.backgroundImageUri != null) {
                    Modifier.background(Color.Black)
                } else {
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                themeSettings.gradientStartColor,
                                themeSettings.gradientEndColor
                            )
                        )
                    )
                }
            )
    ) {
        // Background image if selected
        if (themeSettings.useCustomBackground && themeSettings.backgroundImageUri != null) {
            AsyncImage(
                model = themeSettings.backgroundImageUri,
                contentDescription = "Background image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.7f,
                onError = { /* Handle error silently */ }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Roll the Dice!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = textColor
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Single Dice") },
                            onClick = {
                                numberOfDice = 1
                                diceValues = listOf(1)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Two Dice") },
                            onClick = {
                                numberOfDice = 2
                                diceValues = List(2) { 1 }
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Three Dice") },
                            onClick = {
                                numberOfDice = 3
                                diceValues = List(3) { 1 }
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Four Dice") },
                            onClick = {
                                numberOfDice = 4
                                diceValues = List(4) { 1 }
                                showMenu = false
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Theme Settings") },
                            onClick = {
                                showThemeDialog = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear History") },
                            onClick = {
                                rollHistory = emptyList()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("About") },
                            onClick = {
                                showMenu = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                diceValues.forEachIndexed { index, value ->
                    Box(
                        modifier = Modifier
                            .size(if (numberOfDice > 2) 100.dp else 150.dp)
                            .rotate(rotation.value),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = when (value) {
                                    1 -> R.drawable.dice_1
                                    2 -> R.drawable.dice_2
                                    3 -> R.drawable.dice_3
                                    4 -> R.drawable.dice_4
                                    5 -> R.drawable.dice_5
                                    else -> R.drawable.dice_6
                                }
                            ),
                            contentDescription = "Dice ${index + 1} showing $value",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isRolling = true
                    try {
                        // Trigger vibration
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
                        } else {
                            @Suppress("DEPRECATION")
                            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        }?.let { vibrator ->
                            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                        // Play sound
                        onRollDice()
                    } catch (e: Exception) {
                        // Handle errors silently
                    }
                },
                enabled = !isRolling,
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = if (isRolling) "Rolling..." else "Roll Dice",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildString {
                    append("You rolled: ")
                    diceValues.forEachIndexed { index, value ->
                        if (index > 0) append(", ")
                        append(value)
                    }
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Previous Rolls",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(rollHistory.reversed()) { roll ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x22000000)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = buildString {
                                    append("Rolled: ")
                                    roll.values.forEachIndexed { index, value ->
                                        if (index > 0) append(", ")
                                        append(value)
                                    }
                                },
                                color = textColor
                            )
                            Text(
                                text = roll.timestamp,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            try {
                rotation.animateTo(
                    targetValue = rotation.value + 360f * 3,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = FastOutSlowInEasing
                    )
                )
                delay(800) // Wait for animation to nearly finish
                val newValues = List(numberOfDice) { Random.nextInt(1, 7) }
                diceValues = newValues
                rollHistory = rollHistory + RollHistory(
                    values = newValues,
                    timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date())
                )
            } finally {
                isRolling = false
            }
        }
    }
}