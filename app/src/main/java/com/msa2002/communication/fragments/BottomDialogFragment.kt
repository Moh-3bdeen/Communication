package com.msa2002.communication.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa2002.communication.databinding.FragmentBottomDialogBinding
import com.msa2002.communication.models.Contact
import java.text.SimpleDateFormat
import java.util.*

class BottomDialogFragment(var onContactListener: OnContactListener) : BottomSheetDialogFragment() {
    interface OnContactListener {
        fun addContact(contact: Contact)
        fun updateContact(contact: Contact, position: Int)
    }

    private lateinit var binding: FragmentBottomDialogBinding
    lateinit var dialog: ProgressDialog
    val db = Firebase.firestore
    lateinit var id: String
    lateinit var createdAt: String
    var position: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBottomDialogBinding.inflate(inflater, container, false)

        dialog = ProgressDialog(requireContext())
        dialog.setTitle("Adding Contact")
        dialog.setMessage("Please wait...")
        dialog.setCancelable(false)

        if (Contact.type == "editContact") {
            // استقبال البينات المرسلة من الadapter
            val mArgs = arguments
            position = mArgs!!.getInt("position", -1)
            id = mArgs!!.getString("id", "")
            val name = mArgs!!.getString("name")
            val mobile = mArgs!!.getString("mobile")
            val address = mArgs!!.getString("address")
            createdAt = mArgs!!.getString("createdAt", "")

            binding.tvAddAndUpdate.text = "Edit contact data"
            binding.btnAdd.text = "Edit contact"
            binding.etName.setText("$name")
            binding.etMobile.setText("$mobile")
            binding.etAddress.setText("$address")
        }


        binding.btnAdd.setOnClickListener {
            val newName = binding.etName.text.toString()
            val newMobile = binding.etMobile.text.toString()
            val newAddress = binding.etAddress.text.toString()

            if (newName.trim().isEmpty() || newMobile.trim().isEmpty() || newAddress.trim().isEmpty()) {
                Toast.makeText(requireContext(), "Fill all fields !", Toast.LENGTH_SHORT).show()
            } else {
                if (Contact.type == "addContact") {
                    dialog.show()
                    val createdAt = SimpleDateFormat("yyyy/MM/dd - HH:mm:").format(Date())
                    addContact(newName, newMobile, newAddress, createdAt, createdAt)
                } else if (Contact.type == "editContact") {
                    dialog.setTitle("Updating Contact")
                    dialog.show()
                    val lastUpdated = SimpleDateFormat("yyyy/MM/dd - HH:mm:").format(Date())
                    updateContact(id, newName, newMobile, newAddress, lastUpdated)
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }


    private fun addContact(name: String, mobile: String, address: String, createdAt: String, updatedAt: String) {
        val contact = hashMapOf(
            "name" to name,
            "mobile" to mobile,
            "address" to address,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )

        db.collection("Contacts")
            .add(contact)
            .addOnSuccessListener { documentReference ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Contact has been added successfully", Toast.LENGTH_SHORT).show()
                val contact = Contact(documentReference.id, name, mobile, address, createdAt, createdAt)
                onContactListener.addContact(contact)
                dismiss()
            }
            .addOnFailureListener { e ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Error, ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateContact(contactId: String, name: String, mobile: String, address: String, updatedAt: String) {
        val contact = HashMap<String, Any>()
        contact["name"] = name
        contact["mobile"] = mobile
        contact["address"] = address
        contact["updatedAt"] = updatedAt

        db.collection("Contacts").document(contactId)
            .update(contact)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Contact has been updated successfully", Toast.LENGTH_SHORT).show()
                val contact = Contact(contactId, name, mobile, address, createdAt, updatedAt)
                onContactListener.updateContact(contact, position)
                dismiss()
            }

            .addOnFailureListener { error ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

}