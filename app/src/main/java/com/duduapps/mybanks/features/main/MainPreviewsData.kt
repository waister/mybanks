package com.duduapps.mybanks.features.main

import com.duduapps.mybanks.models.Account
import com.duduapps.mybanks.models.Bank

object MainPreviewsData {
    val sampleBanks = listOf(
        Bank(id = 260, name = "Nu Pagamentos S.A.", code = "260"),
        Bank(id = 1, name = "Banco do Brasil S.A.", code = "001"),
        Bank(id = 341, name = "Itaú Unibanco S.A.", code = "341"),
        Bank(id = 104, name = "Caixa Econômica Federal", code = "104"),
    )

    val sampleAccounts = listOf(
        Account(
            id = 1,
            label = "Nubank Principal",
            bankId = 260,
            bank = sampleBanks[0],
            agency = "0001",
            account = "1234567-8",
            type = "Conta Corrente",
            holder = "Maria Oliveira",
            document = "123.456.789-00",
            legalAccount = false,
            pixCode = "maria.oliveira@email.com",
            operation = "",
            synced = true,
        ),
        Account(
            id = 2,
            label = "Banco do Brasil",
            bankId = 1,
            bank = sampleBanks[1],
            agency = "1234-5",
            account = "98765-4",
            type = "Conta Corrente",
            holder = "Maria Oliveira",
            document = "123.456.789-00",
            legalAccount = false,
            pixCode = "+5511999998888",
            operation = "",
            synced = true,
        ),
        Account(
            id = 3,
            label = "Caixa Poupança",
            bankId = 104,
            bank = sampleBanks[3],
            agency = "0456",
            account = "112233-4",
            type = "Poupança",
            holder = "Empresa Silva LTDA",
            document = "12.345.678/0001-90",
            legalAccount = true,
            pixCode = "12.345.678/0001-90",
            operation = "013",
            synced = false,
        ),
        Account(
            id = 4,
            label = "Itaú Empresas",
            bankId = 341,
            bank = sampleBanks[2],
            agency = "4321",
            account = "55443-2",
            type = "Conta Corrente",
            holder = "Empresa Silva LTDA",
            document = "12.345.678/0001-90",
            legalAccount = true,
            pixCode = "financeiro@empresa.com.br",
            operation = "",
            synced = true,
        ),
    )

    val emptyState = MainUiState(
        accounts = emptyList(),
        filteredAccounts = emptyList(),
        isLogged = false,
    )

    val populatedLoggedState = MainUiState(
        accounts = sampleAccounts,
        filteredAccounts = sampleAccounts,
        isLogged = true,
    )

    val populatedNotLoggedState = MainUiState(
        accounts = sampleAccounts,
        filteredAccounts = sampleAccounts,
        isLogged = false,
    )

    val searchActiveState = MainUiState(
        accounts = sampleAccounts,
        filteredAccounts = listOf(sampleAccounts[0]),
        searchQuery = "Nubank",
        isSearchActive = true,
        isLogged = true,
    )
}
