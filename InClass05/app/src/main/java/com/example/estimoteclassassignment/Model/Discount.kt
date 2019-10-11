package com.example.estimoteclassassignment.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Discount(var discount: Int,
                    var name: String,
                    var photo: String,
                    var price: Double,
                    var region: String): Parcelable{

}