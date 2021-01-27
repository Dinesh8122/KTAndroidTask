package com.example.ktandroidtask.RealmInterface

import com.example.ktandroidtask.pojoModels.UpdateLocation
import com.example.ktandroidtask.pojoModels.User
import com.example.ktandroidtask.pojoModels.UserLocation
import io.realm.Realm
import io.realm.RealmResults

interface UserLocationInterface {
    fun addUserLocation(realm: Realm, user: UserLocation): Boolean
    fun CreateUpdateLocation(realm: Realm, user: UpdateLocation): Boolean
    fun updateUserLocation(realm: Realm, user: UserLocation): Boolean
    fun getUserLocation(realm: Realm, email: String): UserLocation?
    fun getAllUserLocation(realm: Realm, email: String): RealmResults<UpdateLocation>?
}