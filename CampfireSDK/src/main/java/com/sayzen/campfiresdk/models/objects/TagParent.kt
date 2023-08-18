package com.sayzen.campfiresdk.models.objects

import com.dzen.campfire.api.models.publications.tags.PublicationTag

import java.util.ArrayList

class TagParent(val tag: PublicationTag) {

    val tags = ArrayList<PublicationTag>()

}
