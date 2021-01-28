package com.blatzarsoft.blatzarsoft.ui.schedule

import android.icu.util.Calendar
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.blatzarsoft.blatzarsoft.MainActivity
import com.blatzarsoft.blatzarsoft.ViewPager2Adapter
import com.blatzarsoft.blatzarsoft.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_schedule.*
import kotlinx.android.synthetic.main.fragment_schedule_bar.*
import kotlinx.android.synthetic.main.week_selector.*
import kotlinx.android.synthetic.main.week_selector.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class ScheduleFragmentBar : Fragment() {



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

        return inflater.inflate(R.layout.fragment_schedule_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager.adapter = ViewPager2Adapter(activity as AppCompatActivity, 5)
        val calendar = GregorianCalendar(Locale("sv"))
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        viewModel.week.observe(viewLifecycleOwner) {
            if (it != 0) {
                weekText.text = String.format(resources.getString(R.string.week_number), it)
            }
            // When a week is changed all the lessons gets deleted
            //val root: RelativeLayout? = view.findViewById(R.id.relativeRoot)
            //root?.removeAllViews()
        }

        val day = SimpleDateFormat("u", Locale.ENGLISH).format(calendar.time.time).toInt()
        // This handles the week selection button
        weekButton.setOnClickListener {
            thread {
                val currentWeekPicker: ConstraintLayout? = view.findViewById(R.id.weekPickerRoot)
                if (currentWeekPicker != null) {
                    // If there's already a popup -> gets selected and deletes popup
                    activity?.runOnUiThread {
                        val selectedWeek: NumberPicker? = view.findViewById(R.id.weekPicker)
                        viewModel.week.value = selectedWeek?.value
                        scheduleBarRoot.removeView(currentWeekPicker)
                        scheduleBarRoot.forceLayout()
                        weekButton.setImageResource(R.drawable.sharp_date_range_24)
                    }

                } else {
                    // If there's no popup -> creates popup
                    val weekPicker: View = layoutInflater.inflate(R.layout.week_selector, null)
                    weekPicker.weekPicker.maxValue = 52
                    weekPicker.weekPicker.minValue = 1
                    weekPicker.weekPicker.value = week
                    val weekParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT, // view width
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT // view height
                    )
                    weekParams.gravity = Gravity.CENTER
                    activity?.runOnUiThread {
                        scheduleBarRoot.addView(weekPicker)
                    }
                    weekButton.setImageResource(R.drawable.sharp_done_24)
                }
            }
        }

        weekText.text = String.format(resources.getString(R.string.week_number), week)

        if ((day >= Calendar.MONDAY) && (day <= Calendar.FRIDAY)) {
            viewPager.currentItem = day - 1
        }
        TabLayoutMediator(
            tabs, viewPager
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = resources.getString(R.string.monday)
                }
                1 -> {
                    tab.text = resources.getString(R.string.tuesday)
                }
                2 -> {
                    tab.text = resources.getString(R.string.wednesday)
                }
                3 -> {
                    tab.text = resources.getString(R.string.thursday)
                }
                4 -> {
                    tab.text = resources.getString(R.string.friday)
                }
            }
        }.attach()
    }
}
