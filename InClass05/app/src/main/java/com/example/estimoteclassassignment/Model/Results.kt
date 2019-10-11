package com.example.estimoteclassassignment.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Results(var discounts: ArrayList<Discount>): Parcelable{

}