package ie.wit.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import ie.wit.R
import ie.wit.adapters.HomeAdapter
import ie.wit.adapters.HomeListener
import ie.wit.main.DonationApp
import ie.wit.models.AdsModel
import ie.wit.utils.createLoader
import ie.wit.utils.hideLoader
import ie.wit.utils.showLoader
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_report.view.*
import kotlinx.android.synthetic.main.fragment_report.view.recyclerView
import kotlinx.android.synthetic.main.fragment_report.view.swiperefresh
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class HomeFragment : Fragment(), AnkoLogger, HomeListener {

    lateinit var app: DonationApp
    var isSearch = false
    lateinit var loader: AlertDialog
    lateinit var eventListener: ValueEventListener
    lateinit var root: View
    lateinit var searchKey: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as DonationApp
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_home, container, false)
        loader = createLoader(activity!!)
        activity?.title = "Home"

        root.recyclerView.layoutManager = GridLayoutManager(activity, 2)
        setSwipeRefresh()
        getAllAdvertisments()

        val username = root.findViewById<EditText>(R.id.search_bar)
        username.onRightDrawableClicked {
            isSearch = true
            searchKey = it.text.toString()
            getAllAdvertisments()
//            it.text.clear()
        }

        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
        this.setOnTouchListener { v, event ->
            var hasConsumed = false
            if (v is EditText) {
                if (event.x >= v.width - v.totalPaddingRight) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        onClicked(this)
                    }
                    hasConsumed = true
                }
            }
            hasConsumed
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    fun setSwipeRefresh() {
        root.swiperefresh.setOnRefreshListener {
            root.swiperefresh.isRefreshing = true
            getAllAdvertisments()
        }
    }

    fun checkSwipeRefresh() {
        if (root.swiperefresh.isRefreshing) root.swiperefresh.isRefreshing = false
    }

    fun getAllAdvertisments() {
        loader = createLoader(activity!!)
        showLoader(loader, "Loading all advertisements from Database")
        val adsList = ArrayList<AdsModel>()
        app.database.child("advertisements")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    info("Firebase Donation error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    children.forEach {
                        val ad = it.getValue(AdsModel::class.java)
                       if (isSearch){
                           if (ad!!.name.contains(searchKey)){
                               adsList.add(ad)
                           }
                       }else
                           adsList.add(ad!!)

                        root.recyclerView.adapter =
                            HomeAdapter(adsList, this@HomeFragment)
                        root.recyclerView.adapter?.notifyDataSetChanged()
                        checkSwipeRefresh()

                        app.database.child("advertisements")
                            .removeEventListener(this)
                    }
                    info { "This is the final list of advertismenrs$adsList" }
                    hideLoader(loader)
                }

            })
    }

    override fun onAdClick(ad: AdsModel) {
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.homeFrame, AdDescriptionFragment.newInstance(ad))
            .addToBackStack(null)
            .commit()
    }
}
