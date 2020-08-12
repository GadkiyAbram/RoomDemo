package com.example.roomdemo

import android.util.Patterns
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomdemo.db.Subscriber
import com.example.roomdemo.db.SubscriberRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SubscriberViewModel(private val repository: SubscriberRepository) : ViewModel(), Observable {

    val subscribers = repository.subscribers
    private var isUpdateOrDelete = false
    private lateinit var subscriberToUpdateOrDelete : Subscriber

    @Bindable
    val inputName = MutableLiveData<String>()
    @Bindable
    val inputEmail = MutableLiveData<String>()
    @Bindable
    val saveOrUpdddateButtonText = MutableLiveData<String>()
    @Bindable
    val clearOrDeleteButtonText = MutableLiveData<String>()

    private val statusMessage = MutableLiveData<Event<String>>()

    val message : LiveData<Event<String>>
        get() = statusMessage

    init {
        saveOrUpdddateButtonText.value = "Save"
        clearOrDeleteButtonText.value = "Clear Or Delete"
    }

    fun saveOrUpdate(){

        if (inputName.value == null){
            statusMessage.value = Event("Name needed")
        }else if (inputEmail.value == null){
            statusMessage.value = Event("Email needed")
        }else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.value!!).matches()){
            statusMessage.value = Event("Wrong email format")
        }else{
            if (isUpdateOrDelete){
                subscriberToUpdateOrDelete.name = inputName.value!!
                subscriberToUpdateOrDelete.email = inputEmail.value!!
                update(subscriberToUpdateOrDelete)
            }else{
                val name : String = inputName.value!!
                val email : String = inputEmail.value!!
                insert(Subscriber(0, name, email))      // since "id" is autoincremented, we can just put id = 0
                inputName.value = null
                inputEmail.value = null
            }
        }
    }

    fun clearAllOrDelete(){
        if (isUpdateOrDelete){
            delete(subscriberToUpdateOrDelete)
        }else{
            clearAll()
        }
    }

    fun insert(subscriber: Subscriber) : Job = viewModelScope.launch{
        val newRowId : Long = repository.insert(subscriber)
        if (newRowId > -1){
            statusMessage.value = Event("Subscriber Inserted Successfully $newRowId")
        }else{
            statusMessage.value = Event("Error Occurred")
        }
    }

    fun update(subscriber: Subscriber) : Job = viewModelScope.launch {
        val noOfRows = repository.update(subscriber)
        if (noOfRows > 0){
            repository.update(subscriber)
            inputName.value = null
            inputEmail.value = null
            isUpdateOrDelete = false
            subscriberToUpdateOrDelete = subscriber
            saveOrUpdddateButtonText.value = "Save"
            clearOrDeleteButtonText.value = "Clear All"
            statusMessage.value = Event("$noOfRows Updated Successfully")
        }else{
            statusMessage.value = Event("Error updating")
        }

    }

    fun delete(subscriber: Subscriber) : Job = viewModelScope.launch {
        val noOfRowsDeleted : Int = repository.delete(subscriber)
        if (noOfRowsDeleted > 0){
            repository.insert(subscriber)
            inputName.value = null
            inputEmail.value = null
            isUpdateOrDelete = false
            subscriberToUpdateOrDelete = subscriber
            saveOrUpdddateButtonText.value = "Save"
            clearOrDeleteButtonText.value = "Clear All"
            statusMessage.value = Event("$noOfRowsDeleted Rows Deleted Successfully")
        }else{
            statusMessage.value = Event("Error Occurred")
        }

    }

    fun clearAll() : Job = viewModelScope.launch {
        val noOfRowsDeleted : Int = repository.deleteAll()
        if (noOfRowsDeleted > 0){
            statusMessage.value = Event("$noOfRowsDeleted Subscribers Deleted Successfully")
        }else{
            statusMessage.value = Event("Error Occurred")
        }
    }

    fun initUpdateAndDelete(subscriber: Subscriber){
        inputName.value = subscriber.name
        inputEmail.value = subscriber.email
        isUpdateOrDelete = true
        subscriberToUpdateOrDelete = subscriber
        saveOrUpdddateButtonText.value = "Update"
        clearOrDeleteButtonText.value = "Delete"
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    }
}