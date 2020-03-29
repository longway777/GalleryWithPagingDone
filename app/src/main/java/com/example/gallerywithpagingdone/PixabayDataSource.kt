package com.example.gallerywithpagingdone

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
enum class NetworkStatus {
    INITIAL_LOADING,
    LOADING,
    LOADED,
    FAILED,
    COMPLETED
}
class PixabayDataSource(private val context: Context):PageKeyedDataSource<Int,PhotoItem>() {
    var retry : (()->Any)? = null
    private val _networkStatus = MutableLiveData<NetworkStatus>()
    val networkStatus : LiveData<NetworkStatus> = _networkStatus
    private val queryKey = arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal").random()
    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, PhotoItem>
    ) {
        retry = null
        _networkStatus.postValue(NetworkStatus.INITIAL_LOADING)
        val url = "https://pixabay.com/api/?key=12472743-874dc01dadd26dc44e0801d61&q=${queryKey}&per_page=50&page=1"
        StringRequest(
            Request.Method.GET,
            url,
            Response.Listener {
                val dataList = Gson().fromJson(it,Pixabay::class.java).hits.toList()
                callback.onResult(dataList,null,2)
                _networkStatus.postValue(NetworkStatus.LOADED)
            },
            Response.ErrorListener {
                retry = {loadInitial(params,callback)}
                _networkStatus.postValue(NetworkStatus.FAILED)
                Log.d("hello", "loadInitial: $it")
            }
        ).also { VolleySingleton.getInstance(context).requestQueue.add(it) }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {
        retry = null
        _networkStatus.postValue(NetworkStatus.LOADING)
        val url = "https://pixabay.com/api/?key=12472743-874dc01dadd26dc44e0801d61&q=${queryKey}&per_page=50&page=${params.key}"
        StringRequest(
            Request.Method.GET,
            url,
            Response.Listener {
                val dataList = Gson().fromJson(it,Pixabay::class.java).hits.toList()
                callback.onResult(dataList,params.key + 1)
                _networkStatus.postValue(NetworkStatus.LOADED)
            },
            Response.ErrorListener {
                if (it.toString() == "com.android.volley.ClientError") {
                    _networkStatus.postValue(NetworkStatus.COMPLETED)
                } else {
                    retry = {loadAfter(params,callback)}
                    _networkStatus.postValue(NetworkStatus.FAILED)
                }

                Log.d("hello", "loadAfter: $it")
            }
        ).also { VolleySingleton.getInstance(context).requestQueue.add(it) }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {

    }
}