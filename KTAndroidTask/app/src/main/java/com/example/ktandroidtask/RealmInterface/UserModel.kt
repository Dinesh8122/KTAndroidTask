package com.example.ktandroidtask.RealmInterface

import com.example.ktandroidtask.pojoModels.User
import io.realm.Realm
import io.realm.RealmResults

class UserModel : UserInterface {

    override fun removeLastUser(realm: Realm) {
        realm.beginTransaction()
        getLastUser(realm)
        realm.commitTransaction()
    }

    override fun addUser(realm: Realm, user: User): Boolean {
        try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(user)
            realm.commitTransaction()
            return true
        } catch (e: Exception) {
            println(e)
            return false
        }
    }

    override fun delUser(realm: Realm, _ID: Int): Boolean {
        try {
            realm.beginTransaction()
            realm.where(User :: class.java).equalTo("_ID", _ID).findFirst()
            realm.commitTransaction()
            return true
        } catch (e: Exception) {
            println(e)
            return false
        }
    }

    override fun editUser(realm: Realm, user: User): Boolean {
        try {
            realm.beginTransaction()
            realm.copyToRealm(user)
            realm.commitTransaction()
            return true
        } catch (e: Exception) {
            println(e)
            return false
        }
    }


    override fun getUser(realm: Realm, email: String): User? {
        return realm.where(User::class.java).equalTo("email", email).findFirst()
    }

    override fun userLogin(realm: Realm, email: String,password:String): RealmResults<User> {
        return realm.where(User::class.java).
                equalTo("email", email)
                .and()
                .equalTo("password", password).findAll()
    }

    fun getLastUser(realm: Realm): User? {
        return realm.where(User::class.java).findAll().last()
    }

    fun getUsers(realm: Realm): RealmResults<User> {
        return realm.where(User::class.java).findAll()
    }

}