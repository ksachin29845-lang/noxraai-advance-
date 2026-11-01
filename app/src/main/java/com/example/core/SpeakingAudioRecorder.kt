package com.example.core

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

private const val TAG = "SpeakingAudioRecorder"

data class RecordedSpeechClip(
    val filePath: String,
    val bytes: ByteArray,
    val mimeType: String,
    val durationMs: Long
)

class SpeakingAudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var outputFile: File? = null
    private var recordStartEpochMs: Long = 0L
    private var isCurrentlyRecording: Boolean = false

    fun startRecording(): Result<Unit> {
        stopPlayback()
        cleanupRecorderOnly()

        return try {
            val targetFile = File(context.cacheDir, "sachinchat_speaking_clip.m4a")
            if (targetFile.exists()) {
                targetFile.delete()
            }
            outputFile = targetFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioSamplingRate(16_000)
                setAudioEncodingBitRate(64_000)
                setOutputFile(targetFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            recordStartEpochMs = System.currentTimeMillis()
            isCurrentlyRecording = true
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start microphone recording", e)
            cleanupRecorderOnly()
            Result.failure(
                IllegalStateException(
                    "Unable to access microphone recorder: ${e.localizedMessage ?: "Hardware busy or unavailable"}"
                )
            )
        }
    }

    fun pollNormalizedAmplitude(): Float {
        if (!isCurrentlyRecording) return 0.06f
        return try {
            val raw = mediaRecorder?.maxAmplitude ?: 0
            (raw / 20_000f).coerceIn(0.06f, 1.0f)
        } catch (_: Exception) {
            0.06f
        }
    }

    fun stopRecording(): Result<RecordedSpeechClip> {
        if (!isCurrentlyRecording) {
            return Result.failure(IllegalStateException("Recorder is not currently active."))
        }
        val durationMs = (System.currentTimeMillis() - recordStartEpochMs).coerceAtLeast(0L)
        isCurrentlyRecording = false

        return try {
            try {
                mediaRecorder?.stop()
            } catch (stopEx: RuntimeException) {
                cleanupRecorderOnly()
                return Result.failure(
                    IllegalArgumentException("Recording was too short. Hold or speak for at least 1 second before stopping.")
                )
            }
            cleanupRecorderOnly()

            val file = outputFile
            if (file == null || !file.exists() || file.length() < 256L) {
                return Result.failure(
                    IllegalArgumentException("No audio captured. Please speak clearly near the microphone and try again.")
                )
            }
            if (durationMs < 450L) {
                return Result.failure(
                    IllegalArgumentException("Recording too brief (${durationMs}ms). Speak the full phrase before stopping.")
                )
            }
            // Keep well below Gemini's 20 MB inline payload cap
            if (file.length() > 18L * 1024L * 1024L) {
                return Result.failure(
                    IllegalArgumentException("Audio clip exceeds the 18 MB limit. Please record a shorter phrase.")
                )
            }

            val bytes = file.readBytes()
            Result.success(
                RecordedSpeechClip(
                    filePath = file.absolutePath,
                    bytes = bytes,
                    mimeType = "audio/mp4",
                    durationMs = durationMs
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to finalize recorded audio", e)
            cleanupRecorderOnly()
            Result.failure(e)
        }
    }

    fun playRecordedClip(
        filePath: String,
        onPlaybackCompleted: () -> Unit,
        onPlaybackError: (String) -> Unit
    ) {
        stopPlayback()
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) {
            onPlaybackError("Recorded audio clip not found.")
            return
        }
        try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    stopPlayback()
                    onPlaybackCompleted()
                }
                setOnErrorListener { _, what, _ ->
                    stopPlayback()
                    onPlaybackError("Audio playback error ($what).")
                    true
                }
                prepare()
                start()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Error playing back user recording", e)
            stopPlayback()
            onPlaybackError("Could not play recording: ${e.localizedMessage ?: "Audio error"}")
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }
    }

    private fun cleanupRecorderOnly() {
        isCurrentlyRecording = false
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (_: Exception) {
        } finally {
            mediaRecorder = null
        }
    }

    fun release() {
        cleanupRecorderOnly()
        stopPlayback()
    }
}
