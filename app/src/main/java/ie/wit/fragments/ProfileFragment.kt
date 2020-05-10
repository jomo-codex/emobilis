package ie.wit.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import ie.wit.R
import ie.wit.adapters.FavouritesAdapter
import ie.wit.main.DonationApp
import ie.wit.models.AdsModel
import ie.wit.models.DonationModel
import ie.wit.models.UserModel
import ie.wit.utils.*
import kotlinx.android.synthetic.main.fragment_postads.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_report.view.*
import kotlinx.android.synthetic.main.home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*

class ProfileFragment : Fragment(), AnkoLogger {

    lateinit var app: DonationApp
    lateinit var loader : AlertDialog
    lateinit var root: View
    lateinit var storageReference: StorageReference
    val IMAGE_REQUEST = 1
    var ImageUrl : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as DonationApp

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_profile, container, false)
        activity?.title = getString(R.string.title_profile)

//        Firebase storage instance
        storageReference = FirebaseStorage.getInstance().getReference(app.auth.currentUser?.email +"_"+ app.auth.currentUser?.uid)

        //Fetch user details and populate in user profile
        LoadingUserDetails( app.auth.currentUser!!.uid)
        val databaseRefrence = FirebaseDatabase.getInstance().reference
        val userDetailsReference = databaseRefrence.child("user-details").child(app.auth.currentUser!!.uid )
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                root.profile_username.text =  dataSnapshot.child("name").getValue(String::class.java).toString()
                Picasso.get().load(dataSnapshot.child("imageUrl").getValue(String::class.java).toString()).into(root.profile_image)
                root.profile_email.text = dataSnapshot.child("email").getValue(String::class.java).toString()

                val database_profile_address    = dataSnapshot.child("address").getValue(String::class.java).toString()
                val database_profile_phone      = dataSnapshot.child("phone").getValue(String::class.java).toString()
                val database_profile_facebook   = dataSnapshot.child("facebook").getValue(String::class.java).toString()
                if (database_profile_address.isNotEmpty() && database_profile_address!="null"){
                    root.profile_address.text = database_profile_address
                }
                if (database_profile_phone.isNotEmpty() && database_profile_phone!="null"){
                    root.profile_phone.text = database_profile_phone
                }
                if (database_profile_facebook.isNotEmpty() && database_profile_facebook!="null"){
                    root.profile_facebook.text = database_profile_facebook
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                activity?.toast("Could not connect to database")
            }
        }
        userDetailsReference.addListenerForSingleValueEvent(menuListener)

        //initialise button listeners
        setButtonListener(root)

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ProfileFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    fun setButtonListener( layout: View) {
        layout.profile_image.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            val chooser = Intent.createChooser(intent, R.string.select_image.toString())
            startActivityForResult(chooser, IMAGE_REQUEST)
        }

        layout.profile_username.setOnClickListener {
            val title = "Edit name"
            val textToEdit = root.profile_username.text
            val fieldToEditInDatabase = "name"
            val textToEditHint = "Enter $fieldToEditInDatabase"
            val builder = android.app.AlertDialog.Builder(view?.context)
            val inflater = layoutInflater
            builder.setTitle(title)
            val dialogLayout = inflater.inflate(R.layout.dialog_popup, null)
            val editText  = dialogLayout.findViewById<TextView>(R.id.edit_dialog)
            editText.text = textToEdit
            editText.hint = textToEditHint
            builder.setView(dialogLayout)
            builder.setPositiveButton("OK") {
                    _, _ ->
                run {
                    if(editText.text.toString().trim().isNotEmpty()){
                        app.database.child("user-details").child(app.auth.currentUser!!.uid ).child(fieldToEditInDatabase).setValue(editText.text.toString().trim())
                        root.profile_username.text = editText.text.toString()
                        Toast.makeText(
                            view?.context,
                            "Updated $fieldToEditInDatabase is " + editText.text.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        Toast.makeText(
                            view?.context,
                            "$fieldToEditInDatabase can't be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
            builder.show()
        }

        layout.profile_address.setOnClickListener {
            val title = "Edit address"
            val textToEdit = root.profile_address.text
            val fieldToEditInDatabase = "address"
            val textToEditHint = "Enter $fieldToEditInDatabase"
            val builder = android.app.AlertDialog.Builder(view?.context)
            val inflater = layoutInflater
            builder.setTitle(title)
            val dialogLayout = inflater.inflate(R.layout.dialog_popup, null)
            val editText  = dialogLayout.findViewById<TextView>(R.id.edit_dialog)
            editText.text = textToEdit
            editText.hint = textToEditHint
            builder.setView(dialogLayout)
            builder.setPositiveButton("OK") {
                    _, _ ->
                run {
                    if(editText.text.toString().trim().isNotEmpty()){
                        app.database.child("user-details").child(app.auth.currentUser!!.uid ).child(fieldToEditInDatabase).setValue(editText.text.toString().trim())
                        root.profile_address.text = editText.text.toString()
                        Toast.makeText(
                            view?.context,
                            "Updated $fieldToEditInDatabase is " + editText.text.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        Toast.makeText(
                            view?.context,
                            "$fieldToEditInDatabase can't be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
            builder.show()
        }

        layout.profile_phone.setOnClickListener {
            val title = "Edit phone"
            val textToEdit = root.profile_phone.text
            val fieldToEditInDatabase = "phone"
            val textToEditHint = "Enter $fieldToEditInDatabase"
            val builder = android.app.AlertDialog.Builder(view?.context)
            val inflater = layoutInflater
            builder.setTitle(title)
            val dialogLayout = inflater.inflate(R.layout.dialog_popup, null)
            val editText  = dialogLayout.findViewById<TextView>(R.id.edit_dialog)
            editText.text = textToEdit
            editText.hint = textToEditHint
            builder.setView(dialogLayout)
            builder.setPositiveButton("OK") {
                    _, _ ->
                run {
                    if(editText.text.toString().trim().isNotEmpty()){
                        app.database.child("user-details").child(app.auth.currentUser!!.uid ).child(fieldToEditInDatabase).setValue(editText.text.toString().trim())
                        root.profile_phone.text = editText.text.toString()
                        Toast.makeText(
                            view?.context,
                            "Updated $fieldToEditInDatabase is " + editText.text.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        Toast.makeText(
                            view?.context,
                            "$fieldToEditInDatabase can't be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
            builder.show()
        }

        layout.profile_facebook.setOnClickListener {
            val title = "Edit facebook profile"
            val textToEdit = root.profile_facebook.text
            val fieldToEditInDatabase = "facebook"
            val textToEditHint = "Enter $fieldToEditInDatabase"
            val builder = android.app.AlertDialog.Builder(view?.context)
            val inflater = layoutInflater
            builder.setTitle(title)
            val dialogLayout = inflater.inflate(R.layout.dialog_popup, null)
            val editText  = dialogLayout.findViewById<TextView>(R.id.edit_dialog)
            editText.text = textToEdit
            editText.hint = textToEditHint
            builder.setView(dialogLayout)
            builder.setPositiveButton("OK") {
                    _, _ ->
                run {
                    if(editText.text.toString().trim().isNotEmpty()){
                        app.database.child("user-details").child(app.auth.currentUser!!.uid ).child(fieldToEditInDatabase).setValue(editText.text.toString().trim())
                        root.profile_facebook.text = editText.text.toString()
                        Toast.makeText(
                            view?.context,
                            "Updated $fieldToEditInDatabase is " + editText.text.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        Toast.makeText(
                            view?.context,
                            "$fieldToEditInDatabase can't be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
            builder.show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IMAGE_REQUEST -> {
                if (data != null) {
                    showLoader(loader, "Updating Image")
                    root.profile_image.setImageBitmap(readImageFragment(data))
                    val uploadTask = storageReference.putFile(data.data!!)
                    uploadTask.continueWithTask{
                            task->
                        if(!task.isSuccessful){
                            info { "uploadFailed" }
                        }
                        storageReference.downloadUrl
                    }.addOnCompleteListener{
                            task->
                        if(task.isSuccessful){
                            val downloadUri = task.result
                            ImageUrl = downloadUri!!.toString()
                            hideLoader(loader)
                            app.database.child("user-details").child(app.auth.currentUser!!.uid ).child("imageUrl").setValue(ImageUrl)
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

    fun LoadingUserDetails(userId: String?) {
        loader = createLoader(activity!!)
        showLoader(loader, "Fetching user details from Firebase")
        app.database.child("user-advertisements").child(userId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    info("Firebase Donation error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoader(loader)
                    val userData = snapshot.children
                    var countPosts = 0
                    var countVotes = 0
                    userData.forEach {
                        val eachPost = it.
                        getValue(AdsModel::class.java)
                        if (eachPost!!.name.isNotEmpty()){
                            countPosts++
                        }
                        if (eachPost.upvotes > 0){
                            countVotes += eachPost.upvotes
                        }
                        app.database.child("user-advertisements").child(userId)
                            .removeEventListener(this)
                    }
                    root.total_ads_posted.text = countPosts.toString()
                    root.total_likes.text = countVotes.toString()
                }
            })
    }

}
