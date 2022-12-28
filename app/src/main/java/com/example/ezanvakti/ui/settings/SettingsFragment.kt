package com.example.ezanvakti.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ezanvakti.*
import com.example.ezanvakti.R
import com.example.ezanvakti.databinding.FragmentSettingsBinding
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private lateinit var database: DatabaseReference
    var alarm_hour = 21
    var alarm_minute = 37

    lateinit var postService: PostService
    var ref = FirebaseDatabase.getInstance().getReference("app_settings")
    var local_Towns: Array<String> = arrayOf()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSettings
        settingsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////////////


        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        //spinnera şehirleri çekiyoruz
        //val cities = resources.getStringArray(R.array.Cities)
        val cities = (activity as MainActivity).Cities
        val spinner = root.findViewById<Spinner>(R.id.cities_spinner)
        (activity as MainActivity).set_spinner(spinner,cities)
        spinner.setSelection(cities.indexOf((activity as MainActivity).currentCity), true)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (activity as MainActivity).currentCity = parent?.getItemAtPosition(position).toString()
                (activity as MainActivity).ref.child("currentCity").setValue(parent?.getItemAtPosition(position).toString())
                syncTown(root,local_Towns)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val towns = (activity as MainActivity).Towns
        val spinner1111 = root.findViewById<Spinner>(R.id.town_spinner)
        (activity as MainActivity).set_spinner(spinner1111,towns)
        spinner1111.setSelection(towns.indexOf((activity as MainActivity).currentTown), true)
        spinner1111.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //yeni seçilen ilçeyi hafızaya alıyoruz
                (activity as MainActivity).currentTown = parent?.getItemAtPosition(position).toString()
                //yeni seçilen ilçeyi dbye kaydediyoruz sonraki açılışta çekmek için
                (activity as MainActivity).ref.child("currentTown").setValue(parent?.getItemAtPosition(position).toString())
                //güncel konumun namaz vakitlerini buluyoruz
                findTownPT()
                println(ref.child("Cities").child((activity as MainActivity).currentCity).child((activity as MainActivity).currentTown).get().toString())
                //prayer time listesinin karışmaması ve büyümemesi için sıfırlıyorum
                //ref.child("PrayerTimes").setValue("")
                for(a in (activity as MainActivity).prayertimeList)
                {
                    //çekilen konumun namaz vakitlerini dbye kaydediyoruz
                    val dateString = a.MiladiTarihKisa
                    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    val date = LocalDate.parse(dateString, formatter)
                    ref.child("PrayerTimes").child(date.toString()).child("Imsak").setValue(a.Imsak).toString()
                    ref.child("PrayerTimes").child(date.toString()).child("Gunes").setValue(a.Gunes).toString()
                    ref.child("PrayerTimes").child(date.toString()).child("Ogle").setValue(a.Ogle).toString()
                    ref.child("PrayerTimes").child(date.toString()).child("Ikindi").setValue(a.Ikindi).toString()
                    ref.child("PrayerTimes").child(date.toString()).child("Aksam").setValue(a.Aksam).toString()
                    ref.child("PrayerTimes").child(date.toString()).child("Yatsi").setValue(a.Yatsi).toString()
                }
                val f: DateFormat = SimpleDateFormat("HH:mm")
                val prayerTH: DateFormat = SimpleDateFormat("HH")
                val prayerTM: DateFormat = SimpleDateFormat("mm")
                var d: Date
                ref.addValueEventListener(object : ValueEventListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        //ilçenin kodunu bulup prayertime fonksiyonuna gönderiyoruz
                        for(a in dataSnapshot.child("PrayerTimes").children){
                            if((activity as MainActivity).todayDateCWDB==a.key.toString()){
                                //root.findViewById<TextView>(R.id.time_imsak).setText("İmsak "+a.child("Imsak").getValue().toString())
                                if(dataSnapshot.child("alarmaktifkapali").child("imsakAK").getValue().toString().toBoolean()){
                                    d = f.parse(a.child("Imsak").getValue().toString())
                                    (activity as MainActivity).sendAlarm(prayerTH.format(d).toInt(),prayerTM.format(d).toInt()+dataSnapshot.child("alarm").child("imsakAlarm").getValue().toString().toInt())
                                    (activity as MainActivity).setAlarmFimsak()
                                }
                                if(dataSnapshot.child("alarmaktifkapali").child("gunesAK").getValue().toString().toBoolean()){
                                    d = f.parse(a.child("Gunes").getValue().toString())
                                    (activity as MainActivity).sendAlarm(prayerTH.format(d).toInt(),prayerTM.format(d).toInt()+dataSnapshot.child("alarm").child("gunesAlarm").getValue().toString().toInt())
                                    (activity as MainActivity).setAlarmFgunes()
                                }
                                if(dataSnapshot.child("alarmaktifkapali").child("ogleAK").getValue().toString().toBoolean()){
                                    d = f.parse(a.child("Ogle").getValue().toString())
                                    (activity as MainActivity).sendAlarm(prayerTH.format(d).toInt(),prayerTM.format(d).toInt()+dataSnapshot.child("alarm").child("ogleAlarm").getValue().toString().toInt())
                                    (activity as MainActivity).setAlarmFogle()
                                }
                                if(dataSnapshot.child("alarmaktifkapali").child("ikindiAK").getValue().toString().toBoolean()){
                                    d = f.parse(a.child("Ikindi").getValue().toString())
                                    (activity as MainActivity).sendAlarm(prayerTH.format(d).toInt(),prayerTM.format(d).toInt()+dataSnapshot.child("alarm").child("ikindiAlarm").getValue().toString().toInt())
                                    (activity as MainActivity).setAlarmFikindi()
                                }
                                if(dataSnapshot.child("alarmaktifkapali").child("aksamAK").getValue().toString().toBoolean()){
                                    d = f.parse(a.child("Aksam").getValue().toString())
                                    (activity as MainActivity).sendAlarm(prayerTH.format(d).toInt(),prayerTM.format(d).toInt()+dataSnapshot.child("alarm").child("aksamAlarm").getValue().toString().toInt())
                                    (activity as MainActivity).setAlarmFaksam()
                                }

                                if(dataSnapshot.child("alarmaktifkapali").child("yatsiAK").getValue().toString().toBoolean()){
                                    d = f.parse(a.child("Yatsi").getValue().toString())
                                    (activity as MainActivity).sendAlarm(prayerTH.format(d).toInt(),prayerTM.format(d).toInt()+dataSnapshot.child("alarm").child("yatsiAlarm").getValue().toString().toInt())
                                    (activity as MainActivity).setAlarmFyatsi()
                                }

                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                    fun onCancelled(firebaseError: FirebaseError?) {}
                })
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //////////////////////////////////////////////////////////////////////////////////////
        val set_times = resources.getStringArray(R.array.time_set)
        val set_melody = resources.getStringArray(R.array.melody_set)
        //alarm spinnnerlarına değer veriyoruz 3-6-10-12-14-16
        //melodi spinnerlarına değer veriyoruz 8-9-11-13-15-17
        //alarm aktif switch değer veriyuruz 1-2-3-6-5-4
        //imsak/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        val spinner1 = root.findViewById<Spinner>(R.id.spinner3)
        (activity as MainActivity).set_spinner(spinner1,set_times)
        spinner1.setSelection(((activity as MainActivity).imsakPos))
        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //println(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("alarm").child("imsakAlarm").setValue(parent?.getItemAtPosition(position).toString().toInt())
                (activity as MainActivity).ref.child("alarm").child("imsakPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val spinner11 = root.findViewById<Spinner>(R.id.spinner8)
        (activity as MainActivity).set_spinner(spinner11,set_melody)
        spinner11.setSelection(((activity as MainActivity).imsakMPos))
        spinner11.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (activity as MainActivity).ref.child("melodi").child("imsakMelodi").setValue(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("melodi").child("imsakMPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val sw1:Switch = root.findViewById(R.id.switch1)
        sw1.setChecked((activity as MainActivity).imsakAK);
        sw1?.setOnCheckedChangeListener({ _ , isChecked ->
            val message = if (isChecked) {
                //println("acik")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("imsakAK").setValue(isChecked)
            } else {
                //println("kapalı")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("imsakAK").setValue(isChecked)
            }
        })
        //güneş/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        val spinner2 = root.findViewById<Spinner>(R.id.spinner6)
        (activity as MainActivity).set_spinner(spinner2,set_times)
        spinner2.setSelection(((activity as MainActivity).gunesPos))
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
               // println(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("alarm").child("gunesAlarm").setValue(parent?.getItemAtPosition(position).toString().toInt())
                (activity as MainActivity).ref.child("alarm").child("gunesPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val spinner12 = root.findViewById<Spinner>(R.id.spinner9)
        (activity as MainActivity).set_spinner(spinner12,set_melody)
        spinner12.setSelection(((activity as MainActivity).gunesMPos))
        spinner12.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (activity as MainActivity).ref.child("melodi").child("gunesMelodi").setValue(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("melodi").child("gunesMPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val sw2:Switch = root.findViewById(R.id.switch2)
        sw2.setChecked((activity as MainActivity).gunesAK);
        sw2?.setOnCheckedChangeListener({ _ , isChecked ->
            val message = if (isChecked) {
                //println("acik")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("gunesAK").setValue(isChecked)
            } else {
               // println("kapalı")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("gunesAK").setValue(isChecked)
            }
        })
        //öğle///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        val spinner3 = root.findViewById<Spinner>(R.id.spinner10)
        (activity as MainActivity).set_spinner(spinner3,set_times)
        spinner3.setSelection(((activity as MainActivity).oglePos))
        spinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
               // println(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("alarm").child("ogleAlarm").setValue(parent?.getItemAtPosition(position).toString().toInt())
                (activity as MainActivity).ref.child("alarm").child("oglePos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val spinner13 = root.findViewById<Spinner>(R.id.spinner11)
        (activity as MainActivity).set_spinner(spinner13,set_melody)
        spinner13.setSelection(((activity as MainActivity).ogleMPos))
        spinner13.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (activity as MainActivity).ref.child("melodi").child("ogleMelodi").setValue(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("melodi").child("ogleMPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val sw3:Switch = root.findViewById(R.id.switch3)
        sw3.setChecked((activity as MainActivity).ogleAK);
        sw3?.setOnCheckedChangeListener({ _ , isChecked ->
            val message = if (isChecked) {
                //println("acik")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("ogleAK").setValue(isChecked)
            } else {
               // println("kapalı")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("ogleAK").setValue(isChecked)
            }
        })
        //ikindi///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        val spinner4 = root.findViewById<Spinner>(R.id.spinner12)
        (activity as MainActivity).set_spinner(spinner4,set_times)
        spinner4.setSelection(((activity as MainActivity).ikindiPos))
        spinner4.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
               // println(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("alarm").child("ikindiAlarm").setValue(parent?.getItemAtPosition(position).toString().toInt())
                (activity as MainActivity).ref.child("alarm").child("ikindiPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val spinner14 = root.findViewById<Spinner>(R.id.spinner13)
        (activity as MainActivity).set_spinner(spinner14,set_melody)
        spinner14.setSelection(((activity as MainActivity).ikindiMPos))
        spinner14.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (activity as MainActivity).ref.child("melodi").child("ikindiMelodi").setValue(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("melodi").child("ikindiMPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val sw4:Switch = root.findViewById(R.id.switch6)
        sw4.setChecked((activity as MainActivity).ikindiAK);
        sw4?.setOnCheckedChangeListener({ _ , isChecked ->
            val message = if (isChecked) {
                //println("acik")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("ikindiAK").setValue(isChecked)
            } else {
                //println("kapalı")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("ikindiAK").setValue(isChecked)
            }
        })
        //akşam///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        val spinner5 = root.findViewById<Spinner>(R.id.spinner14)
        (activity as MainActivity).set_spinner(spinner5,set_times)
        spinner5.setSelection(((activity as MainActivity).aksamPos))
        spinner5.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
               // println(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("alarm").child("aksamAlarm").setValue(parent?.getItemAtPosition(position).toString().toInt())
                (activity as MainActivity).ref.child("alarm").child("aksamPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val spinner15 = root.findViewById<Spinner>(R.id.spinner15)
        (activity as MainActivity).set_spinner(spinner15,set_melody)
        spinner11.setSelection(((activity as MainActivity).aksamMPos))
        spinner11.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (activity as MainActivity).ref.child("melodi").child("aksamMelodi").setValue(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("melodi").child("aksamMPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val sw5:Switch = root.findViewById(R.id.switch5)
        sw5.setChecked((activity as MainActivity).aksamAK);
        sw5?.setOnCheckedChangeListener({ _ , isChecked ->
            val message = if (isChecked) {
               // println("acik")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("aksamAK").setValue(isChecked)
            } else {
               /// println("kapalı")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("aksamAK").setValue(isChecked)
            }
        })
        //yatsı///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        val spinner6 = root.findViewById<Spinner>(R.id.spinner16)
        (activity as MainActivity).set_spinner(spinner6,set_times)
        spinner6.setSelection(((activity as MainActivity).yatsiPos))
        spinner6.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
               // println(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("alarm").child("yatsiAlarm").setValue(parent?.getItemAtPosition(position).toString().toInt())
                (activity as MainActivity).ref.child("alarm").child("yatsiPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val spinner16 = root.findViewById<Spinner>(R.id.spinner17)
        (activity as MainActivity).set_spinner(spinner16,set_melody)
        spinner16.setSelection(((activity as MainActivity).yatsiMPos))
        spinner16.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (activity as MainActivity).ref.child("melodi").child("yatsiMelodi").setValue(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("melodi").child("yatsiMPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val sw6:Switch = root.findViewById(R.id.switch4)
        sw6.setChecked((activity as MainActivity).yatsiAK);
        sw6?.setOnCheckedChangeListener({ _ , isChecked ->
            val message = if (isChecked) {
               // println("acik")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("yatsiAK").setValue(isChecked)
            } else {
               // println("kapalı")
                (activity as MainActivity).ref.child("alarmaktifkapali").child("yatsiAK").setValue(isChecked)
            }
        })
         //namaz vakti sese alma ayarı///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        val spinner111 = root.findViewById<Spinner>(R.id.spinner)
        (activity as MainActivity).set_spinner(spinner111,set_times)
        spinner111.setSelection(((activity as MainActivity).namazoncePos))
        spinner111.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
               // println(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("sessiz").child("namazonce").setValue(parent?.getItemAtPosition(position).toString().toInt())
                (activity as MainActivity).ref.child("sessiz").child("namazoncePos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val set_times_plus = resources.getStringArray(R.array.time_set_plus)
        val spinner112 = root.findViewById<Spinner>(R.id.spinner2)
        (activity as MainActivity).set_spinner(spinner112,set_times_plus)
        spinner112.setSelection(((activity as MainActivity).namazsonraPos))
        spinner112.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
               // println(parent?.getItemAtPosition(position))
                (activity as MainActivity).ref.child("sessiz").child("namazsonra").setValue(parent?.getItemAtPosition(position).toString().toInt())
                (activity as MainActivity).ref.child("sessiz").child("namazsonraPos").setValue(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val sw7:Switch = root.findViewById(R.id.switch7)
        sw7.setChecked((activity as MainActivity).namazOSAK);
        sw7?.setOnCheckedChangeListener({ _ , isChecked ->
            val message = if (isChecked) {
               // println("acik")
                (activity as MainActivity).ref.child("sessiz").child("namazOSAK").setValue(isChecked)
            } else {
               // println("kapalı")
                (activity as MainActivity).ref.child("sessiz").child("namazOSAK").setValue(isChecked)
            }
        })
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        _binding!!.buttonTheme.setOnClickListener { (activity as MainActivity).chooseThemeDialog() }


        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun syncTown(root: View,local_Towns: Array<String>){
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (activity as MainActivity).Towns = arrayOf()
                for (ds in dataSnapshot.child("Cities").child((activity as MainActivity).currentCity).children) {
                    (activity as MainActivity).Towns= (activity as MainActivity).Towns.plus(ds.key.toString())
                }
                //////////////////////////////////////////
                getTown(root,(activity as MainActivity).Towns)
            }
            override fun onCancelled(error: DatabaseError) {
            }
            fun onCancelled(firebaseError: FirebaseError?) {}
        })
    }
    fun getTown(root: View,local_Towns: Array<String>){
        val towns = local_Towns
        val spinner1111 = root.findViewById<Spinner>(R.id.town_spinner)
        (activity as MainActivity).set_spinner(spinner1111,towns)
        spinner1111.setSelection(towns.indexOf((activity as MainActivity).currentTown), true)
    }
    fun findTownPT(){
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //ilçenin kodunu bulup prayertime fonksiyonuna gönderiyoruz
                (activity as MainActivity).getDiyanetPrayerTimes(dataSnapshot.child("Cities").child((activity as MainActivity).currentCity!!).child((activity as MainActivity).currentTown!!).getValue().toString())
            }
            override fun onCancelled(error: DatabaseError) {
            }
            fun onCancelled(firebaseError: FirebaseError?) {}
        })
    }
}