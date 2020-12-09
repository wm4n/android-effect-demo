package com.wm4n.effectdemo;

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wm4n.effectdemo.databinding.ActivityContainerBinding
import com.wm4n.effectdemo.tab.UpdatableTabFragment

class ContainerActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = ActivityContainerBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

      when(intent?.getStringExtra("fragment")?:"") {
          "UpdatableTab" -> {
              val f = UpdatableTabFragment()
              supportFragmentManager.beginTransaction().replace(R.id.view_root, f, "updatableTab")
                  .commit()
          }
          "" -> finish()
      }
  }
}
