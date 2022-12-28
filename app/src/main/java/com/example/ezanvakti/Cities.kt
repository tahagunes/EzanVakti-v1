package com.example.ezanvakti

import com.google.gson.annotations.SerializedName


data class Cities (
  @SerializedName("SehirAdi"   ) var SehirAdi   : String? = null,
  @SerializedName("SehirAdiEn" ) var SehirAdiEn : String? = null,
  @SerializedName("SehirID"    ) var SehirID    : String? = null

)