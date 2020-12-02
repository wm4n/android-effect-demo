package com.wm4n.effectdemo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.wm4n.effectdemo.databinding.ActivityMainBinding
import com.wm4n.effectdemo.dialog.BottomDialogFragment

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    binding.buttonBottomDialogFragment.setOnClickListener { v ->
      val f = BottomDialogFragment()
      f.show(supportFragmentManager, "BottomDialogFragment")
    }
  }
}