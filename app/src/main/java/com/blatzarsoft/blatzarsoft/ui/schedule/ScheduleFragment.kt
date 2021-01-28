package com.blatzarsoft.blatzarsoft.ui.schedule

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.icu.util.Calendar
import android.icu.util.TimeUnit
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.blatzarsoft.blatzarsoft.*
import kotlinx.android.synthetic.main.fragment_schedule.*
import kotlinx.android.synthetic.main.schedule_card.view.*
import kotlin.concurrent.thread
import com.blatzarsoft.blatzarsoft.SchoolSoftApi.Companion.weekStringToList
import java.lang.System.exit
import kotlin.system.exitProcess

val Int.toPx: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.toDp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

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

    private val sizeMultiplier = 1
    private val scheduleStartHour = 8
    private val scheduleStartOffset = 10.toPx

    private fun updateTime() {
        // Needs to be here because it doesn't get updated if time is changed on the device.
        val calendar = Calendar.getInstance()
        thread {
            val offset =
                (((calendar.get(Calendar.HOUR_OF_DAY) - scheduleStartHour) * 60 + calendar.get(Calendar.MINUTE)) * sizeMultiplier).toPx + scheduleStartOffset - 3.toPx

            val timeRightParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                10.toPx, // view width
                6.toPx // view height
            )
            timeRightParams.topMargin = offset
            timeRightParams.marginEnd = (-5).toPx
            timeRightParams.addRule(RelativeLayout.ALIGN_PARENT_END)
            val timeLeftParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                10.toPx, // view width
                6.toPx // view height
            )
            timeLeftParams.topMargin = offset
            timeLeftParams.marginStart = (-5).toPx

            activity?.runOnUiThread {
                timeLeft.layoutParams = timeLeftParams
                timeRight.layoutParams = timeRightParams
            }
        }
    }

    private fun displayLessons(inputWeek: Int = 0) {
        updateTime()
        relativeRoot.removeAllViews()
        val calendar = Calendar.getInstance()
        val week = if (inputWeek == 0) calendar.get(Calendar.WEEK_OF_YEAR) else inputWeek

        // Gets the schedule list from MainActivity.kt

        val lessons = DataStore.getKey<List<Lesson>>(SCHEDULE_DATA_KEY, "lessons", null)

        // Kinda badly done
        val colorList = mapOf(
            "#3123A8" to "Dark Blue",
            "#23A893" to "Light Green",
            "#B8A83D" to "Yellow",
            "#7E30B4" to "Dark Purple",
            "#0E4B16" to "Very Dark Green",
            "#AD2222" to "Dark Red",
            "#BB4411" to "Dark Orange",
            "#C847D6" to "Pink",
            "#D48015" to "Orange",
            "#2CB5F8" to "Light Blue",
            "#395859" to "Dark Green"
        )
        val colors = colorList.keys.toList()
        val colorsToLessons = mutableMapOf<String, String>()
        var colorIndex = 0
        arguments?.getInt("position")?.let { day ->
            if (lessons != null) {
                if (lessons.isNotEmpty()) {
                    val timeRegex = Regex("""(\d{2}):(\d{2})""")
                    lessons.forEach {
                        val weeks = weekStringToList(it.weeksString)
                        val color = colorsToLessons.getOrDefault(it.subjectName, "")
                        if (color == "") {
                            colorsToLessons[it.subjectName] = colors[colorIndex]
                            colorIndex = if (colorIndex + 1 > colors.size - 1) 0 else colorIndex + 1
                        } else {
                            colorsToLessons[it.subjectName] = color
                        }
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

                            println(colorsToLessons)
                            card.cardView.background.setTint(Color.parseColor(colorsToLessons[it.subjectName]))

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
    }

    private val viewModel: MainActivity.ListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*val textView: TextView = root.findViewById(R.id.testText)
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.week.observe(viewLifecycleOwner) {
            displayLessons(it)
        }
        displayLessons(viewModel.week.value!!)

        thread {
            while (true) {
                updateTime()
                Thread.sleep(10000L)
            }
        }
    }
}
