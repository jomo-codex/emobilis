package ie.wit.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ie.wit.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class Fragment1 : Fragment(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.title = ""
info { "rendering from fragment1" }
        return inflater?.inflate(R.layout.fragment_tab1, container, false)

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            Fragment1().apply {
                arguments = Bundle().apply { }
            }
    }
}
