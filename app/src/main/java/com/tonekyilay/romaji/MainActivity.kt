package com.tonekyilay.romaji

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.tonekyilay.romaji.ui.ConverterRoute
import com.tonekyilay.romaji.ui.ConverterViewModel
import com.tonekyilay.romaji.ui.RomajiTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ConverterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // First creation only, so rotating does not overwrite the user's edits with the share again.
        if (savedInstanceState == null) sharedText()?.let(viewModel::onShared)
        setContent { RomajiTheme { ConverterRoute(viewModel) } }
    }

    private fun sharedText(): String? =
        intent
            .takeIf { it.action == Intent.ACTION_SEND }
            ?.getCharSequenceExtra(Intent.EXTRA_TEXT)
            ?.toString()
}
