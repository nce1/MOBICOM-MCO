package com.mobdeve.s18.group5.bayanihanspots.spots

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mobdeve.s18.group5.bayanihanspots.R

import com.mobdeve.s18.group5.bayanihanspots.databinding.FragmentSpotBinding
import com.mobdeve.s18.group5.bayanihanspots.model.Spots

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MySpotRecyclerViewAdapter(
    private val values: List<Spots>
) : RecyclerView.Adapter<MySpotRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentSpotBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.nameView.text = item.name
        holder.typeView.text = item.type
        holder.statusView.text = item.status
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentSpotBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameView: TextView = binding.itemName
        val typeView: TextView = binding.itemType
        val statusView: TextView = binding.itemStatus
    }

}