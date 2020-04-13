package ie.wit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.wit.R
import ie.wit.models.DonationModel
import kotlinx.android.synthetic.main.card_donation.view.*

interface DonationListener {
    fun onDonationClick(donation: DonationModel)
}

class DonationAdapter constructor(var donations: ArrayList<DonationModel>,
                                  private val listener: DonationListener)
    : RecyclerView.Adapter<DonationAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_donation,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val donation = donations[holder.adapterPosition]
        holder.bind(donation,listener)
    }

    override fun getItemCount(): Int = donations.size

    fun removeAt(position: Int) {
        donations.removeAt(position)
        notifyItemRemoved(position)
    }

    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(donation: DonationModel, listener: DonationListener) {
            itemView.tag = donation
            itemView.paymentamount.text = donation.amount.toString()
            itemView.paymentmethod.text = donation.paymenttype
            itemView.imageIcon.setImageResource(R.mipmap.ic_launcher_round)
            itemView.setOnClickListener { listener.onDonationClick(donation) }
        }
    }
}