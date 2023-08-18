package com.sayzen.campfiresdk.models.objects

import com.dzen.campfire.api.models.translate.Translate

class Rule(
        val title:Translate,
        val text: Translate,
        val correct:Translate,
        val incorrect:Translate)