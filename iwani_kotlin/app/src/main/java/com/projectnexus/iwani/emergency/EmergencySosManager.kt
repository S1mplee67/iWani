package com.projectnexus.iwani.emergency

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class EmergencySosManager(context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "EmergencySosManager"
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var currentIsTagalog = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            applyLanguage(currentIsTagalog)
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(1.0f)
            isInitialized = true
            Log.i(TAG, "TTS initialized with default English language")
        } else {
            Log.e(TAG, "Failed to initialize TextToSpeech engine")
        }
    }

    fun setLanguage(isTagalog: Boolean) {
        currentIsTagalog = isTagalog
        if (isInitialized) {
            applyLanguage(isTagalog)
        }
    }

    private fun applyLanguage(isTagalog: Boolean) {
        if (isTagalog) {
            val filLocale = Locale("fil", "PH")
            val tlLocale = Locale("tl", "PH")
            val result = tts?.setLanguage(filLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val fallbackResult = tts?.setLanguage(tlLocale)
                if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Tagalog locale not found, using US English TTS")
                    tts?.language = Locale.US
                } else {
                    Log.i(TAG, "Set TTS language to tl-PH")
                }
            } else {
                Log.i(TAG, "Set TTS language to fil-PH")
            }
        } else {
            tts?.language = Locale.US
            Log.i(TAG, "Set TTS language to en-US")
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not ready yet")
            return
        }

        val utteranceId = "sos_${System.currentTimeMillis()}"
        if (onComplete != null) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    if (id == utteranceId) onComplete()
                }
                override fun onError(id: String?) {}
            })
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
