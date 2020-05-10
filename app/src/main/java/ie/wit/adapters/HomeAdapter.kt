package ie.wit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.R
import ie.wit.fragments.HomeFragment
import ie.wit.models.AdsModel
import ie.wit.models.DonationModel
import kotlinx.android.synthetic.main.card_donation.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.home_cardview.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

interface HomeListener {
    fun onAdClick(ad: AdsModel)
}

class HomeAdapter(
    var ads: ArrayList<AdsModel>,
    private val listener: HomeListener
)
    : RecyclerView.Adapter<HomeAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.home_cardview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val ad = ads[holder.adapterPosition]
        holder.bind(ad, listener)
    }

    override fun getItemCount(): Int = ads.size

    fun removeAt(position: Int) {
        ads.removeAt(position)
        notifyItemRemoved(position)
    }

    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView), AnkoLogger {
        fun bind(ad: AdsModel, listener: HomeListener) {
           info { "this comes from adapter" + ad }
            itemView.product_name.text = ad.name
            itemView.product_price.text = ad.name_price
            Picasso.get().load(ad.imageUrl).into(itemView.product_image)
            itemView.product_image.setOnClickListener { listener.onAdClick(ad) }
        }
    }
}