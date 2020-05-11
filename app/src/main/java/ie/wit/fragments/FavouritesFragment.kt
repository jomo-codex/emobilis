package ie.wit.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import ie.wit.R
import ie.wit.adapters.FavouritesAdapter
import ie.wit.main.TradeList
import ie.wit.models.DonationModel
import ie.wit.utils.*
import kotlinx.android.synthetic.main.fragment_report.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class FavouritesFragment : Fragment(), AnkoLogger {

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

        val swipeDeleteHandler = object : SwipeToDeleteCallback(activity!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = root.recyclerView.adapter as FavouritesAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                removeFromFavourites(app.auth.currentUser!!.uid,
                                  (viewHolder.itemView.tag as DonationModel).uid)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(root.recyclerView)
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
                getAllFavouriteDonations(app.auth.currentUser!!.uid)
            }
        })
    }

    fun checkSwipeRefresh() {
        if (root.swiperefresh.isRefreshing) root.swiperefresh.isRefreshing = false
    }

    fun removeFromFavourites(userId: String, uid: String?) {
        app.database.child("user-donations").child(userId).child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.removeValue()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Donation error : ${error.message}")
                    }
                })
    }

    fun View.snack(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }

    override fun onResume() {
        super.onResume()
        getAllFavouriteDonations(app.auth.currentUser!!.uid)
    }

    fun getAllFavouriteDonations(userId: String?) {
        loader = createLoader(activity!!)
        showLoader(loader, "Downloading Donations from Firebase")
        val favouritesList = ArrayList<DonationModel>()
        app.database.child("user-donations").child(userId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    info("Firebase Donation error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoader(loader)
                    val children = snapshot.children
                    children.forEach {
                        val donation = it.
                            getValue<DonationModel>(DonationModel::class.java)
                        if(donation!!.isfav) {
                            favouritesList.add(donation)
                        }
                        root.recyclerView.adapter =
                            FavouritesAdapter(favouritesList)
                        root.recyclerView.adapter?.notifyDataSetChanged()
                        checkSwipeRefresh()
                    }
                }
            })
    }
}
