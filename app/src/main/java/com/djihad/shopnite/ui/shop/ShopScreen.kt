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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.djihad.shopnite.R
import com.djihad.shopnite.model.CosmeticFilters
import com.djihad.shopnite.model.ShopItem
import com.djihad.shopnite.ui.components.ErrorCard
import com.djihad.shopnite.ui.components.FilterChipRow
import com.djihad.shopnite.ui.components.LoadingCard
import com.djihad.shopnite.ui.components.SearchField
import com.djihad.shopnite.ui.components.SectionHeading
import com.djihad.shopnite.ui.components.VbucksBadge
import com.djihad.shopnite.ui.findRarityBackgroundRes
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
    val filterOptions = CosmeticFilters.orderedOptions(uiState.snapshot.items.map { it.filterLabel })

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
                supporting = Formatters.formatDate(uiState.snapshot.shopDate) ?: stringResource(R.string.shop_supporting),
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SearchField(
                query = uiState.searchQuery,
                label = stringResource(R.string.shop_search),
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
                LoadingCard(message = stringResource(R.string.shop_loading))
            }
        } else if (filteredItems.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorCard(message = stringResource(R.string.shop_empty))
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
    val context = LocalContext.current
    val rarityBackground = context.findRarityBackgroundRes(
        rarityKey = item.rarityKey,
        rarityLabel = item.rarityLabel,
        seriesName = item.seriesName,
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(
                        if (item.tileHexes.isNotEmpty()) {
                            Brush.verticalGradient(
                                item.tileHexes.toComposeColors(
                                    defaultColors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surface,
                                    ),
                                ),
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface,
                                ),
                            )
                        },
                    ),
            ) {
                if (item.tileHexes.isEmpty() && rarityBackground != null) {
                    AsyncImage(
                        model = rarityBackground,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
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
                        VbucksBadge(
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
