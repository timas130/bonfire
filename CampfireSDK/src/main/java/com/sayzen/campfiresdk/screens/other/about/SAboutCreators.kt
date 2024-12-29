package com.sayzen.campfiresdk.screens.other.about

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.compose.util.Avatar
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.SplashMenu
import sh.sit.bonfire.auth.components.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutCreators() {
    data class Creator(
        val name: Long,
        val role: Long,
        val avatar: ImageRef,
        val bonfireTag: String? = null,
        val email: String? = null,
    )

    val creators = listOf(
        Creator(
            name = API_TRANSLATE.about_creators_sit,
            role = API_TRANSLATE.about_creators_sit_subtitle,
            avatar = ApiResources.DEVELOPER_SIT,
            bonfireTag = "sit",
            email = "me@bonfire.moe",
        ),
        Creator(
            name = API_TRANSLATE.about_creators_niki,
            role = API_TRANSLATE.about_creators_niki_subtitle,
            avatar = ApiResources.DEVELOPER_NIKI,
            bonfireTag = "NikiTank",
        ),
        Creator(
            name = API_TRANSLATE.about_creators_operand,
            role = API_TRANSLATE.about_creators_operand_subtitle,
            avatar = ApiResources.DEVELOPER_OPERAND,
            bonfireTag = "Operand",
        ),
        Creator(
            name = API_TRANSLATE.about_creators_zeon,
            role = API_TRANSLATE.about_creators_zeon_subtitle,
            avatar = ApiResources.DEVELOPER_ZEON,
            email = "zeooon@ya.ru",
        ),
        Creator(
            name = API_TRANSLATE.about_creators_saynok,
            role = API_TRANSLATE.about_creators_saynok_subtitle,
            avatar = ApiResources.DEVELOPER_SAYNOK,
            email = "saynokdeveloper@gmail.com",
        ),
        Creator(
            name = API_TRANSLATE.about_creators_egor,
            role = API_TRANSLATE.about_creators_egor_subtitle,
            avatar = ApiResources.DEVELOPER_EGOR,
            email = "georgepro036@gmail.com",
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t(API_TRANSLATE.about_creators)) },
                navigationIcon = { BackButton() },
                actions = {
                    IconButton(onClick = {
                        ToolsAndroid.setToClipboard(API.LINK_CREATORS.asWeb())
                        ToolsToast.show(t(API_TRANSLATE.app_copied))
                    }) {
                        Icon(painterResource(R.drawable.ic_insert_link_white_24dp), t(API_TRANSLATE.app_copy_link))
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { contentPadding ->
        LazyColumn(contentPadding = contentPadding) {
            items(creators) { creator ->
                ListItem(
                    leadingContent = {
                        Avatar(
                            image = creator.avatar,
                            contentDescription = t(creator.name),
                        )
                    },
                    headlineContent = {
                        Text(t(creator.name))
                    },
                    supportingContent = {
                        Text(t(creator.role))
                    },
                    trailingContent = {
                        IconButton(onClick = {
                            val s = SplashMenu()
                            if (creator.bonfireTag != null) {
                                s.add(t(API_TRANSLATE.app_campfire)) {
                                    SProfile.instance(creator.bonfireTag, Navigator.TO)
                                }
                            }
                            if (creator.email != null) {
                                s.add(t(API_TRANSLATE.app_email)) {
                                    ToolsIntent.startMail(creator.email)
                                }
                            }
                            s.asSheetShow()
                        }) {
                            Icon(Icons.Default.MoreVert, t(API_TRANSLATE.about_creators_contacts_alt))
                        }
                    },
                )
            }
        }
    }
}

class SAboutCreators : ComposeScreen() {
    @Composable
    override fun Content() {
        AboutCreators()
    }
}
