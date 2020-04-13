package ie.wit.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import ie.wit.R
import ie.wit.main.DonationApp
import ie.wit.models.DonationModel
import ie.wit.utils.createLoader
import ie.wit.utils.hideLoader
import ie.wit.utils.showLoader
import kotlinx.android.synthetic.main.fragment_edit.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class EditFragment : Fragment(), AnkoLogger {

    lateinit var app: DonationApp
    lateinit var loader : AlertDialog
    lateinit var root: View
    var editDonation: DonationModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as DonationApp

        arguments?.let {
            editDonation = it.getParcelable("editdonation")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_edit, container, false)
        activity?.title = getString(R.string.action_edit)
        loader = createLoader(activity!!)

        root.editAmount.setText(editDonation!!.amount.toString())
        root.editPaymenttype.setText(editDonation!!.paymenttype)
        root.editMessage.setText(editDonation!!.message)
        root.editUpvotes.setText(editDonation!!.upvotes.toString())

        root.editUpdateButton.setOnClickListener {
            showLoader(loader, "Updating Donation on Server...")
            updateDonationData()
            updateDonation(editDonation!!.uid, editDonation!!)
            updateUserDonation(app.auth.currentUser!!.uid,
                               editDonation!!.uid, editDonation!!)
        }

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(donation: DonationModel) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("editdonation",donation)
                }
            }
    }

    fun updateDonationData() {
        editDonation!!.amount = root.editAmount.text.toString().toInt()
        editDonation!!.message = root.editMessage.text.toString()
        editDonation!!.upvotes = root.editUpvotes.text.toString().toInt()
    }

    fun updateUserDonation(userId: String, uid: String?, donation: DonationModel) {
        app.database.child("user-donations").child(userId).child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(donation)
                        activity!!.supportFragmentManager.beginTransaction()
                        .replace(R.id.homeFrame, ReportFragment.newInstance())
                        .addToBackStack(null)
                        .commit()
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Donation error : ${error.message}")
                    }
                })
    }

    fun updateDonation(uid: String?, donation: DonationModel) {
        app.database.child("donations").child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(donation)
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Donation error : ${error.message}")
                    }
                })
    }
}
