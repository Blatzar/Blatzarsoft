package com.blatzarsoft.blatzarsoft.ui.schedule


import android.content.Context
import android.content.res.Resources
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.ViewModelProviders
import com.beust.klaxon.Klaxon
import com.blatzarsoft.blatzarsoft.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import khttp.get
import kotlinx.android.synthetic.main.fragment_schedule.*
import kotlinx.android.synthetic.main.schedule_card.view.*


data class Lesson(
    val weeksString: String,
    val subjectName: String,
    val roomName: String,
    val length: Int,
    val startTime: String,
    val endTime: String,
    val dayId: Int,
    val id: Int
)

val Int.toPx: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.toDp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun getLessons(school: String, token: String, orgId: Int): List<Lesson>? {
    val url = "https://sms.schoolsoft.se/${school}/api/lessons/student/${orgId}"
    val payload = mapOf(
        "appversion" to "2.3.2",
        "appos" to "android",
        "token" to token
    )
    val r = get(url, headers = payload)
    return if (r.statusCode == 200) {
        Klaxon().parseArray<Lesson>(r.text)
    } else {
        null
    }
}


fun weekStringToList(weeks: String): MutableList<Int> {
    // Converts "2, 5-8" to [2, 5, 6, 7, 8]
    val fullList = mutableListOf<Int>()
    val splitList = weeks.split(",")
    splitList.forEach {
        val splitNumbers = it.split("-")
        val first = splitNumbers[0].trim().toIntOrNull()

        if (splitNumbers.size == 1) {
            if (first != null) {
                fullList.add(first)
            }
        } else {
            val second = splitNumbers[1].trim().toIntOrNull()
            if (first != null && second != null) {
                for (i in first..second) {
                    fullList.add(i)
                }
            }
        }
    }
    return fullList
}


class ScheduleFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance(position: Int): ScheduleFragment {
            // Allows passing the current page number, used to generate the schedules
            val fragment = ScheduleFragment()
            val args = Bundle()
            args.putInt("position", position)
            fragment.arguments = args
            return fragment
        }
    }

    private fun displayLessons() {
        val calendar = Calendar.getInstance()
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        // Starting from 0
        //val day = calendar.get(Calendar.DAY_OF_WEEK) - 1
        // Gets the schedule list from MainActivity.kt

        val sharedPrefSchool = activity?.getSharedPreferences("SCHOOL", Context.MODE_PRIVATE)
        val lessonObject = object : TypeToken<List<Lesson>>() {}.type
        val gson = Gson()
        val lessonsJson = sharedPrefSchool?.getString("lessons", "[]")
        val lessons = gson.fromJson<List<Lesson>>(lessonsJson, lessonObject)

        arguments?.getInt("position")?.let { day ->
            if (lessons.isNotEmpty()) {
                val timeRegex = Regex("""(\d{2}):(\d{2})""")
                val sizeMultiplier = 1
                val scheduleStartHour = 8
                val scheduleStartOffset = 10.toPx
                lessons.forEach {
                    val weeks = weekStringToList(it.weeksString)
                    if (week in weeks && it.dayId == day) {

                        val startTime = timeRegex.find(it.startTime)
                        val fixedStartTime = startTime?.groups?.get(0)?.value

                        val startHour = startTime?.groups?.get(1)?.value?.toIntOrNull()
                        val startMinutes = startTime?.groups?.get(2)?.value?.toIntOrNull()

                        val endTime = timeRegex.find(it.endTime)
                        val fixedEndTime = endTime?.groups?.get(0)?.value

                        /*
                        val endHour = startTime?.groups?.get(1)?.value?.toIntOrNull()
                        val endMinutes = startTime?.groups?.get(2)?.value?.toIntOrNull()
                        */

                        val card: View = layoutInflater.inflate(R.layout.schedule_card, null)
                        card.textMain.text = it.subjectName
                        card.textRoom.text = it.roomName
                        card.textTime.text = "$fixedStartTime - $fixedEndTime"

                        val cardHeight = (it.length * sizeMultiplier).toPx

                        // Prevents overlapping time and room (hopefully).
                        if (it.length <= 40) {
                            val roomParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT, // view width
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT // view height
                            )
                            val timeParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT, // view width
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT // view height
                            )
                            // Same as Time, but more marginEnd
                            roomParams.gravity = Gravity.CENTER_VERTICAL or Gravity.END
                            roomParams.marginEnd = 120.toPx
                            timeParams.gravity = Gravity.CENTER_VERTICAL or Gravity.END
                            timeParams.marginEnd = 20.toPx

                            card.textRoom.layoutParams = roomParams
                            card.textTime.layoutParams = timeParams
                        }

                        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                            LinearLayoutCompat.LayoutParams.MATCH_PARENT, // view width
                            cardHeight // view height
                        )
                        if (startHour != null && startMinutes != null) {
                            params.topMargin =
                                (((startHour - scheduleStartHour) * 60 + startMinutes) * sizeMultiplier).toPx + scheduleStartOffset
                            card.layoutParams = params

                            activity?.runOnUiThread {
                                relativeRoot.addView(card)
                            }
                        }
                    }
                }
            }
        }
    }

    private lateinit var dashboardViewModel: ScheduleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this).get(ScheduleViewModel::class.java)
        /*val textView: TextView = root.findViewById(R.id.testText)
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayLessons()
    }
}
