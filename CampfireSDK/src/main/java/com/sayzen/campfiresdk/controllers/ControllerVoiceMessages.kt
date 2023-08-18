package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStateChanged
import com.sayzen.campfiresdk.models.events.chat.EventVoiceMessageStep
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.utils.UtilsAudioPlayer
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerVoiceMessages {

    enum class State {
        NONE, LOADING, PLAY, PAUSE
    }

    private var currentVoice = Voice(0)

    init {
        Navigator.addOnScreenChanged {
            currentVoice.stop()
            false
        }
    }

    //
    //  API
    //

    fun play(id: Long) {
        if (currentVoice.id == id) {
            if (isPause(currentVoice.id)) currentVoice.resume()
            return
        }

        currentVoice.stop()
        currentVoice = Voice(id)
        currentVoice.play()
    }

    fun stop(id:Long) {
        if (currentVoice.id != id) return
        currentVoice.stop()
    }

    fun pause(id:Long) {
        if (currentVoice.id != id) return
        currentVoice.pause()
    }

    fun resume(id:Long) {
        if (currentVoice.id != id) return
        currentVoice.resume()
    }


    //
    //  Getters
    //

    fun isPlay(id: Long): Boolean {
        if (currentVoice.id != id) return false
        return currentVoice.getState() == State.PLAY
    }

    fun isPause(id: Long): Boolean {
        if (currentVoice.id != id) return false
        return currentVoice.getState() == State.PAUSE
    }

    fun isLoading(id: Long): Boolean {
        if (currentVoice.id != id) return false
        return currentVoice.getState() == State.LOADING
    }

    fun getPlayTimeMs(id: Long): Long {
        if (currentVoice.id != id) return 0L
        return currentVoice.playTimeMs
    }

    //
    //  Support
    //

    private class Voice(
            val id: Long
    ) {

        private var state = State.NONE
        private val utilsAudioPlayer = UtilsAudioPlayer()
        var playTimeMs = 0L

        init {
            utilsAudioPlayer.useProximity = true
        }

        fun play() {
            if (id == 0L) return
            setState(State.LOADING)
            RResourcesGet(id)
                    .onComplete {
                        startPlay(it.bytes)
                    }
                    .onError {
                        stop()
                        ToolsToast.show(t(API_TRANSLATE.error_unknown))
                    }
                    .send(apiMedia)
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
                    EventBus.post(EventVoiceMessageStep(id))
                }
            }
            utilsAudioPlayer.play(bytes) {
                if (currentVoice == this) {
                    setState(State.NONE)
                    currentVoice = Voice(0)
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

        fun idDead() = currentVoice != this || id == 0L || state == State.NONE


    }

}