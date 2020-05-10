package ie.wit.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import ie.wit.R
import ie.wit.main.DonationApp
import ie.wit.models.AdsModel
import ie.wit.utils.createLoader
import kotlinx.android.synthetic.main.fragment_tab1.view.*
import kotlinx.android.synthetic.main.fragment_tab2.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class AdDescriptionFragment : Fragment(), AnkoLogger {

    lateinit var app: DonationApp
    lateinit var loader : AlertDialog
    lateinit var root: View
    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout
    var ad: AdsModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as DonationApp
        arguments?.let {
            ad = it.getParcelable("adDescription")
        }
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

        info { "show me different detail each time" + ad?.name }

        val adapter = TabPagerAdapter((activity as AppCompatActivity).supportFragmentManager, tabLayout.tabCount )
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                info { "onTabSelected "+ tab.position }
                viewPager.currentItem = tab.position
                viewPager.product_name.text = ad?.name ?: "testing"
                viewPager.long_description.text = ad?.name ?: "testing desscription"
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                info { "onTabUnselected "+ tab.position }
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
                info { "onTabReselected "+ tab.position }
            }
        })
        tabLayout.getTabAt(0)?.select()
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
    FragmentStatePagerAdapter(fm) {

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

