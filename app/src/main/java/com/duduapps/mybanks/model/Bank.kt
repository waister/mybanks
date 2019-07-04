package com.duduapps.mybanks.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Bank : RealmObject() {
    @PrimaryKey
    var id: Int = 0
    var name: String = ""
    var code: String = ""
}