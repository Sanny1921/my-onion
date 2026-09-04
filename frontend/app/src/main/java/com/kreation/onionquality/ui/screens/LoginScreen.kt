package com.kreation.onionquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kreation.onionquality.theme.*
import com.kreation.onionquality.ui.components.PrimaryButton
import com.kreation.onionquality.ui.components.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineMedium,
                color = DarkPlum
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = SecondaryText
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = White,
                    unfocusedBorderColor = BorderColor,
                    focusedBorderColor = PrimaryMagenta
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = White,
                    unfocusedBorderColor = BorderColor,
                    focusedBorderColor = PrimaryMagenta
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryMagenta)
                    )
                    Text("Remember me", style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = { /* TODO */ }) {
                    Text("Forgot Password?", color = PrimaryMagenta)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "LOGIN",
                onClick = onLoginSuccess
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SecondaryButton(
                text = "Continue with biometric",
                onClick = onLoginSuccess
            )
        }
    }
}
