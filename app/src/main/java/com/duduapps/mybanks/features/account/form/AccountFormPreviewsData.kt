package com.duduapps.mybanks.features.account.form

import com.duduapps.mybanks.models.Bank

object AccountFormPreviewsData {
    val sampleBanks = listOf(
        Bank(id = 260, name = "Nu Pagamentos S.A.", code = "260"),
        Bank(id = 1, name = "Banco do Brasil S.A.", code = "001"),
        Bank(id = 341, name = "Itaú Unibanco S.A.", code = "341"),
        Bank(id = 104, name = "Caixa Econômica Federal", code = "104"),
        Bank(id = 33, name = "Banco Santander (Brasil) S.A.", code = "033"),
        Bank(id = 237, name = "Banco Bradesco S.A.", code = "237"),
        Bank(id = 77, name = "Banco Inter S.A.", code = "077"),
        Bank(id = 290, name = "PagBank PagSeguro", code = "290"),
        Bank(id = 380, name = "PicPay Serviços S.A.", code = "380"),
        Bank(id = 212, name = "Banco Original S.A.", code = "212"),
    )

    val newAccountState = AccountFormUiState(
        isEditMode = false,
        banks = sampleBanks,
        type = "Conta Corrente",
    )

    val editAccountState = AccountFormUiState(
        accountId = 1L,
        isEditMode = true,
        label = "Nubank Principal",
        selectedBank = sampleBanks[0],
        banks = sampleBanks,
        pixCode = "maria.oliveira@email.com",
        agency = "0001",
        account = "1234567-8",
        operation = "",
        type = "Conta Corrente",
        holder = "Maria Oliveira",
        isLegalAccount = false,
        document = "123.456.789-00",
    )

    val errorState = AccountFormUiState(
        isEditMode = false,
        banks = sampleBanks,
        label = "",
        labelError = "O apelido é obrigatório",
        selectedBank = null,
        bankError = "Selecione um banco",
        agency = "",
        agencyError = "A agência é obrigatória",
        account = "",
        accountError = "A conta é obrigatória",
        holder = "",
        holderError = "O titular é obrigatório",
        document = "",
        documentError = "O documento é obrigatório",
    )
}
