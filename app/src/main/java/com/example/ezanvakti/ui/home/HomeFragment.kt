package com.example.ezanvakti.ui.home

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ezanvakti.MainActivity
import com.example.ezanvakti.R
import com.example.ezanvakti.databinding.FragmentHomeBinding
import com.google.firebase.FirebaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQueries.localDate
import java.util.*


class HomeFragment : Fragment() {
    var ref = FirebaseDatabase.getInstance().getReference("app_settings")
    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as MainActivity).flagUpdateDT = true
        //(activity as MainActivity).ref.child("flagUpdateDT").setValue(true)
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        //ana sayfa açıkken sürekli tarihi ve saati yeniliyor
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    if((activity as MainActivity).flagUpdateDT){
                        (activity as MainActivity).showCurrentTime()
                    }
                } catch (e : Exception){
                    //println("Exception is handled.")
                }

                handler.postDelayed(this, 1000)//1 sec delay
            }
        }, 0)




        root.findViewById<TextView>(R.id.current_location).setText((activity as MainActivity).currentCity+"\n"+(activity as MainActivity).currentTown)
        val handler2 = Handler()
        handler2.postDelayed(object : Runnable {
            override fun run() {
                try {
                    var currentDT = (activity as MainActivity).getCTdate()
                    val f: DateFormat = SimpleDateFormat("HH:mm")
                    val prayerTH: DateFormat = SimpleDateFormat("HH")
                    val prayerTM: DateFormat = SimpleDateFormat("mm")
                    var d: Date
                    var todayCountFlag = true
                    var diffNCm = 0

                    ref.addValueEventListener(object : ValueEventListener {

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            //ilçenin kodunu bulup prayertime fonksiyonuna gönderiyoruz
                            for(a in dataSnapshot.child("PrayerTimes").children){
                                try{
                                    if(((activity as MainActivity).todayDateCWDB)==a.key.toString()){
                                        root.findViewById<TextView>(R.id.time_imsak).setText("İmsak "+a.child("Imsak").getValue().toString())
                                        root.findViewById<TextView>(R.id.time_gunes).setText("Gunes "+ a.child("Gunes").getValue().toString())
                                        root.findViewById<TextView>(R.id.time_ogle).setText("Ogle " + a.child("Ogle").getValue().toString())
                                        root.findViewById<TextView>(R.id.time_ikindi).setText("İkindi "+a.child("Ikindi").getValue().toString())
                                        root.findViewById<TextView>(R.id.time_aksam).setText("Aksam "+a.child("Aksam").getValue().toString())
                                        root.findViewById<TextView>(R.id.time_yatsi).setText("Yatsi "+a.child("Yatsi").getValue().toString())
                                        d = f.parse(a.child("Aksam").getValue().toString())
                                        var aksamH=prayerTH.format(d).toInt()
                                        var aksamM=prayerTM.format(d).toInt()
                                        d = f.parse(a.child("Gunes").getValue().toString())
                                        var gunesH=prayerTH.format(d).toInt()
                                        var gunesM=prayerTM.format(d).toInt()
                                        d = f.parse(a.child("Ikindi").getValue().toString())
                                        var ikindiH=prayerTH.format(d).toInt()
                                        var ikindiM=prayerTM.format(d).toInt()
                                        d = f.parse(a.child("Imsak").getValue().toString())
                                        var imsakH=prayerTH.format(d).toInt()
                                        var imsakM=prayerTM.format(d).toInt()
                                        d = f.parse(a.child("Ogle").getValue().toString())
                                        var ogleH=prayerTH.format(d).toInt()
                                        var ogleM=prayerTM.format(d).toInt()
                                        d = f.parse(a.child("Yatsi").getValue().toString())
                                        var yatsiH=prayerTH.format(d).toInt()
                                        var yatsiM=prayerTM.format(d).toInt()

                                        if((prayerTH.format(currentDT).toInt()<imsakH) || (prayerTH.format(currentDT).toInt()==imsakH && prayerTM.format(currentDT).toInt()<imsakM)){

                                            var kalanSaat = imsakH-prayerTH.format(currentDT).toInt()
                                            var kalanDakika = imsakM-prayerTM.format(currentDT).toInt()
                                            if(kalanDakika<0){
                                                kalanDakika=60-(-1*kalanDakika)
                                                kalanSaat=kalanSaat-1
                                            }
                                            root.findViewById<TextView>(R.id.counter_ezan).setText("Ezana kalan süre "+ kalanSaat + " saat "+ kalanDakika + " dakika")
                                        }
                                        else if(prayerTH.format(currentDT).toInt()<gunesH || (prayerTH.format(currentDT).toInt()==gunesH && prayerTM.format(currentDT).toInt()<gunesM)){
                                            var kalanSaat = gunesH-prayerTH.format(currentDT).toInt()
                                            var kalanDakika = gunesM-prayerTM.format(currentDT).toInt()
                                            if(kalanDakika<0){
                                                kalanDakika=60-(-1*kalanDakika)
                                                kalanSaat=kalanSaat-1
                                            }
                                            root.findViewById<TextView>(R.id.counter_ezan).setText("Ezana kalan süre "+ kalanSaat + " saat "+ kalanDakika + " dakika")
                                        }
                                        else if(prayerTH.format(currentDT).toInt()<ogleH || (prayerTH.format(currentDT).toInt()==ogleH && prayerTM.format(currentDT).toInt()<ogleM)){
                                            var kalanSaat = ogleH-prayerTH.format(currentDT).toInt()
                                            var kalanDakika = ogleM-prayerTM.format(currentDT).toInt()
                                            if(kalanDakika<0){
                                                kalanDakika=60-(-1*kalanDakika)
                                                kalanSaat=kalanSaat-1
                                            }
                                            root.findViewById<TextView>(R.id.counter_ezan).setText("Ezana kalan süre "+ kalanSaat + " saat "+ kalanDakika + " dakika")
                                        }
                                        else if(prayerTH.format(currentDT).toInt()<ikindiH || (prayerTH.format(currentDT).toInt()==ikindiH && prayerTM.format(currentDT).toInt()<ikindiM)){
                                            var kalanSaat = ikindiH-prayerTH.format(currentDT).toInt()
                                            var kalanDakika = ikindiM-prayerTM.format(currentDT).toInt()
                                            if(kalanDakika<0){
                                                kalanDakika=60-(-1*kalanDakika)
                                                kalanSaat=kalanSaat-1
                                            }
                                            root.findViewById<TextView>(R.id.counter_ezan).setText("Ezana kalan süre "+ kalanSaat + " saat "+ kalanDakika + " dakika")
                                        }
                                        else if(prayerTH.format(currentDT).toInt()<aksamH || (prayerTH.format(currentDT).toInt()==aksamH && prayerTM.format(currentDT).toInt()<aksamM)){
                                            var kalanSaat = aksamH-prayerTH.format(currentDT).toInt()
                                            var kalanDakika = aksamM-prayerTM.format(currentDT).toInt()
                                            if(kalanDakika<0){
                                                kalanDakika=60-(-1*kalanDakika)
                                                kalanSaat=kalanSaat-1
                                            }
                                            root.findViewById<TextView>(R.id.counter_ezan).setText("Ezana kalan süre "+ kalanSaat + " saat "+ kalanDakika + " dakika")
                                        }
                                        else if(prayerTH.format(currentDT).toInt()<yatsiH  || (prayerTH.format(currentDT).toInt()==yatsiH && prayerTM.format(currentDT).toInt()<yatsiM)){
                                            var kalanSaat = yatsiH-prayerTH.format(currentDT).toInt()
                                            var kalanDakika = yatsiM-prayerTM.format(currentDT).toInt()
                                            if(kalanDakika<0){
                                                kalanDakika=60-(-1*kalanDakika)
                                                kalanSaat=kalanSaat-1
                                            }
                                            root.findViewById<TextView>(R.id.counter_ezan).setText("Ezana kalan süre "+ kalanSaat + " saat "+ kalanDakika + " dakika")
                                        }
                                        todayCountFlag=false
                                            /*if(prayerTH.format(d).toInt()>prayerTH.format(currentDT).toInt()){// ezan saati güncel saatten büyükse
                                                if(prayerTM.format(d).toInt()>prayerTM.format(currentDT).toInt())// ezan dakikası güncel dakikadan büyükse
                                                {
                                                    var kalanSaat = prayerTH.format(d).toInt()-prayerTH.format(currentDT).toInt()
                                                    var kalanDakika = prayerTM.format(d).toInt()-prayerTM.format(currentDT).toInt()
                                                    if(kalanDakika<0){
                                                        kalanDakika=60-(-1*kalanDakika)
                                                        kalanSaat=kalanSaat-1
                                                    }
                                                    root.findViewById<TextView>(R.id.counter_ezan).setText("Ezana kalan süre "+ kalanSaat + " saat "+ kalanDakika + " dakika")
                                                    todayCountFlag=false
                                                }
                                            }*/

                                    }
                                }
                                catch (e: java.lang.Exception){

                                }
                            }
                            try {

                                for(getTomorrow in dataSnapshot.child("PrayerTimes").children){
                                    if(todayCountFlag&&((activity as MainActivity).formattedTomorrow)==getTomorrow.key.toString()){

                                        d = f.parse(getTomorrow.child("Imsak").getValue().toString())
                                        var diffDT = 24-prayerTH.format(currentDT).toInt()
                                        if(diffDT==1) diffDT=0
                                        else diffDT=diffDT-1
                                        var newEzanCountH = prayerTH.format(d).toInt()+diffDT
                                        diffNCm = prayerTM.format(d).toInt()-prayerTM.format(currentDT).toInt()
                                        if(diffNCm>0){
                                            diffNCm=60-diffNCm
                                        }
                                        else{
                                            diffNCm=60-(-1*diffNCm)
                                        }
                                        root.findViewById<TextView>(R.id.counter_ezan).setText("Ezana kalan süre "+ newEzanCountH + " saat "+ diffNCm + " dakika")
                                        todayCountFlag=false
                                    }
                                }
                            }
                            catch (e: Exception){
                            }

                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                        fun onCancelled(firebaseError: FirebaseError?) {}
                    })
                } catch (e : Exception){

                }

                handler.postDelayed(this, 1000)//1 sec delay
            }
        }, 0)


        root.findViewById<TextView>(R.id.hadisler).setText((activity as MainActivity).hadis)



        return root
    }

    override fun onDestroyView() {
        //ana sayfadan çıktığımda yenilemeyi durdurmak için
        (activity as MainActivity).flagUpdateDT = false
        //(activity as MainActivity).ref.child("flagUpdateDT").setValue(false)
        super.onDestroyView()
        _binding = null
    }
}