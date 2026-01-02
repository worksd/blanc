package com.rawgraphy.blanc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder
import com.rawgraphy.blanc.data.BottomMenuResponse

@Composable
fun BottomNavigation(
    currentSelectedRoute: String,
    bottomMenuList: List<BottomMenuResponse>,
    onClick: (String) -> Unit,
) {
    // ImageLoader를 상위 컴포넌트에서 remember로 생성
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
    ) {
        bottomMenuList.forEach { item ->
            key(item.page.route) {
                BottomNavigationItem(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onClick(item.page.route)
                        },
                    currentSelectedRoute = currentSelectedRoute,
                    item = item,
                    imageLoader = imageLoader,
                )
            }
        }
    }
}

@Composable
fun BottomNavigationItem(
    modifier: Modifier = Modifier,
    currentSelectedRoute: String,
    item: BottomMenuResponse,
    imageLoader: ImageLoader,
) {
    // color를 remember로 계산
    val isSelected = currentSelectedRoute == item.page.route
    val color = remember(isSelected) {
        if (isSelected) Color.Black else Color(0xFF86898C)
    }

    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier,  // 불필요한 modifier 전달 제거
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AsyncImage(
                imageLoader = imageLoader,  // 상위에서 전달받은 imageLoader 사용
                modifier = Modifier.size(item.iconSize.dp),
                model = item.iconUrl,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color),
            )
            Text(
                text = item.label,
                color = color,
            )
        }
    }
}