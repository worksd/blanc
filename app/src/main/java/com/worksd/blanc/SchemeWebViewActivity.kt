package com.worksd.blanc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

const val SCHEME_WEB_VIEW_ROUTE = "EXTRAS_ROUTE"

class SchemeWebViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BlancWebView(
                activity = this,
                route = intent.getStringExtra(SCHEME_WEB_VIEW_ROUTE).orEmpty()
            )
        }
    }
}

