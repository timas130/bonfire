package com.sayzen.campfiresdk.compose.other

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeSplash
import java.net.URI
import kotlinx.coroutines.flow.MutableStateFlow
import sh.sit.bonfire.auth.components.BetterModalBottomSheet

class LinkAlertSplash(
    private val link: String,
    private val linkHost: String,
    private val onVisit: (trust: Boolean) -> Unit
) : ComposeSplash() {
    private val isShownFlow = MutableStateFlow(isShown())

    @Composable
    override fun Content() {
        LinkAlert(
            open = isShownFlow.collectAsState().value,
            close = {
                hide()
            },
            onVisit = onVisit,
            link = link,
            linkHost = linkHost
        )
    }

    override fun onShow() {
        super.onShow()
        isShownFlow.tryEmit(isShown())
    }

    override fun onHide() {
        super.onHide()
        isShownFlow.tryEmit(isShown())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkAlert(
    open: Boolean,
    close: () -> Unit,
    onVisit: (trust: Boolean) -> Unit,
    link: String,
    linkHost: String
) {
    var trust by remember { mutableStateOf(false) }

    val linkHostIndex = link.indexOf(string = linkHost, ignoreCase = false)
    val spanLink = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            append(link.substring(0, linkHostIndex))
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(link.substring(linkHostIndex, linkHostIndex + linkHost.length))
        }
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            append(link.substring(linkHostIndex + linkHost.length, link.length))
        }
    }

    BetterModalBottomSheet(open = open, onDismissRequest = close) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.link_alert),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp)
            )

            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surface)
                )

                Text(
                    text = spanLink,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }

            if (linkHost.length > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.Checkbox,
                            onClick = { trust = !trust }
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Checkbox(
                        checked = trust,
                        onCheckedChange = null
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.link_alert_trust).format(linkHost))
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedButton(onClick = close) {
                    Text(stringResource(R.string.link_alert_cancel))
                }

                Button(onClick = { onVisit(trust); close() }) {
                    Text(stringResource(R.string.link_alert_visit))
                }
            }
        }
    }
}
