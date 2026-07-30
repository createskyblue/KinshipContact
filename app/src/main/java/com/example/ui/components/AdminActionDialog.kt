package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Contact

enum class AdminActionType {
    EDIT, DELETE
}

@Composable
fun AdminActionDialog(
    contact: Contact,
    onProceedEdit: () -> Unit,
    onProceedDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    // Default selected: EDIT
    var selectedAction by remember { mutableStateOf(AdminActionType.EDIT) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("admin_action_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "联系人操作选择",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "目标: ${contact.name} (${contact.phoneNumber})",
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Left-Right Radio Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit option
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = (selectedAction == AdminActionType.EDIT),
                                onClick = { selectedAction = AdminActionType.EDIT }
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedAction == AdminActionType.EDIT),
                            onClick = { selectedAction = AdminActionType.EDIT },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1976D2))
                        )
                        Text(
                            text = "✏️ 编辑联系人",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Delete option
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = (selectedAction == AdminActionType.DELETE),
                                onClick = { selectedAction = AdminActionType.DELETE }
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedAction == AdminActionType.DELETE),
                            onClick = { selectedAction = AdminActionType.DELETE },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F))
                        )
                        Text(
                            text = "🗑️ 删除联系人",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("取消", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (selectedAction == AdminActionType.EDIT) {
                                onProceedEdit()
                            } else {
                                onProceedDelete()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedAction == AdminActionType.EDIT) Color(0xFF1976D2) else Color(0xFFD32F2F)
                        ),
                        modifier = Modifier.testTag("admin_action_confirm")
                    ) {
                        Text("确定", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
