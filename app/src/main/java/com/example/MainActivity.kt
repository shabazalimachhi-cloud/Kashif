package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.MandiDatabase
import com.example.data.repository.MandiRepository
import com.example.ui.screens.RozHisabScreen
import com.example.ui.screens.SaleScreen
import com.example.ui.screens.UdhaarKhataScreen
import com.example.ui.state.MandiViewModel
import com.example.ui.theme.SabziMandiKhataTheme

const val ROUTE_SALE = "sale"
const val ROUTE_UDHAAR = "udhaar_khata"
const val ROUTE_ROZ_HISAB = "roz_hisab"

private data class MandiNavItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

private val navItems = listOf(
    MandiNavItem(
        route = ROUTE_SALE,
        labelRes = R.string.nav_sale,
        selectedIcon = Icons.Filled.PointOfSale,
        unselectedIcon = Icons.Outlined.PointOfSale,
        testTag = "nav_sale",
    ),
    MandiNavItem(
        route = ROUTE_UDHAAR,
        labelRes = R.string.nav_udhaar,
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
        testTag = "nav_udhaar",
    ),
    MandiNavItem(
        route = ROUTE_ROZ_HISAB,
        labelRes = R.string.nav_hisab,
        selectedIcon = Icons.Filled.Assessment,
        unselectedIcon = Icons.Outlined.Assessment,
        testTag = "nav_hisab",
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = MandiDatabase.getInstance(applicationContext)
        val repository = MandiRepository(database)

        setContent {
            SabziMandiKhataTheme {
                val viewModel: MandiViewModel = viewModel(
                    factory = MandiViewModel.Factory(repository),
                )
                SabziMandiApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SabziMandiApp(viewModel: MandiViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val saleForm by viewModel.saleForm.collectAsStateWithLifecycle()
    val vegetables by viewModel.vegetables.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val recentCustomers by viewModel.recentCustomerNames.collectAsStateWithLifecycle()
    val pendingUdhaar by viewModel.pendingUdhaarCustomers.collectAsStateWithLifecycle()
    val udhaarSearch by viewModel.udhaarSearch.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val dailyHisab by viewModel.dailyHisab.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val pendingCount = pendingUdhaar.size

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(item.route) },
                                icon = {
                                    MandiNavIcon(
                                        item = item,
                                        selected = selected,
                                        udhaarCount = if (item.route == ROUTE_UDHAAR) pendingCount else 0,
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                                modifier = Modifier.testTag(item.testTag),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(item.route) },
                                icon = {
                                    MandiNavIcon(
                                        item = item,
                                        selected = selected,
                                        udhaarCount = if (item.route == ROUTE_UDHAAR) pendingCount else 0,
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                modifier = Modifier.testTag(item.testTag),
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_SALE,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(ROUTE_SALE) {
                        SaleScreen(
                            formState = saleForm,
                            vegetables = vegetables,
                            sales = sales,
                            recentCustomers = recentCustomers,
                            totalSaleToday = dailyHisab.totalSale,
                            onCustomerNameChange = viewModel::onCustomerNameChanged,
                            onVegetableSelected = viewModel::onVegetableSelected,
                            onRateChange = viewModel::onRateChanged,
                            onKgChange = viewModel::onKgChanged,
                            onQuickAddKg = viewModel::addQuickKg,
                            onPaidChange = viewModel::onPaidChanged,
                            onFillFullCash = viewModel::fillFullCash,
                            onSubmitSale = viewModel::submitSale,
                            onDeleteSale = viewModel::deleteSale,
                        )
                    }
                    composable(ROUTE_UDHAAR) {
                        UdhaarKhataScreen(
                            customers = pendingUdhaar,
                            searchQuery = udhaarSearch,
                            totalPendingUdhaar = dailyHisab.totalKhataUdhaar,
                            onSearchQueryChange = viewModel::onUdhaarSearchChanged,
                            onReceivePayment = viewModel::receiveUdhaarPayment,
                        )
                    }
                    composable(ROUTE_ROZ_HISAB) {
                        RozHisabScreen(
                            summary = dailyHisab,
                            expenses = expenses,
                            onAddExpense = viewModel::addExpense,
                            onDeleteExpense = viewModel::deleteExpense,
                            onEndDay = viewModel::endDay,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MandiNavIcon(
    item: MandiNavItem,
    selected: Boolean,
    udhaarCount: Int,
) {
    val icon = if (selected) item.selectedIcon else item.unselectedIcon
    val desc = stringResource(item.labelRes)
    if (udhaarCount > 0) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ) {
                    Text(udhaarCount.toString())
                }
            },
        ) {
            Icon(imageVector = icon, contentDescription = desc)
        }
    } else {
        Icon(imageVector = icon, contentDescription = desc)
    }
}
