package com.seen.user.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.seen.user.R
import com.seen.user.interfaces.ClickInterface
import com.seen.user.model.CategoryName
import com.seen.user.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.item_name_list.view.*


class NameListAdapter(private val context: Context, private val data:ArrayList<CategoryName>, private val clickInstance:ClickInterface.ClickPosTypeInterface): RecyclerView.Adapter<NameListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_name_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val requestOptions: RequestOptions =
            RequestOptions().error(R.drawable.default_icon).centerCrop()

        if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""].equals("ar")){
            holder.itemView.category.text = data[position].categories_ar
            holder.itemView.address.text = data[position].country_served_name_ar
        }else{
            holder.itemView.category.text = data[position].categories
            holder.itemView.address.text = data[position].country_served_name
        }

        holder.itemView.name.text = data[position].name
        holder.itemView.txtRating.text = data[position].rating.toString()
        holder.itemView.ratingBar.rating = data[position].rating.toFloat()
        if(data[position].like){
            holder.isLike=true
            holder.itemView.imgLike.setImageResource(R.drawable.heart_red)
        }
        else{
            holder.isLike=false
            holder.itemView.imgLike.setImageResource(R.drawable.gray_heart)
        }
        holder.itemView.imgLike.setOnClickListener {
            holder.itemView.imgLike.startAnimation(AlphaAnimation(1f, 0.5f))
            clickInstance.clickPostionType(position, "Like")

        }
        Glide.with(context).load(data[position].profile_picture)
            .listener(object : RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.itemView.itemNameListProgressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.itemView.itemNameListProgressBar.visibility = View.GONE
                    return false
                }

            })
            .apply(requestOptions)
            .into(holder.itemView.img)

        holder.itemView.setOnClickListener {
            holder.itemView.startAnimation(AlphaAnimation(1f, 0.5f))
            clickInstance.clickPostionType(position, "Click")
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var isLike:Boolean=false
    }
}