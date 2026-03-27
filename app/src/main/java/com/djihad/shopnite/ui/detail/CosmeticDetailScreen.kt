package com.djihad.shopnite.ui.detail

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.djihad.shopnite.model.CosmeticDetail
import com.djihad.shopnite.ui.components.ErrorCard
import com.djihad.shopnite.ui.components.InfoChip
import com.djihad.shopnite.ui.toComposeColors
import com.djihad.shopnite.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticDetailScreen(
    uiState: CosmeticDetailUiState,
    onBack: () -> Unit,
    onToggleWishlist: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cosmetic") },
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
                    Text("Loading cosmetic details...")
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
                    modifier = Modifier.padding(innerPadding),
                    onToggleWishlist = onToggleWishlist,
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
    modifier: Modifier = Modifier,
    onToggleWishlist: () -> Unit,
) {
    val cosmetic = detail.cosmetic
    val gradient = cosmetic.paletteHexes.toComposeColors(
        defaultColors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surface,
        ),
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Card {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .background(Brush.verticalGradient(gradient))
                    ) {
                        AsyncImage(
                            model = cosmetic.imageUrl,
                            contentDescription = cosmetic.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
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
                        FilledTonalButton(
                            onClick = onToggleWishlist,
                            modifier = Modifier.fillMaxWidth(),
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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Quick facts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        InfoChip(text = cosmetic.rarityLabel)
                        cosmetic.seriesName?.let { InfoChip(text = it) }
                        detail.currentShopItem?.let {
                            InfoChip(text = "${Formatters.formatPrice(it.price)} V-Bucks")
                            Formatters.formatTimeLeft(it.outDate)?.let { timeLeft ->
                                InfoChip(text = timeLeft)
                            }
                        }
                    }

                    DetailRow("Price", detail.currentShopItem?.price?.let(Formatters::formatPrice)?.plus(" V-Bucks") ?: "Not in shop")
                    DetailRow("Rarity", cosmetic.rarityLabel)
                    DetailRow("Type", cosmetic.typeLabel)
                    DetailRow("Occurrences", detail.occurrences?.toString() ?: "Not provided")
                    DetailRow("Leaving date", Formatters.formatDateTime(detail.currentShopItem?.outDate) ?: "Not in shop")
                    DetailRow("Added", Formatters.formatDate(cosmetic.addedDate) ?: "Unknown")
                    DetailRow("Last appearance", Formatters.formatDate(cosmetic.lastAppearance) ?: "Not provided")
                    DetailRow("Source", cosmetic.source.name.replace('_', ' '))
                }
            }
        }

        cosmetic.description?.takeIf { it.isNotBlank() }?.let { description ->
            item {
                Card {
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
