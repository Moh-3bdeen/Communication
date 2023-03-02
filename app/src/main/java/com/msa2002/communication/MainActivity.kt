package com.msa2002.communication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa2002.communication.adapters.MyContactsAdapter
import com.msa2002.communication.databinding.ActivityMainBinding
import com.msa2002.communication.fragments.BottomDialogFragment
import com.msa2002.communication.models.Contact

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val db = Firebase.firestore
    val allContacts = ArrayList<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkData()

        binding.showDialog.setOnClickListener {
            Contact.type = "addContact"
            val bottomDialog =
                BottomDialogFragment(object : BottomDialogFragment.OnContactListener {
                    override fun addContact(contact: Contact) {
                        allContacts.add(contact)
                        checkData()
                    }

                    override fun updateContact(contact: Contact, position: Int) {}
                })
            bottomDialog.show(this.supportFragmentManager, "addContact")
        }

    }

    private fun checkData() {
        if (allContacts.isEmpty()) {
            getAllContacts()
        } else {
            var myContactsAdapter =
                MyContactsAdapter(this, allContacts, object : MyContactsAdapter.OnContactListener {
                    override fun deleteContact(contact: Contact) {
                        allContacts.remove(contact)
                        checkData()
                    }
                })
            binding.contactsRecyclerView.adapter = myContactsAdapter
            binding.contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        }
    }

    private fun getAllContacts() {
        db.collection("Contacts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documentReference ->
                if (documentReference.isEmpty) {
                    binding.contactsRecyclerView.visibility = View.GONE
                    binding.linearIfImpty.visibility = View.VISIBLE
                } else {
                    binding.linearIfImpty.visibility = View.GONE
                    binding.contactsRecyclerView.visibility = View.VISIBLE

                    for (contact in documentReference) {
                        val userContact = Contact(contact.id, contact.getString("name").toString(),
                            contact.getString("mobile").toString(), contact.getString("address").toString(),
                            contact.getString("createdAt").toString(), contact.getString("updatedAt").toString()
                        )
                        allContacts.add(userContact)
                        checkData()
                    }
                }
                checkData()
            }

            .addOnFailureListener { error ->
                Toast.makeText(this, "Error,  ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("msa", "Error,  ${error.message}")
            }
    }

}