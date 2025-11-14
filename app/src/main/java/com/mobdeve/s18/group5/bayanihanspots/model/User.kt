package com.mobdeve.s18.group5.bayanihanspots.model


class User (var id: Int, var email: String, var firstName: String, var lastName: String) {
    companion object {
        private const val DEFAULT_ID = -1
    }

    constructor(email: String, firstName: String, lastName: String) : this(DEFAULT_ID, email, firstName, lastName)
    constructor() : this(DEFAULT_ID, "BLANK", "BLANK", "BLANK")
}