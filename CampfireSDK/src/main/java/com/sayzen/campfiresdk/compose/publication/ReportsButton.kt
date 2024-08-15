package com.sayzen.campfiresdk.compose.publication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.screens.reports.SReports
import com.sup.dev.android.libs.screens.navigator.Navigator

@Composable
internal fun ReportsButton(pub: Publication, modifier: Modifier = Modifier) {
    val canBlock = ControllerApi.can(pub.fandom.id, pub.fandom.languageId, API.LVL_MODERATOR_BLOCK)

    AnimatedVisibility(pub.reportsCount > 0 && canBlock) {
        CustomFilledTonalButton(
            onClick = {
                Navigator.to(SReports(pub.id))
            },
            modifier = modifier,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_security_white_24dp),
                contentDescription = stringResource(R.string.publication_reports_alt),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))

            Text(pub.reportsCount.toString())
        }
    }
}
