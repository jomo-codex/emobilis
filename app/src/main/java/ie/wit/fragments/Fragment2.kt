package ie.wit.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ie.wit.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class Fragment2 : Fragment(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.title = getString(R.string.aboutus_title)
info { "rendering from fragment2" }
        return inflater?.inflate(R.layout.fragment_tab2, container, false)

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            Fragment2().apply {
                arguments = Bundle().apply { }
            }
    }
}
