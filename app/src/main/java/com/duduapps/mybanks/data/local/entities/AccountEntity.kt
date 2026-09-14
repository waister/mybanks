package com.duduapps.mybanks.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duduapps.mybanks.models.Account
import com.duduapps.mybanks.models.Bank

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: Long,
    val pixCode: String,
    val bankId: Int,
    val label: String,
    val agency: String,
    val account: String,
    val type: String,
    val holder: String,
    val document: String,
    val legalAccount: Boolean,
    val operation: String,
    val created: String,
    val updated: String,
    val deleted: String?,
    val synced: Boolean,
) {
    fun toDomain(bank: Bank? = null): Account = Account(
        id = id,
        pixCode = pixCode,
        bankId = bankId,
        label = label,
        agency = agency,
        account = account,
        type = type,
        holder = holder,
        document = document,
        legalAccount = legalAccount,
        operation = operation,
        created = created,
        updated = updated,
        deleted = deleted,
        synced = synced,
        bank = bank,
    )
}

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    pixCode = pixCode,
    bankId = bankId,
    label = label,
    agency = agency,
    account = account,
    type = type,
    holder = holder,
    document = document,
    legalAccount = legalAccount,
    operation = operation,
    created = created,
    updated = updated,
    deleted = deleted,
    synced = synced,
)
