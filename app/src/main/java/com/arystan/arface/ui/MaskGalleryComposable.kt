package com.arystan.arface.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.arystan.arface.R

private val Accent = Color(0xFFFF4081)

@Composable
fun MaskGallery(vm: ARViewModel) {
    val masks by vm.masks.collectAsState()
    val activeIndex by vm.activeMaskIndex.collectAsState()
    val state = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .background(Color.Black.copy(alpha = 0.55f)),
        ) {
            LazyRow(
                state = state,
                flingBehavior = flingBehavior,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(masks, key = { _, m -> m.id }) { index, mask ->
                    MaskItem(
                        mask = mask,
                        selected = index == activeIndex,
                        onClick = { vm.onMaskSelected(index) },
                    )
                }
                item(key = "__add__") {
                    AddItem(onClick = { vm.showPromptDialog() })
                }
            }
        }
    }
}

@Composable
private fun MaskItem(
    mask: com.arystan.arface.mask.MaskDescriptor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Accent else Color.White.copy(alpha = 0.25f)
    val borderWidth = if (selected) 3.dp else 1.dp
    val context = LocalContext.current
    Column(
        modifier = Modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1B1B1F))
                .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            val data = remember(mask.thumbnail) { resolveThumbnail(mask.thumbnail) }
            AsyncImage(
                model = ImageRequest.Builder(context).data(data).build(),
                contentDescription = mask.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
            )
        }
        Text(
            text = mask.name,
            color = Color.White,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1,
        )
    }
}

@Composable
private fun AddItem(onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Accent.copy(alpha = 0.18f))
                .border(2.dp, Accent, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_filter),
                contentDescription = "Add filter",
                tint = Accent,
                modifier = Modifier.size(32.dp),
            )
        }
        Text(
            text = "AI",
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private fun resolveThumbnail(path: String): Any = when {
    path.startsWith("asset:") -> "file:///android_asset/${path.removePrefix("asset:")}"
    path.startsWith("file:") -> path
    else -> path
}
