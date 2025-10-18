package com.mobicom.s18.group5.mco

object ProgramsData {
    fun getData(): List<ProgramModel> {
        return listOf(
            ProgramModel(R.drawable.pacopark, "Clean the Park", "2025-10-14", "Manila"),
            ProgramModel(R.drawable.elnido_beach, "Collect Turtle Eggs", "2025-11-03", "El Nido"),
            ProgramModel(R.drawable.binondo_church, "Decorate the Church", "2025-12-21", "Binondo"),
            ProgramModel(R.drawable.home_angels, "Teach Students", "2026-01-08", "Quezon City"),
            ProgramModel(R.drawable.boracay_beach, "Beach Cleaning", "2025-11-27", "Boracay"),
            ProgramModel(R.drawable.dlsu_campus,"Examination Procter Volunteer", "2025-12-12", "Dasmariñas"),
            ProgramModel(R.drawable.coffee_academics, "Barista Experience", "2025-10-29", "Baguio"),
            ProgramModel(R.drawable.school_blind, "Blind People Assistance", "2025-11-16", "Cebu City"),
            ProgramModel(R.drawable.gentle_hands_inc, "Activity Coordinator for Orphans", "2026-01-19", "Davao City"),
            ProgramModel(R.drawable.pasig_river, "Clean the River", "2025-12-05", "Pasig")
        )
    }
}