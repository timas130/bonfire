package com.sayzen.campfiresdk.models

import com.sayzen.campfiresdk.compose.publication.post.CardPostProxy

interface PostList {

    fun contains(card: CardPostProxy):Boolean

}
