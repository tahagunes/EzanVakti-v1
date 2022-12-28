package com.example.ezanvakti

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.ezanvakti.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

//alarm kurmak için bildirimlerin ve kanalın IDsini tanımlıyoruz
const val channelID = "ezanvakti"
const val notificationID = 1
const val notificationID2 = 2
const val notificationID3 = 3
const val notificationID4 = 4
const val notificationID5 = 5
const val notificationID6 = 6

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    //database'e erişmek için referans değer tanımladık
    var ref = FirebaseDatabase.getInstance().getReference("app_settings")
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var context: Context
    lateinit var alarmManager: AlarmManager
    lateinit var calendar: Calendar
    private lateinit var pendingIntent: PendingIntent

    lateinit var postService: PostService
    //kayıtlı apiden değerleri çekince kayıt edilecek listeleri tanımlıyoruz
    var citiesList: MutableList<Cities> = arrayListOf()
    var townsList: MutableList<Towns> = arrayListOf()
    var prayertimeList: MutableList<PrayerTime> = arrayListOf()
    //zikirmatikin süre sayacı için
    var flagStart = false
    //ana sayfa tarih güncellemesi için
    var flagUpdateDT = true
    //zikir sayaı
    var zikirCount = 0
    //güncel tarih çekme
    var date = getCurrentDateTime()
    //lateinit var startDate: Date
    var startDate = getCurrentDateTime()
    var calcEzan = getCurrentDateTime()
    //tema kaydedici
    var saveTheme = 0
    //çekilen tarihi formata oturtma
    var dateInString = date.toString("dd/MM/yyyy\n  HH:mm:ss")
    var todayDateCWDB = date.toString("yyyy-MM-dd")
    @RequiresApi(Build.VERSION_CODES.O)
    val tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS)
    @RequiresApi(Build.VERSION_CODES.O)
    val formattedTomorrow = tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    //uygulamaya ilk girişte sync için
    var imsakPos = 0
    var imsakMPos = 0
    var imsakAK = false

    var gunesPos = 0
    var gunesMPos = 0
    var gunesAK = false

    var oglePos = 0
    var ogleMPos = 0
    var ogleAK = false

    var ikindiPos = 0
    var ikindiMPos = 0
    var ikindiAK= false

    var aksamPos = 0
    var aksamMPos = 0
    var aksamAK = false

    var yatsiPos = 0
    var yatsiMPos = 0
    var yatsiAK = false

    var namazonce = 0
    var namazoncePos = 0
    var namazsonra = 0
    var namazsonraPos = 0
    var namazOSAK = true

    var hadis = ""
    var hadiscount = 0

    var Cities: Array<String> = arrayOf()
    var Towns: Array<String> = arrayOf()

    //dbden gelecek son seçili konumu uygulama içerisinde kullanabilmek için boş değerler oluşturuldu
    var currentCity = ""
    var currentTown = ""
    //bildirim atabilmek için şehir ve ilçe kodlarını tutuyoruz
    var PTpath = "9146"
    var Cpath = "500"
    //uygulama ilk açılırken db ile gerekli senkronizasyon sağlanıyor.
    val a = syncDB()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_zikirmatik, R.id.nav_settings, R.id.nav_help
            ), drawerLayout
        )
        navView.setupWithNavController(navController)
        createNotificationChannel()
        }
    fun getDiyanetPrayerTimes(PTtownsID: String){
        postService = ApiClient.getClient().create(PostService::class.java)
        var diyanetTowns = postService.listPrayerTime(PTtownsID)
        diyanetTowns.enqueue(object : Callback<List<PrayerTime>> {
            override fun onFailure(call: Call<List<PrayerTime>>, t: Throwable) {
                Toast.makeText(applicationContext, t.message.toString(), Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<List<PrayerTime>>, response: Response<List<PrayerTime>>) {
                if (response.isSuccessful) {
                    prayertimeList = (response.body() as MutableList<PrayerTime>?)!!
                    println(prayertimeList)
                }
                else println("patladı3")
            }
        })
    }
    fun getDiyanetTowns(){
        postService = ApiClient.getClient().create(PostService::class.java)
        var diyanetTowns = postService.listTowns(Cpath)
        diyanetTowns.enqueue(object : Callback<List<Towns>> {
            override fun onFailure(call: Call<List<Towns>>, t: Throwable) {
                Toast.makeText(applicationContext, t.message.toString(), Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<List<Towns>>, response: Response<List<Towns>>) {
                if (response.isSuccessful) {
                    townsList = (response.body() as MutableList<Towns>?)!!

                }
                else println("patladı2")
            }
        })
    }
    fun getDiyanetCities(){
        postService = ApiClient.getClient().create(PostService::class.java)
        var diyanetCities = postService.listCities("1")
        diyanetCities.enqueue(object : Callback<List<Cities>> {
            override fun onFailure(call: Call<List<Cities>>, t: Throwable) {
                Toast.makeText(applicationContext, t.message.toString(), Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<List<Cities>>, response: Response<List<Cities>>) {
                if (response.isSuccessful) {
                    citiesList = (response.body() as MutableList<Cities>?)!!
                }
                else println("patladı1")
            }
        })
    }
    fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name: CharSequence = "ezanvaktireminderchannel"
            val description = "Channel for Alarm Manager"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
        }
    }
    fun cancelAlarm(){
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this,GunesReceiver::class.java)

        pendingIntent = PendingIntent.getBroadcast(this,0,intent,0)

        alarmManager.cancel(pendingIntent)

        Toast.makeText(this,"Alarm cancelled",Toast.LENGTH_LONG).show()
    }
    fun sendAlarm(alarm_hour: Int,alarm_minute: Int){
        /*calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = getCurrentDateTime().toString("HH").toInt()
        calendar[Calendar.MINUTE] = getCurrentDateTime().toString("mm").toInt()
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0*/
        calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = alarm_hour
        calendar[Calendar.MINUTE] = alarm_minute
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
    }
    fun setAlarmFimsak(){
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this,ImsakReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, notificationID,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
        println("Imsak alarm set succesfully "+calendar[Calendar.HOUR_OF_DAY]+" "+calendar[Calendar.MINUTE]+" "+ calendar[Calendar.SECOND])
    }
    fun setAlarmFgunes(){
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this,GunesReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, notificationID2,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
        println("Gunes alarm set succesfully "+calendar[Calendar.HOUR_OF_DAY]+" "+calendar[Calendar.MINUTE]+" "+ calendar[Calendar.SECOND])
    }
    fun setAlarmFogle(){
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this,OgleReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, notificationID3,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
        println("Ogle alarm set succesfully "+calendar[Calendar.HOUR_OF_DAY]+" "+calendar[Calendar.MINUTE]+" "+ calendar[Calendar.SECOND])
    }
    fun setAlarmFikindi(){
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this,IkindiReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, notificationID4,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
        println("Ikindi alarm set succesfully "+calendar[Calendar.HOUR_OF_DAY]+" "+calendar[Calendar.MINUTE]+" "+ calendar[Calendar.SECOND])
    }
    fun setAlarmFaksam(){
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this,AksamReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, notificationID5,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
        println("Aksam alarm set succesfully "+calendar[Calendar.HOUR_OF_DAY]+" "+calendar[Calendar.MINUTE]+" "+ calendar[Calendar.SECOND])
    }
    fun setAlarmFyatsi(){
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this,YatsiReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, notificationID6,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
        println("Yatsi alarm set succesfully "+calendar[Calendar.HOUR_OF_DAY]+" "+calendar[Calendar.MINUTE]+" "+ calendar[Calendar.SECOND])
    }
    fun openCloseNavigationDrawer(view: View) {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }
    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }
    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }
    fun showCurrentTime() {
            if(this.flagUpdateDT){
                date = getCurrentDateTime()
                dateInString = date.toString("dd/MM/yyyy\n  HH:mm:ss")
                findViewById<TextView>(R.id.current_date).setText(dateInString)
            }
    }
    fun calculateTime(boolean: Boolean){
            if (flagStart){
                date = getCurrentDateTime()
                val diffInMillisec: Long = date.getTime() - startDate.getTime()
                val diffInDays: Long = TimeUnit.MILLISECONDS.toDays(diffInMillisec)
                val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec) - (TimeUnit.MILLISECONDS.toDays(diffInMillisec)*24)
                val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) - (TimeUnit.MILLISECONDS.toHours(diffInMillisec)*60)
                val diffInSec: Long = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec) - (TimeUnit.MILLISECONDS.toMinutes(diffInMillisec)*60)
                findViewById<TextView>(R.id.countertime).setText(diffInDays.toString() +" Gün "+ diffInHours.toString()  + " Saat " + diffInMin.toString() + " Dakika " + diffInSec.toString() + " Saniye ")
        }
    }
    fun set_spinner(spinner: Spinner,sendedListvalues: Array<String>){
        if (spinner != null) {
            val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, sendedListvalues)
            spinner.adapter = adapter}
    }
    fun getCTdate(): Date {
        return getCurrentDateTime()
    }
    fun chooseThemeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("TEMA SEÇİN")
        val styles = arrayOf("Light","Dark","System default")
        val checkedItem = 0
        builder.setSingleChoiceItems(styles, checkedItem) { dialog, which ->
            when (which) {
                0 -> {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    delegate.applyDayNight()
                    dialog.dismiss()
                    saveTheme=0

                }
                1 -> {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    delegate.applyDayNight()
                    dialog.dismiss()
                    saveTheme=1

                }
                2 -> {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    delegate.applyDayNight()
                    dialog.dismiss()
                    saveTheme=2

                }
            }
            ref.child("saveTheme").setValue(saveTheme)
        }
        val dialog = builder.create()
        dialog.show()
    }
    fun checkTheme(){
        if(saveTheme==0){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            delegate.applyDayNight()
        }
        else if(saveTheme==1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            delegate.applyDayNight()
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            delegate.applyDayNight()
        }
    }
    fun syncDB(){
        var dataS: String
        var dataI: Int
        var dataB: Boolean
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //ayarlar spinnerların default değerde başlaması için
                dataI= dataSnapshot.child("alarm").child("imsakPos").getValue().toString().toInt()
                imsakPos=dataI
                dataI= dataSnapshot.child("melodi").child("imsakMPos").getValue().toString().toInt()
                imsakMPos=dataI
                dataB= dataSnapshot.child("alarmaktifkapali").child("imsakAK").getValue().toString().toBoolean()
                imsakAK=dataB
                ///////////////////////////////////////////////////////////////////////////////////////////////
                dataI= dataSnapshot.child("alarm").child("gunesPos").getValue().toString().toInt()
                gunesPos=dataI
                dataI= dataSnapshot.child("melodi").child("gunesMPos").getValue().toString().toInt()
                gunesMPos=dataI
                dataB= dataSnapshot.child("alarmaktifkapali").child("gunesAK").getValue().toString().toBoolean()
                gunesAK=dataB
                ///////////////////////////////////////////////////////////////////////////////////////////////
                dataI= dataSnapshot.child("alarm").child("oglePos").getValue().toString().toInt()
                oglePos=dataI
                dataI= dataSnapshot.child("melodi").child("ogleMPos").getValue().toString().toInt()
                ogleMPos=dataI
                dataB= dataSnapshot.child("alarmaktifkapali").child("ogleAK").getValue().toString().toBoolean()
                ogleAK=dataB
                ///////////////////////////////////////////////////////////////////////////////////////////////
                dataI= dataSnapshot.child("alarm").child("ikindiPos").getValue().toString().toInt()
                ikindiPos=dataI
                dataI= dataSnapshot.child("melodi").child("ikindiMPos").getValue().toString().toInt()
                ikindiMPos=dataI
                dataB= dataSnapshot.child("alarmaktifkapali").child("ikindiAK").getValue().toString().toBoolean()
                ikindiAK=dataB
                ///////////////////////////////////////////////////////////////////////////////////////////////
                dataI= dataSnapshot.child("alarm").child("aksamPos").getValue().toString().toInt()
                aksamPos=dataI
                dataI= dataSnapshot.child("melodi").child("aksamMPos").getValue().toString().toInt()
                aksamMPos=dataI
                dataB= dataSnapshot.child("alarmaktifkapali").child("aksamAK").getValue().toString().toBoolean()
                aksamAK=dataB
                ///////////////////////////////////////////////////////////////////////////////////////////////
                dataI= dataSnapshot.child("alarm").child("yatsiPos").getValue().toString().toInt()
                yatsiPos=dataI
                dataI= dataSnapshot.child("melodi").child("yatsiMPos").getValue().toString().toInt()
                yatsiMPos=dataI
                dataB= dataSnapshot.child("alarmaktifkapali").child("yatsiAK").getValue().toString().toBoolean()
                yatsiAK=dataB
                ///////////////////////////////////////////////////////////////////////////////////////////////
                dataI= dataSnapshot.child("sessiz").child("namazonce").getValue().toString().toInt()
                namazonce=dataI
                dataI= dataSnapshot.child("sessiz").child("namazoncePos").getValue().toString().toInt()
                namazoncePos=dataI
                dataI= dataSnapshot.child("sessiz").child("namazsonra").getValue().toString().toInt()
                namazsonra=dataI
                dataI= dataSnapshot.child("sessiz").child("namazsonraPos").getValue().toString().toInt()
                namazsonraPos=dataI
                dataB= dataSnapshot.child("sessiz").child("namazOSAK").getValue().toString().toBoolean()
                namazOSAK=dataB
                ///////////////////////////////////////////////////////////////////////////////////////////////
                dataB = dataSnapshot.child("flagStart").getValue().toString().toBoolean()
                flagStart=dataB
                dataI = dataSnapshot.child("zikirCount").getValue().toString().toInt()
                zikirCount=dataI
                dataS = dataSnapshot.child("currentCity").getValue().toString()
                currentCity=dataS
                dataS = dataSnapshot.child("currentTown").getValue().toString()
                currentTown=dataS
                ///////////////////////////////////////////////////////////////////////////////////////////////
                hadiscount = getrandom(dataSnapshot.child("hadisler").childrenCount.toInt())
                dataSnapshot.child("hadisler").children
                var count= 0
                for (ds in dataSnapshot.child("hadisler").getChildren()) {
                    try{
                        if(count==hadiscount) {
                            hadis = ds.getValue().toString()
                            findViewById<TextView>(R.id.hadisler).setText(hadis)
                        }
                        count++
                    }
                    catch (e : Exception){
                    }
                }
                ///////////////////////////////////////////////////////////////////////////////////////////////
                for (ds in dataSnapshot.child("Cities").getChildren()) {
                    Cities = Cities.plus(ds.key.toString())
                }
                for (ds in dataSnapshot.child("Cities").child(currentCity).children) {
                    val towns=ds.key
                    Towns= Towns.plus(towns.toString())
                }
                ///////////////////////////////////////////////////////////////////////////////////////////////
                //try catch olmadığında anasayfadan çıkınca patlıyor
                try{
                    findViewById<TextView>(R.id.current_location).setText(currentCity+" \n "+currentTown)
                }
                catch (e : Exception){
                }
                dataI = dataSnapshot.child("saveTheme").getValue().toString().toInt()
                saveTheme=dataI
                //databasedeki boş format hatalıysa patlamasın diye
                try {
                    dataS = dataSnapshot.child("startDate").getValue().toString()
                    val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    val transferdate = formatter.parse(dataS)
                    startDate = transferdate
                } catch (e: Exception) {
                }
                checkTheme()
                }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        ref.addListenerForSingleValueEvent(menuListener)
    }
    fun getrandom(limitnumber: Int): Int{
        return (0 until limitnumber).random()
    }
}