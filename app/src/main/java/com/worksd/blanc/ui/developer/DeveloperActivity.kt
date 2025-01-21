package com.worksd.blanc.ui.developer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worksd.blanc.util.PrefUtils
import com.worksd.blanc.util.WebEndPointKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class DeveloperActivity : AppCompatActivity() {

    @Inject
    lateinit var prefUtils: PrefUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DeveloperScreen(
                currentEndPoint = prefUtils.getString(WebEndPointKey).orEmpty(),
            ) {
                prefUtils.setString(WebEndPointKey, it)
                exitProcess(0)
            }
        }
    }
}

@Composable
private fun DeveloperScreen(
    currentEndPoint: String,
    onClickEndpoint: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Current Server Endpoint = $currentEndPoint",
        )
        DeveloperMenu(
            key = "Production Environment",
            description = "(API 서버 : Production: Web: Production)",
            modifier = Modifier.clickable { onClickEndpoint("https://kloud-alpha.vercel.app/") },
        )
        DeveloperMenu(
            key = "Staging Environment",
            description = "(API 서버 : Staging: Web: Production)",
            modifier = Modifier.clickable { onClickEndpoint("kloud-git-develop-rawgraphy-inc.vercel.app/") },
        )
    }
}

@Composable
private fun DeveloperMenu(
    modifier: Modifier = Modifier,
    key: String,
    description: String,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Text(
            text = key,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = description,
        )
    }
}
