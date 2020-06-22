package com.app.tiktok.mock

import android.content.Context
import com.app.tiktok.R
import com.app.tiktok.model.StoriesDataModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject


class Mock @Inject constructor(private val context: Context) {
    fun loadMockData(): MutableList<StoriesDataModel>? {
        val mockData = context.resources.openRawResource(R.raw.stories_data)
        val dataString = mockData.bufferedReader().readText()

        val gson = Gson()
        val storiesType = object : TypeToken<MutableList<StoriesDataModel>>() {}.type
        val storiesDataModelList = gson.fromJson<MutableList<StoriesDataModel>>(dataString, storiesType)

        return storiesDataModelList
    }
}