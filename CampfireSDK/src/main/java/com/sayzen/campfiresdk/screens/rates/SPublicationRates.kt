package com.sayzen.campfiresdk.screens.rates

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Rate
import com.dzen.campfire.api.requests.post.RPostRatesGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.support.adapters.XKarma
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.views.screens.SLoadingRecycler

class SPublicationRates(
        val publicationId: Long,
        var myKarma: Long,
        val creatorId: Long,
        val publicationStatus: Long

) : SLoadingRecycler<CardRateText, Rate>(R.layout.screen_publication_rates) {

    val vMenuContainer: View = findViewById(R.id.vMenuContainer)
    val vAnon: CheckBox = findViewById(R.id.vAnon)
    val vAnonLabel: TextView = findViewById(R.id.vAnonLabel)
    val vKarma: ViewKarma = findViewById(R.id.vKarma)
    var anon = ControllerSettings.anonRates

    init {
        disableShadows()
        disableNavigation()

        setTitle(t(API_TRANSLATE.app_rates))
        setTextEmpty(t(API_TRANSLATE.post_rates_empty))
        vAnon.text = t(API_TRANSLATE.app_anonymously)

        vAnon.isChecked = anon
        vAnon.isEnabled = ControllerApi.can(API.LVL_ANONYMOUS)
        vAnonLabel.setText(t(API_TRANSLATE.settings_anon_rate_hint))
        vAnonLabel.visibility = if (ControllerApi.can(API.LVL_ANONYMOUS)) View.GONE else View.VISIBLE

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RPostRatesGetAll(publicationId, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.rates) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }.addOnFinish {
            var sum = 0L
            for (c in adapter.get(CardRateText::class)) sum += c.rate.karmaCount
            ControllerKarma.set(publicationId, sum)
        }

    }

    override fun onFirstShow() {
        super.onFirstShow()
        reload()
        updateKarma()
    }

    override fun classOfCard() = CardRateText::class

    override fun map(item: Rate) = CardRateText(item)

    fun updateKarma() {

        val xKarma = XKarma(publicationId, myKarma, creatorId, publicationStatus) {
            if (myKarma != it.myKarma) {
                reload()
                myKarma = it.myKarma
            }
            updateKarma()
        }

        if (xKarma.myKarma == 0L && !ControllerApi.isCurrentAccount(creatorId)) {
            xKarma.setView(vKarma)
            vAnon.setOnClickListener {
                anon = vAnon.isChecked
                xKarma.anon = vAnon.isChecked
            }
            vAnon.isChecked = anon
        } else {
            vMenuContainer.visibility = View.GONE
        }
    }

}
