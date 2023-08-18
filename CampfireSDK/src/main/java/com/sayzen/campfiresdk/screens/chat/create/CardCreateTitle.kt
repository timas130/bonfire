package com.sayzen.campfiresdk.screens.chat.create

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatParamsConf
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.splash.SplashChooseImage

class CardCreateTitle(
        var chatId: Long,
        val myLvl: Long,
        val changeName: String,
        val changeImageId: Long,
        val params: ChatParamsConf,
        val updateFinish: () -> Unit
) : Card(R.layout.screen_chat_create_card_title) {

    var image: ByteArray? = null
    var text = changeName

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vImageIcon: View = view.findViewById(R.id.vImageIcon)
        val vUsers: TextView = view.findViewById(R.id.vUsers)
        val vName: SettingsField = view.findViewById(R.id.vName)
        val vNameTitle: TextView = view.findViewById(R.id.vNameTitle)
        val vAllowInvites: SettingsCheckBox = view.findViewById(R.id.vAllowInvites)
        val vAllowEdit: SettingsCheckBox = view.findViewById(R.id.vAllowEdit)
        val vChatIsPublic: SettingsCheckBox = view.findViewById(R.id.vChatIsPublic)

        vName.setHint(t(API_TRANSLATE.app_naming))
        vName.vField.imeOptions = EditorInfo.IME_ACTION_DONE    //  Вылетало при нажатии Enter
        ToolsView.onFieldEnterKey(vName.vField){
            ToolsView.hideKeyboard(vName.vField)
        }

        vAllowInvites.setTitle(t(API_TRANSLATE.chat_create_allow_invites))
        vAllowEdit.setTitle(t(API_TRANSLATE.chat_create_allow_edit))
        vChatIsPublic.setTitle(t(API_TRANSLATE.chat_create_public))

        vAllowInvites.isEnabled = myLvl == API.CHAT_MEMBER_LVL_ADMIN
        vAllowEdit.isEnabled = myLvl == API.CHAT_MEMBER_LVL_ADMIN
        vChatIsPublic.isEnabled = myLvl == API.CHAT_MEMBER_LVL_ADMIN

        vAllowInvites.setChecked(params.allowUserInvite)
        vAllowEdit.setChecked(params.allowUserNameAndImage)
        vChatIsPublic.setChecked(params.isPublic)

        vAllowInvites.setOnClickListener { params.allowUserInvite = vAllowInvites.isChecked() }
        vAllowEdit.setOnClickListener { params.allowUserNameAndImage = vAllowEdit.isChecked() }
        vChatIsPublic.setOnClickListener { params.isPublic = vChatIsPublic.isChecked() }

        if(chatId > 0) {
            vChatIsPublic.setSubtitle(ControllerLinks.linkToConf(chatId))
            vChatIsPublic.setOnLongClickListener{
                ToolsAndroid.setToClipboard(ControllerLinks.linkToConf(chatId));
                ToolsToast.show(t(API_TRANSLATE.app_copied))
                true
            }
        }
        vName.setText(changeName)
        vNameTitle.setText(changeName)

        vName.setMaxLength(API.CHAT_NAME_MAX)
        vName.addOnTextChanged {
            text = it
            updateFinish.invoke()
        }

        if (changeImageId != 0L) {
            ImageLoader.load(changeImageId).into(vImage)
            vImageIcon.visibility = View.GONE
        }

        if( params.allowUserNameAndImage || myLvl != API.CHAT_MEMBER_LVL_USER){
            vImage.isEnabled = true
            vName.visibility = View.VISIBLE
            vNameTitle.visibility = View.GONE
        } else{
            vImage.isEnabled = false
            vName.visibility = View.GONE
            vNameTitle.visibility = View.VISIBLE
        }


        vImage.setOnClickListener { chooseImage(vImage, vImageIcon) }
        vUsers.text = t(API_TRANSLATE.app_users) + ":"
    }

    private fun chooseImage(vImage: ImageView, vImageIcon: View) {
        SplashChooseImage()
                .setOnSelectedBitmap { _, b ->
                    Navigator.to(SCrop(b, API.CHAT_IMG_SIDE, API.CHAT_IMG_SIDE) { screen, b2, _, _, _, _ ->
                        this.image = ToolsBitmap.toBytes(ToolsBitmap.resize(b2, API.CHAT_IMG_SIDE), API.CHAT_IMG_WEIGHT)
                        vImage.setImageBitmap(b2)
                        vImageIcon.visibility = View.GONE
                        updateFinish.invoke()
                    }
                    )
                }
                .asSheetShow()
    }

}