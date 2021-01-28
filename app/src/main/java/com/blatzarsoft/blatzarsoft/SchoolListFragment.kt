package com.blatzarsoft.blatzarsoft

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.TextView
import com.blatzarsoft.blatzarsoft.DataStore.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flow
import khttp.get
import kotlinx.android.synthetic.main.fragment_school_list.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class School(val name: String, val url: String)

class TitleFragment : androidx.fragment.app.Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        /*val binding = DataBindingUtil.inflate<FragmentTitleBinding>(
            inflater,
            R.layout.fragment_school_list, container, false
        )*/
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_school_list, container, false)
        //return binding.root
    }

    @InternalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fun getSchools(): kotlinx.coroutines.flow.Flow<List<School>?> = flow {
            val url = "https://sms.schoolsoft.se/rest/app/schoollist/prod"
            val r = get(url)
            val wordDict = mapper.readValue<List<School>>(r.text)
            emit(wordDict)
        }

        // list to populate list view
        val list = mutableListOf<String>()
        val fullList = mutableListOf<School>()

        // initialize an array adapter
        val adapter = activity?.let {
            ArrayAdapter<String>(
                it,
                R.layout.list_view, list
            )
        }

        // attach the array adapter with list view

        listView.adapter = adapter

        // list view item click listener
        val schoolRegex = Regex("""schoolsoft\..{2,3}?/(.*)/""")
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position)
            val selectedSchool = fullList.filter { it.name == selectedItem }[0]
            val matchResult = schoolRegex.find(selectedSchool.url)
            val (match) = matchResult!!.destructured
            activity?.runOnUiThread {
                val textView = activity?.findViewById<TextView>(R.id.schoolText)?.apply {
                    text = match
                }
            }
            val currentFragment = parentFragmentManager.fragments.last()
            parentFragmentManager.beginTransaction().remove(currentFragment).commit()
        }

        adapter?.notifyDataSetChanged()

        val data = getSchools()
        GlobalScope.launch {
            data.collect { value ->
                activity?.runOnUiThread {
                    value?.forEach {
                        list.add(it.name)
                        fullList.add(it)
                    }
                    list.sortBy { it }
                    fullList.sortBy { it.name }
                    adapter?.notifyDataSetChanged()
                }
            }
        }

        search_bar.onActionViewExpanded()
        search_bar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                list.removeIf { true }
                list.addAll(fullList.filter { it.name.toLowerCase().contains(newText.toLowerCase()) }.map { it.name })
                adapter?.notifyDataSetChanged()
                return true
            }
        })
    }

}