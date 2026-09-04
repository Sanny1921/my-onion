package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreation.onionquality.theme.DarkPlum
import com.kreation.onionquality.theme.PrimaryMagenta
import com.kreation.onionquality.theme.White
import com.kreation.onionquality.ui.components.PrimaryButton
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        showButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPlum),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Logo placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(PrimaryMagenta, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🧅", fontSize = 64.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "ONION\nQUALITY\nASSESSMENT",
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "AI-POWERED • ACCURATE • TRANSPARENT",
                color = White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (showButton) {
                PrimaryButton(
                    text = "GET STARTED",
                    onClick = onNavigateToLogin,
                    containerColor = PrimaryMagenta
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
