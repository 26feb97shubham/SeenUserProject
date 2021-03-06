package com.seen.user.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.seen.user.R
import com.seen.user.activity.LoginActivity
import com.seen.user.adapter.CartAdapter
import com.seen.user.interfaces.ClickInterface
import com.seen.user.model.CalcShippingFees
import com.seen.user.model.Cart
import com.seen.user.rest.ApiClient
import com.seen.user.rest.ApiInterface
import com.seen.user.utils.LogUtils
import com.seen.user.utils.SharedPreferenceUtility
import com.seen.user.utils.Utility
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_cart.view.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class CartFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mView:View
    lateinit var cartAdapter: CartAdapter
    var cartList=ArrayList<Cart>()
    var userId:Int=0
    var responseBody:String=""
    var myShippingFees=0
    var myTotalPrice=0
    var myTotalDisc=0
    var myTaxes=0
    var mySubTotal=0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_cart, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        setUpViews()
        myCart()
        return mView
    }

    private fun setUpViews() {
        //requireActivity().itemCart.setImageResource(R.drawable.basket_active_icon)
        userId= SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0]

        requireActivity().frag_other_backImg.visibility=View.VISIBLE

        requireActivity().frag_other_backImg.setOnClickListener {
            requireActivity().frag_other_backImg.startAnimation(AlphaAnimation(1f, 0.5f))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().frag_other_backImg)
            findNavController().navigate(R.id.homeFragment)
        }

        mView.rvList.layoutManager=LinearLayoutManager(requireContext())
        cartAdapter= CartAdapter(requireContext(), cartList, object : ClickInterface.ClickPosTypeInterface{
            override fun clickPostionType(pos: Int, getType: String) {
                var type=""
                var product_type=""
                when (getType) {
                    "Details" -> {
                        val bundle=Bundle()
                        bundle.putInt("product_id", cartList[pos].product_id)
                        findNavController().navigate(R.id.action_cartFragment_to_productDetailsFragment, bundle)
                    }
                    "Minus" -> {
                        type="2"
                        product_type="2"
                        cartAdd(cartList[pos].product_id, cartList[pos].product_item_id,  cartList[pos].id, 1,  type, product_type)
                    }
                    "Plus" -> {
                        type="2"
                        product_type="1"
                        cartAdd(cartList[pos].product_id, cartList[pos].product_item_id,  cartList[pos].id, 1,  type, product_type)
                    }
                    "Supplier" -> {
                        val bundle=Bundle()
                        bundle.putInt("supplier_user_id", cartList[pos].supplier_id)
                        findNavController().navigate(R.id.supplierDetailsFragment, bundle)
                    }

                    else -> {
                        type="3"
                        product_type=""
                        cartAdd(
                            cartList[pos].product_id,
                            cartList[pos].product_item_id,
                            cartList[pos].id,
                            cartList[pos].quantity,
                            type,
                            product_type
                        )
                    }
                }
            }

        })
        mView.rvList.adapter=cartAdapter


        mView.btnCheckOut.setOnClickListener {
            mView.btnCheckOut.startAnimation(AlphaAnimation(1f, 0.5f))
            if(userId == 0){
                LogUtils.shortToast(requireContext(), getString(R.string.please_login_signup_to_access_this_functionality))
//                    startActivity(Intent(requireContext(), ChooseLoginSignUpActivity::class.java))
                val args=Bundle()
                args.putString("reference", "CheckOut")
//                findNavController().navigate(R.id.chooseLoginSingUpFragment, args)
                requireContext().startActivity(Intent(requireContext(), LoginActivity::class.java).putExtras(args))
            }
            else{
                findNavController().navigate(R.id.action_cartFragment_to_checkOutFragment)
            }

        }

    }
    private fun myCart() {
        mView.progressBar.visibility= View.VISIBLE
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val builder = ApiClient.createBuilder(arrayOf("user_id", "device_id", "lang"),
                arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString(), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.DeviceId, ""], SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""].toString()))


        val call = apiInterface.myCart(builder.build())
        call!!.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                mView.progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                /*if(mView.swipeRefresh.isRefreshing){
                    mView.swipeRefresh.isRefreshing=false
                }*/
                try {
                    if (response.body() != null) {
                        responseBody=response.body()!!.string()
                        val jsonObject = JSONObject(responseBody)
                        if (jsonObject.getInt("response") == 1){

                /*            if(jsonObject.getString("total_discount")!="0"){
                                mView.txtTotalDiscount.visibility=View.VISIBLE
                                mView.totalDiscAmt.visibility=View.VISIBLE
                            }
                            else{
                                mView.txtTotalDiscount.visibility=View.GONE
                                mView.totalDiscAmt.visibility=View.GONE
                            }*/

                            mView.txtTotalDiscount.visibility=View.VISIBLE
                            mView.totalDiscAmt.visibility=View.VISIBLE


                            mySubTotal = jsonObject.getString("sub_total").toInt()
                            myTotalDisc = jsonObject.getString("total_discount").toInt()
                            myTaxes = jsonObject.getString("taxes").toInt()

                            val carts=jsonObject.getJSONArray("carts")
                            cartList.clear()
                            mView.txtNoDataFound.visibility=View.GONE
                            mView.rvList.visibility=View.VISIBLE
                            mView.bottomView.visibility=View.VISIBLE
                            for(i in 0  until carts.length()){
                                val obj = carts.getJSONObject(i)
                                val c = Cart()
                                c.id = obj.getInt("id")
                                c.user_id = obj.getInt("user_id")
                                c.add_offer = obj.getInt("add_offer")
                                c.allow_coupans = obj.getInt("allow_coupans")
                                c.quantity = obj.getInt("quantity")
                                c.sold_out = obj.getInt("sold_out")
                                c.like = obj.getBoolean("like")
                                c.product_available_status = obj.getBoolean("product_available_status")
                                c.product_id = obj.getInt("product_id")
                                c.price = obj.getString("price")
                                c.discount = obj.getString("discount")
                                c.product_available_message = obj.getString("product_available_message")
                                c.product_item_id = obj.getString("product_item_id")
                                c.category_name = obj.getString("category_name")
                                c.product_name = obj.getString("product_name")
                                c.supplier_name = obj.getString("supplier_name")
                                c.supplier_image = obj.getString("supplier_image")
                                c.from_date = obj.getString("from_date")
                                c.files = obj.getString("files")
                                c.to_date = obj.getString("to_date")
                                c.supplier_id = obj.getInt("supplier_id")
                                cartList.add(c)
                            }

                            calculateShippingFees()

                        }

                        else {
                            cartList.clear()
                            mView.txtNoDataFound.visibility=View.VISIBLE
                            mView.bottomView.visibility=View.GONE
                            mView.rvList.visibility=View.GONE
//                            LogUtils.shortToast(requireContext(), jsonObject.getString("message"))
                        }
                        cartAdapter.notifyDataSetChanged()
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
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView.progressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
               /* if(mView.swipeRefresh.isRefreshing){
                    mView.swipeRefresh.isRefreshing=false
                }*/
            }
        })


    }

    private fun calculateShippingFees() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView.progressBar.visibility= View.VISIBLE

        val apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val builder = ApiClient.createBuilder(arrayOf("user_id"),
            arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString()))

        val call = apiInterface.calcShippingFees(builder.build())
        call!!.enqueue(object : Callback<CalcShippingFees?> {
            override fun onResponse(call: Call<CalcShippingFees?>, response: Response<CalcShippingFees?>) {
                mView.progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        myShippingFees = response.body()!!.response!!.shippingFee!!
                        mView.totalDiscAmt.text="- AED "+myTotalDisc
                        mView.subTot.text="AED "+mySubTotal
                        mView.shipFee.text="AED "+myShippingFees
                        mView.taxes.text="AED "+myTaxes
                        myTotalPrice = myShippingFees + myTaxes + mySubTotal - myTotalDisc
                        mView.totalPrice.text="AED "+myTotalPrice
                        Log.e("Shipping Fee", response.body()!!.response.toString())
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<CalcShippingFees?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView.progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }

    private fun cartAdd(
        productId: Int,
        productItemId: String,
        cartId: Int,
        quantity: Int,
        type: String,
        productType: String
    ) {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView.progressBar.visibility= View.VISIBLE

        val apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val builder = ApiClient.createBuilder(arrayOf("product_id", "product_item_id", "type", "quantity", "product_type", "cart_id", "device_id", "user_id", "lang"),
                arrayOf(productId.toString(), productItemId, type, quantity.toString(), productType, cartId.toString(), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.DeviceId, ""]
                        , SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString(), SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""].toString()))

        val call = apiInterface.cartAdd(builder.build())
        call!!.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                mView.progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        val jsonObject = JSONObject(response.body()!!.string())
                        if (jsonObject.getInt("response") == 1) {
                            if(jsonObject.getInt("carts_count")!=0){
                                requireActivity().cartWedgeCount.visibility=View.VISIBLE
                                requireActivity().frag_other_cartWedgeCount.visibility=View.VISIBLE
                            }
                            else{
                                requireActivity().cartWedgeCount.visibility=View.GONE
                                requireActivity().frag_other_cartWedgeCount.visibility=View.GONE
                            }
                           myCart()
                        }

                        else {
                            LogUtils.shortToast(requireContext(), jsonObject.getString("message"))
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
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView.progressBar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

    }
    override fun onResume() {
        super.onResume()
        /* requireActivity().backImg.visibility=View.GONE*/
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        requireActivity().frag_other_toolbar.visibility=View.VISIBLE
        requireActivity().home_frag_categories.visibility = View.GONE
        requireActivity().toolbar.visibility=View.GONE
        requireActivity().profile_fragment_toolbar.visibility=View.GONE
        requireActivity().supplier_fragment_toolbar.visibility=View.GONE
        requireActivity().about_us_fragment_toolbar.visibility=View.GONE
    }
    override fun onDestroy() {
        super.onDestroy()
//        requireActivity().backImg.visibility=View.VISIBLE
        requireActivity().frag_other_toolbar.visibility=View.GONE
        requireActivity().toolbar.visibility=View.GONE
        requireActivity().profile_fragment_toolbar.visibility=View.GONE
        requireActivity().supplier_fragment_toolbar.visibility=View.GONE
        requireActivity().about_us_fragment_toolbar.visibility=View.GONE
    }

    override fun onStop() {
        super.onStop()
//        requireActivity().backImg.visibility=View.VISIBLE
        requireActivity().frag_other_toolbar.visibility=View.VISIBLE
        requireActivity().toolbar.visibility=View.GONE
        requireActivity().profile_fragment_toolbar.visibility=View.GONE
        requireActivity().supplier_fragment_toolbar.visibility=View.GONE
        requireActivity().about_us_fragment_toolbar.visibility=View.GONE

    }

}