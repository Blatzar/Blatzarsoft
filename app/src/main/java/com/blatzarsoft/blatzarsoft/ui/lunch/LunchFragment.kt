package com.blatzarsoft.blatzarsoft.ui.lunch

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.beust.klaxon.Klaxon
import com.blatzarsoft.blatzarsoft.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import khttp.get
import kotlinx.android.synthetic.main.fragment_lunch.*


data class Lunch(
    val week: Int,
    val dates: List<String>,

    val monday: String,
    val tuesday: String,
    val wednesday: String,
    val thursday: String,
    val friday: String,
    // Not used
    val saturday: String,
    val sunday: String
)


fun getLunch(school: String, token: String, orgId: Int): List<Lunch>? {
    val url = "https://sms.schoolsoft.se/${school}/api/lunchmenus/student/${orgId}"
    val payload = mapOf(
        "appversion" to "2.3.2",
        "appos" to "android",
        "token" to token
    )
    val r = get(url, headers = payload)
    return if (r.statusCode == 200) {
        Klaxon().parseArray<Lunch>(r.text)
    } else {
        null
    }
}


class LunchFragment : Fragment() {

    private lateinit var lunchViewModel: LunchViewModel

    private fun displayLunch() {
        // val calendar = Calendar.getInstance()
        // val week = calendar.get(Calendar.WEEK_OF_YEAR)
        // Starting from 0
        // val day = calendar.get(Calendar.DAY_OF_WEEK) - 1

        // Gets the schedule list from MainActivity.kt
        val sharedPrefSchool = activity?.getSharedPreferences("SCHOOL", Context.MODE_PRIVATE)
        val lunchObject = object : TypeToken<List<Lunch>>() {}.type
        val gson = Gson()
        val lunchJson = sharedPrefSchool?.getString("lunch", "")
        if (lunchJson != "") {
            val lunchList = gson.fromJson<List<Lunch>>(lunchJson, lunchObject)
            if (lunchList is List<Lunch>) {
                val lunch = lunchList[0]
                mondayText.text = lunch.monday
                tuesdayText.text = lunch.tuesday
                wednesdayText.text = lunch.wednesday
                thursdayText.text = lunch.thursday
                fridayText.text = lunch.friday
                weekText.text = "Vecka ${lunch.week}"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lunchViewModel =
            ViewModelProviders.of(this).get(LunchViewModel::class.java)
        return inflater.inflate(R.layout.fragment_lunch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayLunch()

        // Currently pretty useless
        lunchRefreshLayout.setOnRefreshListener {
            displayLunch()
            lunchRefreshLayout.isRefreshing = false
        }
    }
}
