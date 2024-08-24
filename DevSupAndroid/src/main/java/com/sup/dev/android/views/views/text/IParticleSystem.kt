package com.sup.dev.android.views.views.text

import android.graphics.Bitmap

interface IParticleSystem {
    fun drawCached(elapsed: Long): Bitmap
}
