package com.duduapps.mybanks.models

import com.duduapps.mybanks.data.local.entities.AccountEntity
import com.google.gson.annotations.SerializedName

data class AccountDto(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("user_id")
    val userId: Long? = null,
    @SerializedName("bank_id")
    val bankId: Int = 0,
    @SerializedName("pix_code")
    val pixCode: String? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("agency")
    val agency: String? = null,
    @SerializedName("account")
    val account: String? = null,
    @SerializedName("operation")
    val operation: String? = null,
    @SerializedName("holder")
    val holder: String? = null,
    @SerializedName("document")
    val document: String? = null,
    @SerializedName("legal_account")
    val legalAccount: Boolean = false,
    @SerializedName("sort")
    val sort: Int = 0,
    @SerializedName("created_at")
    val created: String? = null,
    @SerializedName("updated_at")
    val updated: String? = null,
    @SerializedName("deleted_at")
    val deleted: String? = null,
    @SerializedName("bank_name")
    val bankName: String? = null,
    @SerializedName("bank_code")
    val bankCode: String? = null,
) {
    fun toDomain(bank: Bank? = null): Account = Account(
        id = id,
        pixCode = pixCode.orEmpty(),
        bankId = bankId,
        label = label.orEmpty(),
        agency = agency.orEmpty(),
        account = account.orEmpty(),
        type = type.orEmpty(),
        holder = holder.orEmpty(),
        document = document.orEmpty(),
        legalAccount = legalAccount,
        operation = operation.orEmpty(),
        created = created.orEmpty(),
        updated = updated.orEmpty(),
        deleted = deleted,
        synced = true,
        bank = bank ?: if (!bankName.isNullOrEmpty() || !bankCode.isNullOrEmpty()) {
            Bank(id = bankId, name = bankName.orEmpty(), code = bankCode.orEmpty())
        } else {
            null
        },
    )

    fun toEntity(): AccountEntity = AccountEntity(
        id = id,
        pixCode = pixCode.orEmpty(),
        bankId = bankId,
        label = label.orEmpty(),
        agency = agency.orEmpty(),
        account = account.orEmpty(),
        type = type.orEmpty(),
        holder = holder.orEmpty(),
        document = document.orEmpty(),
        legalAccount = legalAccount,
        operation = operation.orEmpty(),
        created = created.orEmpty(),
        updated = updated.orEmpty(),
        deleted = deleted,
        synced = true,
    )
}
