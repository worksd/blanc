package com.rawgraphy.blanc.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import com.rawgraphy.blanc.data.RouteInfo
import com.rawgraphy.blanc.databinding.FragmentBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class KloudBottomSheetFragment : DialogFragment() {

    private lateinit var binding: FragmentBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) initView()
    }

    private fun initView() {
        val screen = arguments?.getString(ARG_ROUTE) ?: return
        val routeInfo = Gson().fromJson(screen, RouteInfo::class.java)
        val fragment = WebViewFragment.newInstance(
            route = routeInfo.route,
            title = null,
            ignoreSafeArea = false,
            isBottomMenu = false
        )
        childFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(binding.fragmentContainer.id, fragment, routeInfo.route)
            .commit()
    }

    companion object {
        private const val ARG_ROUTE = "ARG_ROUTE"
        fun newInstance(route: String) = KloudBottomSheetFragment().apply {
            arguments = Bundle().apply { putString(ARG_ROUTE, route) }
        }
    }
}