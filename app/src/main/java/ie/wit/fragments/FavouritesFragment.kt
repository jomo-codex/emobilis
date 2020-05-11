package ie.wit.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import ie.wit.R
import ie.wit.adapters.FavListener
import ie.wit.adapters.FavouritesAdapter
import ie.wit.main.TradeList
import ie.wit.models.AdsModel
import ie.wit.utils.*
import kotlinx.android.synthetic.main.fragment_report.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class FavouritesFragment : Fragment(), AnkoLogger , FavListener {

    lateinit var app: TradeList
    lateinit var loader : AlertDialog
    lateinit var root: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as TradeList
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_favourites, container, false)
        activity?.title = getString(R.string.action_favourites)

        root.recyclerView.setLayoutManager(LinearLayoutManager(activity))
        setSwipeRefresh()

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            FavouritesFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    fun setSwipeRefresh() {
        root.swiperefresh.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                root.swiperefresh.isRefreshing = true
                getAllFavouriteAds(app.auth.currentUser!!.uid)
            }
        })
    }

    fun checkSwipeRefresh() {
        if (root.swiperefresh.isRefreshing) root.swiperefresh.isRefreshing = false
    }


    override fun onResume() {
        super.onResume()
        getAllFavouriteAds(app.auth.currentUser!!.uid)
    }

    fun getAllFavouriteAds(userId: String?) {
        loader = createLoader(activity!!)
        showLoader(loader, "Downloading ads from Firebase")
        val favouritesList = ArrayList<String>()
        app.database.child("user-details").child(userId!!).child("favourites")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    info("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoader(loader)
                    val children = snapshot.children
                    children.forEach {
                        favouritesList.add(it.key.toString())
                        app.database.child("user-details").child("favourites")
                            .removeEventListener(this)
                    }
                    getAllFavouriteAdsFromDatabase(favouritesList)
                }
            })

    }

    fun getAllFavouriteAdsFromDatabase(key: ArrayList<String>) {
        val AdsList = ArrayList<AdsModel>()
        key.forEach { s ->
            app.database.child("user-advertisements").child(s)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase error : ${error.message}")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        hideLoader(loader)
                        val children = snapshot.children
                        children.forEach {
                            val userAd = it.getValue<AdsModel>(AdsModel::class.java)
                           if(userAd!!.name.isNotEmpty()) {
                               AdsList.add(userAd)
                               info { "this is my adlist"+ AdsList }
                               root.recyclerView.adapter =
                                   FavouritesAdapter(AdsList, this@FavouritesFragment)
                               root.recyclerView.adapter?.notifyDataSetChanged()
                           }
                            checkSwipeRefresh()
                            app.database.child("user-advertisements").child(s)
                                .removeEventListener(this)
                        }
                    }
                })
        }
    }

}
