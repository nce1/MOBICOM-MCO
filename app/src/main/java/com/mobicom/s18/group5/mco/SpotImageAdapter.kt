package com.mobicom.s18.group5.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobicom.s18.group5.mco.databinding.ItemReviewSpotBinding
import com.mobicom.s18.group5.mco.databinding.ItemSpotImageBinding

class SpotImageAdapter (): Adapter<SpotsViewHolder>(){
    private var dataIV: List<Int> = GenerateData.loadSpotImages()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotsViewHolder {
        val imageSpotsBinding: ItemSpotImageBinding = ItemSpotImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return SpotsViewHolder(imageSpotsBinding.root)

    }

    override fun onBindViewHolder(holder: SpotsViewHolder, position: Int) {
        holder.bindDataImage(dataIV[position])
    }

    override fun getItemCount(): Int {
        return dataIV.size
    }
}