package com.blatzarsoft.blatzarsoft.ui.lunch

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.blatzarsoft.blatzarsoft.*
import com.blatzarsoft.blatzarsoft.ui.schedule.toPx
import kotlinx.android.synthetic.main.fragment_lunch.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class LunchFragment : Fragment() {
    class LunchViewModel : ViewModel() {}
    private lateinit var lunchViewModel: LunchViewModel
    private val viewModel: MainActivity.ListViewModel by activityViewModels()

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
                        context?.let { ContextCompat.getColorStateList(it, R.color.black_overlay) }
                }
                activity?.runOnUiThread {
                    calendarList[day].layoutParams = lunchParams
                }
            }
        }
    }

    private fun displayLunch(inputWeek: Int = 0) {
        // Gets the schedule list from MainActivity.kt
        val calendar = Calendar.getInstance()
        val week = if (inputWeek == 0) calendar.get(Calendar.WEEK_OF_YEAR) else inputWeek
        val lunchList = DataStore.getKey<List<Lunch>>(SCHEDULE_DATA_KEY, "lunch", null)

        if (lunchList != null) {
            if (lunchList.isNotEmpty()) {
                var lunch = lunchList[0]
                lunchList.forEach {
                    if (it.week == week) {
                        lunch = it
                    }
                }
                mondayText.text = lunch.monday
                tuesdayText.text = lunch.tuesday
                wednesdayText.text = lunch.wednesday
                thursdayText.text = lunch.thursday
                fridayText.text = lunch.friday
                weekText.text = String.format(resources.getString(R.string.week_number), lunch.week)
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

        viewModel.week.observe(viewLifecycleOwner) {
            displayLunch(it)
        }
        displayLunch(viewModel.week.value!!)
        updateTime()

        // Currently pretty useless
        lunchRefreshLayout.setOnRefreshListener {
            displayLunch()
            updateTime()
            lunchRefreshLayout.isRefreshing = false
        }
    }
}
