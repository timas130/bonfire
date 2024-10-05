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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeSplash
import java.net.URI
import kotlinx.coroutines.flow.MutableStateFlow
import sh.sit.bonfire.auth.components.BetterModalBottomSheet

class LinkAlertSplash(
    private val link: String,
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
            link = link
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
    link: String
) {
    var trust by remember { mutableStateOf(false) }

    // *probably* the best way to do this
    val uri = URI(link)
    val spanLink = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            uri.scheme?.let { append(it + ':') }
            if (uri.isOpaque()) {
                uri.schemeSpecificPart?.let { append(it) }
            } else {
                if (uri.host != null) {
                    append("//")
                    uri.userInfo?.let { append(it + '@') }
                    val needBrackets = ((uri.host!!.indexOf(':') >= 0) &&
                        !uri.host!!.startsWith("[") &&
                        !uri.host!!.endsWith("]"));
                    if (needBrackets) append('[')
                    withStyle(SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )) {
                        append(uri.host!!)
                    }
                    if (needBrackets) append(']')
                    uri.port.takeIf { it != -1 }?.let { append(':' + it.toString()) }
                } else if (uri.authority != null) {
                    append("//")
                    append(uri.authority!!)
                }
                uri.path?.let { append(it) }
                uri.query?.let { append('?' + it) }
            }
            uri.fragment?.let { append('#' + it) }
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

            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = spanLink,
                    style = MaterialTheme.typography.bodyLarge,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (uri.host != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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

                    Text(stringResource(R.string.link_alert_trust).format(uri.host!!))
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
