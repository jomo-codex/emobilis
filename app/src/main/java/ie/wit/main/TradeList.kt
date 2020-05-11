package ie.wit.main

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class TradeList : Application(), AnkoLogger {

    lateinit var auth: FirebaseAuth
    lateinit var database: DatabaseReference

    override fun onCreate() {
        super.onCreate()
        info("App started")
    }
}

