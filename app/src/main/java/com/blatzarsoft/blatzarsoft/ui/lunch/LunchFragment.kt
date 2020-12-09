package com.blatzarsoft.blatzarsoft.ui.lunch

import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.beust.klaxon.Klaxon
import com.blatzarsoft.blatzarsoft.R
import com.blatzarsoft.blatzarsoft.ui.schedule.toPx
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import khttp.get
import kotlinx.android.synthetic.main.fragment_lunch.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.thread


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

    private fun updateTime() {
        val calendar = Calendar.getInstance()
        val currentDay = SimpleDateFormat("u", Locale.ENGLISH).format(calendar.time.time).toInt()

        val calendarList = listOf<CardView>(monday, tuesday, wednesday, thursday, friday)
        thread {
            for (day in 0..4) {
                val lunchParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, // view width
                    0 // view height
                )

                when (day) {
                    0 -> {
                        lunchParams.topToBottom = weekCard.id
                        lunchParams.bottomToTop = tuesday.id
                    }
                    4 -> {
                        lunchParams.topToBottom = thursday.id
                        lunchParams.bottomToBottom = lunchRoot.id
                    }
                    else -> {
                        lunchParams.topToBottom = calendarList[day - 1].id
                        lunchParams.bottomToTop = calendarList[day + 1].id
                    }
                }

                if (currentDay - 1 == day) {
                    calendarList[day].alpha = 1F
                    lunchParams.setMargins(5.toPx, 5.toPx, 5.toPx, 5.toPx)
                    calendarList[day].backgroundTintList =
                        context?.let { ContextCompat.getColorStateList(it, R.color.colorPrimaryDark) }

                } else {
                    calendarList[day].alpha = 0.7F
                    lunchParams.setMargins(10.toPx, 5.toPx, 10.toPx, 5.toPx)
                    calendarList[day].backgroundTintList =
                        context?.let { ContextCompat.getColorStateList(it, R.color.colorPrimary) }
                }
                activity?.runOnUiThread {
                    calendarList[day].layoutParams = lunchParams
                }
            }
        }

    }

    private fun displayLunch() {
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
        updateTime()
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
        updateTime()

        // Currently pretty useless
        lunchRefreshLayout.setOnRefreshListener {
            displayLunch()
            updateTime()
            lunchRefreshLayout.isRefreshing = false
        }
    }
}
