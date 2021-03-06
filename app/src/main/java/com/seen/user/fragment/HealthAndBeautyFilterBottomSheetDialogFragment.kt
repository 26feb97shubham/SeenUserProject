package com.seen.user.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.seen.user.R
import com.seen.user.adapter.CountriesListDataAdapter
import com.seen.user.interfaces.ClickInterface
import com.seen.user.model.CountriesItem
import com.seen.user.model.CountryListResponse
import com.seen.user.utils.LogUtils
import com.seen.user.utils.SharedPreferenceUtility
import com.seen.user.utils.Utility
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_health_and_beauty_filter_bottom_sheet_dialog.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class HealthAndBeautyFilterBottomSheetDialogFragment : BottomSheetDialogFragment() {
    lateinit var countriesListDataAdapter: CountriesListDataAdapter
    var countryList = ArrayList<CountriesItem>()
    private var mView : View?=null
    private var onFilterClickallback: OnFilterClick? = null
    private var country_id:String = ""
    private var country_clicked = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_health_and_beauty_filter_bottom_sheet_dialog, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getCountries()

        mView!!.emi_filter_layout1.setOnClickListener {
            if (!country_clicked){
                country_clicked = true
                mView!!.ll_countries_list1.visibility = View.VISIBLE
            }else{
                country_clicked = false
                mView!!.ll_countries_list1.visibility = View.GONE
            }
        }

        mView!!.apply_filter_tv1.setOnClickListener {
            val queryMap = HashMap<String, String>()
            queryMap.put("user_id", SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0).toString())
            queryMap.put("account_types_id", acc_type_id)
            queryMap.put("search", "")
            queryMap.put("country_id", country_id)
            onFilterClickallback!!.onFilter(queryMap)
            dismiss()
        }

    }
    fun setFilterClickListenerCallback(onFilterClickallback: OnFilterClick){
        this.onFilterClickallback = onFilterClickallback
    }

    private fun getCountries() {
        val call = Utility.apiInterface.getCountriesList()
        call!!.enqueue(object : Callback<CountryListResponse?> {
            override fun onResponse(call: Call<CountryListResponse?>, response: Response<CountryListResponse?>) {
                try {
                    if (response.body() != null) {
                        countryList = response.body()!!.countries as ArrayList<CountriesItem>
//                        txtCountryCode.text=country_code[0]
                        if (countryList.size!=0){
                            mView!!.txtNoDataFound_countries1.visibility = View.GONE
                            mView!!.rv_countries_list1.visibility = View.VISIBLE

                            val servedCountryList = ArrayList<CountriesItem>()
                            for (i in 0 until countryList.size){
                                if (countryList[i].countries_to_be_served==1){
                                    servedCountryList.add(countryList[i])
                                }
                            }

                            mView!!.rv_countries_list1.layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                            countriesListDataAdapter= CountriesListDataAdapter(requireContext(), servedCountryList,  object : ClickInterface.ClickPositionInterface{
                                override fun clickPostion(pos: Int) {
                                    country_id = countryList[pos].id.toString()
                                    mView!!.ll_countries_list1.visibility = View.GONE
                                    if (SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""].equals("ar")){
                                        mView!!.emi_filter_tv1.text = countryList[pos].country_name_ar
                                    }else{
                                        mView!!.emi_filter_tv1.text = countryList[pos].country_name
                                    }

                                }

                            })
                            mView!!.rv_countries_list1.adapter=countriesListDataAdapter
                            countriesListDataAdapter.notifyDataSetChanged()
                        }else{
                            mView!!.txtNoDataFound_countries1.visibility = View.VISIBLE
                            mView!!.rv_countries_list1.visibility = View.GONE
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

            override fun onFailure(call: Call<CountryListResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
            }
        })
    }

    companion object {
        const val TAG = "HealthAndBeautyFilterBottomSheetDialogFragment"
        private var acc_type_id : String = ""
        fun newInstance(context: Context?, account_types_id: String): HealthAndBeautyFilterBottomSheetDialogFragment {
            //this.context = context;
            this.acc_type_id = account_types_id
            return HealthAndBeautyFilterBottomSheetDialogFragment()
        }
    }

    interface OnFilterClick{
        fun onFilter(queryMap : HashMap<String, String>)
    }
}