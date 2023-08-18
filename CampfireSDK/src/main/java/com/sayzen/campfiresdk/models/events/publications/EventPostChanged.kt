package com.sayzen.campfiresdk.models.events.publications

import com.dzen.campfire.api.models.publications.post.Page

class EventPostChanged(var publicationId: Long, var pages: Array<Page>)
