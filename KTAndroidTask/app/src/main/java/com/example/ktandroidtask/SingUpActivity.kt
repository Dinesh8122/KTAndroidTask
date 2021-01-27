package com.example.ktandroidtask

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ktandroidtask.RealmInterface.UserModel
import com.example.ktandroidtask.Utils.Utils
import com.example.ktandroidtask.pojoModels.User
import com.example.ktandroidtask.sharedPreference.SharedPreference
import io.realm.Realm
import io.realm.RealmResults
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.activity_sing_up.*


class SingUpActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var realm: Realm
    lateinit var userModel :UserModel
    lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up)


        realm = Realm.getDefaultInstance();
        userModel = UserModel()
        sharedPreference = SharedPreference()

        if(signin_constraintLayout.visibility == VISIBLE){
            showLoginLayout()
        }else{
            showSingUpLayout()
        }

        textWatcher()

        setButtonListener()


    }

    private fun showLoginLayout(){
        signUp_constraintLayout.visibility = GONE
        signin_constraintLayout.visibility = VISIBLE
        tv_title.text=getString(R.string.login)
        signUp_button.text=getString(R.string.login)
        singUp_info_text.text= getString(R.string.singUp_info_text)
        singUp_text.text = getString(R.string.signUp)
    }

    private fun showSingUpLayout(){
        signin_constraintLayout.visibility = GONE
        signUp_constraintLayout.visibility = VISIBLE
        tv_title.text="SingUp"
        signUp_button.text="SingUp"
        singUp_info_text.text= getString(R.string.already_account_text)
        singUp_text.text = getString(R.string.login)
    }

    private fun setButtonListener(){

        signUp_button.setOnClickListener(this)
        singUp_text.setOnClickListener(this)
        cancel_button.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            signUp_button -> {
                if (signUp_button.text.toString() == getString(R.string.login)) {
                    login()
                }else{
                    signUp()
                }
            }
            cancel_button -> {
                if (signUp_button.text.toString() == getString(R.string.login)) {
                    finish()
                }else{
                    showLoginLayout()
                }
            }
            singUp_text -> {
                if (singUp_text.text.toString() == getString(R.string.login)) {
                    showLoginLayout()
                } else {
                    showSingUpLayout()

                }
            }
        }
    }

    private fun textWatcher(){

        et_signin_email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!Utils().isEmailValid(s.toString())) {
                    tv_signin_email_error.visibility = VISIBLE
                    tv_signin_email_error.text = getString(R.string.mail_id_error)
                } else {
                    tv_signin_email_error.visibility = GONE
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        et_signin_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length < 8) {
                    tv_signin_password_error.visibility = VISIBLE
                    tv_signin_password_error.text = getString(R.string.password_error)
                } else {
                    tv_signin_password_error.visibility = GONE
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        et_email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!Utils().isEmailValid(s.toString())) {
                    tv_email_error.visibility = VISIBLE
                    tv_email_error.text = getString(R.string.mail_id_error)
                } else {
                    tv_email_error.visibility = GONE
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        et_name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length < 3) {
                    tv_name_error.visibility = VISIBLE
                    tv_name_error.text = getString(R.string.name_error)
                } else {
                    tv_name_error.visibility = GONE
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        et_confirm_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (et_password.text.toString() != s.toString()) {
                    tv_confirm_password_error.visibility = VISIBLE
                    tv_confirm_password_error.text = getString(R.string.confirm_password_error)
                } else {
                    tv_confirm_password_error.visibility = GONE
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        et_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length < 8) {
                    tv_password_error.visibility = VISIBLE
                    tv_password_error.text = getString(R.string.password_error)
                } else {
                    tv_password_error.visibility = GONE
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    fun signUp() {

        if (TextUtils.isEmpty(et_name.text.toString())) {
            tv_name_error.visibility = VISIBLE
            tv_name_error.text = getString(R.string.name_error)
            return
        }
        if (TextUtils.isEmpty(et_email.text.toString())) {
            tv_email_error.visibility = VISIBLE
            tv_email_error.text = getString(R.string.mail_id_error)
            return
        }
        if (TextUtils.isEmpty(et_password.text.toString())) {
            tv_password_error.visibility = VISIBLE
            tv_password_error.text = getString(R.string.password_error)
            return
        }
        if (TextUtils.isEmpty(et_confirm_password.text.toString())) {
            tv_confirm_password_error.visibility = VISIBLE
            tv_confirm_password_error.text = "Please enter a confirm password"
            return
        }
        try {
            val user = User(
                    email = et_email.text.toString(),
                    name = et_name.text.toString(),
                    password = et_password.text.toString()
            )
            val response = userModel.addUser(realm, user)
            if(response){
                showLoginLayout()
            }else{
                Toast.makeText(applicationContext, "User not registered, Please register and try again.", Toast.LENGTH_SHORT).show()

            }

        } catch (e: Exception) {
            Log.i("signUp", "signUp: RealmPrimaryKeyConstraintException ${e.message}")
            Toast.makeText(applicationContext, "Primary Key exists, Press Update instead", Toast.LENGTH_SHORT).show()
        }
    }
    fun login(){
        if (TextUtils.isEmpty(et_signin_email.text.toString())) {
            tv_signin_email_error.visibility = VISIBLE
            tv_signin_email_error.text = getString(R.string.mail_id_error)
            return
        }
        if (TextUtils.isEmpty(et_signin_password.text.toString())) {
            tv_signin_password_error.visibility = VISIBLE
            tv_signin_password_error.text = getString(R.string.password_error)
            return
        }
        val response = userModel.userLogin(realm,et_signin_email.text.toString(),et_signin_password.text.toString())
                 Log.i("SingUp", "login: response ${response}")
        if(response.isNotEmpty()){
            response.forEach { it ->
                Log.i("SingUp", "login:email ${it.email} ")
                sharedPreference.save(this,"userEmailId",it.email)
            }
            val indent = Intent(this,Home::class.java)
            startActivity(indent)
            finish()
        }else{
             Toast.makeText(applicationContext, "User not registered, Please register and try again.", Toast.LENGTH_SHORT).show()
        }

//        if (response != null) {
//            if(response.email === et_signin_email.text.toString()){
//                if(response.password != et_signin_password.text.toString() ){
//                    Toast.makeText(applicationContext, "Password is wrong", Toast.LENGTH_SHORT).show()
//
//                }else{
//                    val indent = Intent(this,Home::class.java)
//                    startActivity(indent)
//                }
//            }else{
//                Toast.makeText(applicationContext, "User not registered, Please register and try again.", Toast.LENGTH_SHORT).show()
//            }
//        }


    }


    private fun readEmployeeRecords() {
        val userModel = UserModel()
        val results = userModel.getUsers(realm)
        results.forEach { result ->

            Log.d("Singup", "signUp: result ${result} ")

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
