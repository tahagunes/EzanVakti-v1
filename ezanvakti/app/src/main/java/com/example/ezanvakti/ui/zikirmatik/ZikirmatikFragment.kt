package com.example.ezanvakti.ui.zikirmatik

import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ezanvakti.MainActivity
import com.example.ezanvakti.databinding.FragmentZikirmatikBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ZikirmatikFragment : Fragment() {

    private var _binding: FragmentZikirmatikBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val zikirmatikViewModel =
            ViewModelProvider(this).get(ZikirmatikViewModel::class.java)

        _binding = FragmentZikirmatikBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textZikirmatik
        zikirmatikViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        //süreyi başlatma bağlantısı
        _binding!!.buttonStarttimer.setOnClickListener {
            (activity as MainActivity).flagStart = true
            (activity as MainActivity).ref.child("flagStart").setValue(true)
            (activity as MainActivity).startDate = (activity as MainActivity).getCurrentDateTime()
            val dateFormat: DateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val date = Date()
            val strDate: String = dateFormat.format(date).toString()
            (activity as MainActivity).ref.child("startDate").setValue(strDate)
        }
        //tüm değerleri sıfırlama bağlantısı
        _binding!!.buttonResetzikirmatik.setOnClickListener {
            (activity as MainActivity).flagStart = false
            (activity as MainActivity).ref.child("flagStart").setValue(false)
            (activity as MainActivity).zikirCount=0
            (activity as MainActivity).ref.child("zikirCount").setValue(0)
            _binding?.countertime?.text = "0 Gün 0 Saat 0 Dakika 0 Saniye"
            (activity as MainActivity).ref.child("startDate").setValue("")
            calculateZikir()
        }
        //zikir butonu bağlantısı
        _binding!!.buttonForZikir.setOnClickListener {
            (activity as MainActivity).zikirCount= 1+(activity as MainActivity).zikirCount
            (activity as MainActivity).ref.child("zikirCount").setValue(1+(activity as MainActivity).zikirCount)
            calculateZikir()
        }
        //sayaç başlamışsa sayacı güncellemek için
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    if((activity as MainActivity).flagStart){
                        (activity as MainActivity).calculateTime((activity as MainActivity).flagStart)
                    }
                } catch (e : Exception){
                    //println("Exception is handled.")
                }
                handler.postDelayed(this, 1000)//1 sec delay
            }
        }, 0)

        calculateZikir()
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun calculateZikir(){
        _binding?.zikircounter?.text = (activity as MainActivity).zikirCount.toString()
    }

}