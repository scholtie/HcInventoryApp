package com.example.inventory.service

import android.content.Context
import android.util.Log
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.FileInputStream
import java.io.FileOutputStream


class MyFTPClientFunctions {
    var mFTPClient: FTPClient? = null

    // Method to connect to FTP server:
    fun ftpConnect(
        host: String, username: String?, password: String?,
        port: Int
    ): Boolean {
        try {
            mFTPClient = FTPClient()
            // connecting to the host
            mFTPClient!!.connect(host, port)

            // now check the reply code, if positive mean connection success
            if (FTPReply.isPositiveCompletion(mFTPClient!!.replyCode)) {
                // login using username & password
                val status = mFTPClient!!.login(username, password)

                /*
				 * Set File Transfer Mode
				 *
				 * To avoid corruption issue you must specified a correct
				 * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
				 * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE for
				 * transferring text, image, and compressed files.
				 */mFTPClient!!.setFileType(FTP.BINARY_FILE_TYPE)
                mFTPClient!!.enterLocalPassiveMode()
                return status
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: could not connect to host $host")
        }
        return false
    }

    // Method to disconnect from FTP server:
    fun ftpDisconnect(): Boolean {
        try {
            mFTPClient!!.logout()
            mFTPClient!!.disconnect()
            return true
        } catch (e: Exception) {
            Log.d(TAG, "Error occurred while disconnecting from ftp server.")
        }
        return false
    }

    // Method to get current working directory:
    fun ftpGetCurrentWorkingDirectory(): String? {
        try {
            return mFTPClient!!.printWorkingDirectory()
        } catch (e: Exception) {
            Log.d(
                TAG,
                "Error: could not get current working directory."
            )
        }
        return null
    }

    // Method to change working directory:
    fun ftpChangeDirectory(directory_path: String): Boolean {
        try {
            mFTPClient!!.changeWorkingDirectory(directory_path)
        } catch (e: Exception) {
            Log.d(
                TAG,
                "Error: could not change directory to $directory_path"
            )
        }
        return false
    }

    // Method to list all files in a directory:
    fun ftpPrintFilesList(dir_path: String?): Array<String?>? {
        var fileList: Array<String?>? = null
        return try {
            val ftpFiles = mFTPClient!!.listFiles(dir_path)
            val length = ftpFiles.size
            fileList = arrayOfNulls(length)
            for (i in 0 until length) {
                val name = ftpFiles[i].name
                val isFile = ftpFiles[i].isFile
                if (isFile) {
                    fileList[i] = "File :: $name"
                    Log.i(TAG, "File : $name")
                } else {
                    fileList[i] = "Directory :: $name"
                    Log.i(TAG, "Directory : $name")
                }
            }
            fileList
        } catch (e: Exception) {
            e.printStackTrace()
            fileList
        }
    }

    // Method to create new directory:
    fun ftpMakeDirectory(new_dir_path: String): Boolean {
        try {
            return mFTPClient!!.makeDirectory(new_dir_path)
        } catch (e: Exception) {
            Log.d(
                TAG, "Error: could not create new directory named "
                        + new_dir_path
            )
        }
        return false
    }

    // Method to delete/remove a directory:
    fun ftpRemoveDirectory(dir_path: String): Boolean {
        try {
            return mFTPClient!!.removeDirectory(dir_path)
        } catch (e: Exception) {
            Log.d(
                TAG,
                "Error: could not remove directory named $dir_path"
            )
        }
        return false
    }

    // Method to delete a file:
    fun ftpRemoveFile(filePath: String?): Boolean {
        try {
            return mFTPClient!!.deleteFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    // Method to rename a file:
    fun ftpRenameFile(from: String, to: String): Boolean {
        try {
            return mFTPClient!!.rename(from, to)
        } catch (e: Exception) {
            Log.d(TAG, "Could not rename file: $from to: $to")
        }
        return false
    }
    // Method to download a file from FTP server:
    /**
     * mFTPClient: FTP client connection object (see FTP connection example)
     * srcFilePath: path to the source file in FTP server desFilePath: path to
     * the destination file to be saved in sdcard
     */
    fun ftpDownload(srcFilePath: String?, desFilePath: String?): Boolean {
        var status = false
        try {
            val desFileStream = FileOutputStream(desFilePath)
            status = mFTPClient!!.retrieveFile(srcFilePath, desFileStream)
            desFileStream.close()
            return status
        } catch (e: Exception) {
            Log.d(TAG, "download failed")
        }
        return status
    }
    // Method to upload a file to FTP server:
    /**
     * mFTPClient: FTP client connection object (see FTP connection example)
     * srcFilePath: source file path in sdcard desFileName: file name to be
     * stored in FTP server desDirectory: directory path where the file should
     * be upload to
     */
    fun ftpUpload(
        srcFilePath: String?, desFileName: String?,
        desDirectory: String?, context: Context?
    ): Boolean {
        var status = false
        try {
            val srcFileStream = FileInputStream(srcFilePath)

            // change working directory to the destination directory
            // if (ftpChangeDirectory(desDirectory)) {
            status = mFTPClient!!.storeFile(desFileName, srcFileStream)
            // }
            srcFileStream.close()
            return status
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "upload failed: $e")
        }
        return status
    }

    companion object {
        // Now, declare a public FTP client object.
        private const val TAG = "MyFTPClientFunctions"
    }
}