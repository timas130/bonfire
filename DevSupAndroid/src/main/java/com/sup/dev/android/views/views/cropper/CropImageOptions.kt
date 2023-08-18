package com.sup.dev.android.views.views.cropper

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.TypedValue


class CropImageOptions : Parcelable {

    var cropShape: ViewCropImage.CropShape
    var snapRadius: Float = 0.toFloat()
    var touchRadius: Float = 0.toFloat()
    var guidelines: ViewCropImage.Guidelines
    var scaleType: ViewCropImage.ScaleType
    var showCropOverlay: Boolean = false
    var showProgressBar: Boolean = false
    var autoZoomEnabled: Boolean = false
    var multiTouchEnabled: Boolean = false
    var maxZoom: Int = 0
    var initialCropWindowPaddingRatio: Float = 0.toFloat()
    var fixAspectRatio: Boolean = false
    var aspectRatioX: Int = 0
    var aspectRatioY: Int = 0
    var borderLineThickness: Float = 0.toFloat()
    var borderLineColor: Int = 0
    var borderCornerThickness: Float = 0.toFloat()
    var borderCornerOffset: Float = 0.toFloat()
    var borderCornerLength: Float = 0.toFloat()
    var borderCornerColor: Int = 0
    var guidelinesThickness: Float = 0.toFloat()
    var guidelinesColor: Int = 0
    var backgroundColor: Int = 0
    var minCropWindowWidth: Int = 0
    var minCropWindowHeight: Int = 0
    var minCropResultWidth: Int = 0
    var minCropResultHeight: Int = 0
    var maxCropResultWidth: Int = 0
    var maxCropResultHeight: Int = 0
    var activityTitle: CharSequence
    var activityMenuIconColor: Int = 0
    var outputUri: Uri
    var outputCompressFormat: Bitmap.CompressFormat
    var outputCompressQuality: Int = 0
    var outputRequestWidth: Int = 0
    var outputRequestHeight: Int = 0
    var outputRequestSizeOptions: ViewCropImage.RequestSizeOptions
    var noOutputImage: Boolean = false
    var initialCropWindowRectangle: Rect? = null
    var initialRotation: Int = 0
    var allowRotation: Boolean = false
    var allowFlipping: Boolean = false
    var allowCounterRotation: Boolean = false
    var rotationDegrees: Int = 0
    var flipHorizontally: Boolean = false
    var flipVertically: Boolean = false
    var cropMenuCropButtonTitle: CharSequence? = null
    var cropMenuCropButtonIcon: Int = 0

    constructor() {

        val dm = Resources.getSystem().displayMetrics

        cropShape = ViewCropImage.CropShape.RECTANGLE
        snapRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, dm)
        touchRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, dm)
        guidelines = ViewCropImage.Guidelines.ON_TOUCH
        scaleType = ViewCropImage.ScaleType.FIT_CENTER
        showCropOverlay = true
        showProgressBar = true
        autoZoomEnabled = true
        multiTouchEnabled = false
        maxZoom = 4
        initialCropWindowPaddingRatio = 0.1f

        fixAspectRatio = false
        aspectRatioX = 1
        aspectRatioY = 1

        borderLineThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, dm)
        borderLineColor = Color.argb(170, 255, 255, 255)
        borderCornerThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, dm)
        borderCornerOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, dm)
        borderCornerLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14f, dm)
        borderCornerColor = Color.WHITE

        guidelinesThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm)
        guidelinesColor = Color.argb(170, 255, 255, 255)
        backgroundColor = Color.argb(119, 0, 0, 0)

        minCropWindowWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42f, dm).toInt()
        minCropWindowHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42f, dm).toInt()
        minCropResultWidth = 40
        minCropResultHeight = 40
        maxCropResultWidth = 99999
        maxCropResultHeight = 99999

        activityTitle = ""
        activityMenuIconColor = 0

        outputUri = Uri.EMPTY
        outputCompressFormat = Bitmap.CompressFormat.JPEG
        outputCompressQuality = 90
        outputRequestWidth = 0
        outputRequestHeight = 0
        outputRequestSizeOptions = ViewCropImage.RequestSizeOptions.NONE
        noOutputImage = false

        initialCropWindowRectangle = null
        initialRotation = -1
        allowRotation = true
        allowFlipping = true
        allowCounterRotation = false
        rotationDegrees = 90
        flipHorizontally = false
        flipVertically = false
        cropMenuCropButtonTitle = null

        cropMenuCropButtonIcon = 0
    }

    protected constructor(inp: Parcel) {
        cropShape = ViewCropImage.CropShape.values()[inp.readInt()]
        snapRadius = inp.readFloat()
        touchRadius = inp.readFloat()
        guidelines = ViewCropImage.Guidelines.values()[inp.readInt()]
        scaleType = ViewCropImage.ScaleType.values()[inp.readInt()]
        showCropOverlay = inp.readByte().toInt() != 0
        showProgressBar = inp.readByte().toInt() != 0
        autoZoomEnabled = inp.readByte().toInt() != 0
        multiTouchEnabled = inp.readByte().toInt() != 0
        maxZoom = inp.readInt()
        initialCropWindowPaddingRatio = inp.readFloat()
        fixAspectRatio = inp.readByte().toInt() != 0
        aspectRatioX = inp.readInt()
        aspectRatioY = inp.readInt()
        borderLineThickness = inp.readFloat()
        borderLineColor = inp.readInt()
        borderCornerThickness = inp.readFloat()
        borderCornerOffset = inp.readFloat()
        borderCornerLength = inp.readFloat()
        borderCornerColor = inp.readInt()
        guidelinesThickness = inp.readFloat()
        guidelinesColor = inp.readInt()
        backgroundColor = inp.readInt()
        minCropWindowWidth = inp.readInt()
        minCropWindowHeight = inp.readInt()
        minCropResultWidth = inp.readInt()
        minCropResultHeight = inp.readInt()
        maxCropResultWidth = inp.readInt()
        maxCropResultHeight = inp.readInt()
        activityTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(inp)
        activityMenuIconColor = inp.readInt()
        outputUri = inp.readParcelable(Uri::class.java.classLoader!!)?: Uri.EMPTY
        outputCompressFormat = Bitmap.CompressFormat.valueOf(inp.readString()?:"")
        outputCompressQuality = inp.readInt()
        outputRequestWidth = inp.readInt()
        outputRequestHeight = inp.readInt()
        outputRequestSizeOptions = ViewCropImage.RequestSizeOptions.values()[inp.readInt()]
        noOutputImage = inp.readByte().toInt() != 0
        initialCropWindowRectangle = inp.readParcelable(Rect::class.java.classLoader)
        initialRotation = inp.readInt()
        allowRotation = inp.readByte().toInt() != 0
        allowFlipping = inp.readByte().toInt() != 0
        allowCounterRotation = inp.readByte().toInt() != 0
        rotationDegrees = inp.readInt()
        flipHorizontally = inp.readByte().toInt() != 0
        flipVertically = inp.readByte().toInt() != 0
        cropMenuCropButtonTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(inp)
        cropMenuCropButtonIcon = inp.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(cropShape.ordinal)
        dest.writeFloat(snapRadius)
        dest.writeFloat(touchRadius)
        dest.writeInt(guidelines.ordinal)
        dest.writeInt(scaleType.ordinal)
        dest.writeByte((if (showCropOverlay) 1 else 0).toByte())
        dest.writeByte((if (showProgressBar) 1 else 0).toByte())
        dest.writeByte((if (autoZoomEnabled) 1 else 0).toByte())
        dest.writeByte((if (multiTouchEnabled) 1 else 0).toByte())
        dest.writeInt(maxZoom)
        dest.writeFloat(initialCropWindowPaddingRatio)
        dest.writeByte((if (fixAspectRatio) 1 else 0).toByte())
        dest.writeInt(aspectRatioX)
        dest.writeInt(aspectRatioY)
        dest.writeFloat(borderLineThickness)
        dest.writeInt(borderLineColor)
        dest.writeFloat(borderCornerThickness)
        dest.writeFloat(borderCornerOffset)
        dest.writeFloat(borderCornerLength)
        dest.writeInt(borderCornerColor)
        dest.writeFloat(guidelinesThickness)
        dest.writeInt(guidelinesColor)
        dest.writeInt(backgroundColor)
        dest.writeInt(minCropWindowWidth)
        dest.writeInt(minCropWindowHeight)
        dest.writeInt(minCropResultWidth)
        dest.writeInt(minCropResultHeight)
        dest.writeInt(maxCropResultWidth)
        dest.writeInt(maxCropResultHeight)
        TextUtils.writeToParcel(activityTitle, dest, flags)
        dest.writeInt(activityMenuIconColor)
        dest.writeParcelable(outputUri, flags)
        dest.writeString(outputCompressFormat.name)
        dest.writeInt(outputCompressQuality)
        dest.writeInt(outputRequestWidth)
        dest.writeInt(outputRequestHeight)
        dest.writeInt(outputRequestSizeOptions.ordinal)
        dest.writeInt(if (noOutputImage) 1 else 0)
        dest.writeParcelable(initialCropWindowRectangle, flags)
        dest.writeInt(initialRotation)
        dest.writeByte((if (allowRotation) 1 else 0).toByte())
        dest.writeByte((if (allowFlipping) 1 else 0).toByte())
        dest.writeByte((if (allowCounterRotation) 1 else 0).toByte())
        dest.writeInt(rotationDegrees)
        dest.writeByte((if (flipHorizontally) 1 else 0).toByte())
        dest.writeByte((if (flipVertically) 1 else 0).toByte())
        TextUtils.writeToParcel(cropMenuCropButtonTitle, dest, flags)
        dest.writeInt(cropMenuCropButtonIcon)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun validate() {
        if (maxZoom < 0) {
            throw IllegalArgumentException("Cannot set max zoom to a number < 1")
        }
        if (touchRadius < 0) {
            throw IllegalArgumentException("Cannot set touch radius value to a number <= 0 ")
        }
        if (initialCropWindowPaddingRatio < 0 || initialCropWindowPaddingRatio >= 0.5) {
            throw IllegalArgumentException(
                    "Cannot set initial crop window padding value to a number < 0 or >= 0.5")
        }
        if (aspectRatioX <= 0) {
            throw IllegalArgumentException(
                    "Cannot set aspect ratio value to a number less than or equal to 0.")
        }
        if (aspectRatioY <= 0) {
            throw IllegalArgumentException(
                    "Cannot set aspect ratio value to a number less than or equal to 0.")
        }
        if (borderLineThickness < 0) {
            throw IllegalArgumentException(
                    "Cannot set line thickness value to a number less than 0.")
        }
        if (borderCornerThickness < 0) {
            throw IllegalArgumentException(
                    "Cannot set corner thickness value to a number less than 0.")
        }
        if (guidelinesThickness < 0) {
            throw IllegalArgumentException(
                    "Cannot set guidelines thickness value to a number less than 0.")
        }
        if (minCropWindowHeight < 0) {
            throw IllegalArgumentException(
                    "Cannot set min crop window height value to a number < 0 ")
        }
        if (minCropResultWidth < 0) {
            throw IllegalArgumentException("Cannot set min crop result width value to a number < 0 ")
        }
        if (minCropResultHeight < 0) {
            throw IllegalArgumentException(
                    "Cannot set min crop result height value to a number < 0 ")
        }
        if (maxCropResultWidth < minCropResultWidth) {
            throw IllegalArgumentException(
                    "Cannot set max crop result width to smaller value than min crop result width")
        }
        if (maxCropResultHeight < minCropResultHeight) {
            throw IllegalArgumentException(
                    "Cannot set max crop result height to smaller value than min crop result height")
        }
        if (outputRequestWidth < 0) {
            throw IllegalArgumentException("Cannot set request width value to a number < 0 ")
        }
        if (outputRequestHeight < 0) {
            throw IllegalArgumentException("Cannot set request height value to a number < 0 ")
        }
        if (rotationDegrees < 0 || rotationDegrees > 360) {
            throw IllegalArgumentException(
                    "Cannot set rotation degrees value to a number < 0 or > 360")
        }
    }

    companion object {

        val CREATOR: Parcelable.Creator<CropImageOptions> = object : Parcelable.Creator<CropImageOptions> {
            override fun createFromParcel(inp: Parcel): CropImageOptions {
                return CropImageOptions(inp)
            }

            override fun newArray(size: Int): Array<CropImageOptions?> {
                return arrayOfNulls(size)
            }
        }
    }
}
