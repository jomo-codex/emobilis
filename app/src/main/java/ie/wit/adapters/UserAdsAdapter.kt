package ie.wit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.R
import ie.wit.models.AdsModel
import kotlinx.android.synthetic.main.card_ad.view.*

interface AdListener {
    fun onAdClick(userad: AdsModel)
}

class UserAdsAdapter constructor(var userads: ArrayList<AdsModel>,
                                  private val listener: AdListener)
    : RecyclerView.Adapter<UserAdsAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_ad,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val userad = userads[holder.adapterPosition]
        holder.bind(userad,listener)
    }

    override fun getItemCount(): Int = userads.size

    fun removeAt(position: Int) {
        userads.removeAt(position)
        notifyItemRemoved(position)
    }

    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(userad: AdsModel, listener: AdListener) {
            itemView.tag = userad
            itemView.productName.text = userad.name
            itemView.shortdersciption.text = userad.short_description
            Picasso.get().load(userad.imageUrl).into(itemView.AdimageIcon)
            itemView.setOnClickListener { listener.onAdClick(userad) }
        }
    }
}