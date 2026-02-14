package com.example.spongebob.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spongebob.ui.theme.Shapes

enum class ThemeOption(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System Default");

    companion object {
        fun fromString(value: String?): ThemeOption {
            return when (value?.lowercase()) {
                "light" -> LIGHT
                "dark" -> DARK
                "system", null -> SYSTEM
                else -> SYSTEM
            }
        }

        fun fromPreference(isDark: Boolean?, isSystem: Boolean): ThemeOption {
            return when {
                isSystem -> SYSTEM
                isDark == true -> DARK
                else -> LIGHT
            }
        }
    }
}

@Composable
fun ThemeSwitcher(
    currentTheme: ThemeOption,
    onThemeChange: (ThemeOption) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (showLabel) {
                Text(
                    text = "Theme",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.medium)
                        .selectable(
                            selected = false,
                            onClick = {}
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            ThemeOption.entries.forEach { option ->
                ThemeOptionItem(
                    option = option,
                    selected = currentTheme == option,
                    onClick = { onThemeChange(option) }
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionItem(
    option: ThemeOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.medium)
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = option.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )

        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
