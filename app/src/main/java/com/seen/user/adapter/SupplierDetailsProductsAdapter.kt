package com.seen.user.adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.seen.user.R
import com.seen.user.interfaces.ClickInterface
import com.seen.user.model.ProductsItem
import com.seen.user.model.Profile
import com.seen.user.rest.ApiClient
import com.seen.user.rest.ApiInterface
import com.seen.user.utils.LogUtils
import com.seen.user.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.item_recent_products.view.*
import kotlinx.android.synthetic.main.item_recent_products.view.iv_add_to_cart
import kotlinx.android.synthetic.main.item_recent_products.view.iv_like
import kotlinx.android.synthetic.main.item_recent_products.view.iv_recent_product
import kotlinx.android.synthetic.main.item_recent_products.view.tv_recent_product_item_name
import kotlinx.android.synthetic.main.item_recent_products.view.tv_recent_product_item_price
import kotlinx.android.synthetic.main.item_supplier_products.view.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class SupplierDetailsProductsAdapter(
    private val context: Context,
    private val productsList : ArrayList<ProductsItem>,
    private val profile: Profile,
    private val navController: NavController,
    private val clickPosInterface: ClickInterface.ClickPosInterface
) :RecyclerView.Adapter<SupplierDetailsProductsAdapter.SupplierDetailsProductsAdapterVH>()  {
    inner class SupplierDetailsProductsAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView){
        var isLike:Boolean=false

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SupplierDetailsProductsAdapterVH {
        val view = LayoutInflater.from(context).inflate(R.layout.item_supplier_products, parent, false)
        return SupplierDetailsProductsAdapterVH(view)
    }

    override fun onBindViewHolder(holder: SupplierDetailsProductsAdapterVH, position: Int) {
        Log.e("pos_1", position.toString())
        Glide.with(context).load(profile.profile_picture)
            .placeholder(R.drawable.user).into(holder.itemView.civ_supplier_image)
        Glide.with(context).load(productsList[position].files)
            .placeholder(R.drawable.user).into(holder.itemView.iv_recent_product)
        holder.itemView.tv_recent_product_item_name.text = productsList[position].name
        holder.itemView.tv_recent_product_item_price.text = "AED "+productsList[position].price

        if(productsList[position].like == true){
            holder.itemView.iv_like.setImageResource(R.drawable.heart_red)
        }
        else{
            holder.itemView.iv_like.setImageResource(R.drawable.heart_white)
        }
        holder.itemView.iv_like.setOnClickListener {
            Log.e("position_3", ""+position)
            val products = productsList[position]
            Log.e("position_3_id", ""+productsList[position].id+ "   "+position)
            if(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0]==0){
                LogUtils.shortToast(context, context.getString(R.string.please_login_signup_to_access_this_functionality))
                val args= Bundle()
                args.putString("reference", "OffersDiscount")
                navController.navigate(R.id.chooseLoginSingUpFragment, args)
                return@setOnClickListener
            }

            Log.e("product_id", productsList[position].id.toString())
            Log.e("pos", position.toString())

            val apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)

            val builder = ApiClient.createBuilder(arrayOf("user_id", "product_id", "lang"),
                arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString(), products.id.toString(), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""].toString()))

            val call = apiInterface.likeUnlikeProduct(builder.build())
            call!!.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                    try {
                        if (response.body() != null) {
                            val jsonObject = JSONObject(response.body()!!.string())

                            if(jsonObject.getInt("response")==1){
                                products.like = !products.like!!
                                LogUtils.shortToast(context, jsonObject.getString("message"))
                                notifyDataSetChanged()

                            }
                            else{
                                LogUtils.shortToast(context, jsonObject.getString("message"))
                            }


                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, throwable: Throwable) {
                    LogUtils.e("msg", throwable.message)
                    LogUtils.shortToast(context, context.getString(R.string.check_internet))
                }
            })
        }

        holder.itemView.iv_add_to_cart.setOnClickListener {
            clickPosInterface.clickPostion(position, "Cart")
        }



        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            productsList[position].id?.let { it1 -> bundle.putInt("product_id", it1) }
            navController.navigate(R.id.productDetailsFragment, bundle)
        }
    }

    override fun getItemCount(): Int {
        return productsList.size
    }
}