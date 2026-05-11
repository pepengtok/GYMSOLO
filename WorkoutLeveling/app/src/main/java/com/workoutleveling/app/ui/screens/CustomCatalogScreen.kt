@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.workoutleveling.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workoutleveling.app.data.local.db.ExerciseCatalogEntity
import com.workoutleveling.app.ui.components.AssetImage
import com.workoutleveling.app.ui.catalog.CustomCatalogViewModel

@Composable
fun CustomCatalogScreen(
    viewModel: CustomCatalogViewModel,
    onBack: () -> Unit,
) {
    val entries by viewModel.customEntries.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val filtered = entries.filter { it.displayName.contains(query.trim(), ignoreCase = true) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Katalog Custom") },
                navigationIcon = {
                    TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .padding(24.dp),
            ) {
                AssetImage(
                    assetPath = "image/empty_no_quests.png.png",
                    contentDescription = "Belum ada katalog custom",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentScale = ContentScale.FillWidth,
                )
                Text(
                    "Belum ada latihan custom. Tambahkan dari Gate Session melalui tombol \"Simpan ke katalog saya\".",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Cari latihan custom") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                items(filtered, key = { it.id }) { row ->
                    CustomCatalogRow(
                        row = row,
                        onSave = { id, name, hg60 -> viewModel.renameEntry(id, name, hg60) },
                        onDelete = { id -> viewModel.deleteEntry(id) },
                    )
                }
                if (filtered.isEmpty()) {
                    item {
                        Text(
                            "Tidak ada hasil untuk pencarian ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomCatalogRow(
    row: ExerciseCatalogEntity,
    onSave: (Long, String, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var name by remember(row.id, row.displayName) { mutableStateOf(row.displayName) }
    var isHg60 by remember(row.id, row.isHg60Station) { mutableStateOf(row.isHg60Station) }
    var confirmDelete by remember(row.id) { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama latihan") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(
            modifier = Modifier.padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = isHg60, onCheckedChange = { isHg60 = it })
            Text("Masuk kategori HG60")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { onSave(row.id, name, isHg60) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Simpan")
            }
            OutlinedButton(
                onClick = { confirmDelete = true },
                modifier = Modifier.weight(1f),
            ) {
                Text("Hapus")
            }
        }
        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Hapus latihan?") },
                text = { Text("Entri ini akan dihapus dari katalog custom.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            confirmDelete = false
                            onDelete(row.id)
                        },
                    ) { Text("Hapus") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDelete = false }) { Text("Batal") }
                },
            )
        }
    }
}
