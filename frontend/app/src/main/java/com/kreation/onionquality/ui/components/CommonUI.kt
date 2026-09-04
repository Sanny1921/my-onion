package com.kreation.onionquality.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreation.onionquality.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = SecondaryText) } },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = White,
            unfocusedBorderColor = BorderColor,
            focusedBorderColor = PrimaryMagenta,
            focusedLabelColor = PrimaryMagenta,
            unfocusedLabelColor = SecondaryText
        ),
        singleLine = true
    )
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "PASS" -> Pair(SemanticGood.copy(alpha = 0.15f), SemanticGood)
        "REVIEW" -> Pair(SemanticUrs.copy(alpha = 0.15f), SemanticUrs)
        "REJECTED" -> Pair(SemanticRotten.copy(alpha = 0.15f), SemanticRotten)
        else -> Pair(SecondaryText.copy(alpha = 0.1f), SecondaryText)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun CustomFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val bgColor = if (selected) PrimaryMagenta else White
    val textColor = if (selected) White else SecondaryText
    val borderColor = if (selected) PrimaryMagenta else BorderColor

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = textColor,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SectionHeader(title: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = DarkPlum
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText, color = PrimaryMagenta, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
