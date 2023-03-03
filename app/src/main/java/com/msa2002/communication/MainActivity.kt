package com.msa2002.communication

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
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
    lateinit var dialog: ProgressDialog
    val allContacts = ArrayList<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialog.setTitle("The data is loading")
        dialog.setMessage("Please wait...")
        dialog.setCancelable(false)
        dialog.show()

        checkData(allContacts)

        // هيبحث عن النص المكتوب بالسماء والأرقام, اذا في جهات اتصال هيعرضها
        binding.etSearch.addTextChangedListener {
            val searchContacts = ArrayList<Contact>()
            val search = binding.etSearch.text.toString()
            if(search.isNotEmpty()){
                // البحث بكون من الArrayList عشان البحث يكون سريع.. وببحث عن النص بالعنوان والملاحظة
                for(i in 0 until allContacts.size){
                    if(allContacts[i].name.contains(search, true) ||
                        allContacts[i].mobileNumber.contains(search)){
                        searchContacts.add(allContacts[i])
                    }
                }
                if(searchContacts.isEmpty()){
                    binding.contactsRecyclerView.visibility = View.GONE
                    binding.linearIfImpty.visibility = View.VISIBLE
                    binding.tvNoContact.setText("There is no contact with this name or number")
                }else{
                    binding.linearIfImpty.visibility = View.GONE
                    binding.contactsRecyclerView.visibility = View.VISIBLE
                    checkData(searchContacts)
                }
            }else{
                binding.linearIfImpty.visibility = View.GONE
                binding.contactsRecyclerView.visibility = View.VISIBLE
                checkData(allContacts)
            }
        }


        binding.showDialog.setOnClickListener {
            Contact.type = "addContact"
            val bottomDialog = BottomDialogFragment(object : BottomDialogFragment.OnContactListener {
                override fun addContact(contact: Contact) {
                    allContacts.add(contact)
                    checkData(allContacts)
                }

                override fun updateContact(contact: Contact, position: Int) {}
            })
            bottomDialog.show(this.supportFragmentManager, "addContact")
        }

    }

    private fun checkData(array: ArrayList<Contact>) {
        if (array.isEmpty()) {
            getAllContacts()
        } else {
            var myContactsAdapter = MyContactsAdapter(this, array, object : MyContactsAdapter.OnContactListener {
                override fun deleteContact(contact: Contact) {
                    array.remove(contact)
                    checkData(array)
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
                    }
                }
                checkData(allContacts)
                dialog.dismiss()
            }

            .addOnFailureListener { error ->
                Toast.makeText(this, "Error,  ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("msa", "Error,  ${error.message}")
            }
    }

}