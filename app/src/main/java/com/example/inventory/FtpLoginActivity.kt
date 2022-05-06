package com.example.inventory

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.inventory.databinding.ActivityFtpLoginBinding
import com.example.inventory.service.MyFTPClientFunctions
import java.util.*

class FtpLoginActivity : AppCompatActivity() {

    private var ftpclient: MyFTPClientFunctions? = null
    private lateinit var binding: ActivityFtpLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFtpLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ftpclient = MyFTPClientFunctions()
        binding.btnLogin.isVisible = true
        binding.btnLogin.setOnClickListener { checkPassword() }
        binding.loginBtn4.setOnClickListener { connectFtp() }
    }

    private fun checkPassword() {
        if (binding.editLoginPass.text.toString() != "")
        {
            if (binding.editLoginPass.text.toString().toInt() == Pointer8000())
            {
                binding.btnLogin.isVisible = false
                binding.editLoginPass.isVisible = false
                hideKeyboard()
                initData()
            }
            else{
                runOnUiThread { Toast.makeText(this@FtpLoginActivity,
                    getString(R.string.invalid_password), Toast.LENGTH_SHORT).show() }
            }
        }
        else{
            runOnUiThread { Toast.makeText(this@FtpLoginActivity,
                getString(R.string.invalid_password), Toast.LENGTH_SHORT).show() }
        }

    }

    private fun Pointer8000(): Int{
        val today = Date()
        val month: Int = today.month + 1
        val day: Int = today.date
        return 8731 - ((month + day) * 100 + Math.abs(month - day))
    }

    private fun initData(){
        val sharedPreferencesFtp = this.getSharedPreferences(
            "FtpDetails", Context.MODE_PRIVATE)
        val srcFilePath: String? = sharedPreferencesFtp.getString("path", "")
        val srcFilePathSend : String? = sharedPreferencesFtp.getString("pathSend", "")
        val username: String? = sharedPreferencesFtp.getString("username", "")
        val password: String? = sharedPreferencesFtp.getString("password", "")
        val host: String? = sharedPreferencesFtp.getString("host", "")
        val port: String? = sharedPreferencesFtp.getString("port", "0")
        binding.editPasswordFtp.setText(password, TextView.BufferType.SPANNABLE)
        binding.editTextHost.setText(host, TextView.BufferType.SPANNABLE)
        binding.editTextPortFtp.setText(port, TextView.BufferType.SPANNABLE)
        binding.editTextFilePath.setText(srcFilePath, TextView.BufferType.SPANNABLE)
        binding.editTextUserFtp.setText(username, TextView.BufferType.SPANNABLE)
        binding.editTextFilePathSend.setText(srcFilePathSend, TextView.BufferType.SPANNABLE)
        binding.editPasswordFtp.isVisible = true
        binding.editTextHost.isVisible = true
        binding.editTextPortFtp.isVisible = true
        binding.editTextFilePath.isVisible = true
        binding.editTextUserFtp.isVisible = true
        binding.editTextFilePathSend.isVisible = true
        binding.loginBtn4.isVisible = true
        binding.textView2.isVisible = true
        binding.textView3.isVisible = true
        binding.editTextHost.requestFocus()
    }

    private fun connectFtp(){
        runOnUiThread {
            findViewById<ProgressBar>(R.id.progressBar2).isVisible = true }
        val username = binding.editTextUserFtp.text.toString()
        val password = binding.editPasswordFtp.text.toString()
        val host = binding.editTextHost.text.toString()
        val port = binding.editTextPortFtp.text.toString()
        val srcFilePath = binding.editTextFilePath.text.toString()
        val srcFilePathSend = binding.editTextFilePathSend.text.toString()
        Thread {
            // host – your FTP address
            // username & password – for your secured login
            // 21 default gateway for FTP
            val status: Boolean = ftpclient!!.ftpConnect(host, username, password, port.toInt())
            val directoryExists: Boolean = ftpclient!!.ftpChangeDirectory(srcFilePath)
            val directoryExistsSend : Boolean = ftpclient!!.ftpChangeDirectory(srcFilePathSend)
            if (status) {
                Log.d(ContentValues.TAG, "Connection Success")
                if (directoryExists && directoryExistsSend){
                    ftpclient!!.ftpChangeDirectory(srcFilePath)
                    val sharedPreferences = this
                        .getSharedPreferences("FtpDetails", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString("path", srcFilePath)
                    editor.putString("host", host)
                    editor.putString("username", username)
                    editor.putString("password", password)
                    editor.putString("port", port)
                    editor.putString("pathSend", srcFilePathSend)
                    editor.apply()
                    //downloadFtp()
                    //uploadFtp()
                    //ftpclient!!.ftpPrintFilesList(srcFilePath)
                    val switchActivityIntent = Intent(this, LoginActivity::class.java)
                    startActivity(switchActivityIntent)
                }else{
                    runOnUiThread {
                        findViewById<ProgressBar>(R.id.progressBar2).isVisible = false }
                    Log.d(ContentValues.TAG, "Elérési út nem létezik!")
                    runOnUiThread { Toast.makeText(this@FtpLoginActivity,
                        getString(R.string.pathnotexist), Toast.LENGTH_SHORT).show() }
                }
            } else {
                runOnUiThread {
                    findViewById<ProgressBar>(R.id.progressBar2).isVisible = false }
                Log.d(ContentValues.TAG, "Connection failed")
                runOnUiThread { Toast.makeText(this@FtpLoginActivity,
                    getString(R.string.unsuccessfulloig), Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE)
                as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /*private fun uploadFtp(){
        val srcFilePath = binding.editTextFilePath.text.toString()
        val sdf = SimpleDateFormat("yyyy.M.dd.hh.mm.ss")
        val currentDate = sdf.format(Date()).toString()
        println()
        val desFilePath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
        ftpclient!!.ftpUpload("$desFilePath/items.txt", "items$currentDate.txt", srcFilePath, this)
    }

    private fun downloadFtp(){
        val srcFilePath = binding.editTextFilePath.text.toString()
        val srcFileNameCikk = "cikk.txt"
        val srcFileNameVonalkod = "vonalkod.txt"
        val srcFileNameLeltarhely = "leltarhely.txt"
        val srcFileNameLeltarfej = "leltarfej.txt"
        val srcFileNameUgyintezo = "ugyintezo.txt"
        val desFilePath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
        val desFileP = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val fileCikk = File(desFileP, srcFileNameCikk)
        fileCikk.delete()
        val fileVonalkod = File(desFileP, srcFileNameVonalkod)
        fileVonalkod.delete()
        val fileLelarhely = File(desFileP, srcFileNameLeltarhely)
        fileLelarhely.delete()
        val fileLeltarfej = File(desFileP, srcFileNameLeltarfej)
        fileLeltarfej.delete()
        val fileUgyintezo = File(desFileP, srcFileNameUgyintezo)
        fileUgyintezo.delete()
        ftpclient!!.ftpDownload("$srcFilePath/$srcFileNameCikk",
            "$desFilePath/$srcFileNameCikk"
        )
        ftpclient!!.ftpDownload("$srcFilePath/$srcFileNameVonalkod",
            "$desFilePath/$srcFileNameVonalkod"
        )
        ftpclient!!.ftpDownload("$srcFilePath/$srcFileNameLeltarhely",
            "$desFilePath/$srcFileNameLeltarhely"
        )
        ftpclient!!.ftpDownload("$srcFilePath/$srcFileNameLeltarfej",
            "$desFilePath/$srcFileNameLeltarfej"
        )
        ftpclient!!.ftpDownload("$srcFilePath/$srcFileNameUgyintezo",
            "$desFilePath/$srcFileNameUgyintezo"
        )
    }

    private fun disconnectFtp(){
        Thread{
            val status : Boolean = ftpclient!!.ftpDisconnect()
            if (status) {
                Log.d(ContentValues.TAG, "Disconnection Success")
            } else {
                Log.d(ContentValues.TAG, "Disconnection failed")
            }
        }.start()
    }*/
}