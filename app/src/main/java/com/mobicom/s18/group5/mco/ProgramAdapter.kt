package com.mobicom.s18.group5.mco

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ProgramAdapter(private val dataList: List<ProgramModel>):
RecyclerView.Adapter<ProgramAdapter.ViewHolder> (){

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val programImage : ImageView = itemView.findViewById(R.id.programImage)
        val programName : TextView = itemView.findViewById(R.id.programName)
        val location: TextView = itemView.findViewById(R.id.locationText)
        val date : TextView = itemView.findViewById(R.id.dateText)
        val favouriteButton: ImageButton = itemView.findViewById(R.id.favouriteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.programs_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val programs = dataList[position]
        holder.programImage.setImageResource(programs.imageResId)
        holder.programName.text = programs.text
        holder.location.text = programs.location
        holder.date.text = programs.date

        holder.favouriteButton.setOnClickListener {
            val isFavourited = it.tag as? Boolean ?: false

            if (!isFavourited){
                Toast.makeText(holder.itemView.context, "Added to favourites", Toast.LENGTH_SHORT).show()
                holder.favouriteButton.setImageResource(R.drawable.heart_red)
                it.tag = true
            }
            else {
                Toast.makeText(holder.itemView.context, "Removed from favourites", Toast.LENGTH_SHORT).show()
                holder.favouriteButton.setImageResource(R.drawable.heart_black)
                it.tag = false

            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }



}