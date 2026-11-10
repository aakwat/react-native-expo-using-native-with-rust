package com.tonekyilay.romaji.ui

import androidx.lifecycle.ViewModel
import com.tonekyilay.romaji.data.Romanizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ConverterState(
    val korean: String = "",
    val romanized: String = "",
)

class ConverterViewModel(
    private val romanize: (String) -> String = Romanizer::romanize,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ConverterState())
    val state: StateFlow<ConverterState> = mutableState.asStateFlow()

    fun onKoreanChange(korean: String) = mutableState.update { it.copy(korean = korean) }

    fun onConvert() = mutableState.update { it.copy(romanized = romanize(it.korean)) }

    fun onShared(text: String) {
        val korean = text.take(MAX_SHARED_LENGTH)
        mutableState.value = ConverterState(korean = korean, romanized = romanize(korean))
    }

    companion object {
        // Any app can share into this one; the cap keeps a huge share from freezing the text fields.
        const val MAX_SHARED_LENGTH = 10_000
    }
}
