package com.example.playsnapui.ui.like

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LikeViewModel : ViewModel() {

    private val _recentItems = MutableLiveData<List<RecentItem>>()
    val recentItems: LiveData<List<RecentItem>> = _recentItems

    init {
        // Sample data, you should load real data from a repository
        _recentItems.value = listOf(
            RecentItem("Item 1", "Description 1"),
            RecentItem("Item 2", "Description 2")
        )
    }
}

data class RecentItem(val title: String, val description: String)
