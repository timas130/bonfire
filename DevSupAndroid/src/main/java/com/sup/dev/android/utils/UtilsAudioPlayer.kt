package com.sup.dev.android.utils

import android.media.AudioFormat
import android.media.AudioManager.STREAM_MUSIC
import android.media.AudioManager.STREAM_VOICE_CALL
import android.media.AudioTrack
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.java.classes.items.Item
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads

class UtilsAudioPlayer {

    var onStep: (Long) -> Unit = {}
    var useProximity = false
    private val sampleRate = 8000
    private val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
    )

    private var subPlayer: SubPlayer? = null

    fun playBuffered(onStop: () -> Unit = {}): ArrayList<ByteArray> {
        val buffer = ArrayList<ByteArray>()
        playBuffered(buffer, onStop)
        return buffer
    }

    fun playBuffered(buffer: ArrayList<ByteArray>, onStop: () -> Unit = {}) {
        subPlayer = SubPlayer({
            if (buffer.isEmpty()) ByteArray(0) { 0 }
            else buffer.removeAt(0)
        }, onStop)
    }


    fun play(stream: () -> ByteArray, onStop: () -> Unit = {}) {
        subPlayer = SubPlayer(stream, onStop)
    }

    fun play(bytes: ByteArray, onStop: () -> Unit = {}) {
        stop()
        val flag = Item(0)
        subPlayer = SubPlayer({
            if(flag.a == bytes.size) return@SubPlayer null
            val l = ToolsMath.min(200, bytes.size - flag.a)
            val arr = ToolsCollections.subarray(bytes, flag.a, l)
            flag.a += l
            return@SubPlayer arr
        }, onStop)
    }

    fun stop() {
        subPlayer?.stop()
        subPlayer = null
    }

    fun isPlaying() = subPlayer != null && !subPlayer!!.stop

    fun pause() {
        subPlayer?.audioTrack?.pause()
        ToolsAndroid.releaseAudioFocus()
    }

    fun resume() {
        subPlayer?.audioTrack?.play()
        ToolsAndroid.requestAudioFocus()
    }


    private inner class SubPlayer(
            val stream: () -> ByteArray?,
            val onStop: () -> Unit
    ) {

        var utilsProximity: UtilsProximity? = null
        var stop = false
        var audioTrack: AudioTrack? = null
        var playbackOffset = 0

        init {
            startPlay(STREAM_MUSIC)
            if (useProximity) {
                utilsProximity = UtilsProximity {
                    if (useProximity) {
                        startPlay(if (it) STREAM_VOICE_CALL else STREAM_MUSIC)
                    }
                }
            }

            ToolsThreads.timerMain(20) {
                if (stop) it.unsubscribe()
                try {
                    if (audioTrack != null) onStep.invoke((audioTrack!!.playbackHeadPosition + playbackOffset) / (sampleRate / 1000L))
                } catch (e: Exception) {
                    err(e)
                }
            }

        }

        fun startPlay(streamType: Int) {
            if (stop) {
                utilsProximity?.release()
                return
            }
            if (this.audioTrack != null) {
                playbackOffset += this.audioTrack!!.playbackHeadPosition
            }
            SupAndroid.activity!!.volumeControlStream = streamType
            val audioTrack = AudioTrack(
                    streamType,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize,
                    AudioTrack.MODE_STREAM
            )
            this.audioTrack = audioTrack
            ToolsThreads.thread {

                ToolsAndroid.requestAudioFocus()
                try {
                    audioTrack.play()
                    while (!stop && this.audioTrack == audioTrack) {
                        if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            val byteArray = stream.invoke()
                            if (byteArray == null) {
                                stop()
                                continue
                            }
                            if (byteArray.isEmpty()) {
                                ToolsThreads.sleep(1)
                                continue
                            }
                            audioTrack.write(byteArray, 0, byteArray.size)
                        } else {
                            ToolsThreads.sleep(1)
                        }
                    }
                } catch (e: Exception) {
                    if (e !is IllegalStateException) err(e)
                }
                try {
                    audioTrack.stop()
                } catch (e: Exception) {
                    err(e)
                }
                audioTrack.release()
                if (this.audioTrack == audioTrack || stop) {
                    stop = true
                    ToolsAndroid.releaseAudioFocus()
                    utilsProximity?.release()
                    ToolsThreads.main { onStop.invoke() }
                }
            }
        }

        fun stop() {
            stop = true
            utilsProximity?.release()
            try {
                if (audioTrack != null) audioTrack!!.stop()
            } catch (e: Exception) {
                err(e)
            }
        }

    }


}