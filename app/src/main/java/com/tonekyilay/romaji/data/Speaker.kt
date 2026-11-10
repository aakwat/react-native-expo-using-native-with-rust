package com.tonekyilay.romaji.data

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

sealed interface SpeakResult {
    data object Spoken : SpeakResult

    data object MissingVoice : SpeakResult

    data object Unavailable : SpeakResult
}

/** Reads Korean aloud with the phone's text-to-speech engine. */
class Speaker(
    context: Context,
) : AutoCloseable {
    // Set from the engine's init callback, which arrives on the main thread after construction.
    private var ready = false
    private val tts = TextToSpeech(context.applicationContext) { status -> ready = status == TextToSpeech.SUCCESS }

    fun speak(korean: String): SpeakResult {
        if (!ready) return SpeakResult.Unavailable
        val language = tts.setLanguage(Locale.KOREA)
        if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
            return SpeakResult.MissingVoice
        }
        val queued = tts.speak(korean, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID) == TextToSpeech.SUCCESS
        return if (queued) SpeakResult.Spoken else SpeakResult.Unavailable
    }

    override fun close() = tts.shutdown()

    private companion object {
        const val UTTERANCE_ID = "korean"
    }
}
