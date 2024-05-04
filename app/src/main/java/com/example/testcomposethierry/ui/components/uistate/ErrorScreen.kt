package com.example.testcomposethierry.ui.components.uistate

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.testcomposethierry.R
import com.example.testcomposethierry.ui.view_models.UiState

// layout from https://stackoverflow.com/questions/63719072/jetpack-compose-centering-text
@Composable
fun ErrorScreen(
    state: UiState
) {
    // the UiState.Error.error could be used to give more detail on the type of error
    val context = LocalContext.current
    Column(modifier = Modifier
        .padding(top = 60.dp, bottom = 30.dp, start = 30.dp, end = 30.dp)
        .fillMaxWidth()
        .wrapContentSize(Alignment.Center)
        .clickable(onClick = {
            // we restart the app on click because the app is in a too corrupted state
            // we could implement a retry button or a back button to avoid restarting the app
            // https://stackoverflow.com/questions/72932093/jetpack-compose-is-there-a-way-to-restart-whole-app-programmatically
            val packageManager: PackageManager = context.packageManager
            val intent: Intent? = packageManager.getLaunchIntentForPackage(context.packageName)
            var couldRestart = false
            intent?.let {
                val componentName: ComponentName? = intent.component
                componentName?.let {
                    val restartIntent: Intent = Intent.makeRestartActivityTask(componentName)
                    couldRestart = true
                    context.startActivity(restartIntent)
                }
            }
            if (!couldRestart) {
                val errorStringUnableToRestart = context.resources.getString(R.string.error_string_unable_to_restart)
                Toast.makeText(context,errorStringUnableToRestart, Toast.LENGTH_LONG).show()
            }
        } )
        .clip(shape = RoundedCornerShape(16.dp)),
    ) {
        val errorStringAnErrorHasOccurred = context.resources.getString(R.string.error_an_error_has_occurred)
        Box(modifier = Modifier
            .size(350.dp)
            .border(width = 4.dp, color = Gray, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorStringAnErrorHasOccurred,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                style = typography.h4,
            )
            //...
        }
    }
}
