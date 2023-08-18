package com.sup.dev.android.views.views

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.sup.dev.android.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLink
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.tools.ToolsThreads


class ViewImagesSwipe constructor(
        context: Context,
        attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val vRecycler = RecyclerView(context)
    private val vNext: ViewIcon = ToolsView.inflate(R.layout.z_icon)
    private val vBack: ViewIcon = ToolsView.inflate(R.layout.z_icon)
    private val adapter = RecyclerCardAdapter()
    private var onClickGlobal: (CardSwipe) -> Boolean = { false }

    init {
        adapter.setCardW(ViewGroup.LayoutParams.WRAP_CONTENT)
        adapter.setCardH(ViewGroup.LayoutParams.MATCH_PARENT)
        vRecycler.layoutManager = LinearLayoutManager(context, GridLayoutManager.HORIZONTAL, false)
        vRecycler.adapter = adapter

        vNext.setIconBackgroundColor(ToolsResources.getColor(R.color.focus_dark))
        vBack.setIconBackgroundColor(ToolsResources.getColor(R.color.focus_dark))
        vNext.setImageResource(R.drawable.ic_keyboard_arrow_right_white_24dp)
        vBack.setImageResource(R.drawable.ic_keyboard_arrow_left_white_24dp)

        addView(vRecycler)
        addView(vNext)
        addView(vBack)
        vNext.layoutParams.width = ToolsView.dpToPx(48).toInt()
        vNext.layoutParams.height = ToolsView.dpToPx(48).toInt()
        (vNext.layoutParams as LayoutParams).gravity = Gravity.RIGHT or Gravity.CENTER
        (vNext.layoutParams as LayoutParams).rightMargin = ToolsView.dpToPx(8).toInt()
        vBack.layoutParams.width = ToolsView.dpToPx(48).toInt()
        vBack.layoutParams.height = ToolsView.dpToPx(48).toInt()
        (vBack.layoutParams as LayoutParams).gravity = Gravity.LEFT or Gravity.CENTER
        (vBack.layoutParams as LayoutParams).leftMargin = ToolsView.dpToPx(8).toInt()

        vNext.setOnClickListener {
            if (lastItem() < adapter.size() - 1) {
                if (adapter.get(lastItem()) is CardSpace) vRecycler.smoothScrollToPosition(lastItem() + 1)
                else vRecycler.smoothScrollToPosition(lastItem() + 2)
            }
        }
        vBack.setOnClickListener {
            if (firstItem() > 0) {
                if (adapter.get(firstItem()) is CardSpace) vRecycler.smoothScrollToPosition(firstItem() - 1)
                else vRecycler.smoothScrollToPosition(firstItem() - 2)
            }
        }
        vRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                updateVisibility()
            }
        })
        updateVisibility()
    }

    private fun updateVisibility() {
        vNext.visibility = if (lastItem() == -1 || lastItem() >= adapter.size() - 1) View.INVISIBLE else View.VISIBLE
        vBack.visibility = if (firstItem() < 1) View.INVISIBLE else View.VISIBLE
    }

    private fun lastItem() = (vRecycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()

    private fun firstItem() = (vRecycler.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

    fun clear() {
        adapter.clear()
        updateVisibility()
    }

    fun add(imageLoader: ImageLink, onClick: ((ImageLink) -> Unit)? = null) {
        add(imageLoader, onClick, null)
    }

    fun add(imageLoader: ImageLink, onClick: ((ImageLink) -> Unit)? = null, onLongClick: ((ImageLink) -> Unit)? = null) {
        if (!adapter.isEmpty) adapter.add(CardSpace(0, 4))
        adapter.add(CardSwipe(imageLoader, onClick, onLongClick))
    }

    fun remove(index: Int) {
        adapter.remove(adapter.get(CardSwipe::class)[index])
        ToolsThreads.main(true) { updateVisibility() }
    }

    fun scrollToEnd() {
        vRecycler.scrollToPosition(adapter.size() - 1)
    }


    //
    //  Setters
    //

    fun setOnClickGlobal(onClickGlobal: (CardSwipe) -> Boolean) {
        this.onClickGlobal = onClickGlobal
    }

    //
    //  Getters
    //

    fun size() = adapter.size(CardSwipe::class)

    fun isEmpty() = adapter.isEmpty

    fun isNotEmpty() = !adapter.isEmpty

    //
    //  Card
    //

    inner class CardSwipe(
            val imageLoader:ImageLink,
            val onClick: ((ImageLink) -> Unit)?,
            val onLongClick: ((ImageLink) -> Unit)?
    ) : Card(R.layout.view_image_swipe_card) {

        override fun bindView(view: View) {
            super.bindView(view)
            val vImage: ImageView = view.findViewById(R.id.vImage)
            view.setOnClickListener {
                if (onClickGlobal(this)) {
                    return@setOnClickListener
                } else if (onClick == null) {
                    toImageView()
                } else {
                    onClick.invoke(imageLoader)
                }
            }
            if (onLongClick != null)
                view.setOnLongClickListener {
                    onLongClick.invoke(imageLoader)
                    true
                }
            set(view, vImage)
            updateVisibility()
        }

        fun set(view: View, vImage: ImageView) {
            imageLoader.setOnSetHolder { ToolsThreads.main(10) { updateVisibility() } }.into(vImage){ ToolsThreads.main(10) { updateVisibility() } }
        }

        fun toImageView() {
            val cards = this@ViewImagesSwipe.adapter.get(CardSwipe::class)
            val array = Array(cards.size) { cards[it].imageLoader }
            var index = 0
            for (i in array.indices) if (array[i] == imageLoader) index = i
            Navigator.to(SImageView(index, array))
        }

    }

}
