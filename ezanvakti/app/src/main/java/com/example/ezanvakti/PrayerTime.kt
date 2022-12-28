package com.example.ezanvakti

import com.google.gson.annotations.SerializedName

data class PrayerTime(
    @SerializedName("Aksam"                  ) var Aksam                  : String? = null,
    @SerializedName("AyinSekliURL"           ) var AyinSekliURL           : String? = null,
    @SerializedName("Gunes"                  ) var Gunes                  : String? = null,
    @SerializedName("GunesBatis"             ) var GunesBatis             : String? = null,
    @SerializedName("GunesDogus"             ) var GunesDogus             : String? = null,
    @SerializedName("HicriTarihKisa"         ) var HicriTarihKisa         : String? = null,
    @SerializedName("HicriTarihKisaIso8601"  ) var HicriTarihKisaIso8601  : String? = null,
    @SerializedName("HicriTarihUzun"         ) var HicriTarihUzun         : String? = null,
    @SerializedName("HicriTarihUzunIso8601"  ) var HicriTarihUzunIso8601  : String? = null,
    @SerializedName("Ikindi"                 ) var Ikindi                 : String? = null,
    @SerializedName("Imsak"                  ) var Imsak                  : String? = null,
    @SerializedName("KibleSaati"             ) var KibleSaati             : String? = null,
    @SerializedName("MiladiTarihKisa"        ) var MiladiTarihKisa        : String? = null,
    @SerializedName("MiladiTarihKisaIso8601" ) var MiladiTarihKisaIso8601 : String? = null,
    @SerializedName("MiladiTarihUzun"        ) var MiladiTarihUzun        : String? = null,
    @SerializedName("MiladiTarihUzunIso8601" ) var MiladiTarihUzunIso8601 : String? = null,
    @SerializedName("Ogle"                   ) var Ogle                   : String? = null,
    @SerializedName("Yatsi"                  ) var Yatsi                  : String? = null
)
