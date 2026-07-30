package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Contact
import com.example.utils.SystemContactsUtils

@Composable
fun SystemContactsImportDialog(
    onImportConfirmed: (List<Contact>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var systemContactsList by remember { mutableStateOf<List<Contact>>(emptyList()) }
    val selectedContacts = remember { mutableStateListOf<Contact>() }

    LaunchedEffect(Unit) {
        systemContactsList = SystemContactsUtils.getSystemContacts(context)
        selectedContacts.addAll(systemContactsList) // Select all by default
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("system_import_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "批量从系统通讯录导入",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                Text(
                    text = "系统权限仅能读取姓名和手机号，无头像",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                if (systemContactsList.isEmpty()) {
                    BoxContainerMessage("未找到手机通讯录联系人，或未获得通讯录权限")
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "已选 ${selectedContacts.size} / ${systemContactsList.size} 位",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row {
                            Text(
                                text = "全选",
                                fontSize = 13.sp,
                                color = Color(0xFF1976D2),
                                modifier = Modifier
                                    .clickable {
                                        selectedContacts.clear()
                                        selectedContacts.addAll(systemContactsList)
                                    }
                                    .padding(horizontal = 6.dp)
                            )
                            Text(
                                text = "清空",
                                fontSize = 13.sp,
                                color = Color.Red,
                                modifier = Modifier
                                    .clickable { selectedContacts.clear() }
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        items(systemContactsList) { item ->
                            val isChecked = selectedContacts.contains(item)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedContacts.remove(item)
                                        else selectedContacts.add(item)
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (it) selectedContacts.add(item)
                                        else selectedContacts.remove(item)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1976D2))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(text = item.phoneNumber, fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("取消", fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { onImportConfirmed(selectedContacts.toList()) },
                        enabled = selectedContacts.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("确认导入 (${selectedContacts.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxContainerMessage(msg: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = msg, fontSize = 14.sp, color = Color.Gray)
    }
}
