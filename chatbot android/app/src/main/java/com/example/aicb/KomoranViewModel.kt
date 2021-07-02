package com.example.aicb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.shineware.nlp.komoran.core.Komoran

class KomoranViewModel: ViewModel() {
    private var komoran: MutableLiveData<Komoran> = MutableLiveData<Komoran>()
    private var shortest_name: MutableLiveData<String> = MutableLiveData<String>()
    private var shortest_latitude: MutableLiveData<Double> = MutableLiveData<Double>()
    private var shortest_longitude: MutableLiveData<Double> = MutableLiveData<Double>()
    private var shortest_number: MutableLiveData<String> = MutableLiveData<String>()
    private var shortest_address: MutableLiveData<String> = MutableLiveData<String>()

    fun setKomoran(komoran: Komoran)
    {
        this.komoran.value = komoran
    }

    fun getKomoran(): Komoran {
        return this.komoran.value!!
    }

    fun setShortest_name(shortest_name: String)
    {
        this.shortest_name.value = shortest_name
    }

    fun getShortest_name(): LiveData<String> {
        return this.shortest_name
    }

    fun setShortest_latitude(shortest_latitude: Double)
    {
        this.shortest_latitude.value = shortest_latitude
    }

    fun getShortest_latitude(): LiveData<Double> {
        return this.shortest_latitude
    }

    fun setShortest_longitude(shortest_longitude: Double)
    {
        this.shortest_longitude.value = shortest_longitude
    }

    fun getShortest_longitude(): LiveData<Double> {
        return this.shortest_longitude
    }

    fun setShortest_number(shortest_number: String)
    {
        this.shortest_number.value = shortest_number
    }

    fun getShortest_number(): LiveData<String> {
        return this.shortest_number
    }

    fun setShortest_address(shortest_address: String)
    {
        this.shortest_address.value = shortest_address
    }

    fun getShortest_address(): LiveData<String> {
        return this.shortest_address
    }


}