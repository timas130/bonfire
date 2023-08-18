package com.sup.dev.android.views.views


import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsPermission
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.utils.UtilsVoiceRecorder
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads
import kotlin.math.max
import kotlin.math.min

class ViewVoiceRecord @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    var maxRecordingTimeMs = -1L
    var onRecordingStart: () -> Unit = {}
    var onRecordingStop: (ByteArray?) -> Unit = {}
    var onRecordingProgress: (Long) -> Unit = {}

    val view: View = ToolsView.inflate(R.layout.view_voice_recorder)
    val vIconContainer: ViewGroup = view.findViewById(R.id.vIconContainer)
    val vIcon: ImageView = view.findViewById(R.id.vIcon)
    val vIconBig: ViewIcon = view.findViewById(R.id.vIconBig)
    val vIconStop: ViewIcon = view.findViewById(R.id.vIconStop)
    val vIconLock: ViewIcon = view.findViewById(R.id.vIconLock)

    val voiceRecorder = UtilsVoiceRecorder()

    var recordingStartTime = 0L
    var isRecordingMode = false
    var isLocked = false
    var currentSelectedIcon: ViewIcon = vIconBig
    var dropNextAction = false

    init {
        addView(view)
        setWillNotDraw(false)

        vIconBig.isClickable = false
        vIconStop.isClickable = false
        vIconLock.isClickable = false

        stopRecording()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (isRecordingMode) {
            updateTimer()
            invalidate()
        }
    }

    private fun startRecording() {
        if (ToolsPermission.hasMicrophonePermission()) {
            recordingStartTime = System.currentTimeMillis()
            isRecordingMode = true
            vIconContainer.visibility = View.VISIBLE
            vIcon.visibility = View.INVISIBLE
            vIconLock.visibility = View.VISIBLE
            vIconBig.setImageResource(R.drawable.ic_mic_white_24dp)
            currentSelectedIcon = vIconBig
            isLocked = false
            voiceRecorder.start()
            onRecordingStart.invoke()
            updateTimer()
            invalidate()
        } else {
            ToolsPermission.requestMicrophonePermission({

            }, {
                ToolsToast.show(SupAndroid.TEXT_ERROR_PERMISSION_MIC)
            })
        }
    }

    private fun stopRecording() {
        isRecordingMode = false
        vIconContainer.visibility = View.GONE
        vIcon.visibility = View.VISIBLE
        voiceRecorder.stop()
        onRecordingStop.invoke(null)
        invalidate()
    }

    private fun onFinishClicked() {
        val array = voiceRecorder.getAsArray()
        stopRecording()
        isLocked = false
        onRecordingStop.invoke(array)
    }

    fun lock() {
        dropNextAction = true
        isLocked = true
        vIconLock.visibility = View.INVISIBLE
        vIconBig.setImageResource(R.drawable.ic_send_white_24dp)
        updateCircles(vIconBig.x + vIconBig.width / 2, vIconBig.y + vIconBig.height / 2)
    }

    private fun onStopClicked() {
        stopRecording()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_DOWN)
            if (!isRecordingMode)
                if (ToolsMath.collisionRectAndPoint(event.x, event.y, vIcon.x, vIcon.y, vIcon.x + vIcon.width, vIcon.y + vIcon.height)) {
                    startRecording()
                    ToolsThreads.main(true) {
                        updateCircles(event.x, event.y)
                    }
                }

        if (isRecordingMode) updateCircles(event.x, event.y)

        if (event.action == MotionEvent.ACTION_UP) {
            if(dropNextAction){
                dropNextAction = false
            } else {
                if (isRecordingMode) {
                    if (currentSelectedIcon == vIconBig) {
                        if (System.currentTimeMillis() - recordingStartTime < 1000)
                            onStopClicked()
                        else
                            onFinishClicked()
                    } else if (currentSelectedIcon == vIconStop) {
                        onStopClicked()
                    } else if (currentSelectedIcon == vIconLock) {
                        lock()
                        dropNextAction = false
                    }
                }
            }
        }


        return super.onTouchEvent(event) || isRecordingMode
    }

    private fun updateCircles(x: Float, y: Float) {
        val r = vIconBig.x + vIconBig.width / 2
        val b = vIconBig.y + vIconBig.height / 2
        val l = vIconStop.x + vIconStop.width
        val t = vIconLock.y + vIconLock.height / 2
        val w = r - l
        val h = b - t

        var bigPadding = 0f
        var stopPadding = 0f
        var lockPadding = 0f

        bigPadding = max(bigPadding, vIconBig.width / 4f - (vIconBig.width / 4f * (x - l) / w))
        stopPadding = max(stopPadding, vIconStop.width / 4f * (x - l) / w)
        lockPadding = max(lockPadding, vIconLock.width / 4f - (vIconLock.width / 4f * (x - l) / w))

        lockPadding = max(lockPadding, vIconLock.height / 4f * (y - t) / h)
        bigPadding = max(bigPadding, vIconBig.height / 4f - (vIconBig.height / 4f * (y - t) / h))
        stopPadding = max(stopPadding, vIconStop.height / 4f - (vIconStop.height / 4f * (y - t) / h))

        if (bigPadding < stopPadding && bigPadding < lockPadding) {
            currentSelectedIcon = vIconBig
        } else if (lockPadding < bigPadding && lockPadding < stopPadding) {
            currentSelectedIcon = vIconLock
        } else {
            currentSelectedIcon = vIconStop
        }

        if (isLocked) {
            bigPadding = 0f
            stopPadding = vIconStop.width / 4f
        }

        vIconBig.setPaddingCircle(max(0f, min(vIconBig.height / 4f, bigPadding)))
        vIconStop.setPaddingCircle(max(0f, min(vIconStop.height / 4f, stopPadding)))
        vIconLock.setPaddingCircle(max(0f, min(vIconLock.height / 4f, lockPadding)))
    }

    private fun updateTimer() {
        val time = System.currentTimeMillis() - recordingStartTime
        onRecordingProgress.invoke(time)
        if (time >= maxRecordingTimeMs) onFinishClicked()
    }

}