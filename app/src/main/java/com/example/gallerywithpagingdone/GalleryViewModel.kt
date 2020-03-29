package com.example.gallerywithpagingdone

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val factory = PixabayDataSourceFactory(application)
    val pagedListLiveData = factory.toLiveData(1)
    val networkStatus = Transformations.switchMap(factory.pixabayDataSource) {it.networkStatus}
    fun resetQuery() {
        pagedListLiveData.value?.dataSource?.invalidate()
    }
    fun retry() {
        factory.pixabayDataSource.value?.retry?.invoke()
    }
}