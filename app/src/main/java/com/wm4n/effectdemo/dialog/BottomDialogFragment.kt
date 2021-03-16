package com.wm4n.effectdemo.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.wm4n.effectdemo.R
import com.wm4n.effectdemo.databinding.BottomDialogFragmentBinding

class BottomDialogFragment(private val url: String) : DialogFragment() {

  private var _binding : BottomDialogFragmentBinding? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setStyle(
      STYLE_NORMAL,
      R.style.BottomDialogFragment
    )
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = BottomDialogFragmentBinding.inflate(inflater, container, false)
    return _binding?.viewRoot
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    _binding?.webView?.loadUrl(url)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}