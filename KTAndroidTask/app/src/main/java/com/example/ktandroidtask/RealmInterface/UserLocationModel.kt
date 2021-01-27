package com.example.ktandroidtask.RealmInterface

import com.example.ktandroidtask.pojoModels.UpdateLocation
import com.example.ktandroidtask.pojoModels.User
import com.example.ktandroidtask.pojoModels.UserLocation
import io.realm.Realm
import io.realm.RealmResults

class UserLocationModel : UserLocationInterface {


    override fun addUserLocation(realm: Realm, user: UserLocation): Boolean {
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


    override fun updateUserLocation(realm: Realm, user: UserLocation): Boolean {
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


    override fun getUserLocation(realm: Realm, email: String): UserLocation? {
        return realm.where(UserLocation::class.java).equalTo("email", email).findFirst()
    }

    override fun CreateUpdateLocation(realm: Realm, location: UpdateLocation): Boolean {
        try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(location)
            realm.commitTransaction()
            return true
        } catch (e: Exception) {
            println(e)
            return false
        }
    }

    override fun getAllUserLocation(realm: Realm, email: String): RealmResults<UpdateLocation>? {
        return realm.where(UpdateLocation::class.java).equalTo("email", email).findAll()
    }


    fun getUsers(realm: Realm): RealmResults<UserLocation> {
        return realm.where(UserLocation::class.java).findAll()
    }

}