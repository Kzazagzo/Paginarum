package put.paginarum.ui.components.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import put.paginarum.R

@Composable
fun LoadingFragment(modifier: Modifier = Modifier) {
    val superAnimacja = remember { Animatable(0f) }
    val size by animateDpAsState(targetValue = 300.dp * superAnimacja.value, label = "")
    val rotation by animateFloatAsState(
        targetValue = -70f + (70f) * superAnimacja.value,
        label = "",
    )
    val pidEffect by animateFloatAsState(
        targetValue = if (superAnimacja.value >= 1f) 360f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "",
    )

    LaunchedEffect(Unit) {
        superAnimacja.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3000),
        )
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.onPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_menu_book_24),
                contentDescription = null,
                modifier =
                    Modifier.size(size).rotate(rotation + pidEffect),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator()
        }
    }
}
