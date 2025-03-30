import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimerUI() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF000000)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FlipClockDisplay()
        }
        
        // Stats Button
        FloatingActionButton(
            onClick = { /* Navigate to stats */ },
            backgroundColor = Color(0xFF00FFFF),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Timeline,
                contentDescription = "Statistics",
                tint = Color.Black
            )
        }
    }
}

@Composable
fun FlipClockDisplay() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        // Hours
        FlipNumber(number = "00")
        Text(
            text = ":",
            color = Color(0xFF00FFFF),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        // Minutes
        FlipNumber(number = "00")
        Text(
            text = ":",
            color = Color(0xFF00FFFF),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        // Seconds
        FlipNumber(number = "00")
    }
}

@Composable
fun FlipNumber(number: String) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2A2A2A),
                        Color(0xFF1A1A1A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            color = Color(0xFF00FFFF),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    backgroundColor = 0xFF1A1A1A,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun TimerUIPreviewLight() {
    TimerUI()
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360,
    heightDp = 640,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TimerUIPreviewDark() {
    TimerUI()
}

@Preview(
    name = "Tablet",
    showBackground = true,
    backgroundColor = 0xFF1A1A1A,
    widthDp = 800,
    heightDp = 1200,
    device = "id:pixel_c"
)
@Composable
fun TimerUIPreviewTablet() {
    TimerUI()
} 