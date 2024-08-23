package com.sayzen.campfiresdk.compose.attach

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.util.shimmerExt
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer

@Composable
internal fun AttachGifFooter(model: AttachFlyoutModel, modifier: Modifier = Modifier) {
    Column {
        AttachGifSearchBar(model, modifier)
        AttachGifSearchSuggestions(model)
    }
}

@Composable
private fun AttachGifSearchBar(model: AttachFlyoutModel, modifier: Modifier = Modifier) {
    val gifQuery by model.gifQuery.collectAsState()

    var value by remember { mutableStateOf(TextFieldValue(gifQuery)) }

    LaunchedEffect(gifQuery) {
        value = if (value.selection.end >= value.text.length && value.selection.length == 0) {
            value.copy(text = gifQuery, selection = TextRange(gifQuery.length, gifQuery.length))
        } else {
            value.copy(text = gifQuery)
        }
    }

    Surface(modifier) {
        BasicTextField(
            value = value,
            onValueChange = {
                value = it
                model.setGifQuery(it.text)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
                capitalization = KeyboardCapitalization.None
            ),
            decorationBox = { inner ->
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .height(32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(painterResource(R.drawable.search_20px), contentDescription = null)

                        Box(Modifier.weight(1f)) {
                            if (gifQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.attach_search_tenor),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = LocalContentColor.current,
                                    modifier = Modifier.alpha(0.6f)
                                )
                            }
                            inner()
                        }

                        AnimatedVisibility(gifQuery.isNotEmpty()) {
                            IconButton(onClick = { model.setGifQuery("") }, Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.attach_search_clear),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
            cursorBrush = SolidColor(LocalContentColor.current),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AttachGifSearchSuggestions(model: AttachFlyoutModel, modifier: Modifier = Modifier) {
    val searchSuggestions by model.searchSuggestions.collectAsState()
    val searchQuery by model.gifQuery.collectAsState()

    val shimmer = rememberShimmer(ShimmerBounds.Window)

    LazyRow(
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        item(key = "__scroll_fixer__") {
            Box {}
        }

        if (searchSuggestions?.first.isNullOrBlank()) {
            item(key = "__scroll_markers__") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.animateItem()
                ) {
                    ScrollMarkerItem(model, AttachFlyoutModel.ScrollMarker.Recent)
                    ScrollMarkerItem(model, AttachFlyoutModel.ScrollMarker.Favourite)
                    ScrollMarkerItem(model, AttachFlyoutModel.ScrollMarker.Trending)
                    VerticalDivider(Modifier.height(24.dp))
                }
            }
        }

        val suggestions = searchSuggestions
        if (suggestions != null && suggestions.first == searchQuery && suggestions.second?.isEmpty() == true) {
            item(key = "suggestion:$searchQuery") {
                SearchSuggestion(
                    suggestion = searchQuery,
                    model = model,
                    shimmer = shimmer,
                    modifier = Modifier.animateItem()
                )
            }
        }
        items(suggestions?.second ?: emptyList(), key = { "suggestion:$it" }) {
            SearchSuggestion(
                suggestion = it,
                model = model,
                shimmer = shimmer,
                modifier = Modifier.animateItem(),
            )
        }
        if (suggestions?.second?.isEmpty() == true) {
            item(key = "__spacer__") {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SearchSuggestion(
    suggestion: String,
    model: AttachFlyoutModel,
    shimmer: Shimmer,
    modifier: Modifier = Modifier
) {
    val searchQuery by model.gifQuery.collectAsState()
    val searchSuggestions by model.searchSuggestions.collectAsState()

    val isLoading by derivedStateOf {
        searchSuggestions?.first != searchQuery
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            color = if (searchQuery == suggestion) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surface
            },
            shape = MaterialTheme.shapes.small,
            onClick = {
                model.setGifQuery(suggestion)
            },
            enabled = !isLoading,
            modifier = modifier.shimmerExt(isLoading, shimmer)
        ) {
            Box(
                Modifier
                    .sizeIn(minHeight = 32.dp, minWidth = 32.dp)
                    .padding(horizontal = 8.dp)) {
                Text(
                    text = suggestion,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ScrollMarkerItem(
    model: AttachFlyoutModel,
    scrollMarker: AttachFlyoutModel.ScrollMarker,
    modifier: Modifier = Modifier,
) {
    val gifScrollMarker by model.gifScrollMarker.collectAsState()

    ScrollMarkerItem(
        scrollMarker = scrollMarker,
        active = gifScrollMarker == scrollMarker,
        onClick = {
            model.scrollToGifMarker(scrollMarker)
        },
        modifier = modifier
    )
}

@Composable
private fun ScrollMarkerItem(
    scrollMarker: AttachFlyoutModel.ScrollMarker,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        val shape = MaterialTheme.shapes.small
        val background = if (active) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }

        Box(
            modifier
                .clip(shape)
                .background(
                    color = background,
                    shape = shape
                )
                .clickable(onClick = onClick)
        ) {
            Icon(
                painter = when (scrollMarker) {
                    AttachFlyoutModel.ScrollMarker.Recent -> painterResource(R.drawable.history_20px)
                    AttachFlyoutModel.ScrollMarker.Favourite -> painterResource(R.drawable.favourite_20px)
                    AttachFlyoutModel.ScrollMarker.Trending -> painterResource(R.drawable.trending_up_20px)
                    AttachFlyoutModel.ScrollMarker.Search -> painterResource(R.drawable.search_20px)
                },
                contentDescription = when (scrollMarker) {
                    AttachFlyoutModel.ScrollMarker.Recent -> stringResource(R.string.attach_scroll_recent)
                    AttachFlyoutModel.ScrollMarker.Favourite -> stringResource(R.string.attach_scroll_favourite)
                    AttachFlyoutModel.ScrollMarker.Trending -> stringResource(R.string.attach_scroll_trending)
                    AttachFlyoutModel.ScrollMarker.Search -> stringResource(R.string.attach_scroll_search)
                },
                tint = contentColorFor(background),
                modifier = Modifier
                    .padding(6.dp)
                    .size(20.dp)
            )
        }
    }
}
