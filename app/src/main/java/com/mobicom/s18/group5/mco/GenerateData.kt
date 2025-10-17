package com.mobicom.s18.group5.mco

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class GenerateData{
    companion object {
        fun loadSpots(mMap: GoogleMap) {
            val libHenry = LatLng(14.565108711009975, 120.99327711163771)
            mMap.addMarker(MarkerOptions().position(libHenry).title("Library").snippet("Click me for more info!"))
            val cafeBarn = LatLng(14.567643191462741, 120.99211092364685)
            mMap.addMarker(MarkerOptions().position(cafeBarn).title("Cafe").snippet("Click me for more info!"))
            val plSherwood = LatLng(14.567662267182852, 120.9931236477381)
            mMap.addMarker(MarkerOptions().position(plSherwood).title("Plaza").snippet("Click me for more info!"))
            val rrManRes = LatLng(14.566542312601708, 120.99360957683905)
            mMap.addMarker(MarkerOptions().position(rrManRes).title("Rest Room").snippet("Click me for more info!"))
            val ccRazon = LatLng(14.566975282815504, 120.99210238668144)
            mMap.addMarker(MarkerOptions().position(ccRazon).title("Covered Court").snippet("Click me for more info!"))
            val paAgno = LatLng(14.565716708189116, 120.99277476096034)
            mMap.addMarker(MarkerOptions().position(paAgno).title("Park").snippet("Click me for more info!"))
        }
    }
}