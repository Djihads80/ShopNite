package com.djihad.shopnite.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.djihad.shopnite.R
import com.djihad.shopnite.model.CosmeticFilters
import com.djihad.shopnite.model.CosmeticSort
import com.djihad.shopnite.ui.RarityPillColors
import com.djihad.shopnite.ui.rarityPillColors

@Composable
fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
        ),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
fun LoadingCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun VbucksBadge(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(
            if (isSystemInDarkTheme()) {
                R.drawable.vbucks
            } else {
                R.drawable.vbucks_dark
            },
        ),
        contentDescription = "V-Bucks",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun SearchField(
    query: String,
    label: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = CircleShape,
        label = { Text(label) },
        leadingIcon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
    )
}

@Composable
fun SearchControlsRow(
    query: String,
    label: String,
    rarityOptions: List<String>,
    selectedRarities: Set<String>,
    selectedSort: CosmeticSort,
    onQueryChange: (String) -> Unit,
    onRaritiesSelected: (Set<String>) -> Unit,
    onSortSelected: (CosmeticSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    var rarityDialogOpen by remember { mutableStateOf(false) }
    var sortDialogOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(
            query = query,
            label = label,
            onQueryChange = onQueryChange,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { rarityDialogOpen = true }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter rarity",
                    tint = if (selectedRarities.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        }
        Box {
            IconButton(onClick = { sortDialogOpen = true }) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort cosmetics",
                )
            }
        }
    }

    if (rarityDialogOpen) {
        RarityFilterDialog(
            rarityOptions = rarityOptions,
            selectedRarities = selectedRarities,
            onDismiss = { rarityDialogOpen = false },
            onSelected = onRaritiesSelected,
        )
    }

    if (sortDialogOpen) {
        SortDialog(
            selectedSort = selectedSort,
            onDismiss = { sortDialogOpen = false },
            onSelected = {
                onSortSelected(it)
                sortDialogOpen = false
            },
        )
    }
}

@Composable
private fun RarityFilterDialog(
    rarityOptions: List<String>,
    selectedRarities: Set<String>,
    onDismiss: () -> Unit,
    onSelected: (Set<String>) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter rarities") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = selectedRarities.isEmpty(),
                            onCheckedChange = { checked ->
                                if (checked) onSelected(emptySet())
                            },
                        )
                        Text(stringResource(R.string.common_all))
                    }
                }
                items(rarityOptions.filterNot { it == CosmeticFilters.All }) { rarity ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = rarity in selectedRarities,
                            onCheckedChange = { checked ->
                                onSelected(
                                    if (checked) {
                                        selectedRarities + rarity
                                    } else {
                                        selectedRarities - rarity
                                    },
                                )
                            },
                        )
                        RarityPill(text = rarity)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}

@Composable
private fun SortDialog(
    selectedSort: CosmeticSort,
    onDismiss: () -> Unit,
    onSelected: (CosmeticSort) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort") },
        text = {
            Column {
                CosmeticSort.entries.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = option == selectedSort,
                            onClick = { onSelected(option) },
                        )
                        Text(option.label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}

@Composable
fun RarityPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = rarityPillColors(
        rarityLabel = text,
        fallback = RarityPillColors(
            background = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    )
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.background,
        contentColor = colors.content,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
fun FilterChipRow(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelected(option) },
                shape = CircleShape,
                label = {
                    Text(
                        text = if (option == CosmeticFilters.All) {
                            stringResource(R.string.common_all)
                        } else {
                            option
                        },
                    )
                },
            )
        }
    }
}

@Composable
fun SectionHeading(
    title: String,
    supporting: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        androidx.compose.foundation.layout.Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        modifier = modifier,
        onClick = {},
        shape = CircleShape,
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(),
    )
}
