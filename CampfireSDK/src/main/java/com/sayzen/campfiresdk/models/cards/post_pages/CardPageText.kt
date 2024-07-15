package com.sayzen.campfiresdk.models.cards.post_pages

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.AbstractComposeView
import com.dzen.campfire.api.models.notifications.publications.NotificationMention
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageText
import com.posthog.PostHog
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.compose.BonfireTheme
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewText
import kotlinx.coroutines.flow.MutableStateFlow
import sh.sit.bonfire.formatting.BonfireMarkdown
import sh.sit.bonfire.formatting.compose.BonfireMarkdown

class CardPageText(
        pagesContainer: PagesContainer?,
        page: PageText
) : CardPage(R.layout.card_page_text, pagesContainer, page) {
    override fun bindView(view: View) {
        super.bindView(view)
        val page = page as PageText

        val vCompose: ComposeCardPageText = view.findViewById(R.id.vCompose)
        val vText: ViewText = view.findViewById(R.id.vText)
        val vTextIcon: ImageView = view.findViewById(R.id.vTextIcon)

        if (PostHog.isFeatureEnabled("compose_text_page")) {
            vText.visibility = View.GONE
            vCompose.setPage(page)
            vCompose.visibility = View.VISIBLE
            return
        } else {
            vText.visibility = View.VISIBLE
            vCompose.visibility = View.GONE
        }

        vText.text = page.text
        vText.setTextIsSelectable(clickable)
        vText.textSize = (if (page.size == PageText.SIZE_0) {
            ControllerSettings.postFontSize
        } else {
            ControllerSettings.postFontSize * 3 / 2
        }).toFloat()
        if (page.newFormatting) {
            BonfireMarkdown.setMarkdown(vText, page.text)
            ControllerLinks.linkifyShort(vText)
        } else {
            ControllerLinks.makeLinkable(vText)
        }

        if (page.icon > 0 && page.icon < CampfireConstants.TEXT_ICONS.size) {
            vTextIcon.setImageDrawable(ToolsResources.getDrawable(CampfireConstants.TEXT_ICONS[page.icon]))
            vTextIcon.visibility = View.VISIBLE
            (vText.layoutParams as LinearLayout.LayoutParams).leftMargin = ToolsView.dpToPx(4).toInt()
        } else {
            vTextIcon.setImageDrawable(null)
            vTextIcon.visibility = View.GONE
            (vText.layoutParams as LinearLayout.LayoutParams).leftMargin = ToolsView.dpToPx(0).toInt()
        }

        vText.gravity = Gravity.LEFT or Gravity.TOP
        if (page.align == PageText.ALIGN_CENTER) vText.gravity = Gravity.CENTER or Gravity.TOP
        if (page.align == PageText.ALIGN_RIGHT) vText.gravity = Gravity.RIGHT or Gravity.TOP

        if (pagesContainer != null) ControllerNotifications.removeNotificationFromNew(NotificationMention::class, pagesContainer.getSourceId())
    }

    class ComposeCardPageText @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
    ) : AbstractComposeView(context, attrs) {
        private val _page = MutableStateFlow(PageText())

        fun setPage(page: PageText) {
            _page.tryEmit(page)
        }

        @Composable
        override fun Content() {
            val page = _page.collectAsState().value

            BonfireTheme {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                    Box {
                        BonfireMarkdown(text = page.formattedText)
                    }
                }
            }
        }
    }

    override fun notifyItem() {}
}
