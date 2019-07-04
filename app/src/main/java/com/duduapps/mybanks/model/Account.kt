package com.duduapps.mybanks.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Account : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var bankId: Int = 0
    var label: String = ""
    var agency: String = ""
    var account: String = ""
    var type: String = ""
    var holder: String = ""
    var document: String = ""
    var legalAccount: Boolean = false
    var operation: String = ""
    var created: String = ""
    var updated: String = ""
    var deleted: String? = null
    var synced: Boolean = false
    var bank: Bank? = null
}