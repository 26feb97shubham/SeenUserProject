package com.seen.user.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.seen.user.R
import com.seen.user.model.*
import com.seen.user.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.item_category_wise_searches_layout.view.*

class CategoryWiseListAdapter(
    private val context: Context,
    private val categoryWiseList: ArrayList<DataItem?>?,
    private val findNavController: NavController
) : RecyclerView.Adapter<CategoryWiseListAdapter.CategoryWiseListAdapterVH>() {
    private var brandsListAdapter : BrandListAdapter?=null
    private var homemadesuppliersListAdapter : HmeMadeSuppAdapter?=null
    private var healthandbeautyListAdapter : HlthBeautyAdapter?=null
    private var bloggersListAdapter : BlgrsAdapter?=null
    private var brandsList : ArrayList<BrandsItem?>?=null
    private var homemadesuppList : ArrayList<HomeMadeSuppliersItem?>?=null
    private var healthandbeautyList : ArrayList<HealthAndBeautyItem?>?=null
    private var bloggersList : ArrayList<BloggersItem?>?=null
    inner class CategoryWiseListAdapterVH(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryWiseListAdapterVH {
        val view= LayoutInflater.from(context).inflate(R.layout.item_category_wise_searches_layout, parent, false)
        return CategoryWiseListAdapterVH(view)
    }

    override fun onBindViewHolder(holder: CategoryWiseListAdapterVH, position: Int) {
        val categoryWiseItem = categoryWiseList?.get(position)


        val lang = SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")

        if (categoryWiseItem?.name.equals("BRANDS", false)){
            val name = if (lang.equals("ar")){
                "العلامات التجارية"
            }else{
                "BRANDS"
            }
            holder.itemView.tv_category_searches.text = name
        }else if (categoryWiseItem?.name.equals("HOMEMADE SUPPLIERS", false)){
            val name = if (lang.equals("ar")){
                "الموردين المحليين"
            }else{
                "HOMEMADE SUPPLIERS"
            }
            holder.itemView.tv_category_searches.text = name
        }else if (categoryWiseItem?.name.equals("BLOGGERS", false)){
            val name = if (lang.equals("ar")){
                "المدونون"
            }else{
                "BLOGGERS"
            }
            holder.itemView.tv_category_searches.text = name
        }else if (categoryWiseItem?.name.equals("HEALTH AND BEAUTY", false)){
            val name = if (lang.equals("ar")){
                "الصحة والجمال"
            }else{
                "HEALTH AND BEAUTY"
            }
            holder.itemView.tv_category_searches.text = name
        }


        if (categoryWiseItem?.data?.size==0){
            holder.itemView.tv_no_items_found.visibility = View.VISIBLE
            holder.itemView.rv_category_wise_product.visibility = View.GONE
        }else{
            holder.itemView.tv_no_items_found.visibility = View.GONE
            holder.itemView.rv_category_wise_product.visibility = View.VISIBLE
        }

        holder.itemView.rv_category_wise_product.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        brandsListAdapter = BrandListAdapter(context,
            categoryWiseItem?.data as ArrayList<DataItem?>?, findNavController)
        holder.itemView.rv_category_wise_product.adapter = brandsListAdapter
        brandsListAdapter?.notifyDataSetChanged()

       /* homemadesuppliersListAdapter = HmeMadeSuppAdapter(context, homemadesuppList, findNavController)
        holder.itemView.rv_category_wise_product.adapter = brandsListAdapter
        homemadesuppliersListAdapter?.notifyDataSetChanged()

        healthandbeautyListAdapter = HlthBeautyAdapter(context, healthandbeautyList, findNavController)
        holder.itemView.rv_category_wise_product.adapter = brandsListAdapter
        healthandbeautyListAdapter?.notifyDataSetChanged()

        bloggersListAdapter = BlgrsAdapter(context, bloggersList, findNavController)
        holder.itemView.rv_category_wise_product.adapter = brandsListAdapter
        bloggersListAdapter?.notifyDataSetChanged()*/


    }

    override fun getItemCount(): Int {
        return categoryWiseList?.size!!
    }
}