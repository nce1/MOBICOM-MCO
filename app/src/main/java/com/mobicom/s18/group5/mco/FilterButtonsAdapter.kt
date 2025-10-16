package com.mobicom.s18.group5.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobicom.s18.group5.mco.databinding.ItemFilterButtonsBinding

class FilterButtonsAdapter (): Adapter<SpotsViewHolder>(){
    private var data: ArrayList<String> = arrayListOf<String>("Court Court", "Plaza", "Park", "Library", "Cafe", "Restroom")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotsViewHolder {
        val filterSpotsBinding: ItemFilterButtonsBinding = ItemFilterButtonsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return SpotsViewHolder( filterSpotsBinding.root)
    }

    override fun onBindViewHolder(holder: SpotsViewHolder, position: Int) {
        val flag = position % 2 == 0
        holder.bindData(data[position], flag)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}