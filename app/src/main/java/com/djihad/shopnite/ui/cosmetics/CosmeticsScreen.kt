package com.djihad.shopnite.ui.cosmetics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.djihad.shopnite.model.CosmeticCardItem
import com.djihad.shopnite.model.CosmeticFilters
import com.djihad.shopnite.ui.components.ErrorCard
import com.djihad.shopnite.ui.components.FilterChipRow
import com.djihad.shopnite.ui.components.LoadingCard
import com.djihad.shopnite.ui.components.SearchField
import com.djihad.shopnite.ui.components.SectionHeading
import com.djihad.shopnite.ui.findRarityBackgroundRes
import com.djihad.shopnite.ui.toComposeColors

@Composable
fun CosmeticsScreen(
    uiState: CosmeticsUiState,
    filteredItems: List<CosmeticCardItem>,
    onSearchChange: (String) -> Unit,
    onSelectType: (String) -> Unit,
    onSelectCollection: (String) -> Unit,
    onOpenCosmetic: (String) -> Unit,
) {
    val filterOptions = CosmeticFilters.orderedOptions(uiState.snapshot.items.map { it.filterLabel })

    LazyVerticalGrid(
        columns = GridCells.Adaptive(144.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeading(
                title = stringResource(R.string.title_cosmetics),
                supporting = if (uiState.selectedCollection == CosmeticFilters.Wishlist) {
                    "Wishlisted cosmetics"
                } else {
                    stringResource(R.string.cosmetics_supporting)
                },
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(
                    CosmeticFilters.All to stringResource(R.string.common_all),
                    CosmeticFilters.New to stringResource(R.string.common_new),
                    CosmeticFilters.Wishlist to "Wishlist",
                ).forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = uiState.selectedCollection == option.first,
                        onClick = { onSelectCollection(option.first) },
                        shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = 3,
                        ),
                        label = { Text(option.second) },
                    )
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SearchField(
                query = uiState.searchQuery,
                label = stringResource(R.string.cosmetics_search),
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
            uiState.debugDetails?.let { details ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorCard(message = "Debug: $details")
                }
            }
        } else if (uiState.isLoading && uiState.snapshot.items.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingCard(message = stringResource(R.string.cosmetics_loading))
                }
            }
        } else if (filteredItems.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorCard(message = stringResource(R.string.cosmetics_empty))
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                CosmeticTile(item = item, onClick = { onOpenCosmetic(item.id) })
            }
        }
    }
}

@Composable
private fun CosmeticTile(
    item: CosmeticCardItem,
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
                    .height(172.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (rarityBackground != null) {
                    AsyncImage(
                        model = rarityBackground,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    val gradient = item.paletteHexes.toComposeColors(
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
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.isNew) {
                    Text(
                        text = "NEW",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}
