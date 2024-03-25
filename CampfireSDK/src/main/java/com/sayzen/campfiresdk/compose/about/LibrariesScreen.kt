package com.sayzen.campfiresdk.compose.about

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dzen.campfire.api.API_TRANSLATE
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.controllers.t
import sh.sit.bonfire.auth.components.BackButton

class LibrariesScreen : ComposeScreen() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { BackButton() },
                    title = { Text(t(API_TRANSLATE.about_libraries)) },
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { contentPadding ->
            LibrariesContainer(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            )
        }
    }
}
