package com.sup.dev.android.utils

import android.media.MediaPlayer
import android.net.Uri
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.libs.debug.err
import java.io.IOException

class UtilsMediaPlayer : MediaPlayer.OnPreparedListener {

    private var streamType: Int = 0
    private var looping: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    private var state = State.NONE

    //
    //  Getters
    //

    val isPlaying: Boolean
        get() = state == State.PLAY

    val isPaused: Boolean
        get() = state == State.PAUSE

    private enum class State {
        NONE, PLAY, PAUSE
    }

    fun setMedia(mediaUri: String) {
        setMedia(Uri.parse(mediaUri))
    }

    @Suppress("DEPRECATION")
    fun setMedia(mediaUri: Uri) {
        try {

            stop()
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setAudioStreamType(streamType)
            mediaPlayer!!.isLooping = looping
            mediaPlayer!!.setDataSource(SupAndroid.appContext!!, mediaUri)
            mediaPlayer!!.setOnPreparedListener(this)
            mediaPlayer!!.setVolume(1f, 1f)

        } catch (ex: IOException) {
            err(ex)
        }

    }

    //
    //  Control
    //

    fun play(raw: Int) {
        play(Uri.parse("android.resource://" + SupAndroid.appContext?.packageName + "/" + raw))
    }

    fun play(mediaUri: String) {
        play(Uri.parse(mediaUri))
    }

    fun play(mediaUri: Uri) {
        setMedia(mediaUri)
        state = State.PLAY
        mediaPlayer!!.prepareAsync()
    }

    fun pause() {
        if (mediaPlayer == null) return
        state = State.PAUSE
        mediaPlayer!!.pause()
    }

    fun resume() {
        if (mediaPlayer == null) return
        state = State.PLAY
        mediaPlayer!!.start()
    }

    fun stop() {
        if (mediaPlayer == null) return
        state = State.NONE
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    //
    //  Callback
    //

    override fun onPrepared(mp: MediaPlayer) {
        if (mediaPlayer != null && mediaPlayer === mp) mediaPlayer!!.start()
    }

    //
    //  Setters
    //

    fun setLooping(looping: Boolean) {
        this.looping = looping
        if (mediaPlayer != null) mediaPlayer!!.isLooping = looping
    }

    @Suppress("DEPRECATION")
    fun setAudioStreamType(streamType: Int) {
        this.streamType = streamType
        if (mediaPlayer != null) mediaPlayer!!.setAudioStreamType(streamType)
    }
}
