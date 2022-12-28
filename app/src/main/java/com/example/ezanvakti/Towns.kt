package com.example.ezanvakti

import com.google.gson.annotations.SerializedName

data class Towns(
    @SerializedName("IlceAdi"   ) var IlceAdi   : String? = null,
    @SerializedName("IlceAdiEn" ) var IlceAdiEn : String? = null,
    @SerializedName("IlceID"    ) var IlceID    : String? = null

)
