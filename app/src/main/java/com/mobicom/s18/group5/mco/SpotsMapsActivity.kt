package com.mobicom.s18.group5.mco

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.appcompat.app.AlertDialog
import com.mobicom.s18.group5.mco.databinding.ActivitySpotsMapsBinding
import java.util.logging.Filter

class SpotsMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivitySpotsMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySpotsMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapSMA) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.svSMA.requestFocus()
        val adapter = FilterButtonsAdapter()
        binding.rvFilterSMA.adapter = adapter
        binding.rvFilterSMA.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val location = LatLng(14.564376175229377, 120.99388768121005)
        val zoomLevel = 20f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
        GenerateData.loadSpots(mMap)
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.setOnMarkerClickListener { marker ->
            AlertDialog.Builder(this)
                .setTitle(marker.title)
                .setMessage("Welcome to the ${marker.title}!")
                .setNeutralButton("Close") { dialog, _ -> dialog.dismiss()}
                .setPositiveButton("View") {dialog, _ ->
                    val intent = Intent(this, SpotsActivity::class.java)
                    intent.putExtra("TITLE", marker.title)
                    startActivity(intent)
                    dialog.dismiss()
                }
                .show()
            true
        }
    }
}