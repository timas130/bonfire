package com.sup.dev.android.views.support.adapters.recycler_view

import android.util.SparseBooleanArray
import androidx.annotation.StringRes
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardLoading
import com.sup.dev.android.views.support.adapters.CardAdapter
import com.sup.dev.java.classes.callbacks.CallbacksList
import com.sup.dev.java.tools.ToolsClass
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsThreads
import kotlin.collections.ArrayList
import kotlin.reflect.KClass


@Suppress("DIFFERENT_NAMES_FOR_THE_SAME_PARAMETER_IN_SUPERTYPES")
open class RecyclerCardAdapterLoading<K : Card, V>(
        private val cardClass: KClass<K>,
        private var mapper: ((V) -> K)?
) : RecyclerCardAdapter(), CardAdapter, RecyclerCardAdapterLoadingInterface {

    private var bottomLoader: (((Array<V>?) -> Unit, ArrayList<K>) -> Unit)? = null
    private var topLoader: (((Array<V>?) -> Unit, ArrayList<K>) -> Unit)? = null
    private val cardLoading: CardLoading = CardLoading()
    private val cardsHash = SparseBooleanArray()

    private var removeSame = true
    private var addToSameCards = false
    private var addBottomPositionOffset = 0
    private var addTopPositionOffset = 0
    private var startBottomLoadOffset = 0
    private var startTopLoadOffset = 0
    private var isLockTop = false
    private var isLockBottom = false
    private var isInProgress = false
    private var isLoadedAtListOneTime = false
    private var loadingTag = 0L
    private var sameRemovedCount = 0

    private var retryEnabled = false
    private var actionEnabled = false
    private var showLoadingCard = true
    private var showLoadingCardIfEmpty = true
    private var showErrorCardIfEmpty = true
    private var showLoadingTop = true
    private var showLoadingBottom = true



    private var onStart_Empty = CallbacksList()
    private var onStart_NotEmpty = CallbacksList()
    private var onLoadedPack = CallbacksList()
    private var onLoadedPack_NotEmpty = CallbacksList()
    private var onLoadedPack_Empty = CallbacksList()
    private var onFinish_Empty = CallbacksList()
    private var onFinish = CallbacksList()
    private var onError_Empty = CallbacksList()

    override fun onBindViewHolder(holder: Holder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)

        if (!isLockBottom && position >= itemCount - 1 - startBottomLoadOffset) {
            ToolsThreads.main(true) {
                loadNow(true)
                Unit
            }
        } else if (!isLockTop && topLoader != null && position <= startTopLoadOffset) {
            ToolsThreads.main(true) {
                loadNow(false)
                Unit
            }
        }
    }

    private fun loadNow(bottom: Boolean) {
        if (isInProgress) return
        isInProgress = true
        if (bottom) isLockBottom = false
        else isLockTop = false

        loadingTag = System.currentTimeMillis()
        val loadingTagLocal = loadingTag

        remove(cardLoading)
        cardLoading.setOnRetry { load(bottom) }
        cardLoading.setState(CardLoading.State.LOADING)

        val cards = get(cardClass)
        ToolsCollections.removeIf(cards) { !cardsHash.get(it.hashCode(), false) }

        if (!contains(cardClass)) {
            onStart_Empty.invoke()
            if (!contains(cardLoading) && showLoadingCard && showLoadingCardIfEmpty) {
                if (bottom) {
                    if (showLoadingBottom) add(findBottomAdposition(), cardLoading)
                } else {
                    if (showLoadingTop) add(findTopAddPosition(), cardLoading)
                }
            }
        } else {
            onStart_NotEmpty.invoke()
            if (!contains(cardLoading) && showLoadingCard)
                if (bottom) {
                    if (showLoadingBottom) add(findBottomAdposition(), cardLoading)
                } else {
                    if (showLoadingTop) add(findTopAddPosition(), cardLoading)
                }
        }

        if (bottom) bottomLoader!!.invoke({ result -> onLoaded(result, bottom, loadingTagLocal) }, cards)
        else topLoader!!.invoke({ result -> onLoaded(result, bottom, loadingTagLocal) }, cards)
    }

    private fun onLoaded(result: Array<V>?, bottom: Boolean, loadingTagLocal: Long) {
        isInProgress = false
        isLoadedAtListOneTime = true
        if (loadingTagLocal != loadingTag) return

        if (result == null) {

            lockBottom()
            lockTop()

            if (retryEnabled && (contains(cardClass) || showErrorCardIfEmpty)) {
                if (!contains(cardLoading)) {
                    if (bottom) {
                        add(findBottomAdposition(), cardLoading)
                    } else {
                        add(findTopAddPosition(), cardLoading)
                    }
                }
                cardLoading.setState(CardLoading.State.RETRY)
            } else remove(cardLoading)

            if (!contains(cardClass)) {
                onError_Empty.invoke()
            }
        } else {
            if (!contains(cardClass) && result.isEmpty()) {
                if (bottom) lockBottom()
                else lockTop()

                if (actionEnabled) {
                    if (!contains(cardLoading))
                        if (bottom) add(findBottomAdposition(), cardLoading) else add(findTopAddPosition(), cardLoading)
                    cardLoading.setState(CardLoading.State.ACTION)
                } else {
                    remove(cardLoading)
                }
                onFinish_Empty.invoke()
            } else {
                if (result.isEmpty()) {
                    if (bottom) lockBottom()
                    else lockTop()
                }
                remove(cardLoading)
            }

            for (i in result.indices) {
                val card = mapper!!.invoke(result[i]!!)
                if (removeSame) {
                    val cards = get(cardClass)
                    var b = false
                    for (c in cards) if (card == c) {
                        b = true
                        break
                    }
                    if (b) {
                        sameRemovedCount++
                        continue
                    }
                }
                cardsHash.put(card.hashCode(), true)
                if (bottom) add(findBottomAdposition(), card)
                else add(findTopAddPosition() + i, card)
            }

            onLoadedPack.invoke()
            if (contains(cardClass) || result.isNotEmpty()) onLoadedPack_NotEmpty.invoke()
            else onLoadedPack_Empty.invoke()
            if (result.isEmpty()) onFinish.invoke()
        }
    }

    fun addWithHashBottom(card: K) {
        cardsHash.put(card.hashCode(), true)
        add(findBottomAdposition(), card)
    }

    fun addWithHashTop(card: K) {
        cardsHash.put(card.hashCode(), true)
        add(findTopAddPosition(), card)
    }

    fun findTopAddPosition(): Int {
        if (addToSameCards) {
            val cards = get(cardClass)
            if (cards.isEmpty()) {
                return addTopPositionOffset
            } else {
                return indexOf(cards[0]) - addTopPositionOffset
            }
        } else {
            return addTopPositionOffset
        }
    }

    fun findBottomAdposition(): Int {
        if (addToSameCards) {
            val cards = get(cardClass)
            if (cards.isEmpty()) {
                return size() - addBottomPositionOffset
            } else {
                return indexOf(cards[cards.size - 1]) + 1 - addBottomPositionOffset
            }
        } else {
            return size() - addBottomPositionOffset
        }
    }

    fun loadTop() {
        load(false)
    }

    override fun loadBottom() {
        load(true)
    }

    fun load(bottom: Boolean) {
        loadNow(bottom)
    }

    fun reloadTop() {
        reload(false)
    }

    fun reloadBottom() {
        reload(true)
    }

    fun reload(bottom: Boolean) {
        sameRemovedCount = 0
        loadingTag = 0
        isLoadedAtListOneTime = false
        isInProgress = false

        var i = 0
        while (i < itemCount) {
            val c = get(i)
            if (ToolsClass.instanceOf(c, cardClass) && cardsHash.get(c.hashCode(), false))
                remove(i--)
            i++
        }
        cardsHash.clear()

        remove(cardLoading)
        load(bottom)
    }


    fun lockBottom() {
        isLockBottom = true
    }

    fun lockTop() {
        isLockTop = true
    }

    fun unlockBottom() {
        isLockBottom = false
    }

    fun unlockTop() {
        isLockTop = false
    }

    override fun remove(position: Int) {
        super.remove(position)
        if (!contains(cardClass) && isLoadedAtListOneTime) onFinish_Empty.invoke()
    }

    //
    //  Setters
    //

    fun setAddBottomPositionOffset(i: Int): RecyclerCardAdapterLoading<K, V> {
        this.addBottomPositionOffset = i
        return this
    }

    fun setAddTopPositionOffset(i: Int): RecyclerCardAdapterLoading<K, V> {
        this.addTopPositionOffset = i
        return this
    }

    fun addOnLoadedPack_NotEmpty(onLoadedPack_NotEmpty: () -> Unit): RecyclerCardAdapterLoading<K, V> {
        this.onLoadedPack_NotEmpty.add(onLoadedPack_NotEmpty)
        return this
    }

    fun addOnLoadedPack_Empty(onLoadedPack_Empty: () -> Unit): RecyclerCardAdapterLoading<K, V> {
        this.onLoadedPack_Empty.add(onLoadedPack_Empty)
        return this
    }

    fun addOnLoadedPack(onLoadedPack: () -> Unit): RecyclerCardAdapterLoading<K, V> {
        this.onLoadedPack.add(onLoadedPack)
        return this
    }

    fun addOnFinish(onFinish: () -> Unit): RecyclerCardAdapterLoading<K, V> {
        this.onFinish.add(onFinish)
        return this
    }

    fun addOnStart_NotEmpty(onStart_NotEmpty: () -> Unit): RecyclerCardAdapterLoading<K, V> {
        this.onStart_NotEmpty.add(onStart_NotEmpty)
        return this
    }

    fun addOnStart_Empty(onStart_Empty: () -> Unit): RecyclerCardAdapterLoading<K, V> {
        this.onStart_Empty.add(onStart_Empty)
        return this
    }

    fun setActionEnabled(actionEnabled: Boolean): RecyclerCardAdapterLoading<K, V> {
        this.actionEnabled = actionEnabled
        return this
    }

    fun addOnFinish_Empty(onFinish_Empty: () -> Unit): RecyclerCardAdapterLoading<K, V> {
        this.onFinish_Empty.add(onFinish_Empty)
        return this
    }

    fun addOnError_Empty(onError_Empty: () -> Unit): RecyclerCardAdapterLoading<K, V> {
        this.onError_Empty.add(onError_Empty)
        return this
    }

    fun setRetryMessage(@StringRes message: Int, @StringRes button: Int): RecyclerCardAdapterLoading<K, V> {
        return setRetryMessage(ToolsResources.s(message), ToolsResources.s(button))
    }

    fun setRetryMessage(message: String?, button: String?): RecyclerCardAdapterLoading<K, V> {
        retryEnabled = true
        cardLoading.setRetryMessage(message)
        cardLoading.setRetryButton(button, {})
        return this
    }

    fun setRemoveSame(b: Boolean) {
        removeSame = b
    }

    fun setAddToSameCards(b: Boolean) {
        addToSameCards = b
    }

    fun setEmptyMessage(@StringRes message: Int): RecyclerCardAdapterLoading<K, V> {
        return setEmptyMessage(ToolsResources.s(message))
    }

    fun setEmptyMessage(@StringRes message: Int, @StringRes button: Int, onAction: () -> Unit): RecyclerCardAdapterLoading<K, V> {
        return setEmptyMessage(ToolsResources.s(message), ToolsResources.s(button), onAction)
    }

    fun setShowLoadingCard(b: Boolean): RecyclerCardAdapterLoading<K, V> {
        showLoadingCard = b
        return this
    }

    fun setShowLoadingCardBottom(b: Boolean): RecyclerCardAdapterLoading<K, V> {
        showLoadingBottom = b
        return this
    }

    fun setShowLoadingCardTop(b: Boolean): RecyclerCardAdapterLoading<K, V> {
        showLoadingTop = b
        return this
    }

    fun setShowLoadingCardIfEmpty(b: Boolean): RecyclerCardAdapterLoading<K, V> {
        showLoadingCardIfEmpty = b
        return this
    }

    fun setShowErrorCardIfEmpty(b: Boolean): RecyclerCardAdapterLoading<K, V> {
        showErrorCardIfEmpty = b
        return this
    }


    @JvmOverloads
    fun setEmptyMessage(message: String?, button: String? = null, onAction: () -> Unit = {}): RecyclerCardAdapterLoading<K, V> {
        actionEnabled = message != null
        cardLoading.setActionMessage(message)
        cardLoading.setActionButton(button) { onAction.invoke() }
        return this
    }

    fun setTopLoader(topLoader: (((Array<V>?) -> Unit, ArrayList<K>) -> Unit)?): RecyclerCardAdapterLoading<K, V> {
        this.topLoader = topLoader
        return this
    }

    fun setBottomLoader(bottomLoader: (((Array<V>?) -> Unit, ArrayList<K>) -> Unit)?): RecyclerCardAdapterLoading<K, V> {
        this.bottomLoader = bottomLoader
        return this
    }

    fun setStartBottomLoadOffset(i: Int): RecyclerCardAdapterLoading<K, V> {
        this.startBottomLoadOffset = i
        return this
    }

    fun setTopBottomLoadOffset(i: Int): RecyclerCardAdapterLoading<K, V> {
        this.startTopLoadOffset = i
        return this
    }

    fun setMapper(mapper: (V) -> K): RecyclerCardAdapterLoading<K, V> {
        this.mapper = mapper
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun setNotifyCount(notifyCount: Int): RecyclerCardAdapterLoading<K, V> {
        return super.setNotifyCount(notifyCount) as RecyclerCardAdapterLoading<K, V>
    }

    //
    //  Getters
    //

    fun isTopLock() = isLockTop

    fun isLockBottom() = isLockBottom

    fun isInProgress() = isInProgress

    fun getSAmeRemovedCount() = sameRemovedCount

}