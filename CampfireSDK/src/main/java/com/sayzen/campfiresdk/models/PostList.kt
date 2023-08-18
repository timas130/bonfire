package com.sayzen.campfiresdk.models

import com.sayzen.campfiresdk.models.cards.CardPost

interface PostList {

    fun contains(card: CardPost):Boolean

}