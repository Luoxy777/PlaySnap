package com.example.playsnapui.ui.`object`

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ObjectViewModel : ViewModel() {

    private val _objects = MutableLiveData<List<String>>()
    val objects: LiveData<List<String>> get() = _objects

    fun setDetectedObjects(detectedObjects: List<String>){
        println(detectedObjects.toString())
        _objects.value = detectedObjects.toList()
        println("test object $_objects")
    }

    fun addObject(newObject: String) {
        // Get the current list (null check to avoid crashes if _objects is null)
        val currentList = _objects.value.orEmpty()

        // Create a new list with the new object added
        val updatedList = currentList.toMutableList().apply {
            add(newObject)
        }

        // Update the LiveData with the new list
        _objects.value = updatedList
    }

    fun removeObjectAt(position: Int) {
        _objects.value = _objects.value?.toMutableList()?.apply {
            if (position in indices) removeAt(position)
        }
    }
}
