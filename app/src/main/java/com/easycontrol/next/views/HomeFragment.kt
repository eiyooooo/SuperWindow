package com.easycontrol.next.views

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.easycontrol.next.adapters.HomeFragmentAdapter
import com.easycontrol.next.databinding.FragmentHomeBinding
import com.easycontrol.next.viewmodels.MainActivityViewModel
import rikka.recyclerview.addEdgeSpacing
import rikka.recyclerview.addItemSpacing
import rikka.recyclerview.fixEdgeEffect

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val mainModel: MainActivityViewModel by activityViewModels() //TODO
    private val adapter by lazy { HomeFragmentAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.updateData()

        val recyclerView = binding.list
        recyclerView.adapter = adapter
        recyclerView.fixEdgeEffect()
        recyclerView.addItemSpacing(top = 4f, bottom = 4f, unit = TypedValue.COMPLEX_UNIT_DIP)
        recyclerView.addEdgeSpacing(top = 4f, bottom = 4f, left = 16f, right = 16f, unit = TypedValue.COMPLEX_UNIT_DIP)
    }
}
