package com.sayzen.campfiresdk.compose.publication.post.pages.polling

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.compose.BonfireTheme
import com.sayzen.campfiresdk.compose.publication.post.pages.PagesSource
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.tools.ToolsDate
import sh.sit.bonfire.auth.components.TextLoadingButton
import sh.sit.bonfire.formatting.compose.buildInlineAnnotatedString
import sh.sit.bonfire.formatting.core.BonfireFormatter
import kotlin.math.roundToInt

@Composable
internal fun PagePollingRenderer(page: PagePolling, source: PagesSource = PagesSource.Unknown) {
    val model = remember(page.pollingId, source) { PollingModel(page, source) }

    val colors = MaterialTheme.colorScheme
    val formattedTitle = remember(page.title, colors) {
        BonfireFormatter
            .parse(page.title, inlineOnly = true)
            .buildInlineAnnotatedString(colors)
    }

    val showResults by model.getShowResults().collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(),
    ) {
        Column(
            Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            if (formattedTitle.isNotBlank()) {
                Text(
                    text = formattedTitle,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            PollLimits(page = page, source = source)

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                page.options.forEachIndexed { index, option ->
                    PollingItem(model = model, optionTitle = option, index = index)
                }
            }

            AnimatedVisibility(visible = showResults) {
                PollButtons(model)
            }
        }
    }
}

@Composable
private fun PollButtons(model: PollingModel) {
    val isTryingToLoad by model.isTryingToLoad.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextLoadingButton(onClick = model::reload, isLoading = isTryingToLoad) {
            Text(stringResource(R.string.poll_reload))
        }

        TextButton(
            onClick = {
                Navigator.to(PollingResultsScreen(model.page, model.source))
            },
        ) {
            Text(stringResource(R.string.poll_results))
        }
    }
}

@Composable
private fun PollLimits(page: PagePolling, source: PagesSource) {
    // if no limits
    if (
        page.duration <= 0 &&
        page.minDays <= 1 &&
        page.minKarma <= 0 &&
        page.minLevel <= 100 &&
        page.blacklist.isEmpty()
    ) return

    Column(Modifier.padding(bottom = 8.dp)) {
        Text(
            text = stringResource(R.string.poll_limits),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (page.duration > 0) {
                    val dateCreate = source.sourceDateCreate.coerceAtLeast(page.dateCreate)

                    FilterChip(
                        selected = page.duration >= (System.currentTimeMillis() - dateCreate),
                        onClick = {},
                        label = {
                            Text(stringResource(R.string.poll_limits_duration).format(
                                ToolsDate.dateToString(dateCreate + page.duration)
                            ))
                        }
                    )
                }
                if (page.minLevel > 100) {
                    FilterChip(
                        selected = ControllerApi.account.getLevel() >= page.minLevel,
                        onClick = {},
                        label = {
                            Text(stringResource(R.string.poll_limits_level).format(page.minLevel / 100f))
                        }
                    )
                }
                if (page.minKarma > 0) {
                    FilterChip(
                        selected = ControllerApi.account.getKarma30() >= page.minKarma,
                        onClick = {},
                        label = {
                            Text(stringResource(R.string.poll_limits_karma).format((page.minKarma / 100f).roundToInt()))
                        }
                    )
                }
                if (page.minDays > 1) {
                    FilterChip(
                        selected = ControllerApi.account.getAccount().ageDays >= page.minDays,
                        onClick = {},
                        label = {
                            Text(stringResource(R.string.poll_limits_age).format(page.minDays))
                        }
                    )
                }
                if (page.blacklist.isNotEmpty()) {
                    FilterChip(
                        selected = page.blacklist.find { it.id == ControllerApi.account.getId() } == null,
                        onClick = {
                            Navigator.to(PollingBlacklistScreen(page))
                        },
                        label = {
                            Text(stringResource(R.string.poll_limits_blacklist))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PollingItem(model: PollingModel, optionTitle: String, index: Int) {
    val scope = rememberCoroutineScope()

    val colors = MaterialTheme.colorScheme
    val formattedOption = remember(optionTitle, colors) {
        BonfireFormatter
            .parse(optionTitle, inlineOnly = true)
            .buildInlineAnnotatedString(colors)
    }

    val votingFor by model.votingFor.collectAsState()
    val totalVotes by model.totalVotes.collectAsState()
    val result by model.getResult(index).collectAsState()
    val showResults by model.getShowResults().collectAsState()
    val canVote = model.getCanVote()

    val targetValue = if (votingFor == index) 1f else 0f
    val votingProgress by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = if (targetValue == 1f) {
            tween(durationMillis = CampfireConstants.VOTE_TIME.toInt())
        } else {
            tween(durationMillis = 300)
        },
        label = "VotingProgress"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .drawWithContent {
                drawRect(colors.tertiaryContainer)
                if (showResults) {
                    drawRect(
                        color = colors.tertiary.copy(alpha = 0.5f),
                        size = Size(
                            size.width * (result.count / totalVotes.toFloat()),
                            size.height
                        )
                    )
                }
                drawRect(
                    color = colors.tertiary.copy(alpha = 0.5f),
                    size = Size(
                        size.width * votingProgress,
                        size.height,
                    ),
                )
                drawContent()
            }
            .clickable(canVote) {
                model.vote(scope, index)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formattedOption,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )

            AnimatedVisibility(visible = showResults) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = result.count.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 2.dp),
                        softWrap = false,
                    )

                    if (totalVotes > 0) {
                        Text(
                            text = "${(result.count / totalVotes.toFloat() * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            softWrap = false,
                            modifier = Modifier.alpha(0.7f),
                        )
                    }
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PollPreview() {
    BonfireTheme(useDarkTheme = isSystemInDarkTheme()) {
        Surface {
            PollLimits(
                page = PagePolling().apply {
                    duration = 1000L * 60 * 60
                    minLevel = 1000
                    minKarma = 50000
                    minDays = 5
                },
                source = PagesSource(
                    sourceType = 0,
                    sourceId = API.PAGES_SOURCE_TYPE_POST,
                    sourceDateCreate = System.currentTimeMillis()
                )
            )
        }
    }
}
