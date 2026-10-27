package com.projectnexus.iwani.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundProfile(val labelEn: String, val labelTl: String) {
    CRYSTAL("Crystal Chime", "Kristal na Tunog"),
    CYBER("Cyber Pulse", "Cyber Pulse"),
    BUBBLE("Soft Pop", "Malambot na Pop"),
    TACTILE("Mechanical Tactile", "Tactile Click")
}

/**
 * Zero-latency procedural PCM sound synthesizer for interactive button clicks,
 * navigation transitions, toggles, and completion chimes.
 */
object UiSoundManager {
    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true
    var profile: SoundProfile = SoundProfile.CRYSTAL
    var volume: Float = 0.75f

    fun loadPrefs(context: Context) {
        val prefs = context.getSharedPreferences("iwani_ui_settings", Context.MODE_PRIVATE)
        soundEnabled = prefs.getBoolean("sound_enabled", true)
        hapticsEnabled = prefs.getBoolean("haptics_enabled", true)
        volume = prefs.getFloat("sound_volume", 0.75f)
        val profName = prefs.getString("sound_profile", SoundProfile.CRYSTAL.name) ?: SoundProfile.CRYSTAL.name
        profile = try {
            SoundProfile.valueOf(profName)
        } catch (_: Exception) {
            SoundProfile.CRYSTAL
        }
    }

    fun savePrefs(context: Context) {
        context.getSharedPreferences("iwani_ui_settings", Context.MODE_PRIVATE).edit()
            .putBoolean("sound_enabled", soundEnabled)
            .putBoolean("haptics_enabled", hapticsEnabled)
            .putFloat("sound_volume", volume)
            .putString("sound_profile", profile.name)
            .apply()
    }

    fun playClick(context: Context? = null) {
        context?.let { triggerHaptic(it, 12L) }
        if (!soundEnabled) return
        when (profile) {
            SoundProfile.CRYSTAL -> playSynthesizedTone(startHz = 740.0, endHz = 1180.0, durationMs = 45, harmonicRatio = 2.0)
            SoundProfile.CYBER -> playSynthesizedTone(startHz = 520.0, endHz = 960.0, durationMs = 50, harmonicRatio = 1.5)
            SoundProfile.BUBBLE -> playSynthesizedTone(startHz = 360.0, endHz = 780.0, durationMs = 42, harmonicRatio = 1.0)
            SoundProfile.TACTILE -> playSynthesizedTone(startHz = 920.0, endHz = 340.0, durationMs = 28, harmonicRatio = 2.5)
        }
    }

    fun playNav(context: Context? = null) {
        context?.let { triggerHaptic(it, 15L) }
        if (!soundEnabled) return
        playSynthesizedTone(startHz = 587.33, endHz = 880.0, durationMs = 55, harmonicRatio = 1.5)
    }

    fun playToggle(context: Context? = null, isOn: Boolean = true) {
        context?.let { triggerHaptic(it, 18L) }
        if (!soundEnabled) return
        if (isOn) {
            playSynthesizedTone(startHz = 659.25, endHz = 987.77, durationMs = 65, harmonicRatio = 2.0)
        } else {
            playSynthesizedTone(startHz = 880.0, endHz = 587.33, durationMs = 60, harmonicRatio = 2.0)
        }
    }

    fun playSuccess(context: Context? = null) {
        context?.let { triggerHaptic(it, 30L) }
        if (!soundEnabled) return
        thread {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            for (freq in notes) {
                playSynthesizedToneSync(startHz = freq, endHz = freq * 1.01, durationMs = 70, harmonicRatio = 2.0)
                Thread.sleep(55)
            }
        }
    }

    private fun triggerHaptic(context: Context, durationMs: Long) {
        if (!hapticsEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    private fun playSynthesizedTone(
        startHz: Double,
        endHz: Double,
        durationMs: Int,
        harmonicRatio: Double
    ) {
        thread {
            playSynthesizedToneSync(startHz, endHz, durationMs, harmonicRatio)
        }
    }

    private fun playSynthesizedToneSync(
        startHz: Double,
        endHz: Double,
        durationMs: Int,
        harmonicRatio: Double
    ) {
        try {
            val sampleRate = 24000
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)
            var phase = 0.0
            var harmonicPhase = 0.0
            val amp = (volume.coerceIn(0.1f, 1.0f) * 14000).toInt()

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = startHz + (endHz - startHz) * progress
                // Fast attack (first 8%) + smooth exponential decay
                val attack = if (progress < 0.08) (progress / 0.08) else 1.0
                val decay = exp(-4.8 * progress)
                val env = attack * decay

                phase += (2.0 * PI * freq) / sampleRate
                harmonicPhase += (2.0 * PI * freq * harmonicRatio) / sampleRate

                val wave = 0.75 * sin(phase) + 0.25 * sin(harmonicPhase)
                samples[i] = (wave * env * amp).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val bufferSizeBytes = numSamples * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSizeBytes.coerceAtLeast(AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, numSamples)
            track.play()
            Thread.sleep(durationMs.toLong() + 25L)
            track.stop()
            track.release()
        } catch (_: Exception) {}
    }
}
