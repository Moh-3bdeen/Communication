package com.msa2002.communication.adapters

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa2002.communication.MainActivity
import com.msa2002.communication.R
import com.msa2002.communication.databinding.RvContactListBinding
import com.msa2002.communication.fragments.BottomDialogFragment
import com.msa2002.communication.models.Contact

class MyContactsAdapter(var activity: Activity, var data: ArrayList<Contact>, var onContactListener: OnContactListener) :
    RecyclerView.Adapter<MyContactsAdapter.ContactsViewHolder>() {
    // عشان أمرر الcontect  وأحذفه من الواجهة بعد ما ينحذف من الfirebase
    interface OnContactListener {
        fun deleteContact(contact: Contact)
    }
    inner class ContactsViewHolder(var binding: RvContactListBinding) :
        RecyclerView.ViewHolder(binding.root)

    val db = Firebase.firestore
    lateinit var dialog: ProgressDialog

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        return ContactsViewHolder(RvContactListBinding.inflate(LayoutInflater.from(activity), parent, false))
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val contact = data[position]

        holder.binding.tvName.text = contact.name
        holder.binding.tvMobile.text = contact.mobileNumber
        holder.binding.tvAddress.text = contact.address

        holder.binding.btnEdit.setOnClickListener {
            Contact.type = "editContact"

            // إرسال البيانات الى الdialog عشان أعرض البيانات عشان يعدلها
            val args = Bundle()
            args.putInt("position", position)
            args.putString("id", contact.contactId)
            args.putString("name", contact.name)
            args.putString("mobile", contact.mobileNumber)
            args.putString("address", contact.address)
            args.putString("createdAt", contact.createdAt)
            val bottomSheet = BottomDialogFragment(object : BottomDialogFragment.OnContactListener {
                override fun addContact(contact: Contact) {}

                // تعديل العنصر وتحيث الصفحة
                override fun updateContact(contact: Contact, position: Int) {
                    data[position] = contact
                    notifyDataSetChanged()
                }
            })
            bottomSheet.setArguments(args)
            bottomSheet.show((activity as MainActivity).supportFragmentManager, bottomSheet.tag)
        }

        holder.binding.btnDelete.setOnClickListener {
            val alert = AlertDialog.Builder(activity)
            alert.setTitle("Delete Contact")
            alert.setIcon(R.drawable.ic_delete)
            alert.setMessage("Are you sure you want delete this contact ?")

            alert.setPositiveButton("Yes") { d, i ->
                dialog = ProgressDialog(activity)
                dialog.setTitle("Deleting Contact")
                dialog.setMessage("Please wait...")
                dialog.setCancelable(false)
                dialog.show()

                deleteContact(contact)
            }

            alert.setNegativeButton("No") { d, i ->
                d.cancel()
            }
            alert.show()
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun deleteContact(contact: Contact) {
        db.collection("Contacts").document(contact.contactId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(activity, "Contact has been deleted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                data.remove(contact)
                onContactListener.deleteContact(contact)
                notifyDataSetChanged()
            }

            .addOnFailureListener { error ->
                dialog.dismiss()
                Toast.makeText(activity, "Delete failed.\n${error.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

}
