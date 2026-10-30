package com.projectnexus.iwani.emergency

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.projectnexus.iwani.ui.sound.UiSoundManager
import java.util.Locale

class EmergencySosManager(context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "EmergencySosManager"
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private var isInitialized = false
    private var currentIsTagalog = false
    private var activeUtteranceId: String? = null
    private var fallbackStopRunnable: Runnable? = null

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
        if (text.isBlank()) return

        fallbackStopRunnable?.let { mainHandler.removeCallbacks(it) }
        val utteranceId = "sos_${System.currentTimeMillis()}"
        activeUtteranceId = utteranceId

        // Start continuous speaking vibration so Deaf users physically feel that the phone is speaking now
        UiSoundManager.startSpeakingVibration(appContext)

        val finishSpeaking = {
            if (activeUtteranceId == utteranceId) {
                activeUtteranceId = null
                fallbackStopRunnable?.let { mainHandler.removeCallbacks(it) }
                fallbackStopRunnable = null
                UiSoundManager.stopSpeakingVibration(appContext)
                mainHandler.post { onComplete?.invoke() }
            }
        }

        // Safety fallback timer based on text length in case an OEM TTS engine omits onDone()
        val wordCount = text.trim().split("\\s+".toRegex()).size.coerceAtLeast(1)
        val estimatedMs = (wordCount * 430L + 1100L).coerceIn(1600L, 16000L)
        val safetyRunnable = Runnable { finishSpeaking() }
        fallbackStopRunnable = safetyRunnable
        mainHandler.postDelayed(safetyRunnable, estimatedMs)

        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not ready yet; running haptic speaking pulse for estimated duration")
            return
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                if (id == utteranceId) finishSpeaking()
            }
            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) finishSpeaking()
            }
            override fun onError(id: String?, errorCode: Int) {
                if (id == utteranceId) finishSpeaking()
            }
        })

        val res = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (res == TextToSpeech.ERROR) {
            finishSpeaking()
        }
    }

    fun stop() {
        activeUtteranceId = null
        fallbackStopRunnable?.let { mainHandler.removeCallbacks(it) }
        fallbackStopRunnable = null
        UiSoundManager.stopSpeakingVibration(appContext)
        tts?.stop()
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
