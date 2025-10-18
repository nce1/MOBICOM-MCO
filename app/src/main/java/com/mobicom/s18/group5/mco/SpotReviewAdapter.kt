package com.mobicom.s18.group5.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobicom.s18.group5.mco.databinding.ItemReviewSpotBinding

class SpotReviewAdapter (): Adapter<SpotsViewHolder>(){
    private var dataRV: List<String> = GenerateData.loadReviews()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotsViewHolder {
        val reviewSpotsBinding: ItemReviewSpotBinding = ItemReviewSpotBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return SpotsViewHolder(reviewSpotsBinding.root)
    }

    override fun onBindViewHolder(holder: SpotsViewHolder, position: Int) {
        holder.bindReview(dataRV[position])
    }

    override fun getItemCount(): Int {
        return dataRV.size
    }
}