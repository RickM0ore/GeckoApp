package re.rickmoo.gecko.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import re.rickmoo.gecko.activity.HiddenConfigModel
import re.rickmoo.gecko.datasource.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenScreen(url: String, preferences: Preferences) {

    val viewModel = viewModel {
        return@viewModel HiddenConfigModel(url, preferences)
    }

    val lifecycleScope = LocalLifecycleOwner.current.lifecycleScope
    // 收集 ViewModel 的状态
    val geckoUrls by viewModel.userList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val currentEnv by viewModel.currentEnv.collectAsState()
    val updateChannelState by viewModel.updateChannel.collectAsState()

    // 下拉框的 UI 状态
    var envExpanded by remember { mutableStateOf(false) }
    var channelExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "配置菜单", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            // 下拉框组件
            ExposedDropdownMenuBox(
                expanded = envExpanded,
                onExpandedChange = { envExpanded = !envExpanded }
            ) {
                TextField(
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    readOnly = true,
                    value = currentEnv?.name?.ifBlank { "请选择环境" } ?: "请选择环境",
                    onValueChange = { },
                    label = { Text("环境列表") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = envExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = envExpanded,
                    onDismissRequest = { envExpanded = false }
                ) {
                    geckoUrls.forEach { geckoUrl ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = geckoUrl.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(text = geckoUrl.desc, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                viewModel.setEnv(geckoUrl)
                                envExpanded = false
                                lifecycleScope.launch {
                                    preferences[Preferences.GeckoView.ENV_ID] = geckoUrl.id
                                    preferences[Preferences.GeckoView.DEFAULT_URL] = geckoUrl.url
                                }

                            }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = channelExpanded,
            onExpandedChange = { channelExpanded = !channelExpanded }
        ) {
            TextField(
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                readOnly = true,
                value = updateChannelState.ifEmpty { "请选择更新渠道" },
                onValueChange = { },
                label = { Text("渠道列表") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = envExpanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
            ExposedDropdownMenu(
                expanded = channelExpanded,
                onDismissRequest = { channelExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(text = "Release", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "正式发版通道, 正常选这个", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    onClick = {
                        channelExpanded = false
                        lifecycleScope.launch {
                            preferences[Preferences.App.UPDATE_CHANNEL] = "release"
                            viewModel.setChannel("release")
                        }
                    }
                )
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(text = "Nightly", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "每夜版, 包含正在开发的内容", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    onClick = {
                        channelExpanded = false
                        lifecycleScope.launch {
                            preferences[Preferences.App.UPDATE_CHANNEL] = "nightly"
                            viewModel.setChannel("nightly")
                        }
                    }
                )
            }
        }
    }
}