package ie.wit.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import ie.wit.R
import ie.wit.main.DonationApp
import ie.wit.utils.createLoader
import ie.wit.utils.hideLoader
import ie.wit.utils.showLoader
import kotlinx.android.synthetic.main.fragment_postads.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.IOException
import java.util.*
import android.widget.Toast
import ie.wit.models.AdsModel


class PostAdsFragment : Fragment(), AnkoLogger {

    lateinit var app: DonationApp
    var totalDonated = 0
    lateinit var loader : AlertDialog
    lateinit var root: View
    lateinit var eventListener : ValueEventListener
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
        storageReference = FirebaseStorage.getInstance().getReference(app.auth.currentUser?.email +"_"+ app.auth.currentUser?.uid+"_adPosting_"+ SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()))
        root = inflater.inflate(R.layout.fragment_postads, container, false)
        loader = createLoader(activity!!)
        activity?.title = getString(R.string.action_postads)

        setButtonListener(root)
        return root;
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            PostAdsFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    fun setButtonListener( layout: View) {
        layout.imageButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            val chooser = Intent.createChooser(intent, R.string.select_image.toString())
            startActivityForResult(chooser, IMAGE_REQUEST)
        }

        layout.postNow.setOnClickListener{
            val productName = layout.product_name.text.trim().toString()
            val priceOrBarter = layout.product_price_name.text.trim().toString()
            val productShortDescription = layout.product_short_description.text.trim().toString()
            val productDescription = layout.product_description.text.trim().toString()
            val postedOn = SimpleDateFormat("yyyy:MM:dd:HH:mm:ss").format(Date())
            if (productName.isNotEmpty() && priceOrBarter.isNotEmpty() && productShortDescription.isNotEmpty() && productDescription.isNotEmpty() && ImageUrl.length > 5)
                savePostDataToDatabase(AdsModel(name= productName, name_price=priceOrBarter, short_description=productShortDescription, description=productDescription, posted_on=postedOn , imageUrl=ImageUrl))
            else
                Toast.makeText(context , "All fields are required!", Toast.LENGTH_SHORT).show()
        }

    }


    fun savePostDataToDatabase(ads: AdsModel) {
        showLoader(loader, "Adding Post")
        info("Firebase DB Reference : $app.database")
        val uid = app.auth.currentUser!!.uid
        val key = app.database.child("advertisements").push().key
        if (key == null) {
            info("Firebase Error : Key Empty")
            return
        }
        ads.uid = key
        ads.userId = uid
        val adsValues = ads.toMap()
        val childUpdates = HashMap<String, Any>()
        childUpdates["/advertisements/$key"] = adsValues
        childUpdates["/user-advertisements/$uid/$key"] = adsValues

        app.database.updateChildren(childUpdates)
        hideLoader(loader)
        Toast.makeText(context , "Your Ad is now live!", Toast.LENGTH_SHORT).show()
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.homeFrame, HomeFragment.newInstance())
            .addToBackStack(null)
            .commit()

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
                            val downloadUri = task.result
                            ImageUrl = downloadUri!!.toString()
                            hideLoader(loader)
//                            app.database.child("user-details").child(app.auth.currentUser!!.uid ).child("imageUrl").setValue(url)
                            Log.d("directLink", ImageUrl)
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

