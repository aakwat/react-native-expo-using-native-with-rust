package com.tonekyilay.romaji.data

/** Hangul to Revised Romanization, implemented in rust/romaji. */
object Romanizer {
    init {
        System.loadLibrary("romaji")
    }

    @JvmStatic
    external fun romanize(korean: String): String
}
