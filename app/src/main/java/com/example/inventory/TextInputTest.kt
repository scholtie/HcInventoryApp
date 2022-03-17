package com.example.inventory

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


class TextInputTest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_input_test)
        val btn = findViewById<View>(R.id.btnGetText) as Button
        btn.setOnClickListener { getTextFromFile() }
        //val sdcard = getExternalFilesDir()
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ugyintezo.txt")
        val text = StringBuilder()


        /*try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                val arr = line!!.split(";").toTypedArray()
                println("arr[0] = " + arr[0])
                text.append(line)
                text.append('\n')
            }
            br.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }*/

        /*val tv = findViewById<View>(R.id.textInputTestText) as TextView
        tv.text = text.toString()*/

        /*try{
            val inputStream: InputStream = file.inputStream()
            val lineList = mutableListOf<String>()

            inputStream.bufferedReader().forEachLine { lineList.add(it) }
            lineList.forEach{text.append(it)}
        } catch (e: IOException){

        }*/


    }

    private fun getTextFromFile(){
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ugyintezo.txt")
        val text = StringBuilder()
        var br: BufferedReader? = null
        val characters =
            arrayOfNulls<String>(1024) //just an example - you have to initialize it to be big enough to hold all the lines!
        try {
            br = BufferedReader(FileReader(file))
            var sCurrentLine: String?
            var i = 0
            while (br.readLine().also { sCurrentLine = it } != null) {
                val arr = sCurrentLine!!.split(";").toTypedArray()
                //for the first line it'll print
                //text.append("arr[id] = " + arr[0]) // 20000008
                text.append(arr[1]) // username
                text.append('\n')
                //text.append("arr[empty] = " + arr[2]) //
                //text.append("arr[false] = " + arr[3]) // false
                //text.append("arr[true] = " + arr[4]) // true

                //Now if you want to enter them into separate arrays
                characters[i] = arr[0]
                // and you can do the same with
                // names[1] = arr[1]
                //etc
                i++
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                br?.close();
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        val tv = findViewById<View>(R.id.textInputTestText) as TextView
        tv.text = text.toString()
    }
}