package com.example.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import java.util.UUID

class VoiceRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentlyPlayingPath = MutableStateFlow<String?>(null)
    val currentlyPlayingPath: StateFlow<String?> = _currentlyPlayingPath.asStateFlow()

    fun startRecording(): String? {
        try {
            stopPlayback()
            val voiceDir = File(context.filesDir, "voice_memos")
            if (!voiceDir.exists()) voiceDir.mkdirs()

            val file = File(voiceDir, "voice_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.m4a")
            currentRecordingFile = file

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(file.absolutePath)
                }
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(file.absolutePath)
                }
            }

            mediaRecorder?.prepare()
            mediaRecorder?.start()
            _isRecording.value = true
            return file.absolutePath
        } catch (e: Exception) {
            Log.e("VoiceRecorderManager", "Error starting recording", e)
            _isRecording.value = false
            return null
        }
    }

    fun stopRecording(): String? {
        if (!_isRecording.value) return null
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
            currentRecordingFile?.absolutePath
        } catch (e: Exception) {
            Log.e("VoiceRecorderManager", "Error stopping recording", e)
            _isRecording.value = false
            null
        }
    }

    fun cancelRecording() {
        try {
            if (_isRecording.value) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            }
            currentRecordingFile?.delete()
        } catch (e: Exception) {
            Log.e("VoiceRecorderManager", "Error cancelling recording", e)
        } finally {
            mediaRecorder = null
            _isRecording.value = false
            currentRecordingFile = null
        }
    }

    fun playAudio(filePath: String, onCompletion: () -> Unit = {}) {
        try {
            stopPlayback()
            val file = File(filePath)
            if (!file.exists()) return

            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentlyPlayingPath.value = null
                    onCompletion()
                }
            }
            _isPlaying.value = true
            _currentlyPlayingPath.value = filePath
        } catch (e: IOException) {
            Log.e("VoiceRecorderManager", "Playback error", e)
            _isPlaying.value = false
            _currentlyPlayingPath.value = null
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("VoiceRecorderManager", "Error stopping playback", e)
        } finally {
            _isPlaying.value = false
            _currentlyPlayingPath.value = null
        }
    }
}
