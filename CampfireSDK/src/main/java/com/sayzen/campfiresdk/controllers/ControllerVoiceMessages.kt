package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStateChanged
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStep
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.utils.UtilsAudioPlayer
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerVoiceMessages {

    enum class State {
        NONE, LOADING, PLAY, PAUSE
    }

    private var currentVoice = Voice(ImageRef())

    init {
        Navigator.addOnScreenChanged {
            currentVoice.stop()
            false
        }
    }

    //
    //  API
    //

    fun play(ref: ImageRef) {
        if (currentVoice.ref == ref) {
            if (isPause(currentVoice.ref)) currentVoice.resume()
            return
        }

        currentVoice.stop()
        currentVoice = Voice(ref)
        currentVoice.play()
    }

    fun stop(ref: ImageRef) {
        if (currentVoice.ref != ref) return
        currentVoice.stop()
    }

    fun pause(ref: ImageRef) {
        if (currentVoice.ref != ref) return
        currentVoice.pause()
    }

    fun resume(ref: ImageRef) {
        if (currentVoice.ref != ref) return
        currentVoice.resume()
    }


    //
    //  Getters
    //

    fun isPlay(ref: ImageRef): Boolean {
        if (currentVoice.ref != ref) return false
        return currentVoice.getState() == State.PLAY
    }

    fun isPause(ref: ImageRef): Boolean {
        if (currentVoice.ref != ref) return false
        return currentVoice.getState() == State.PAUSE
    }

    fun isLoading(ref: ImageRef): Boolean {
        if (currentVoice.ref != ref) return false
        return currentVoice.getState() == State.LOADING
    }

    fun getPlayTimeMs(ref: ImageRef): Long {
        if (currentVoice.ref != ref) return 0L
        return currentVoice.playTimeMs
    }

    //
    //  Support
    //

    private class Voice(
        val ref: ImageRef,
    ) {

        private var state = State.NONE
        private val utilsAudioPlayer = UtilsAudioPlayer()
        var playTimeMs = 0L

        init {
            utilsAudioPlayer.useProximity = true
        }

        fun play() {
            if (ref.isEmpty()) return
            setState(State.LOADING)
            ImageLoader.load(ref).intoBytes {
                if (it != null) {
                    startPlay(it)
                } else {
                    stop()
                    ToolsToast.show(t(API_TRANSLATE.error_unknown))
                }
            }
        }

        private fun setState(state: State) {
            this.state = state
            EventBus.post(EventVoiceMessageStateChanged())
        }

        fun getState() = state

        private fun startPlay(bytes: ByteArray) {
            if (idDead()) return
            setState(State.PLAY)
            playTimeMs = 0L
            utilsAudioPlayer.onStep = {
                if (currentVoice == this) {
                    playTimeMs = it
                    EventBus.post(EventVoiceMessageStep(ref))
                }
            }
            utilsAudioPlayer.play(bytes) {
                if (currentVoice == this) {
                    setState(State.NONE)
                    currentVoice = Voice(ImageRef())
                }
            }
        }

        fun stop() {
            if (idDead()) return
            setState(State.NONE)
            utilsAudioPlayer.stop()
        }

        fun pause() {
            if (idDead()) return
            setState(State.PAUSE)
            utilsAudioPlayer.pause()
        }

        fun resume() {
            if (idDead()) return
            setState(State.PLAY)
            utilsAudioPlayer.resume()
        }

        fun idDead() = currentVoice != this || ref.isEmpty() || state == State.NONE
    }
}
