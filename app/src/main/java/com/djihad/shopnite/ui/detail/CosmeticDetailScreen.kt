package com.djihad.shopnite.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.djihad.shopnite.R
import coil.compose.AsyncImage
import com.djihad.shopnite.model.CosmeticDetail
import com.djihad.shopnite.model.CosmeticImageOption
import com.djihad.shopnite.ui.components.ErrorCard
import com.djihad.shopnite.ui.components.InfoChip
import com.djihad.shopnite.ui.components.LoadingCard
import com.djihad.shopnite.ui.components.RarityPill
import com.djihad.shopnite.ui.findRarityBackgroundRes
import com.djihad.shopnite.ui.toComposeColors
import com.djihad.shopnite.util.Formatters
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticDetailScreen(
    uiState: CosmeticDetailUiState,
    onBack: () -> Unit,
    onToggleWishlist: () -> Unit,
    onForceDebugWishlistNotification: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.detail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingCard(message = "Loading cosmetic details...")
                }
            }
            uiState.errorMessage != null && uiState.detail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(20.dp),
                ) {
                    ErrorCard(message = uiState.errorMessage)
                }
            }
            uiState.detail != null -> {
                CosmeticDetailContent(
                    detail = uiState.detail,
                    isWishlisted = uiState.isWishlisted,
                    showForceNotificationDebugButton = uiState.showForceNotificationDebugButton,
                    modifier = Modifier.padding(innerPadding),
                    onToggleWishlist = onToggleWishlist,
                    onForceDebugWishlistNotification = onForceDebugWishlistNotification,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CosmeticDetailContent(
    detail: CosmeticDetail,
    isWishlisted: Boolean,
    showForceNotificationDebugButton: Boolean,
    modifier: Modifier = Modifier,
    onToggleWishlist: () -> Unit,
    onForceDebugWishlistNotification: () -> Unit,
) {
    val cosmetic = detail.cosmetic
    val context = LocalContext.current
    val rarityBackground = context.findRarityBackgroundRes(
        rarityKey = cosmetic.rarityKey,
        rarityLabel = cosmetic.rarityLabel,
        seriesName = cosmetic.seriesName,
    )
    val imageOptions = cosmetic.imageOptions.ifEmpty {
        cosmetic.imageUrl?.let { listOf(CosmeticImageOption("Default", it)) }.orEmpty()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column {
                    CosmeticImagePager(
                        cosmeticName = cosmetic.name,
                        paletteHexes = cosmetic.paletteHexes,
                        rarityBackground = rarityBackground,
                        imageOptions = imageOptions,
                    )
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = cosmetic.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = cosmetic.typeLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = onToggleWishlist,
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                            )
                            Text(
                                text = if (isWishlisted) "Remove from wishlist" else "Add to wishlist",
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        detail.currentShopItem?.bannerText?.takeIf { it.isNotBlank() }?.let {
                            InfoChip(text = it)
                        }
                        detail.currentShopItem?.let {
                            Formatters.formatTimeLeft(it.outDate)?.let { timeLeft ->
                                InfoChip(text = timeLeft)
                            }
                        }
                    }

                    detail.currentShopItem?.let { shopItem ->
                        DetailPriceRow(
                            label = "Price",
                            price = shopItem.price,
                        )
                    }
                    DetailRarityRow("Rarity", cosmetic.rarityLabel)
                    DetailRow("Type", cosmetic.typeLabel)
                    detail.currentShopItem?.outDate?.let { outDate ->
                        Formatters.formatDateTime(outDate)?.let { formatted ->
                            DetailRow("Leaving date", formatted)
                        }
                    }
                    DetailRow("Added", Formatters.formatDate(cosmetic.addedDate) ?: "Unknown")
                }
            }
        }

        cosmetic.description?.takeIf { it.isNotBlank() }?.let { description ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        if (showForceNotificationDebugButton) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.cosmetic_detail_debug_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.cosmetic_detail_debug_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = onForceDebugWishlistNotification,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.cosmetic_detail_debug_action))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CosmeticImagePager(
    cosmeticName: String,
    paletteHexes: List<String>,
    rarityBackground: Int?,
    imageOptions: List<CosmeticImageOption>,
) {
    val pagerState = rememberPagerState(pageCount = { imageOptions.size.coerceAtLeast(1) })
    var showPageChrome by remember { mutableStateOf(imageOptions.size > 1) }

    LaunchedEffect(pagerState.currentPage, imageOptions.size) {
        showPageChrome = imageOptions.size > 1
        if (imageOptions.size > 1) {
            delay(1_000)
            showPageChrome = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (rarityBackground != null) {
            AsyncImage(
                model = rarityBackground,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            val gradient = paletteHexes.toComposeColors(
                defaultColors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surface,
                ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(gradient)),
            )
        }

        if (imageOptions.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { imageOptions[it].imageUrl },
            ) { page ->
                AsyncImage(
                    model = imageOptions[page].imageUrl,
                    contentDescription = cosmeticName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillWidth,
                )
            }
        }

        if (imageOptions.size > 1 && showPageChrome) {
            Text(
                text = imageOptions[pagerState.currentPage].label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                imageOptions.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                            .background(
                                color = if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
                                },
                                shape = androidx.compose.foundation.shape.CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailPriceRow(
    label: String,
    price: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.vbucks),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "${Formatters.formatPrice(price)} V-Bucks",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DetailRarityRow(
    label: String,
    rarity: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RarityPill(text = rarity)
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
