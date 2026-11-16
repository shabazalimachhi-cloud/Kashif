package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.Sale
import com.example.data.local.VegetableRateEntity
import com.example.ui.components.MandiEmptyState
import com.example.ui.components.MandiTopHeader
import com.example.ui.components.formatKg
import com.example.ui.components.formatRs
import com.example.ui.components.formatTime
import com.example.ui.components.vegetableAccentColor
import com.example.ui.state.SaleFormState
import com.example.ui.theme.MandiNumberFont

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaleScreen(
    formState: SaleFormState,
    vegetables: List<VegetableRateEntity>,
    sales: List<Sale>,
    recentCustomers: List<String>,
    totalSaleToday: Double,
    onCustomerNameChange: (String) -> Unit,
    onVegetableSelected: (VegetableRateEntity) -> Unit,
    onRateChange: (String) -> Unit,
    onKgChange: (String) -> Unit,
    onQuickAddKg: (Double) -> Unit,
    onPaidChange: (String) -> Unit,
    onFillFullCash: () -> Unit,
    onSubmitSale: () -> Unit,
    onDeleteSale: (Sale) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        MandiTopHeader(
            title = stringResource(R.string.app_name),
            subtitle = stringResource(R.string.sale_subtitle),
            badgeLabel = "Aaj ki Sale",
            badgeValue = formatRs(totalSaleToday),
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
                item(key = "sale_form") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                        tonalElevation = 1.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            // 1. Customer Name
                            OutlinedTextField(
                                value = formState.customerName,
                                onValueChange = onCustomerNameChange,
                                label = { Text(stringResource(R.string.customer_name_label)) },
                                placeholder = { Text(stringResource(R.string.customer_name_hint)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                },
                                trailingIcon = {
                                    if (formState.customerName.isNotEmpty()) {
                                        IconButton(onClick = { onCustomerNameChange("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Naam saaf karein",
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next,
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("customer_name_input"),
                            )

                            if (recentCustomers.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    recentCustomers.forEach { name ->
                                        SuggestionChip(
                                            onClick = { onCustomerNameChange(name) },
                                            label = { Text(name, style = MaterialTheme.typography.labelMedium) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            ),
                                            border = null,
                                        )
                                    }
                                }
                            }

                            // 2. Sabzi Select
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(R.string.select_sabzi_label),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    vegetables.forEach { veg ->
                                        val isSelected = veg.name == formState.selectedVegetable
                                        SabziChipItem(
                                            vegetable = veg,
                                            selected = isSelected,
                                            onClick = { onVegetableSelected(veg) },
                                        )
                                    }
                                }
                            }

                            // 3. Rate + KG Inputs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                OutlinedTextField(
                                    value = formState.rateInput,
                                    onValueChange = onRateChange,
                                    label = { Text(stringResource(R.string.rate_label)) },
                                    prefix = { Text("Rs ") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next,
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("rate_input"),
                                )

                                OutlinedTextField(
                                    value = formState.kgInput,
                                    onValueChange = onKgChange,
                                    label = { Text(stringResource(R.string.kg_label)) },
                                    suffix = { Text("kg") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next,
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("kg_input"),
                                )
                            }

                            // Quick KG Buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf(1.0, 2.0, 5.0, 10.0, 20.0).forEach { kgStep ->
                                    Surface(
                                        onClick = { onQuickAddKg(kgStep) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier
                                            .height(40.dp)
                                            .testTag("quick_kg_${kgStep.toInt()}"),
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = "+${kgStep.toInt()} kg",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontFamily = MandiNumberFont,
                                                ),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. Live Total Card (Rate x KG)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text(
                                            text = "Kul Raqam (Rate × KG)",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                        Text(
                                            text = "${formatRs(formState.rateValue)} × ${formatKg(formState.kgValue)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = MandiNumberFont,
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                                        )
                                    }
                                    Text(
                                        text = formatRs(formState.totalAmount),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = MandiNumberFont,
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.testTag("live_total_text"),
                                    )
                                }
                            }

                            // 5. Kitne Paise Diye + Poora Cash button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedTextField(
                                    value = formState.paidInput,
                                    onValueChange = onPaidChange,
                                    label = { Text(stringResource(R.string.paid_label)) },
                                    prefix = { Text("Rs ") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() },
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("paid_input"),
                                )

                                FilledTonalButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        onFillFullCash()
                                    },
                                    enabled = formState.totalAmount > 0.0,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .height(56.dp)
                                        .testTag("full_cash_button"),
                                ) {
                                    Text(
                                        text = stringResource(R.string.full_cash_button),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            }

                            // Udhaar / Cash Status Preview
                            if (formState.totalAmount > 0.0) {
                                val hasUdhaar = formState.udhaarPreview > 0.01
                                val badgeBg = if (hasUdhaar) {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                }
                                val badgeFg = if (hasUdhaar) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                }
                                val statusText = when {
                                    hasUdhaar -> "Baqi Udhaar: ${formatRs(formState.udhaarPreview)}"
                                    formState.changePreview > 0.01 ->
                                        "Clear (Wapsi: ${formatRs(formState.changePreview)})"
                                    else -> "Poora Cash Clear"
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = badgeBg,
                                ) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = MandiNumberFont,
                                        ),
                                        color = badgeFg,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    )
                                }
                            }

                            if (formState.errorMessage != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = formState.errorMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    )
                                }
                            }

                            // 6. Big Green Sale Add Karo Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    onSubmitSale()
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .testTag("add_sale_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.add_sale_button),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                // Today's Sales Header
                item(key = "sales_list_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${stringResource(R.string.todays_sales_header)} (${sales.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        if (sales.isNotEmpty()) {
                            Text(
                                text = formatRs(totalSaleToday),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = MandiNumberFont,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                if (sales.isEmpty()) {
                    item(key = "sales_empty") {
                        MandiEmptyState(
                            icon = Icons.Default.ReceiptLong,
                            title = "Aaj koi sale nahi",
                            subtitle = stringResource(R.string.no_sales_empty),
                        )
                    }
                } else {
                    items(items = sales, key = { it.id }) { sale ->
                        SaleHistoryCard(
                            sale = sale,
                            onRequestDelete = { saleToDelete = sale },
                        )
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (saleToDelete != null) {
        val target = saleToDelete!!
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text("Sale Delete Karein?") },
            text = {
                Text(
                    "${target.customerName} ki ${target.vegetableName} (${formatRs(target.totalAmount)}) ki sale hata di jayegi.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSale(target)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Delete Karein")
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text("Nahi")
                }
            },
        )
    }
}

@Composable
private fun SabziChipItem(
    vegetable: VegetableRateEntity,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    }
    val fgColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val border = if (selected) {
        null
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    }
    val rateLabel = if (vegetable.ratePerKg % 1.0 == 0.0) {
        "Rs ${vegetable.ratePerKg.toLong()}"
    } else {
        "Rs ${vegetable.ratePerKg}"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = border,
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("sabzi_chip_${vegetable.name.lowercase()}"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = fgColor,
                    modifier = Modifier.size(16.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(vegetableAccentColor(vegetable.name)),
                )
            }
            Text(
                text = vegetable.name,
                style = MaterialTheme.typography.titleSmall,
                color = fgColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            )
            Text(
                text = rateLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MandiNumberFont),
                color = fgColor.copy(alpha = if (selected) 0.9f else 0.72f),
            )
        }
    }
}

@Composable
private fun SaleHistoryCard(
    sale: Sale,
    onRequestDelete: () -> Unit,
) {
    val hasUdhaar = sale.udhaarAmount > 0.01
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sale_card_${sale.id}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(vegetableAccentColor(sale.vegetableName)),
                    )
                    Column {
                        Text(
                            text = sale.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${sale.vegetableName} • ${formatTime(sale.timestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                IconButton(
                    onClick = onRequestDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_sale_${sale.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Sale delete karein",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Scale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "${formatKg(sale.weightKg)} × ${formatRs(sale.ratePerKg)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = MandiNumberFont),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatRs(sale.totalAmount),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = MandiNumberFont),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Mila: ${formatRs(sale.paidAmount)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = MandiNumberFont),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (hasUdhaar) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                        ) {
                            Text(
                                text = if (hasUdhaar) "Udhaar ${formatRs(sale.udhaarAmount)}" else "Clear",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MandiNumberFont),
                                color = if (hasUdhaar) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
