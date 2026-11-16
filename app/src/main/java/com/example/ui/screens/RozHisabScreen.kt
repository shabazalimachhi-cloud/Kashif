package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ExpenseEntity
import com.example.ui.components.MandiTopHeader
import com.example.ui.components.formatDateHeader
import com.example.ui.components.formatRs
import com.example.ui.components.formatTime
import com.example.ui.state.DailyHisabSummary
import com.example.ui.theme.MandiNumberFont

@Composable
fun RozHisabScreen(
    summary: DailyHisabSummary,
    expenses: List<ExpenseEntity>,
    onAddExpense: (category: String, note: String, amount: Double) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onEndDay: (clearUdhaarKhata: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var selectedCategory by remember { mutableStateOf("Gaari") }
    var expenseAmountInput by remember { mutableStateOf("") }
    var expenseNoteInput by remember { mutableStateOf("") }
    var expenseError by remember { mutableStateOf<String?>(null) }

    var showDinKhatamDialog by remember { mutableStateOf(false) }
    var clearUdhaarToo by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        MandiTopHeader(
            title = stringResource(R.string.hisab_title),
            subtitle = stringResource(R.string.hisab_subtitle),
            badgeLabel = "Tareekh",
            badgeValue = formatDateHeader(),
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // 1. Hero Munafa Card (Sale - Kharcha)
                item(key = "munafa_hero") {
                    val isProfit = summary.munafa >= 0.0
                    val heroBg = if (isProfit) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                    val heroFg = if (isProfit) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }

                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = heroBg,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = heroFg,
                                    modifier = Modifier.size(22.dp),
                                )
                                Text(
                                    text = "Aaj ka Munafa (Sale - Kharcha)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = heroFg.copy(alpha = 0.92f),
                                )
                            }

                            Text(
                                text = formatRs(summary.munafa),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = MandiNumberFont,
                                ),
                                color = heroFg,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("hisab_munafa"),
                            )

                            Text(
                                text = "Sale (${formatRs(summary.totalSale)}) − Kharcha (${formatRs(summary.totalKharcha)})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = MandiNumberFont,
                                ),
                                color = heroFg.copy(alpha = 0.85f),
                            )
                        }
                    }
                }

                // 2. Total Sale, Total Cash Mila, Total Udhaar Cards
                item(key = "summary_metrics") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HisabMetricRowCard(
                            icon = Icons.Default.Storefront,
                            label = "Aaj ki Total Sale",
                            subLabel = "${summary.saleCount} sales darj hain",
                            value = formatRs(summary.totalSale),
                            containerColor = MaterialTheme.colorScheme.surface,
                            accentColor = MaterialTheme.colorScheme.primary,
                            valueTag = "hisab_total_sale",
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HisabCompactTile(
                                icon = Icons.Default.Payments,
                                label = "Total Cash Mila",
                                value = formatRs(summary.totalCashMila),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                valueTag = "hisab_total_cash",
                                modifier = Modifier.weight(1f),
                            )

                            HisabCompactTile(
                                icon = Icons.Default.AccountBalanceWallet,
                                label = "Total Udhaar",
                                value = formatRs(summary.totalUdhaarToday),
                                subNote = if (summary.totalKhataUdhaar > summary.totalUdhaarToday + 0.01) {
                                    "Khata: ${formatRs(summary.totalKhataUdhaar)}"
                                } else null,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                valueTag = "hisab_total_udhaar",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // 3. Aaj ka Kharcha (Gaari, Mazdoori) Input Card
                item(key = "kharcha_entry_card") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        tonalElevation = 1.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = "Aaj ka Kharcha",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Text(
                                    text = formatRs(summary.totalKharcha),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = MandiNumberFont,
                                    ),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("hisab_total_kharcha"),
                                )
                            }

                            // Breakdown pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "Gaari: ${formatRs(summary.gaariKharcha)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = MandiNumberFont),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "Mazdoori: ${formatRs(summary.mazdooriKharcha)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = MandiNumberFont),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            // Category selector chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf("Gaari", "Mazdoori", "Doosra").forEach { cat ->
                                    val selected = selectedCategory == cat
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedCategory = cat }
                                            .testTag("expense_cat_${cat.lowercase()}"),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = cat,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (selected) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                OutlinedTextField(
                                    value = expenseAmountInput,
                                    onValueChange = { input ->
                                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                            expenseAmountInput = input
                                            expenseError = null
                                        }
                                    },
                                    label = { Text("$selectedCategory (Rs)") },
                                    prefix = { Text("Rs ") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next,
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("expense_amount_input"),
                                )

                                OutlinedTextField(
                                    value = expenseNoteInput,
                                    onValueChange = { expenseNoteInput = it },
                                    label = { Text("Note (ikhtiyari)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("expense_note_input"),
                                )
                            }

                            if (expenseError != null) {
                                Text(
                                    text = expenseError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    val amount = expenseAmountInput.toDoubleOrNull()
                                    if (amount == null || amount <= 0.0) {
                                        expenseError = "Kharcha ki raqam likhein."
                                    } else {
                                        onAddExpense(selectedCategory, expenseNoteInput, amount)
                                        expenseAmountInput = ""
                                        expenseNoteInput = ""
                                        expenseError = null
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("add_expense_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kharcha Add Karo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                // Today's Expenses List
                if (expenses.isNotEmpty()) {
                    item(key = "expenses_list_header") {
                        Text(
                            text = "Aaj ke Kharche (${expenses.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    items(items = expenses, key = { it.id }) { expense ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                    )
                                    Column {
                                        Text(
                                            text = if (expense.note.isNotBlank()) {
                                                "${expense.category} (${expense.note})"
                                            } else {
                                                expense.category
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = formatTime(expense.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatRs(expense.amount),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = MandiNumberFont,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    IconButton(
                                        onClick = { onDeleteExpense(expense) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("delete_expense_${expense.id}"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Kharcha delete karein",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Din Khatam Button
                item(key = "din_khatam_section") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                clearUdhaarToo = false
                                showDinKhatamDialog = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .testTag("din_khatam_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${stringResource(R.string.din_khatam_button)} (Sab Clear Karein)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    if (showDinKhatamDialog) {
        AlertDialog(
            onDismissRequest = { showDinKhatamDialog = false },
            title = { Text("Din Khatam Karein?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Aaj ki saari sales aur kharcha clear ho jayega aur naya hisab shuru ho jayega.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { clearUdhaarToo = !clearUdhaarToo },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = clearUdhaarToo,
                                onCheckedChange = { clearUdhaarToo = it },
                                modifier = Modifier.testTag("clear_udhaar_checkbox"),
                            )
                            Text(
                                text = "Udhaar Khata bhi saaf karein",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEndDay(clearUdhaarToo)
                        showDinKhatamDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier.testTag("confirm_din_khatam_button"),
                ) {
                    Text("Haan, Sab Clear Karein")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDinKhatamDialog = false }) {
                    Text("Nahi")
                }
            },
        )
    }
}

@Composable
private fun HisabMetricRowCard(
    icon: ImageVector,
    label: String,
    subLabel: String,
    value: String,
    containerColor: Color,
    accentColor: Color,
    valueTag: String,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.14f),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                        )
                    }
                }
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = MandiNumberFont),
                color = accentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag(valueTag),
            )
        }
    }
}

@Composable
private fun HisabCompactTile(
    icon: ImageVector,
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    valueTag: String,
    modifier: Modifier = Modifier,
    subNote: String? = null,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.88f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = MandiNumberFont),
                color = contentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag(valueTag),
            )
            if (subNote != null) {
                Text(
                    text = subNote,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MandiNumberFont),
                    color = contentColor.copy(alpha = 0.8f),
                )
            }
        }
    }
}
