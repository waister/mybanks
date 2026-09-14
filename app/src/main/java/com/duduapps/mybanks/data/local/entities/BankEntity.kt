package com.duduapps.mybanks.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duduapps.mybanks.models.Bank

@Entity(tableName = "banks")
data class BankEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val code: String,
) {
    fun toDomain(): Bank = Bank(
        id = id,
        name = name,
        code = code,
    )
}

fun Bank.toEntity(): BankEntity = BankEntity(
    id = id,
    name = name,
    code = code,
)
