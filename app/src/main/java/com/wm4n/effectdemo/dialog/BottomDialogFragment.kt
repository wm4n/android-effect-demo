package com.wm4n.effectdemo.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.wm4n.effectdemo.R
import com.wm4n.effectdemo.databinding.BottomDialogFragmentBinding

class BottomDialogFragment : DialogFragment() {

  private var _binding : BottomDialogFragmentBinding? = null
  private val binding get() = _binding!!

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
    _binding?.webView?.loadUrl("https://wm4n.github.io")
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}