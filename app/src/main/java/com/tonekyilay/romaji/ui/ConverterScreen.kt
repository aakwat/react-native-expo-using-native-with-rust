package com.tonekyilay.romaji.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tonekyilay.romaji.R
import com.tonekyilay.romaji.data.SpeakResult
import com.tonekyilay.romaji.data.Speaker
import kotlinx.coroutines.launch

@Composable
fun ConverterRoute(viewModel: ConverterViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val speaker = rememberSpeaker()
    val missingVoice = stringResource(R.string.korean_voice_missing)
    val install = stringResource(R.string.install)
    val unavailable = stringResource(R.string.speech_unavailable)

    ConverterScreen(
        state = state,
        snackbar = snackbar,
        onKoreanChange = viewModel::onKoreanChange,
        onConvert = viewModel::onConvert,
        onSpeak = {
            when (speaker.speak(state.korean)) {
                SpeakResult.Spoken -> {}

                SpeakResult.Unavailable -> {
                    scope.launch { snackbar.showSnackbar(unavailable) }
                }

                SpeakResult.MissingVoice -> {
                    scope.launch {
                        if (snackbar.showSnackbar(missingVoice, actionLabel = install) ==
                            SnackbarResult.ActionPerformed
                        ) {
                            try {
                                context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                            } catch (_: ActivityNotFoundException) {
                                snackbar.showSnackbar(unavailable)
                            }
                        }
                    }
                }
            }
        },
        onCopy = {
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("romanized", state.romanized)))
                // Android 13+ confirms every copy itself; a toast on top would say it twice.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                }
            }
        },
    )
}

@Composable
private fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val speaker = remember(context) { Speaker(context) }
    DisposableEffect(speaker) { onDispose { speaker.close() } }
    return speaker
}

@Composable
fun ConverterScreen(
    state: ConverterState,
    snackbar: SnackbarHostState,
    onKoreanChange: (String) -> Unit,
    onConvert: () -> Unit,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.title), style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = state.korean,
                onValueChange = onKoreanChange,
                label = { Text(stringResource(R.string.korean)) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onConvert,
                enabled = state.korean.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.convert))
            }
            OutlinedTextField(
                value = state.romanized,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.romanized)) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(onClick = onSpeak, enabled = state.korean.isNotBlank()) {
                    Text(stringResource(R.string.speak))
                }
                if (state.romanized.isNotEmpty()) {
                    OutlinedButton(onClick = onCopy) {
                        Text(stringResource(R.string.copy))
                    }
                }
            }
        }
    }
}
