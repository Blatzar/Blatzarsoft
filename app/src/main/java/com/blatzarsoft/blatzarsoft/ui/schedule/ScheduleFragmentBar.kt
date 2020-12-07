package com.blatzarsoft.blatzarsoft.ui.schedule

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
