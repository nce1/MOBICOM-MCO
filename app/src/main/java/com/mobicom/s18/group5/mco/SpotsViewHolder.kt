package com.mobicom.s18.group5.mco

import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.ArrayList

class SpotsViewHolder(itemView: View): ViewHolder(itemView) {
    fun bindData(btnString: String?, flag: Boolean?){
        if (btnString != null && flag != null){
            val button: Button = itemView.findViewById(R.id.btnFilterItem)
            button.text = btnString
            if (flag){
                button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.secondary)))
                button.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
            } else{
                button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.primary)))
                button.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
            }
        }
    }

    fun bindDataImage(imageID: Int){
        val image: ImageView = itemView.findViewById(R.id.ivImageItem)
        image.setImageResource(imageID)
    }

    fun bindReview(review: String){
        val tvReview: TextView = itemView.findViewById(R.id.tvReviewIRS)
        val tvUser: TextView = itemView.findViewById(R.id.tvUserIRS)
        val tvRate: TextView = itemView.findViewById(R.id.tvRateIRS)

        var temp = "John Piper 101: "
        tvUser.text = temp
        temp = "4.0"
        tvRate.text = temp
        tvReview.text = review
    }
}