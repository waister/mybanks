package com.duduapps.mybanks.features.account.detail

import com.duduapps.mybanks.models.Account
import com.duduapps.mybanks.models.Bank

object AccountDetailPreviewsData {
    val sampleBank = Bank(
        id = 260,
        name = "Nu Pagamentos S.A.",
        code = "260",
    )

    val sampleBankCef = Bank(
        id = 104,
        name = "Caixa Econômica Federal",
        code = "104",
    )

    val sampleIndividualAccount = Account(
        id = 1,
        label = "Nubank Principal",
        bankId = 260,
        bank = sampleBank,
        agency = "0001",
        account = "1234567-8",
        type = "Conta Corrente",
        holder = "Maria Oliveira",
        document = "123.456.789-00",
        legalAccount = false,
        pixCode = "maria.oliveira@email.com",
        operation = "",
        synced = true,
    )

    val sampleLegalAccount = Account(
        id = 2,
        label = "Caixa Poupança Empresa",
        bankId = 104,
        bank = sampleBankCef,
        agency = "0456",
        account = "998877-6",
        type = "Poupança",
        holder = "Tech Solutions LTDA",
        document = "12.345.678/0001-90",
        legalAccount = true,
        pixCode = "12.345.678/0001-90",
        operation = "013",
        synced = true,
    )

    val individualAccountState = AccountDetailUiState(
        isLoading = false,
        account = sampleIndividualAccount,
    )

    val legalAccountState = AccountDetailUiState(
        isLoading = false,
        account = sampleLegalAccount,
    )
}
