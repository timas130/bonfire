package com.sayzen.campfiresdk.screens.fandoms.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.fandoms.RFandomsGetSubscribtion
import com.dzen.campfire.api.requests.fandoms.RFandomsSubscribeChange
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.BonfireTheme
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerStoryQuest
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.fandom.EventFandomCategoryChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomSubscribe
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus
import kotlinx.coroutines.flow.MutableStateFlow

class CardTitle(
        val xFandom: XFandom,
        var category: Long
) : Card(R.layout.screen_fandom_card_title) {

    private var subscriptionType: Long = 0
    private var loaded = false

    private val eventBus = EventBus
            .subscribe(EventFandomCategoryChanged::class) { onEventFandomCategoryChanged(it) }
            .subscribe(EventFandomSubscribe::class) {
                if (xFandom.getId() == it.fandomId && xFandom.getLanguageId() == it.languageId) {
                    subscriptionType = it.subscriptionType
                    updateSubscription()
                }
            }

    override fun bindView(view: View) {
        super.bindView(view)

        updateAvatar()
        updateSubscription()
    }

    fun updateSubscription() {
        val view = getView() ?: return

        val vSubscription: ComposeSubscribeButton = view.findViewById(R.id.vSubscription)

        vSubscription.setSubscriptionType(subscriptionType)
        if (loaded) {
            vSubscription.setLoaded()
        }

        vSubscription.onClick = fun(_) {
            if (!loaded) {
                ToolsToast.show(t(API_TRANSLATE.fandom_loading_in_profess))
                return
            }

            ControllerStoryQuest.incrQuest(API.QUEST_STORY_FANDOM)
            if (subscriptionType == API.PUBLICATION_IMPORTANT_NONE) {
                ApiRequestsSupporter.execute(
                    RFandomsSubscribeChange(
                        xFandom.getId(),
                        xFandom.getLanguageId(),
                        API.PUBLICATION_IMPORTANT_DEFAULT,
                        true
                    )
                ) { _ ->
                    EventBus.post(EventFandomSubscribe(
                        fandomId = xFandom.getId(),
                        languageId = xFandom.getLanguageId(),
                        subscriptionType = API.PUBLICATION_IMPORTANT_DEFAULT,
                        notifyImportant = true
                    ))
                    ControllerApi.setHasFandomSubscribes(true)
                    ToolsToast.show(t(API_TRANSLATE.app_done))
                }
            } else {
                ApiRequestsSupporter.execute(RFandomsGetSubscribtion(xFandom.getId(), xFandom.getLanguageId())) { r ->
                    SplashSubscription(
                        fandomId = xFandom.getId(),
                        languageId = xFandom.getLanguageId(),
                        type = r.subscriptionType,
                        notifyImportant = r.notifyImportant
                    ).asSheetShow()
                }
            }
        }
    }

    fun updateAvatar() {
        val view = getView() ?: return
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        xFandom.setView(vAvatar)
        vAvatar.vAvatar.setOnClickListener { Navigator.to(SImageView(ImageLoader.load(xFandom.getImage()))) }
    }

    fun setParams(subscriptionType: Long) {
        this.subscriptionType = subscriptionType
        loaded = true
        updateSubscription()
    }

    class ComposeSubscribeButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
    ) : AbstractComposeView(context, attrs) {
        private val _subscriptionType = MutableStateFlow(API.PUBLICATION_IMPORTANT_DEFAULT)
        private val _loaded = MutableStateFlow(false);
        var onClick: (View) -> Unit = { }

        fun setSubscriptionType(type: Long) {
            _subscriptionType.value = type
        }

        fun setLoaded() {
            _loaded.value = true
        }

        @Composable
        override fun Content() {
            val subscriptionType = _subscriptionType.collectAsState().value
            val loaded = _loaded.collectAsState().value

            BonfireTheme {
                FilledTonalButton(
                    onClick = { onClick(this) },
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .height(36.dp)
                ) {
                    AnimatedContent(Pair(subscriptionType, loaded), label = "SubscriptionStatus") {
                        Row {
                            if (it.first != API.PUBLICATION_IMPORTANT_NONE && it.second) {
                                Icon(
                                    if (it.first == API.PUBLICATION_IMPORTANT_IMPORTANT) {
                                        Icons.Outlined.Notifications
                                    } else {
                                        Icons.Filled.Notifications
                                    },
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            }

                            if (!it.second) {
                                Text(stringResource(R.string.fandom_subscribe_loading))
                            } else if (it.first == API.PUBLICATION_IMPORTANT_NONE) {
                                Text(stringResource(R.string.fandom_subscribe))
                            } else {
                                Text(stringResource(R.string.fandom_subscribed))
                            }
                        }
                    }
                }
            }
        }
    }

    //
    //  EventBus
    //

    private fun onEventFandomCategoryChanged(e: EventFandomCategoryChanged) {
        if (xFandom.getId() == e.fandomId) {
            category = e.newCategory
            update()
        }
    }

}
