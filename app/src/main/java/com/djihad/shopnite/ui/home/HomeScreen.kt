package com.djihad.shopnite.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.djihad.shopnite.model.AccountType
import com.djihad.shopnite.model.BrSummary
import com.djihad.shopnite.ui.components.ErrorCard
import com.djihad.shopnite.ui.components.SectionHeading
import com.djihad.shopnite.util.Formatters

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSaveProfile: (String, AccountType) -> Unit,
    onRefresh: () -> Unit,
) {
    var playerName by rememberSaveable(uiState.settings.playerName) {
        mutableStateOf(uiState.settings.playerName)
    }
    var accountType by rememberSaveable(uiState.settings.accountType) {
        mutableStateOf(uiState.settings.accountType)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeading(
                    title = "Home",
                    supporting = "Your BR snapshot and the latest Fortnite news.",
                )
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh home",
                    )
                }
            }
        }

        item {
            ProfileCard(
                playerName = playerName,
                accountType = accountType,
                apiKeyConfigured = uiState.settings.apiKey.isNotBlank(),
                onPlayerNameChange = { playerName = it },
                onAccountTypeChange = { accountType = it },
                onSave = { onSaveProfile(playerName, accountType) },
            )
        }

        item {
            when {
                uiState.isLoadingSummary -> {
                    LoadingCard("Loading BR summary...")
                }
                uiState.summary != null -> {
                    SummaryCard(summary = uiState.summary)
                }
                uiState.settings.apiKey.isBlank() -> {
                    ErrorCard(message = "Add your Fortnite API key in Settings to load BR stats.")
                }
                uiState.settings.playerName.isBlank() -> {
                    ErrorCard(message = "Set a username above to load your Battle Royale summary.")
                }
            }
        }

        if (uiState.errorMessage != null) {
            item {
                ErrorCard(message = uiState.errorMessage)
            }
        }

        item {
            SectionHeading(
                title = "BR News",
                supporting = if (uiState.isLoadingNews) "Pulling the latest news cards..." else "Styled like the in-game feed.",
            )
        }

        if (uiState.isLoadingNews && uiState.news.isEmpty()) {
            item {
                LoadingCard("Loading Battle Royale news...")
            }
        } else {
            item {
                NewsPager(news = uiState.news)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileCard(
    playerName: String,
    accountType: AccountType,
    apiKeyConfigured: Boolean,
    onPlayerNameChange: (String) -> Unit,
    onAccountTypeChange: (AccountType) -> Unit,
    onSave: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Player header",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (apiKeyConfigured) {
                    "Set your username and platform for the BR summary."
                } else {
                    "Set your username here, then add your Fortnite API key in Settings to unlock BR stats."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = playerName,
                onValueChange = onPlayerNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Fortnite username") },
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AccountType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = accountType == type,
                        onClick = { onAccountTypeChange(type) },
                        shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = AccountType.entries.size,
                        ),
                        label = { Text(type.label) },
                    )
                }
            }
            Button(
                onClick = onSave,
                enabled = playerName.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save profile")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryCard(summary: BrSummary) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = summary.playerName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = buildString {
                    append(summary.accountType.label)
                    if (summary.battlePassLevel != null) {
                        append(" - BP ")
                        append(summary.battlePassLevel)
                    }
                    if (summary.battlePassProgress != null) {
                        append(" - ")
                        append(summary.battlePassProgress)
                        append("% progress")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                summary.statTiles.forEach { stat ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .width(108.dp)
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stat.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stat.value,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NewsPager(news: List<com.djihad.shopnite.model.NewsCard>) {
    if (news.isEmpty()) {
        ErrorCard(message = "No news cards are available right now.")
        return
    }

    val pagerState = rememberPagerState(pageCount = { news.size })

    HorizontalPager(
        state = pagerState,
        pageSpacing = 16.dp,
    ) { page ->
        val item = news[page]
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                ),
                            ),
                        ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (item.tabTitle.isNotBlank()) {
                        Text(
                            text = item.tabTitle.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingCard(message: String) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
