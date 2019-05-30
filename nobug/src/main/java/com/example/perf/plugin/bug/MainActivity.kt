package com.example.perf.plugin.bug

import android.app.Activity
import android.os.Bundle
import com.example.perf.plugin.nobug.R

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
