import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.antiscam.data.model.request.ReportRequest

@Composable
fun ReportDialog(
    phoneNumber: String,
    onDismiss: () -> Unit,
    onSubmit: (ReportRequest) -> Unit
) {
    var reporterName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(phoneNumber) }
    var email by remember { mutableStateOf("") }
    var scamType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var evidenceLink by remember { mutableStateOf("") }

    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.Gray,
        errorTextColor = Color.Red,
        focusedContainerColor = Color(0xFF2C2C2E),
        unfocusedContainerColor = Color(0xFF2C2C2E),
        disabledContainerColor = Color.DarkGray,
        errorContainerColor = Color.Red.copy(alpha = 0.1f),
        cursorColor = Color.White,
        errorCursorColor = Color.Red,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Gray,
        errorIndicatorColor = Color.Red,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.Gray,
        disabledLabelColor = Color.DarkGray,
        errorLabelColor = Color.Red,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray,
        disabledPlaceholderColor = Color.DarkGray,
        errorPlaceholderColor = Color.Red
    )

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false) // để dialog rộng theo content
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .imePadding() // đẩy UI lên khi bàn phím hiện
                .verticalScroll(rememberScrollState())
                .background(Color(0xFF1C1C1E)),
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFF1C1C1E)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Báo cáo số điện thoại",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                OutlinedTextField(
                    value = reporterName,
                    onValueChange = { reporterName = it },
                    label = { Text("Tên người báo cáo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = scamType,
                    onValueChange = { scamType = it },
                    label = { Text("Loại lừa đảo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = evidenceLink,
                    onValueChange = { evidenceLink = it },
                    label = { Text("Link bằng chứng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Huỷ", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        onSubmit(
                            ReportRequest(
                                reporterName = reporterName,
                                phone = phone,
                                email = email,
                                scamType = scamType,
                                description = description,
                                evidenceLink = evidenceLink
                            )
                        )
                    }) {
                        Text("Gửi", color = Color.White)
                    }
                }
            }
        }
    }
}
