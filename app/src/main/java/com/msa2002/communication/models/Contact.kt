package com.msa2002.communication.models

data class Contact(
    val contactId: String,
    var name: String,
    var mobileNumber: String,
    var address: String,
    var createdAt: String,
    var updatedAt: String
) {
    companion object {
        var type = ""
    }
}