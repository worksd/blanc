package com.worksd.blanc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.worksd.blanc.data.BottomMenuResponse

@Composable
fun BottomNavigation(
    bottomMenuList: List<BottomMenuResponse>,
    onClick: (String) -> Unit,
) {
    Row (
        modifier = Modifier.fillMaxWidth()
    ){
        bottomMenuList.forEach { item ->
            BottomNavigationItem(
                modifier = Modifier.weight(1f).clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onClick(item.page.route)
                },
                item = item,
            )
        }
    }
}

@Composable
fun BottomNavigationItem(
    modifier: Modifier = Modifier,
    item: BottomMenuResponse,
) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                modifier = Modifier.size(item.iconSize.dp),
                model = item.iconUrl,
                contentDescription = null,
            )
            Text(
                text = item.label,
            )
        }
    }
}