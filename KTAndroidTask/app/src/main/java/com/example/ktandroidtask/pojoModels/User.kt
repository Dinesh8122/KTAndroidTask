package com.example.ktandroidtask.pojoModels

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class User(
    @PrimaryKey
    open var email: String = "",
    open var name: String = "",
    open var password: String = ""
)
    : RealmObject() {

    fun copy(
        email: String = this.email,
        name: String = this.name,
        password: String = this.password
    )
            = User(email, name, password)
}

open class UpdateLocation() : RealmObject() {
    var email: String = ""
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var date:String? =""


}

open class UserLocation(
    @PrimaryKey
    open var email: String = "",
    var location: UpdateLocation? =null
)
    : RealmObject() {

}