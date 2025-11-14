package com.mobdeve.s18.group5.bayanihanspots.spots

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.R
import com.mobdeve.s18.group5.bayanihanspots.model.Spots

/**
 * A fragment representing a list of Items.
 */
class SpotFragment : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_spot_list, container, false)

        if (view is RecyclerView) {
            val recyclerView = view
            recyclerView.layoutManager = if (columnCount <= 1)
                LinearLayoutManager(context)
            else
                GridLayoutManager(context, columnCount)

            val db = FirebaseFirestore.getInstance()
            db.collection("spots")
                .get()
                .addOnSuccessListener { result ->
                    val spotList = result.toObjects(Spots::class.java)
                    recyclerView.adapter = MySpotRecyclerViewAdapter(spotList)
                    Log.d("SpotFragment", "$result")
                }
                .addOnFailureListener { e ->
                    Log.e("SpotFragment", "Error loading spots", e)
                }
        }
        return view
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            SpotFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}