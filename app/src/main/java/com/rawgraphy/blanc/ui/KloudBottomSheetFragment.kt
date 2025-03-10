package com.rawgraphy.blanc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rawgraphy.blanc.databinding.FragmentBottomSheetBinding

class KloudBottomSheetFragment: BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        val route = requireArguments().getString(ARG_ROUTE).orEmpty()
        val fragment = WebViewFragment.newInstance(
            route = route,
        )
        childFragmentManager.beginTransaction().apply {
            add(binding.fragmentContainer.id, fragment, route)
            commit()
        }
    }

    companion object {
        private const val ARG_ROUTE = "ARG_ROUTE"
        fun newInstance(route: String): KloudBottomSheetFragment {
            val fragment = KloudBottomSheetFragment()
            val args = Bundle()
            args.putString(ARG_ROUTE, route)
            fragment.arguments = args
            return fragment
        }
    }
}