package com.wm4n.effectdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wm4n.effectdemo.databinding.ActivityMainBinding
import com.wm4n.effectdemo.dialog.BottomDialogFragment

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    binding.buttonBottomDialogFragment.setOnClickListener {
      val f = BottomDialogFragment()
      f.show(supportFragmentManager, "BottomDialogFragment")
    }

    binding.buttonUpdatableTabs.setOnClickListener {
      val intent = Intent(this, ContainerActivity::class.java).apply {
        putExtra("fragment", "UpdatableTab")
      }
      startActivity(intent)
    }
  }
}