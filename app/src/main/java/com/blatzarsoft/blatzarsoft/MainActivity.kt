package com.blatzarsoft.blatzarsoft

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.blatzarsoft.blatzarsoft.SchoolSoftApi.Companion.getLessons
import com.blatzarsoft.blatzarsoft.SchoolSoftApi.Companion.getLunch
import com.blatzarsoft.blatzarsoft.SchoolSoftApi.Companion.getToken
import com.blatzarsoft.blatzarsoft.ui.settings.BaseActivity
import kotlin.concurrent.thread


class MainActivity : BaseActivity() {
    class ListViewModel : ViewModel() {
        private fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }

        // 0 will be treated as current week.
        val week = MutableLiveData<Int>().default(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Needed as LoginActivity isn't getting loaded sometimes when resuming app
        DataStore.init(this)

        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        //supportActionBar?.hide()

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        /*
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.settings_title
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
         */
        navView.setupWithNavController(navController)

        val appKey = DataStore.getKey(LOGIN_KEY, "appKey", "")
        val school = DataStore.getKey(LOGIN_KEY, "school", "")
        val orgId = DataStore.getKey(LOGIN_KEY, "orgId", -1)
        println("$school $appKey $orgId")
        if (school?.isNotEmpty() == true && appKey?.isNotEmpty() == true && orgId != null && orgId != -1) {
            thread {
                val token = getToken(school, appKey)
                println(token)
                if (token is Token) {
                    val lessons = getLessons(school, token.token, orgId)
                    val lunch = getLunch(school, token.token, orgId)
                    if (lessons != null) {
                        DataStore.setKey(SCHEDULE_DATA_KEY, "lessons", lessons)
                    }
                    DataStore.setKey(SCHEDULE_DATA_KEY, "lunch", lunch)

                    // Refreshes current fragment regardless of content
                    // Could be done with LiveData, but this is less complex.
                    supportFragmentManager.fragments.last().onCreate(null)
                }
            }
        }
    }
}