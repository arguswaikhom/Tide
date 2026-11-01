package app.tide.loans.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tide.loans.ui.theme.Tide
import app.tide.loans.ui.theme.Unbounded

@Composable
fun SettingsScreen(defaultCurrency: String, onChange: (String) -> Unit, onBack: () -> Unit) {
    var picking by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Tide.Ground).statusBarsPadding()) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to people", tint = Tide.Text) }
            Text("Settings", color = Tide.Text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        }
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Tide.Panel)
                .clickable { picking = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Default currency", color = Tide.Text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(defaultCurrency, color = Tide.Blue, fontFamily = Unbounded, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
    if (picking) {
        CurrencyPicker(
            selected = defaultCurrency,
            allowDefault = false,
            defaultCurrency = defaultCurrency,
            onPick = { code ->
                if (code != null) onChange(code)
                picking = false
            },
            onDismiss = { picking = false },
        )
    }
}
