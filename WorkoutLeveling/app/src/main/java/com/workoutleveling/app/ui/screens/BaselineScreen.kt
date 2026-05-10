@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.workoutleveling.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.workoutleveling.app.ui.baseline.BaselineViewModel

@Composable
fun BaselineScreen(
    viewModel: BaselineViewModel,
    onDone: () -> Unit,
) {
    var weight by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Baseline tubuh") },
                navigationIcon = {
                    TextButton(
                        onClick = { viewModel.skip(onDone) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("Lewati")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                "Opsional — tidak mengunci latihan. Kamu bisa isi sekarang atau lewati dan lengkapi nanti.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it.filter { ch -> ch.isDigit() || ch == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Berat (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = waist,
                onValueChange = { waist = it.filter { ch -> ch.isDigit() || ch == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Lingkar pinggang (cm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Catatan (opsional)") },
                minLines = 2,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.saveBaseline(weight, waist, notes, onDone) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text("Simpan baseline")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { viewModel.skip(onDone) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text("Lewati, isi nanti")
            }
        }
    }
}
