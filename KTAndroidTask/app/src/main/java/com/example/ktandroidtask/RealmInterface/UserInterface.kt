package com.example.ktandroidtask.RealmInterface

import com.example.ktandroidtask.pojoModels.User
import io.realm.Realm
import io.realm.RealmResults


interface UserInterface {

    fun addUser(realm: Realm, user: User): Boolean
    fun delUser(realm: Realm, _ID: Int): Boolean
    fun editUser(realm: Realm, user: User): Boolean
    fun getUser(realm: Realm, email: String): User?
    fun userLogin(realm: Realm, email: String,password:String): RealmResults<User>
    fun removeLastUser(realm: Realm)
}