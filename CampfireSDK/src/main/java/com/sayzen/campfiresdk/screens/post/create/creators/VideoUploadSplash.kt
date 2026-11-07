package com.sayzen.campfiresdk.screens.post.create.creators

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageVideo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.ComposeSplash
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.video.VideoRepository
import com.sayzen.campfiresdk.video.VideoUploader
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.libs.debug.err
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import sh.sit.bonfire.auth.components.BetterModalBottomSheet

class VideoUploadSplash(
    private val uri: Uri,
    private val requestPutPage: (Page, Screen?, Splash?, ((CardPage) -> Unit)) -> Unit,
    private val requestChangePage: (Page, CardPage, Screen?, Splash?, (Page) -> Unit) -> Unit,
    private val card: CardPage?,
) : ComposeSplash() {

    internal sealed class State {
        data object Preparing : State()
        data class Uploading(val progress: Float) : State()
        data object Finishing : State()
        data class Error(val text: String) : State()
    }

    private val isShownFlow = MutableStateFlow(isShown())
    private val state = MutableStateFlow<State>(State.Preparing)
    private val scope = MainScope()
    private var uploadJob: Job? = null
    private var uploadedPage: PageVideo? = null

    init {
        startUpload()
    }

    private fun startUpload() {
        uploadJob?.cancel()
        uploadJob = scope.launch {
            state.value = State.Preparing
            try {
                val context = SupAndroid.appContext!!
                val precheck = VideoUploader.precheck(context, uri)
                state.value = State.Uploading(0f)
                val page = VideoUploader.upload(context, uri, precheck) { sentBytes ->
                    state.value = if (sentBytes >= precheck.sizeBytes) {
                        State.Finishing
                    } else {
                        State.Uploading(sentBytes.toFloat() / precheck.sizeBytes)
                    }
                }
                state.value = State.Finishing
                onSuccess(page)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                err(e)
                state.value = State.Error(errorText(e))
            }
        }
    }

    private suspend fun errorText(e: Exception): String {
        val reason = (e as? VideoUploader.VideoUploadException)?.reason
            ?: VideoUploader.VideoUploadException.Reason.Unknown
        // session-cached after the precheck; the constants fallback applies otherwise
        val limits = VideoRepository.getUploadLimits()
        return when (reason) {
            VideoUploader.VideoUploadException.Reason.NotVideo ->
                ToolsResources.s(R.string.video_upload_error_not_video)
            VideoUploader.VideoUploadException.Reason.TooLong ->
                ToolsResources.s(R.string.video_upload_error_too_long, limits.maxDurationMs / 1000)
            VideoUploader.VideoUploadException.Reason.TooLarge ->
                ToolsResources.s(R.string.video_upload_error_too_large, limits.maxSizeBytes / (1024 * 1024))
            VideoUploader.VideoUploadException.Reason.UnknownSize ->
                ToolsResources.s(R.string.video_upload_error_unknown_size)
            VideoUploader.VideoUploadException.Reason.UnknownDuration ->
                ToolsResources.s(R.string.video_upload_error_unknown_duration)
            VideoUploader.VideoUploadException.Reason.LevelTooLow ->
                ToolsResources.s(R.string.video_upload_error_level)
            VideoUploader.VideoUploadException.Reason.DailyLimit ->
                ToolsResources.s(R.string.video_upload_error_daily_limit)
            VideoUploader.VideoUploadException.Reason.Network ->
                ToolsResources.s(R.string.video_upload_error_network)
            VideoUploader.VideoUploadException.Reason.Unknown ->
                ToolsResources.s(R.string.video_upload_error_unknown)
        }
    }

    private fun onSuccess(page: PageVideo) {
        uploadedPage = page
        attachPage(page)
    }

    private fun attachPage(page: PageVideo) {
        state.value = State.Finishing
        if (card == null) {
            requestPutPage.invoke(page, null, this) {}
        } else {
            requestChangePage.invoke(page, card, null, this) {}
        }
    }

    private fun retry() {
        val page = uploadedPage
        if (page != null) attachPage(page) else startUpload()
    }

    override fun setEnabled(enabled: Boolean): Splash {
        super.setEnabled(enabled)
        if (enabled && uploadedPage != null && isShown()) {
            state.value = State.Error(ToolsResources.s(R.string.video_upload_error_attach))
        }
        return this
    }

    @Composable
    override fun Content() {
        VideoUploadSheet(
            open = isShownFlow.collectAsState().value,
            close = {
                hide()
            },
            state = state.collectAsState().value,
            onRetry = { retry() },
        )
    }

    override fun onShow() {
        super.onShow()
        isShownFlow.tryEmit(isShown())
    }

    override fun onHide() {
        super.onHide()
        isShownFlow.tryEmit(isShown())
        if (uploadedPage == null) uploadJob?.cancel()
        scope.cancel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VideoUploadSheet(
    open: Boolean,
    close: () -> Unit,
    state: VideoUploadSplash.State,
    onRetry: () -> Unit,
) {
    BetterModalBottomSheet(open = open, onDismissRequest = close) {
        VideoUploadSheetContent(
            state = state,
            onCancel = close,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun VideoUploadSheetContent(
    state: VideoUploadSplash.State,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.video_upload_title),
            style = MaterialTheme.typography.titleLarge,
        )

        when (state) {
            is VideoUploadSplash.State.Preparing -> ProgressLine(stringResource(R.string.video_upload_preparing))
            is VideoUploadSplash.State.Uploading -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { state.progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row {
                        Text(
                            text = stringResource(R.string.video_upload_uploading),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${(state.progress.coerceIn(0f, 1f) * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            is VideoUploadSplash.State.Finishing -> ProgressLine(stringResource(R.string.video_upload_finishing))
            is VideoUploadSplash.State.Error -> Text(
                text = state.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.End),
        ) {
            when (state) {
                is VideoUploadSplash.State.Error -> {
                    TextButton(onClick = onCancel) { Text(t(API_TRANSLATE.app_cancel)) }
                    Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
                is VideoUploadSplash.State.Finishing -> {}
                else -> {
                    TextButton(onClick = onCancel) { Text(t(API_TRANSLATE.app_cancel)) }
                }
            }
        }
    }
}

@Composable
private fun ProgressLine(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(24.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
