package com.sayzen.campfiresdk.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import com.sup.dev.android.app.SupAndroid
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.xml.KonfettiView
import nl.dionsegijn.konfetti.xml.listeners.OnParticleSystemUpdateListener

@Composable
fun KonfettiViewExt(
    party: Party,
    updateListener: OnParticleSystemUpdateListener? = null
) {
    val context = LocalContext.current
    val konfettiView = remember { Ref<KonfettiView>() }

    DisposableEffect(Unit) {
        val view = KonfettiView(context)
        konfettiView.value = view
        SupAndroid.activity!!.vActivityRoot!!.addView(view)

        onDispose {
            SupAndroid.activity!!.vActivityRoot!!.removeView(view)
        }
    }

    DisposableEffect(party) {
        konfettiView.value!!.start(party)

        onDispose {
            konfettiView.value?.stop(party)
        }
    }

    LaunchedEffect(updateListener) {
        konfettiView.value!!.onParticleSystemUpdateListener = updateListener
    }
}
