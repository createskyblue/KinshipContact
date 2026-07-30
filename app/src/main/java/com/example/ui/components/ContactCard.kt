package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Contact
import java.io.File

@Composable
fun ContactCard(
    contact: Contact,
    fontSizeKey: String,
    fontColorKey: String,
    isAdminMode: Boolean,
    isCallLocked: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Font size mapping
    val (nameSize, phoneSize) = when (fontSizeKey) {
        "xlarge" -> 28.sp to 22.sp
        "massive" -> 34.sp to 26.sp
        else -> 22.sp to 18.sp // large
    }

    // Font color mapping
    val textColor = when (fontColorKey) {
        "red" -> Color(0xFFB71C1C)
        "blue" -> Color(0xFF0D47A1)
        "green" -> Color(0xFF1B5E20)
        else -> Color(0xFF1A1C1E) // dark
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
            .testTag("contact_card_${contact.id}")
            .clickable(enabled = !isCallLocked) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Square avatar (top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f) // Square 1:1 ratio
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F3F4)),
                contentAlignment = Alignment.Center
            ) {
                if (!contact.photoPath.isNullOrEmpty() && File(contact.photoPath).exists()) {
                    val avatarFile = File(contact.photoPath)
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(avatarFile)
                            .memoryCacheKey(avatarFile.absolutePath + "_" + avatarFile.lastModified())
                            .diskCacheKey(avatarFile.absolutePath + "_" + avatarFile.lastModified())
                            .crossfade(true)
                            .build(),
                        contentDescription = "联系人头像",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.0f)
                    )
                } else {
                    // Placeholder icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.0f)
                            .background(Color(0xFFECEFF1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFF78909C)
                            )
                        }
                    }
                }

                if (isAdminMode) {
                    // Admin badge overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        color = Color(0xFF1976D2),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Name (centered)
            Text(
                text = contact.name.ifBlank { "未命名" },
                fontSize = nameSize,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Phone Number (centered)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size((phoneSize.value + 2).dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = contact.phoneNumber,
                    fontSize = phoneSize,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Admin reorder controls
            if (isAdminMode) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "向前移",
                            tint = if (canMoveUp) Color(0xFF1976D2) else Color.LightGray
                        )
                    }

                    Text(
                        text = "排序",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "向后移",
                            tint = if (canMoveDown) Color(0xFF1976D2) else Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
