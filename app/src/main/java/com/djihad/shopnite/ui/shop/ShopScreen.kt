package com.djihad.shopnite.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.djihad.shopnite.R
import com.djihad.shopnite.model.ShopItem
import com.djihad.shopnite.ui.colorFromHex
import com.djihad.shopnite.ui.components.ErrorCard
import com.djihad.shopnite.ui.components.FilterChipRow
import com.djihad.shopnite.ui.components.SearchField
import com.djihad.shopnite.ui.components.SectionHeading
import com.djihad.shopnite.ui.toComposeColors
import com.djihad.shopnite.util.Formatters

@Composable
fun ShopScreen(
    uiState: ShopUiState,
    filteredItems: List<ShopItem>,
    onSearchChange: (String) -> Unit,
    onSelectType: (String) -> Unit,
    onOpenCosmetic: (String) -> Unit,
) {
    val filterOptions = listOf("All") + uiState.snapshot.items.map { it.typeLabel }.distinct().sorted()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(164.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeading(
                title = stringResource(R.string.title_shop),
                supporting = Formatters.formatDate(uiState.snapshot.shopDate) ?: "Current live shop",
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SearchField(
                query = uiState.searchQuery,
                label = "Search shop",
                onQueryChange = onSearchChange,
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            FilterChipRow(
                options = filterOptions,
                selected = uiState.selectedType,
                onSelected = onSelectType,
            )
        }

        if (uiState.errorMessage != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorCard(message = uiState.errorMessage)
            }
        } else if (uiState.isLoading && uiState.snapshot.items.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorCard(message = "Loading the current item shop...")
            }
        } else if (filteredItems.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorCard(message = "No shop items match that filter right now.")
            }
        } else {
            items(filteredItems, key = { it.offerId + it.cosmeticId }) { item ->
                ShopTile(
                    item = item,
                    onClick = { onOpenCosmetic(item.cosmeticId) },
                )
            }
        }
    }
}

@Composable
private fun ShopTile(
    item: ShopItem,
    onClick: () -> Unit,
) {
    val gradient = item.tileHexes.toComposeColors(
        defaultColors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surface,
        ),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(Brush.verticalGradient(gradient))
                    .padding(12.dp),
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )

                if (!item.bannerText.isNullOrBlank()) {
                    Text(
                        text = item.bannerText,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(MaterialTheme.shapes.small)
                            .background(colorFromHex(item.textBackgroundHex, MaterialTheme.colorScheme.primary))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.typeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = item.vbuckIconUrl,
                            contentDescription = "V-Bucks",
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = Formatters.formatPrice(item.price),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = Formatters.formatTimeLeft(item.outDate).orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}
