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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.djihad.shopnite.R
import com.djihad.shopnite.model.CosmeticCardItem
import com.djihad.shopnite.ui.components.ErrorCard
import com.djihad.shopnite.ui.components.FilterChipRow
import com.djihad.shopnite.ui.components.SearchField
import com.djihad.shopnite.ui.components.SectionHeading
import com.djihad.shopnite.ui.toComposeColors

@Composable
fun CosmeticsScreen(
    uiState: CosmeticsUiState,
    filteredItems: List<CosmeticCardItem>,
    onSearchChange: (String) -> Unit,
    onSelectType: (String) -> Unit,
    onSetShowNewOnly: (Boolean) -> Unit,
    onOpenCosmetic: (String) -> Unit,
) {
    val filterOptions = listOf("All") + uiState.snapshot.items.map { it.typeLabel }.distinct().sorted()

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
                supporting = "Browse every tracked cosmetic",
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(false to "All", true to "New").forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = uiState.showNewOnly == option.first,
                        onClick = { onSetShowNewOnly(option.first) },
                        shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = 2,
                        ),
                        label = { Text(option.second) },
                    )
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SearchField(
                query = uiState.searchQuery,
                label = "Search cosmetics",
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
                ErrorCard(message = "Loading cosmetics...")
            }
        } else if (filteredItems.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorCard(message = "No cosmetics match that filter right now.")
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
    val gradient = item.paletteHexes.toComposeColors(
        defaultColors = listOf(
            MaterialTheme.colorScheme.primary,
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
                    .height(172.dp)
                    .background(Brush.verticalGradient(gradient))
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
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
                Text(
                    text = item.typeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
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
