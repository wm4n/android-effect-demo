package com.wm4n.effectdemo.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.wm4n.effectdemo.databinding.TabContentFragmentBinding
import com.wm4n.effectdemo.databinding.UpdatableTabFragmentBinding

class UpdatableTabFragment: Fragment() {

  private var _binding : UpdatableTabFragmentBinding? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    _binding = UpdatableTabFragmentBinding.inflate(inflater, container, false)
    _binding?.pager?.adapter = UpdatableTabAdapter(childFragmentManager)
    _binding?.navigationTabs?.setupWithViewPager(_binding?.pager)
    return _binding?.viewRoot
  }
}

class TabContentFragment: Fragment() {

  private var _binding : TabContentFragmentBinding? = null
  var onAdd : View.OnClickListener? = null
  var onRemove : View.OnClickListener? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    _binding = TabContentFragmentBinding.inflate(inflater, container, false)
    _binding?.text?.text = arguments?.getString("text")?:""
    _binding?.add?.setOnClickListener { v -> onAdd?.onClick(v) }
    _binding?.remove?.setOnClickListener { v -> onRemove?.onClick(v) }
    return _binding?.viewRoot
  }
}

class UpdatableTabAdapter(fm: FragmentManager): FragmentPagerAdapter(
  fm,
  BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

  private val tabs: MutableList<Int> = mutableListOf(1, 2, 3)

  override fun getCount(): Int {
    return tabs.size
  }

  override fun getItem(position: Int): Fragment {
    return TabContentFragment().apply {
      arguments = Bundle().apply {
        putString("text", tabs[position].toString())
      }
      onAdd = View.OnClickListener {
        tabs.add(tabs.last()+1)
        notifyDataSetChanged()
      }
      onRemove = View.OnClickListener {
        if(tabs.size > 1) tabs.removeFirst()
        notifyDataSetChanged()
      }
    }
  }

  override fun getPageTitle(position: Int): CharSequence? {
    return tabs[position].toString()
  }

  override fun getItemId(position: Int): Long {
    return tabs[position].toLong()
  }

  override fun getItemPosition(`object`: Any): Int {
    return PagerAdapter.POSITION_NONE
  }
}