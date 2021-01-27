package com.example.ktandroidtask

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration


class MyApplication : Application() {

//    override fun onCreate() {
//        super.onCreate()


//        Realm.init(applicationContext)
//        val config = RealmConfiguration.Builder()
//            .deleteRealmIfMigrationNeeded()
//            .build()
//        Realm.setDefaultConfiguration(config)
//    }

    override fun onCreate() {
        super.onCreate()
        Realm.init(applicationContext)
        val c = RealmConfiguration.Builder()
        c.name("TKAndroidTask")
        c.deleteRealmIfMigrationNeeded()
        Realm.setDefaultConfiguration(c.build())
    }
}