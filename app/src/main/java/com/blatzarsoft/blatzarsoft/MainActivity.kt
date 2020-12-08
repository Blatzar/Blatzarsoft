package com.blatzarsoft.blatzarsoft

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.beust.klaxon.Klaxon
import com.blatzarsoft.blatzarsoft.ui.lunch.getLunch
import com.blatzarsoft.blatzarsoft.ui.schedule.getLessons
import com.google.gson.Gson
import kotlin.concurrent.thread
import khttp.get
import java.lang.Exception

data class Token(val expiryDate: String, val token: String)

fun getToken(school: String, appKey: String): Token? {
    val url = "https://sms.schoolsoft.se/${school}/rest/app/token"
    val payload = mapOf(
        "appversion" to "2.3.2",
        "appos" to "android",
        "appkey" to appKey,
        "deviceid" to ""
    )
    return try {
        val r = get(url, headers = payload)
        if (r.statusCode == 200) {
            Klaxon().parse<Token>(r.text)
        } else {
            null
        }
    }
    catch (e: Exception){
        null
    }
}


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        println("Started main.")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        //supportActionBar?.hide()

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        /*
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
         */
        navView.setupWithNavController(navController)
        val sharedPrefSchool = getSharedPreferences("SCHOOL", Context.MODE_PRIVATE)
        val sharedPrefLogin = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)

        val gson = Gson()

        val appKey = sharedPrefLogin?.getString("appKey", "")
        val school = sharedPrefLogin?.getString("school", "")
        val orgId = sharedPrefLogin?.getInt("orgId", -1)

        if (school?.isNotEmpty()!! && appKey?.isNotEmpty()!! && orgId != null && orgId != -1) {
            thread {
                val token = getToken(school, appKey)
                if (token is Token) {
                    val lessons = getLessons(school, token.token, orgId)
                    val lunch = getLunch(school, token.token, orgId)
                    val lessonsJson = gson.toJson(lessons)
                    val lunchJson = gson.toJson(lunch)

                    with(sharedPrefSchool.edit()) {
                        putString("lessons", lessonsJson)
                        putString("lunch", lunchJson)
                        apply()
                    }
                    // Refreshes current fragment regardless of content, barbaric, but I couldn't find another way
                    supportFragmentManager.fragments.last().onCreate(null)
                }
            }
        }
    }
}