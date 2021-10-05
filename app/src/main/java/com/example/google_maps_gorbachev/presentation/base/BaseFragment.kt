package com.example.geophoto.presentation.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.example.gorbachev_gmail.base.toast
import com.example.geophoto.presentation.util.alertDialog

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {
	
	companion object {
		const val TAG = "myLogTag"
	}
	
	fun toast(mes: String) {
		requireContext().toast(mes)
	}
	
	fun alertDialog(
		title: String,
		mes: String,
		positiveBtn: String,
		neutralBtn: String = "",
		NegativeBtn: String = "",
		countBtn: Int = 1,
	) {
		requireContext().alertDialog(
			title,
			mes,
			positiveBtn,
			neutralBtn,
			NegativeBtn,
			countBtn,
			requireContext()
		)
	}
}