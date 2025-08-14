package com.rawgraphy.blanc.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
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
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false);
        return binding.root
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        val route = requireArguments().getString(ARG_ROUTE).orEmpty()
        val fragment = WebViewFragment.newInstance(
            route = route,
            isBottomMenu = false,
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