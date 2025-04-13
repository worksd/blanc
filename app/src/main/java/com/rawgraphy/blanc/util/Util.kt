package com.rawgraphy.blanc.util

import kotlinx.coroutines.flow.MutableSharedFlow

val refreshWebView: MutableSharedFlow<List<String>> = MutableSharedFlow()