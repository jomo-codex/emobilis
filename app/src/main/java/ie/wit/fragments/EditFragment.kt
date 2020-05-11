package ie.wit.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

import ie.wit.R
import ie.wit.main.TradeList
import ie.wit.models.AdsModel
import ie.wit.utils.createLoader
import ie.wit.utils.hideLoader
import ie.wit.utils.showLoader
import kotlinx.android.synthetic.main.fragment_editads.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.IOException
import java.util.*

class EditFragment : Fragment(), AnkoLogger {

    lateinit var app: TradeList
    lateinit var loader : AlertDialog
    lateinit var root: View
    var editAd: AdsModel? = null
    lateinit var storageReference: StorageReference
    val IMAGE_REQUEST = 1
    lateinit var downloadUri : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as TradeList

        arguments?.let {
            editAd = it.getParcelable("editAd")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_editads, container, false)
        activity?.title = "Edit Ad"
        loader = createLoader(activity!!)
        storageReference = FirebaseStorage.getInstance().getReference(app.auth.currentUser?.email +"_"+ app.auth.currentUser?.uid+"_adPosting_"+ SimpleDateFormat("yyyyMMdd_HHmmss").format(
            Date()
        ))

        // Assign values to fields
        root.product_name.setText(editAd!!.name)
        root.product_price_name.setText(editAd!!.name_price)
        root.product_short_description.setText(editAd!!.short_description)
        root.product_description.setText(editAd!!.description)
        downloadUri = editAd!!.imageUrl
        Picasso.get().load(editAd!!.imageUrl).into(root.imageUploaded)

        ActivateListners()

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(ad: AdsModel) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("editAd", ad)
                }
            }
    }

    fun ActivateListners(){
        root.postNow.setOnClickListener {
            showLoader(loader, "Updating ad on Server...")
            updateAdData()
            updateAd(editAd!!.uid, editAd!!)
            updateUserAd(app.auth.currentUser!!.uid,
                editAd!!.uid, editAd!!)
        }

        root.imageButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            val chooser = Intent.createChooser(intent, R.string.select_image.toString())
            startActivityForResult(chooser, IMAGE_REQUEST)
        }

    }

    fun updateAdData() {
        editAd!!.name = root.product_name.text.toString()
        editAd!!.name_price = root.product_price_name.text.toString()
        editAd!!.short_description = root.product_short_description.text.toString()
        editAd!!.description = root.product_description.text.toString()
        editAd!!.imageUrl = downloadUri
    }

    fun updateUserAd(userId: String, uid: String?, ad: AdsModel) {
        app.database.child("user-advertisements").child(userId).child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(ad)
                        activity!!.supportFragmentManager.beginTransaction()
                        .replace(R.id.homeFrame, MyadsFragment.newInstance())
                        .addToBackStack(null)
                        .commit()
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Donation error : ${error.message}")
                    }
                })
    }

    fun updateAd(uid: String?, ad: AdsModel) {
        app.database.child("advertisements").child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(ad)
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Donation error : ${error.message}")
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IMAGE_REQUEST -> {
                if (data != null) {
                    info { "Yes this one $data" }
                    showLoader(loader, "Adding Image")
                    root.imageUploaded.setImageBitmap(readImageFragment(data))
                    val uploadTask = storageReference.putFile(data.data!!)
                    uploadTask.continueWithTask{
                            task->
                        if(!task.isSuccessful){
                            info { "uploadFailed" }
                        }
                        storageReference!!.downloadUrl
                    }.addOnCompleteListener{
                            task->
                        if(task.isSuccessful){
                            downloadUri = task.result.toString()
                            hideLoader(loader)
                        }
                    }
                }
            }
        }
    }

    fun readImageFragment(data: Intent?): Bitmap? {
        var bitmap: Bitmap? = null
        if (data != null && data.data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, data.data)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bitmap
    }


}
