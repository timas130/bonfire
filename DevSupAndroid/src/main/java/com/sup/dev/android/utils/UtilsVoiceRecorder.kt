package com.sup.dev.android.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.STATE_INITIALIZED
import android.media.MediaRecorder
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsThreads

class UtilsVoiceRecorder {

    private val sampleRate = 8000
    private var minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
    private var recorder: SubRecorder? = null

    init {
    }

    fun start() {
        recorder = SubRecorder()
    }

    fun stop() {
        recorder?.stop()
        recorder = null
    }

    fun getAsArray():ByteArray{
        synchronized(recorder!!.frames){
            return ToolsCollections.combine(recorder!!.frames)
        }
    }

    private inner class SubRecorder{

        var frames = ArrayList<ByteArray>()
        var recorder = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize)
        var stop = false

        init{
            recorder.startRecording()

            ToolsThreads.thread {
                while (!stop) {
                    val buffer = ByteArray(minBufferSize)
                    recorder.read(buffer, 0, buffer.size)
                    synchronized(frames){
                        frames.add(buffer)
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