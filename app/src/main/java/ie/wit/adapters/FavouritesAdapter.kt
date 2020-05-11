package ie.wit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.R
import ie.wit.fragments.FavouritesFragment
import ie.wit.models.AdsModel
import kotlinx.android.synthetic.main.card_ad.view.*

interface FavListener {

}
class FavouritesAdapter(var ads: ArrayList<AdsModel>, private val listener: FavListener)
    : RecyclerView.Adapter<FavouritesAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.card_ad,
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

    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(ad: AdsModel, listener: FavListener) {
            itemView.tag = ad
            itemView.productName.text = ad.name
            itemView.shortdersciption.text = ad.short_description
            Picasso.get().load(ad.imageUrl).into(itemView.AdimageIcon)
        }
    }
}