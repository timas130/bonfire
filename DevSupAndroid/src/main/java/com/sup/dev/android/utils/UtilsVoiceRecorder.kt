package com.sup.dev.android.utils

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.STATE_INITIALIZED
import android.media.MediaRecorder
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsThreads

class UtilsVoiceRecorder {

    private val sampleRate = 8000
    private var minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
    private var recorder: SubRecorder? = null

    init {
    }

    // check ToolsPermission.hasMicrophonePermission()
    // before calling this method!
    fun start(): Boolean {
        try {
            recorder = SubRecorder()
            return true
        } catch (e: RuntimeException) {
            err(e)
            return false
        }
    }

    fun stop() {
        recorder?.stop()
        recorder = null
    }

    fun getAsArray(maxMs: Long):ByteArray{
        synchronized(recorder!!.frames){
            val array = ToolsCollections.combine(recorder!!.frames)
            val maxSize = maxMs * sampleRate / 1000 * 2
            return if (maxMs > 0 && array.size > maxSize) array.copyOf(maxSize.toInt()) else array
        }
    }

    private inner class SubRecorder{

        var frames = ArrayList<ByteArray>()
        @SuppressLint("MissingPermission")
        var recorder = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize)
        var stop = false

        init{
            try {
                recorder.startRecording()
                if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) throw IllegalStateException("AudioRecord did not start")
            } catch (e: IllegalStateException) {
                recorder.release()
                throw e
            }

            ToolsThreads.thread {
                while (!stop) {
                    val buffer = ByteArray(minBufferSize)
                    val count = recorder.read(buffer, 0, buffer.size)
                    if (count < 0) break
                    if (count > 0) synchronized(frames){
                        frames.add(if (count == buffer.size) buffer else buffer.copyOf(count))
                    }
                }
                if (recorder.state == STATE_INITIALIZED)
                    recorder.stop()
                recorder.release()
            }
        }

        fun stop(){
            stop = true
        }

    }


}
