package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CustomerUdhaar
import com.example.ui.components.MandiEmptyState
import com.example.ui.components.MandiTopHeader
import com.example.ui.components.formatRs
import com.example.ui.components.formatTime
import com.example.ui.theme.MandiNumberFont

@Composable
fun UdhaarKhataScreen(
    customers: List<CustomerUdhaar>,
    searchQuery: String,
    totalPendingUdhaar: Double,
    onSearchQueryChange: (String) -> Unit,
    onReceivePayment: (CustomerUdhaar, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCustomerForPayment by remember { mutableStateOf<CustomerUdhaar?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        MandiTopHeader(
            title = stringResource(R.string.udhaar_title),
            subtitle = stringResource(R.string.udhaar_subtitle),
            badgeLabel = "Kul Baqi",
            badgeValue = formatRs(totalPendingUdhaar),
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (customers.isNotEmpty() || searchQuery.isNotEmpty()) {
                    item(key = "udhaar_search") {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Customer ka naam talash karein...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Search saaf karein",
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("udhaar_search_input"),
                        )
                    }
                }

                if (customers.isEmpty()) {
                    item(key = "udhaar_empty") {
                        MandiEmptyState(
                            icon = Icons.Default.CheckCircle,
                            title = if (searchQuery.isNotEmpty()) "Koi customer nahi mila" else "Saara Khata Clear Hai",
                            subtitle = if (searchQuery.isNotEmpty()) {
                                "\"$searchQuery\" ke naam ka koi baqi udhaar nahi hai."
                            } else {
                                stringResource(R.string.no_udhaar_empty)
                            },
                        )
                    }
                } else {
                    items(items = customers, key = { it.customerKey }) { customer ->
                        UdhaarCustomerCard(
                            customer = customer,
                            onPaiseMilGayeClick = { selectedCustomerForPayment = customer },
                        )
                    }
                }

                item(key = "udhaar_bottom_spacer") {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (selectedCustomerForPayment != null) {
        val target = selectedCustomerForPayment!!
        UdhaarPaymentDialog(
            customer = target,
            onDismiss = { selectedCustomerForPayment = null },
            onConfirmPayment = { paidAmount ->
                onReceivePayment(target, paidAmount)
                selectedCustomerForPayment = null
            },
        )
    }
}

@Composable
private fun UdhaarCustomerCard(
    customer: CustomerUdhaar,
    onPaiseMilGayeClick: () -> Unit,
) {
    val initial = customer.displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("udhaar_card_${customer.customerKey}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Column {
                        Text(
                            text = customer.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Aakhri update: ${formatTime(customer.lastUpdated)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "Baqi Udhaar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        Text(
                            text = formatRs(customer.pendingAmount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MandiNumberFont,
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Button(
                onClick = onPaiseMilGayeClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("paise_mil_gaye_${customer.customerKey}"),
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.paise_mil_gaye_button),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun UdhaarPaymentDialog(
    customer: CustomerUdhaar,
    onDismiss: () -> Unit,
    onConfirmPayment: (Double) -> Unit,
) {
    var customAmountText by remember(customer.customerKey) {
        mutableStateOf(
            if (customer.pendingAmount % 1.0 == 0.0) {
                customer.pendingAmount.toLong().toString()
            } else {
                customer.pendingAmount.toString()
            },
        )
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = customer.displayName,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Kul Baqi Udhaar",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        Text(
                            text = formatRs(customer.pendingAmount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MandiNumberFont,
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { onConfirmPayment(customer.pendingAmount) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pay_full_udhaar_button"),
                ) {
                    Text(
                        text = "Poora Udhaar Mila (${formatRs(customer.pendingAmount)})",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            customAmountText = input
                            errorMsg = null
                        }
                    },
                    label = { Text("Kitne Paise Mile (Rs)") },
                    prefix = { Text("Rs ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("udhaar_payment_amount_input"),
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = customAmountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMsg = "Sahi raqam darj karein."
                    } else {
                        onConfirmPayment(amount)
                    }
                },
                modifier = Modifier.testTag("pay_partial_udhaar_button"),
            ) {
                Text("Wasooli Darj Karein")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Band Karein")
            }
        },
    )
}
