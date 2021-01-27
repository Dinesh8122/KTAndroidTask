package com.example.ktandroidtask

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.ktandroidtask.RealmInterface.UserModel
import com.example.ktandroidtask.sharedPreference.SharedPreference
import io.realm.Realm

class WelcomeScreen : AppCompatActivity() {
    private lateinit var sharedPreference: SharedPreference
    private var userEmailId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        sharedPreference = SharedPreference()
        userEmailId = sharedPreference.getValue(this, "userEmailId")
        val realm = Realm.getDefaultInstance()
        val userModel = UserModel()
        val handler =Handler()
        val runnable: Runnable = Runnable()
        {
            var i: Intent

            if(userEmailId !=null) {
                val  response = userModel.getUser(realm, userEmailId!!)
                if(response?.email == userEmailId) {
                    i = Intent(this, Home::class.java)

                }else {
                    i = Intent(this, SingUpActivity::class.java)

                }

            }else {
                i = Intent(this, SingUpActivity::class.java)
            }

            startActivity(i)
            // close this activity
            finish()
        }
        handler.postDelayed(runnable, 2000)
    }

}