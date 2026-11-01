package app.tide.loans

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.tide.loans.ui.theme.Tide
import app.tide.loans.ui.theme.TideTheme
import app.tide.loans.ui.theme.Unbounded

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TideTheme {
                Box(Modifier.fillMaxSize().background(Tide.Ground), Alignment.Center) {
                    Text("Tide", fontFamily = Unbounded, fontWeight = FontWeight.SemiBold, fontSize = 40.sp, color = Tide.Text)
                }
            }
        }
    }
}
