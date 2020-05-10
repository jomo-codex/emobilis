package ie.wit.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.PagerAdapter.POSITION_NONE
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import ie.wit.R
import ie.wit.main.DonationApp
import ie.wit.models.AdsModel
import ie.wit.utils.createLoader
import ie.wit.utils.hideLoader
import ie.wit.utils.showLoader
import kotlinx.android.synthetic.main.fragment_ads_description.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_tab1.view.*
import kotlinx.android.synthetic.main.fragment_tab2.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast

class AdDescriptionFragment : Fragment(), AnkoLogger {

    lateinit var app: DonationApp
    lateinit var loader : AlertDialog
    lateinit var root: View
    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout
    var ad: AdsModel? = null
    var adUserDetails : HashMap<String, String>  = HashMap<String, String> ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as DonationApp
        arguments?.let {
            ad = it.getParcelable("adDescription")
        }

        // get user contact details who posted ad
        val databaseReference = FirebaseDatabase.getInstance().reference
        val userDetailsReference = databaseReference.child("user-details").child(ad!!.userId)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                adUserDetails["email"] = dataSnapshot.child("email").getValue(String::class.java).toString()
                adUserDetails["username"] = dataSnapshot.child("name").getValue(String::class.java).toString()
                adUserDetails["facebook"] = dataSnapshot.child("facebook").getValue(String::class.java).toString()
                adUserDetails["phone"] = dataSnapshot.child("phone").getValue(String::class.java).toString()
                dataSnapshot.child("favourites").children.forEach{
                    if(it.key.toString() == ad?.uid){
                        root.addTofav.tag = "FabTrue"
                        root.addTofav.setImageResource(R.drawable.star_big_on)
                    }
//                    getValue<DonationModel>(DonationModel::class.java)
//                    if(donation!!.isfav) {
//                        favouritesList.add(donation)
//                    }
//                    root.recyclerView.adapter =
//                        FavouritesAdapter(favouritesList)
//                    root.recyclerView.adapter?.notifyDataSetChanged()
//                    checkSwipeRefresh()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                activity?.toast("Could not connect to database$databaseError")
            }
        }
        userDetailsReference.addListenerForSingleValueEvent(menuListener)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_ads_description, container, false)
        activity?.title = "Ad description"
        loader = createLoader(activity!!)

        viewPager = root.findViewById(R.id.viewPager)
        tabLayout = root.findViewById(R.id.tabLayout)

        tabLayout.addTab(tabLayout.newTab().setText("Product details"))
        tabLayout.addTab(tabLayout.newTab().setText("Product description"))

        val adapter = TabPagerAdapter((activity as AppCompatActivity).supportFragmentManager, tabLayout.tabCount )
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                info { "onTabSelected "+ tab.position }
//                info { "onTabSelected "+ ad!!.name }

                viewPager.currentItem = tab.position
                root.product_name.text = ad?.name
                root.product_shortdescription.text = ad?.short_description
                root.posted_on.text = ad?.posted_on
                root.posted_by.text = adUserDetails["username"]
                root.contact_details.text = adUserDetails["email"]
                root.long_description.text = ad?.description
                root.price_exchange.text = ad?.name_price

            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                info { "onTabUnselected "+ tab.position }
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
                info { "onTabReselected "+ tab.position }
            }
        })

        root.addTofav.setOnClickListener {
            info { "these2" + root.addTofav.tag }
            if (root.addTofav.tag.toString() == "FabFalse") {
                val uid = app.auth.currentUser!!.uid
//                val key = app.database.child("user-details").child(app.auth.currentUser!!.uid).child("favourites").push().key

                val key= ad?.uid
                val addingFav = ad?.toMap()
                val childUpdates = HashMap<String, Any>()
                childUpdates["/user-details/$uid/favourites/$key"] = addingFav.toString()

                app.database.updateChildren(childUpdates)
                Toast.makeText(context, "Added to favourites!", Toast.LENGTH_SHORT).show()
                root.addTofav.tag = "FabTrue"
                root.addTofav.setImageResource(R.drawable.star_big_on)
            }else {
                val uid = app.auth.currentUser!!.uid
                app.database.child("user-details").child(app.auth.currentUser!!.uid).child("favourites").child(ad!!.uid.toString()).removeValue()
//
//                ad?.uid = key
//                val addingFav = ad?.toMap()
//                val childUpdates = HashMap<String, Any>()
//                childUpdates["/user-details/$uid/favourites/$key"] = addingFav.toString()
//
//                app.database.updateChildren(childUpdates)

                root.addTofav.tag = "FabFalse"
                root.addTofav.setImageResource(R.drawable.star_big_off)
                Toast.makeText(context, "Removed from favourites!", Toast.LENGTH_SHORT).show()
            }

        }

        Picasso.get().load(ad!!.imageUrl).into(root.product_image)

//        tabLayout.getTabAt(0)?.select()

        info {  "showmethis" + adUserDetails["favourites"] }
        return root
    }


    companion object {
        @JvmStatic
        fun newInstance(ad: AdsModel) =
            AdDescriptionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("adDescription", ad)
                }
            }
    }
}


class TabPagerAdapter(fm: FragmentManager, private var tabCount: Int) :
    FragmentStatePagerAdapter(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return Fragment1()
            1 -> return Fragment2()
        }
        throw IllegalStateException("position $position is invalid for this viewpager")
    }

    override fun getCount(): Int {
        return tabCount
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "Tab " + (position + 1)
    }

}

