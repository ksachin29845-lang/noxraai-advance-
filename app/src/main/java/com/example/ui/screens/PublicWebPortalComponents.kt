package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.PublicSeoConfig

data class PublicSearchEntry(
    val id: String,
    val category: String,
    val breadcrumbUrl: String,
    val title: String,
    val snippet: String,
    val metaBadges: List<String>,
    val shareUrl: String,
    val primaryActionLabel: String,
    val onPrimaryAction: () -> Unit
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoogleSerpPreviewCard(
    publicUrl: String,
    seoConfig: PublicSeoConfig,
    developerName: String,
    onOpenUrl: () -> Unit,
    onTestOnGoogle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_serp_preview_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Noxra AI",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = publicUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (seoConfig.allowGoogleIndexing) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = if (seoConfig.allowGoogleIndexing) "Google Index: ON" else "NoIndex",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (seoConfig.allowGoogleIndexing) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = seoConfig.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenUrl)
                    .padding(vertical = 2.dp)
            )

            Text(
                text = seoConfig.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "★ 4.9 • Educational Application • Free / ₹49 VIP • Founder: $developerName",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("English Levels 1–50", "Math 1–50 + AI", "Global Leaderboard", "AI Speaking Lab").forEach { link ->
                    AssistChip(
                        onClick = onOpenUrl,
                        label = { Text(link, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenUrl,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("serp_open_live_link_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Web")
                }
                Button(
                    onClick = onTestOnGoogle,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("serp_test_google_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search Google")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicDirectoryResultCard(
    entry: PublicSearchEntry,
    onCopyUrl: (String) -> Unit,
    onShareUrl: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("public_search_result_${entry.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.breadcrumbUrl,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = entry.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = entry.onPrimaryAction)
            )

            Text(
                text = entry.snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (entry.metaBadges.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.metaBadges.forEach { badge ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = entry.onPrimaryAction,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("result_action_${entry.id}")
                ) {
                    Text(entry.primaryActionLabel, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = { onCopyUrl(entry.shareUrl) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("result_copy_${entry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.cd_public_copy_link),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { onShareUrl(entry.title, entry.shareUrl) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("result_share_${entry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.cd_public_share_link),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ShareableLinkVisualMatrix(
    url: String,
    modifier: Modifier = Modifier
) {
    val fgColor = MaterialTheme.colorScheme.onSurface
    val accentColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surface
    val cdText = stringResource(R.string.cd_public_qr_badge)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .size(116.dp)
            .semantics { contentDescription = cdText }
            .testTag("public_link_visual_matrix")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            val grid = 13
            val cellW = size.width / grid
            val cellH = size.height / grid
            val seed = url.hashCode()

            fun isFinder(r: Int, c: Int): Boolean {
                val topLeft = r in 0..3 && c in 0..3
                val topRight = r in 0..3 && c in (grid - 4) until grid
                val bottomLeft = r in (grid - 4) until grid && c in 0..3
                return topLeft || topRight || bottomLeft
            }

            fun drawFinderSquare(startRow: Int, startCol: Int) {
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(startCol * cellW, startRow * cellH),
                    size = Size(cellW * 3.6f, cellH * 3.6f),
                    cornerRadius = CornerRadius(cellW * 0.7f, cellH * 0.7f)
                )
                drawRoundRect(
                    color = bgColor,
                    topLeft = Offset((startCol + 0.6f) * cellW, (startRow + 0.6f) * cellH),
                    size = Size(cellW * 2.4f, cellH * 2.4f),
                    cornerRadius = CornerRadius(cellW * 0.4f, cellH * 0.4f)
                )
                drawRoundRect(
                    color = fgColor,
                    topLeft = Offset((startCol + 1.15f) * cellW, (startRow + 1.15f) * cellH),
                    size = Size(cellW * 1.3f, cellH * 1.3f),
                    cornerRadius = CornerRadius(cellW * 0.3f, cellH * 0.3f)
                )
            }

            drawFinderSquare(0, 0)
            drawFinderSquare(0, grid - 4)
            drawFinderSquare(grid - 4, 0)

            for (r in 0 until grid) {
                for (c in 0 until grid) {
                    if (isFinder(r, c)) continue
                    val mix = ((r * 31 + c * 17) xor (seed ushr ((r + c) % 16))) and 0xFF
                    if (mix % 3 == 0 || (r == 6 && c % 2 == 0)) {
                        drawRoundRect(
                            color = if ((r + c) % 5 == 0) accentColor else fgColor,
                            topLeft = Offset(c * cellW + cellW * 0.1f, r * cellH + cellH * 0.1f),
                            size = Size(cellW * 0.8f, cellH * 0.8f),
                            cornerRadius = CornerRadius(cellW * 0.25f, cellH * 0.25f)
                        )
                    }
                }
            }
        }
    }
}
