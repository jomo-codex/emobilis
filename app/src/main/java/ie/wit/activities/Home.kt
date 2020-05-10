package ie.wit.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ie.wit.R
import ie.wit.main.DonationApp
import ie.wit.utils.showImagePicker
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import ie.wit.utils.readImage
import com.squareup.picasso.Picasso
import android.app.AlertDialog
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import ie.wit.fragments.*


class Home : AppCompatActivity(), AnkoLogger,
    NavigationView.OnNavigationItemSelectedListener {

    lateinit var ft: FragmentTransaction
    lateinit var app: DonationApp
    lateinit var storageReference: StorageReference
    val IMAGE_REQUEST = 1
    lateinit var imageUrl: String
    lateinit var userName: String
    lateinit var bottomNavView :BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        setSupportActionBar(toolbar)
        app = application as DonationApp
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action",
                Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }

        navView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Initialize bottom navigation
        bottomNavView = findViewById(R.id.navigation)
        bottomNavView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        //Populate user email in profile
        navView.getHeaderView(0).nav_header_email.text = app.auth.currentUser?.email

        //Create imageName for user profile image
        storageReference = FirebaseStorage.getInstance().getReference(app.auth.currentUser?.email +"_"+ app.auth.currentUser?.uid)

        //Change user profile image on click
        navView.getHeaderView(0).chooseImage.setOnClickListener{
            showImagePicker(this, IMAGE_REQUEST)
        }

        //Fetch user details and populate in user profile
        app.database = FirebaseDatabase.getInstance().reference
        var userDetailsReference = app.database.child("user-details").child(app.auth.currentUser!!.uid )
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                imageUrl =  dataSnapshot.child("imageUrl").getValue(String::class.java).toString()
                userName =  dataSnapshot.child("name").getValue(String::class.java).toString()
                info { imageUrl}
                Picasso.get().load(imageUrl).into(navView.getHeaderView(0).chooseImage);
                navView.getHeaderView(0).nav_header_username.text = userName
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        userDetailsReference.addListenerForSingleValueEvent(menuListener)

        // Load home fragment
        ft = supportFragmentManager.beginTransaction()
        val fragment = HomeFragment.newInstance()
        ft.replace(R.id.homeFrame, fragment)
        ft.commit()
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    bottomNavView.menu.findItem(R.id.navigation_home).isChecked = true
                    navigateTo(HomeFragment.newInstance())
                }
                R.id.navigation_postads-> {
                    bottomNavView.menu.findItem(R.id.navigation_postads).isChecked = true
                    navigateTo(PostAdsFragment.newInstance())
                }
                R.id.navigation_profile -> {
                    bottomNavView.menu.findItem(R.id.navigation_profile).isChecked = true
                    navigateTo(ProfileFragment.newInstance())
                }
            }
            false
        }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_donate ->
                navigateTo(HomeFragment.newInstance())
            R.id.nav_report ->
                navigateTo(ReportFragment.newInstance())
            R.id.nav_favourites ->
                navigateTo(FavouritesFragment.newInstance())
            R.id.nav_aboutus ->
                navigateTo(AboutUsFragment.newInstance())
            R.id.nav_sign_out ->
                signOut()

            else -> toast("You Selected Something Else")
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_donate -> toast("You Selected Donate")
            R.id.action_report -> toast("You Selected Report")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
         else
            super.onBackPressed()
    }

    private fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.homeFrame, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IMAGE_REQUEST -> {
                if (data != null) {
                    navView.getHeaderView(0).chooseImage.setImageBitmap(readImage(this, resultCode, data))
                    val uploadTask = storageReference.putFile(data.data!!)
                    uploadTask.continueWithTask{
                        task->
                            if(!task.isSuccessful){
                                toast("uploadFailed")
                            }
                        storageReference!!.downloadUrl
                    }.addOnCompleteListener{
                        task->
                            if(task.isSuccessful){
                                val downloadUri = task.result
                                val url = downloadUri!!.toString()
                                app.database.child("user-details").child(app.auth.currentUser!!.uid ).child("imageUrl").setValue(url)
                                Log.d("directLink", url)
                            }
                    }
                }
            }
        }
    }

    private fun signOut(){
        info{ "signOutClicked" }
        app.auth.signOut()
        startActivity<Login>()
        finish()
    }

    fun triggerDialogBox(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Edit name")
        val dialogLayout = inflater.inflate(R.layout.dialog_popup, null)
        val editText  = dialogLayout.findViewById<TextView>(R.id.edit_dialog)
        editText.text = userName
        builder.setView(dialogLayout)
        builder.setPositiveButton("OK") {
                dialogInterface, i ->
            run {
                if(editText.text.toString().trim().length > 0){
                    app.database.child("user-details").child(app.auth.currentUser!!.uid ).child("name").setValue(editText.text.toString().trim())
                    app.database.child("user-details").child(app.auth.currentUser!!.uid ).child("email").setValue(app.auth.currentUser!!.email)
                    navView.getHeaderView(0).nav_header_username.text = editText.text.toString().trim()
                    Toast.makeText(
                        applicationContext,
                        "Updated name is " + editText.text.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    Toast.makeText(
                        applicationContext,
                        "Name can't be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
        builder.show()
    }


}
