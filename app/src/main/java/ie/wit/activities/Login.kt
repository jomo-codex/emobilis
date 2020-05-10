package ie.wit.activities


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ie.wit.R
import ie.wit.main.DonationApp
import ie.wit.models.UserModel
import ie.wit.utils.createLoader
import ie.wit.utils.hideLoader
import ie.wit.utils.showLoader
import org.jetbrains.anko.*
import java.util.*


class Login : AppCompatActivity(), AnkoLogger {

    lateinit var app: DonationApp
    lateinit var loader : AlertDialog
    private val RC_SIGN_IN = 9001
    private lateinit var userPic : String
    private lateinit var userName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as DonationApp
        app.auth = FirebaseAuth.getInstance()
        app.database = FirebaseDatabase.getInstance().reference
        loader = createLoader(this)

        if (app.auth.currentUser != null) {
            // Already signed in
            startActivity<Home>()
            finish()
        }
        else {
            // Not signed in. Start the login flow.
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                        listOf(
                            EmailBuilder().build(),
                            GoogleBuilder().build()
                        )
                    )
                    .setTheme(R.style.AppTheme)
                    .setIsSmartLockEnabled(false)
                    .build(),
                RC_SIGN_IN
            )
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // RC_SIGN_IN is the request code you passed when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (response != null) {
                if(response.isNewUser){
                    if(response.user.photoUri.toString().length > 5){
                        this.userPic = response.user.photoUri.toString()
                        this.userName = response.user.name.toString()
                    }else{
                        this.userPic = "https://firebasestorage.googleapis.com/v0/b/donationo-app.appspot.com/o/fallback.png?alt=media&token=95087e2a-ae35-4664-ab59-9bd708fc1c82"
                        this.userName = "Add your name"
                    }
                    addUserDetails(UserModel(uid = app.auth.currentUser!!.uid, email = app.auth.currentUser?.email,
                        name= this.userName, imageUrl = this.userPic
                    ))
                }
            }
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                startActivity<Home>()
                finish()
                return
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.e("Login", "Login canceled by User")
                    return
                }
                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    Log.e("Login", "No Internet Connection")
                    return
                }
                if (response.error!!.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    Log.e("Login", "Unknown Error")
                    return
                }
            }
            Log.e("Login", "Unknown sign in response")
        }
    }

    private fun addUserDetails(user: UserModel) {
        showLoader(loader, "Adding user")
        val uid = app.auth.currentUser!!.uid
        val userDetails = user.toMap()
        val childUpdates = HashMap<String, Any>()
        childUpdates["/user-details/$uid"] = userDetails
        app.database.updateChildren(childUpdates)
        hideLoader(loader)
    }


}
