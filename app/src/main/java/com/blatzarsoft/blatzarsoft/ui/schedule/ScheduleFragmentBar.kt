package com.blatzarsoft.blatzarsoft.ui.schedule

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.blatzarsoft.blatzarsoft.ViewPager2Adapter
import com.blatzarsoft.blatzarsoft.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_schedule_bar.*
import java.text.SimpleDateFormat
import java.util.*


class ScheduleFragmentBar : Fragment() {

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

        return inflater.inflate(R.layout.fragment_schedule_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager.adapter = ViewPager2Adapter(activity as AppCompatActivity, 5)

        val calendar = Calendar.getInstance()
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        val day = SimpleDateFormat("u", Locale.ENGLISH).format(calendar.time.time).toInt()
        weekButton.setOnClickListener {

        }
        //val fragment = activity?.supportFragmentManager?.findFragmentById(R.id.relativeRoot)
        // TODO https://developer.android.com/guide/fragments/communicate
        weekText.text = "Vecka $week"
        if ((day >= Calendar.MONDAY) && (day <= Calendar.FRIDAY)) {
            viewPager.currentItem = day - 1
        }
        TabLayoutMediator(
            tabs, viewPager
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Måndag"
                }
                1 -> {
                    tab.text = "Tisdag"
                }
                2 -> {
                    tab.text = "Onsdag"
                }
                3 -> {
                    tab.text = "Torsdag"
                }
                4 -> {
                    tab.text = "Fredag"
                }
            }
        }.attach()
    }
}
