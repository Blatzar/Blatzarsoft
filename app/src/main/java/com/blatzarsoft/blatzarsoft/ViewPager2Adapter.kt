package com.blatzarsoft.blatzarsoft

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blatzarsoft.blatzarsoft.ui.schedule.ScheduleFragment

class ViewPager2Adapter(activity: AppCompatActivity, val itemsCount: Int) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return itemsCount
    }

    override fun createFragment(position: Int): Fragment {
        return ScheduleFragment.newInstance(position)
    }
}