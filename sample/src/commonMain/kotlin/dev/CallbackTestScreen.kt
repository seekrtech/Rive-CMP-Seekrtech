package dev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.muazkadan.rivecmp.CustomRiveAnimation
import dev.muazkadan.rivecmp.RiveCompositionSpec
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.rememberRiveComposition
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import rivecmp.sample.generated.resources.Res

data class CallbackEvent(
    val type: String,
    val details: String
)

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
fun CallbackTestScreen(onBack: (() -> Unit)? = null) {
    val events = remember { mutableStateListOf<CallbackEvent>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val alligatorComposition by rememberRiveComposition(
        spec = { RiveCompositionSpec.byteArray(Res.readBytes("files/relax_onboarding_autolayout_test.riv")) }
    )
    
    // Auto-scroll to bottom when new event is added
    LaunchedEffect(events.size) {
        if (events.isNotEmpty()) {
            listState.animateScrollToItem(events.size - 1)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001C1C))
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rive Callback Test",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            if (onBack != null) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF003C3D)
                    )
                ) {
                    Text("← Back")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Animation with callbacks
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            CustomRiveAnimation(
                modifier = Modifier.fillMaxSize(),
                composition = alligatorComposition,
                stateMachineName = "State Machine 1",
                fit = RiveFit.COVER,
                alignment = RiveAlignment.CENTER,
                onStateChanged = { stateMachineName, stateName ->
                    events.add(
                        CallbackEvent(
                            type = "State Changed",
                            details = "[$stateMachineName] → $stateName"
                        )
                    )
                    println("🎯 State Changed: $stateMachineName -> $stateName")
                },
                onRiveEvent = { eventName, properties ->
                    events.add(
                        CallbackEvent(
                            type = "Rive Event",
                            details = "$eventName: $properties"
                        )
                    )
                    println("🎉 Rive Event: $eventName with $properties")
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF003C3D)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Instructions:",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Interact with the animation above\n" +
                          "• State changes will be logged below\n" +
                          "• Swipe or touch the alligator to see events",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Event log header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Event Log (${events.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            if (events.isNotEmpty()) {
                Button(
                    onClick = { events.clear() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.7f)
                    )
                ) {
                    Text("Clear")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Event list
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events yet.\nInteract with the animation above!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(events) { event ->
                        EventCard(event)
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: CallbackEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (event.type) {
                "State Changed" -> Color(0xFF2E7D32)
                "Rive Event" -> Color(0xFF1565C0)
                else -> Color.DarkGray
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.type,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.details,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val seconds = (timestamp / 1000) % 60
    val minutes = (timestamp / (1000 * 60)) % 60
    val hours = (timestamp / (1000 * 60 * 60)) % 24
    return "${hours.toString().padStart(2, '0')}:" +
           "${minutes.toString().padStart(2, '0')}:" +
           "${seconds.toString().padStart(2, '0')}"
}

