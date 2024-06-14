package put.paginarum.ui.components.providerMainScreen

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ResolveCloudFlareFragment(onCLick: () -> Unit) {
    TextButton(
        onClick = { onCLick() },
        colors =
            ButtonDefaults.textButtonColors().copy(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Text(text = "Wykonaj Captche", color = MaterialTheme.colorScheme.onSurface)
    }
}
