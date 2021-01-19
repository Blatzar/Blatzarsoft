package com.blatzarsoft.blatzarsoft

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_login.*
import com.blatzarsoft.blatzarsoft.SchoolSoftApi.Companion.getAppKey
import kotlin.concurrent.thread

const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"

class LoginActivity : AppCompatActivity() {

    override fun onBackPressed() {
        //super.onBackPressed()
        val currentFragment = supportFragmentManager.fragments.last()
        supportFragmentManager.beginTransaction().remove(currentFragment).commit()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        DataStore.init(this)

        // Setting the theme
        val settingsManager = PreferenceManager.getDefaultSharedPreferences(this)
        val autoDarkMode = settingsManager.getBoolean("auto_dark_mode", true)
        val darkMode = settingsManager.getBoolean("dark_mode", false)

        if (autoDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            if (darkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


        if (DataStore.getKey(LOGIN_KEY, "appKey", "")?.isNotEmpty()!!) {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, "")
            }
            startActivity(intent)
            finish()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        inputSchool.setEndIconOnClickListener {
            val schoolFragment = TitleFragment()
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.loginRoot, schoolFragment)
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
                        DataStore.setKey(LOGIN_KEY, "name", appKeyResponse.name)
                        DataStore.setKey(LOGIN_KEY, "appKey", appKeyResponse.appKey)
                        DataStore.setKey(LOGIN_KEY, "orgId", appKeyResponse.orgs[0].orgId)
                        DataStore.setKey(LOGIN_KEY, "school", school)

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
