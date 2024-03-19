package com.dzen.campfire.api.models.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

class QuestPartText : QuestPart() {
    override fun getQuestPartType(): Long = API.QUEST_PART_TYPE_TEXT

    var image = ImageRef()
    var gif = ImageRef()
    @Deprecated("use ImageRefs")
    var imageId = 0L
    @Deprecated("use ImageRefs")
    var gifId = 0L
    @Deprecated("use ImageRefs")
    var w = 0
    @Deprecated("use ImageRefs")
    var h = 0
    var insertBytes: ByteArray? = null

    var newFormatting = false
    var title = ""
    var text = ""
    var inputs = emptyArray<QuestInput>()
    var buttons = emptyArray<QuestButton>()
    var effects = emptyArray<QuestEffect>()

    override fun json(inp: Boolean, json: Json): Json {
        image = json.m(inp, "image", image)
        gif = json.m(inp, "gif", gif)
        imageId = json.m(inp, "imageId", imageId)
        w = json.m(inp, "w", w)
        h = json.m(inp, "h", h)
        newFormatting = json.m(inp, "newFormatting", newFormatting)
        title = json.m(inp, "title", title)
        text = json.m(inp, "text", text)
        inputs = json.m(inp, "inputs", inputs, Array<QuestInput>::class)
        buttons = json.m(inp, "buttons", buttons, Array<QuestButton>::class)
        effects = json.m(inp, "effects", effects, Array<QuestEffect>::class)
        return super.json(inp, json)
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        receiver.add(image, imageId, w, h)
        receiver.add(gif, gifId, w, h)
    }

    override fun addInsertData(request: Request<*>) {
        request.addDataOutput(insertBytes)
    }

    override fun restoreInsertData(dataOutput: Iterator<ByteArray?>) {
        insertBytes = dataOutput.next()
    }

    override fun checkValid(details: QuestDetails, parts: List<QuestPart>, errors: MutableList<QuestException>) {
        for (input in inputs) {
            assert(errors, details.variablesMap!![input.varId] != null) {
                QuestException(API_TRANSLATE.quests_edit_error_1, input.hint)
            }
        }

        for (button in buttons) {
            assert(errors, button.jumpToId < 0 || parts.any { it.id == button.jumpToId }) {
                QuestException(API_TRANSLATE.quests_edit_error_2, button.label)
            }
        }
    }
}
