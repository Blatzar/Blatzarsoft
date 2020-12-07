package com.blatzarsoft.blatzarsoft

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_login.*
import com.beust.klaxon.Klaxon
import khttp.get
import java.lang.Exception
import kotlin.concurrent.thread

const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"

class LoginActivity : AppCompatActivity() {

    data class Orgs(val orgId: Int)
    data class Person(val name: String, val appKey: String, val orgs: List<Orgs>)

    override fun onBackPressed() {
        //super.onBackPressed()
        val currentFragment = supportFragmentManager.fragments.last()
        supportFragmentManager.beginTransaction().remove(currentFragment).commit()
    }

    private fun getAppKey(school: String, name: String, password: String): Any? {
        // Example login return
        // """{"pictureUrl":"pictureFile.jsp?studentId=9000","name":"LagradOst","isOfAge":true,"appKey":"string",
        // "orgs":[
        //          {"name":"string","blogger":false,"schoolType":9,"leisureSchool":0,"class":"string","orgId":int,"tokenLogin":"url"}
        // ],
        // "type":1,"userId":9000}"""
        val url = "https://sms.schoolsoft.se/${school}/rest/app/login"

        val payload = mapOf(
            "identification" to name,
            "verification" to password,
            "logintype" to "4",
            "usertype" to "1"
        )
        try {
            val r = get(url, data = payload)
            if (r.statusCode != 200) {
                return r.statusCode
            }
            return Klaxon().parse<Person>(r.text)
        } catch (ex: Exception) {
            return null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // If has login credentials
        val sharedPref = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        if (sharedPref.getString("appKey", "")?.isNotEmpty()!!) {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, "")
            }
            startActivity(intent)
            finish()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        val textView = findViewById<TextView>(R.id.inputName).apply {
            text = message
        }
        */

        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        // Toast.makeText(this, "Text", Toast.LENGTH_SHORT).show()
        inputSchool.setEndIconOnClickListener {
            val schoolFragment = TitleFragment()
            if (savedInstanceState == null) { // initial transaction should be wrapped like this
                supportFragmentManager.beginTransaction()
                    .replace(R.id.viewPager, schoolFragment)
                    .commitAllowingStateLoss()
            }
        }

        // Removes errors
        schoolText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                inputSchool.isErrorEnabled = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        passwordText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                inputPassword.isErrorEnabled = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        btn_login.setOnClickListener {
            /*findViewById<TextView>(R.id.inputName).apply {
                text = defaultValue.toString()
            }*/

            val password = passwordText.text.toString()
            val name = inputName.text.toString()
            val school = schoolText.text.toString()

            if (password.isNotEmpty() && name.isNotEmpty() && school.isNotEmpty()) {
                thread {
                    val appKeyResponse = getAppKey(school, name, password)
                    runOnUiThread {
                        when (appKeyResponse) {
                            // TODO STRING RESOURCES
                            404 -> {
                                inputSchool.error = "Kan inte ansluta till skola"
                            }
                            401 -> {
                                inputPassword.error = "Felaktig inloggningsinformation"
                            }
                            null -> {
                                inputPassword.error = "Fel i inloggningen, kolla din anslutning."
                            }
                            is Int -> {
                                inputPassword.error = "Error: $appKeyResponse"
                            }
                        }
                    }
                    // Redundant but more safe for future me
                    if (appKeyResponse is Person) {
                        with(sharedPref.edit()) {
                            putString("name", appKeyResponse.name)
                            putString("appKey", appKeyResponse.appKey)
                            putInt("orgId", appKeyResponse.orgs[0].orgId)
                            putString("school", school)
                            apply()
                        }
                        // Switches activity
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra(EXTRA_MESSAGE, "")
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}
