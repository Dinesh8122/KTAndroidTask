package com.example.ktandroidtask

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ktandroidtask.pojoModels.UpdateLocation
import com.example.ktandroidtask.pojoModels.UserLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.realm.RealmResults
import kotlinx.android.synthetic.main.user_location_item.view.*

class UserLocationAdapter(var items: RealmResults<UpdateLocation>, var context: Context) : RecyclerView.Adapter<UserLocationAdapter.RecyclerViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {

        return RecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.user_location_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val item = items.get(position)

        Log.i("UserLocationAdapter", "check datas in recyclerView Adapter" + items.count());

        if (item != null) {
            holder.email.text = item.email
            try {

                val geoCoder = Geocoder(context)
                val addresses = item.latitude?.let {
                    item.longitude?.let { it1 ->
                        geoCoder.getFromLocation(
                            it,
                            it1,
                            1
                        )
                    }
                }

                if (addresses?.size == 0) {
                    Log.i("Home", "showMarker (line 1168): No address")
                    return
                }
                val addressFromApi = addresses?.get(0)!!.getAddressLine(0)
                val city = addresses.get(0).locality
                if (addressFromApi != null && city != null) {
                    Log.i("Home", "showMarker:$ address:$addressFromApi city ${city}" )
                    holder.location.text = city

                }

            } catch (ex: Exception) {
                Log.i("Home", "showMarker (line 104): Location is null.")
                ex.printStackTrace()

            }
            holder.date.text = item.date
            holder.itemHolder.setOnClickListener {
              (context as Home).showMarker(item.email,LatLng(item.latitude!!,item.longitude!!))
            }
        }

    }

    override fun getItemCount(): Int {

        return items.size

    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val email = itemView.email_display
        val location = itemView.location_display
        val date = itemView.date_display
        val itemHolder = itemView.item_holder
    }
}