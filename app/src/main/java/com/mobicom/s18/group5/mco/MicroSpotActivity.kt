package com.mobicom.s18.group5.mco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobicom.s18.group5.mco.databinding.ActivitySpotsBinding

class MicroSpotActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val spot = intent.getStringExtra("TITLE") ?: ""
        val viewBinding = ActivitySpotsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.tvTitleSA.text = spot
        var temp = "$spot - Taft Avenue, Malate, Manila"
        viewBinding.tvLocSA.text = temp
        temp = "A designated space accessible to the public, often intended to facilitate a specific activity or need. It serves as a point of interest or utility, whether for gathering, relaxation, learning, or essential services. This location provides a setting where people can engage with their environment or with one another."
        viewBinding.tvDescSA.text = temp
        temp = "Rating: 4.0/5.0"
        viewBinding.tvRatingSA.text = temp

        val imageAdapter = SpotImageAdapter()
        viewBinding.rvImagesSA.adapter = imageAdapter
        viewBinding.rvImagesSA.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val revAdapter = SpotReviewAdapter()
        viewBinding.rvReviewSA.adapter = revAdapter
        viewBinding.rvReviewSA.layoutManager = LinearLayoutManager(this)
    }
}