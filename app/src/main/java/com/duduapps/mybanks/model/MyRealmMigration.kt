package com.duduapps.mybanks.model

import com.duduapps.mybanks.util.appLog
import io.realm.DynamicRealm
import io.realm.RealmMigration

class MyRealmMigration : RealmMigration {

    companion object {
        const val TAG = "RealmMigration"
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        appLog(TAG, "Realm scheme version changes from version '$oldVersion' to '$newVersion'")

        var version = oldVersion

        val schema = realm.schema

        if (version == 0L) {
            schema.get("Account")!!
                .addField("pixCode", String::class.java)
            version++
        }

        appLog(TAG, "Realm scheme migration finish with count version: '$version'")
    }
}