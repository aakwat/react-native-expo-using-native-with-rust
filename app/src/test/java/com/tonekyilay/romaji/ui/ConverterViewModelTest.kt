package com.tonekyilay.romaji.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterViewModelTest {
    private val viewModel = ConverterViewModel(romanize = { it.uppercase() })

    @Test
    fun sharedTextIsShownAndConverted() {
        viewModel.onShared("abc")
        assertEquals(ConverterState(korean = "abc", romanized = "ABC"), viewModel.state.value)
    }

    @Test
    fun hugeShareIsCut() {
        viewModel.onShared("a".repeat(ConverterViewModel.MAX_SHARED_LENGTH + 1))
        assertEquals(ConverterViewModel.MAX_SHARED_LENGTH, viewModel.state.value.korean.length)
    }
}
