package com.example.inventory
import android.content.Context
import android.os.Bundle


const val PROFILE_NAME = "HCInventory"

//const val PROFILE_INTENT_ACTION = "com.hcdelta.eleltar.activity.SCAN"
const val PROFILE_INTENT_ACTION = "com.hcdelta.eleltar.SCAN"

//const val PROFILE_INTENT_START_ACTIVITY = "0"
const val PROFILE_INTENT_BROADCAST_INTENT = "2"

fun Context.createDataWedgeProfile(dataWedgeInterface: DataWedgeInterface) {
    //  Create and configure the DataWedge profile associated with this application
    //  For readability's sake, I have not defined each of the keys in the DWInterface file
    dataWedgeInterface.sendCommandString(
        this, DataWedgeInterface.DATAWEDGE_SEND_CREATE_PROFILE,
        PROFILE_NAME
    )
    val profileConfig = Bundle()
    profileConfig.putString("PROFILE_NAME", PROFILE_NAME)
    profileConfig.putString("PROFILE_ENABLED", "true") //  These are all strings
    profileConfig.putString("CONFIG_MODE", "UPDATE")
//    profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST")
    profileConfig.putString("RESET_CONFIG", "true")

    val barcodeConfig = Bundle()
    barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
    barcodeConfig.putString(
        "RESET_CONFIG",
        "true"
    ) //  This is the default but never hurts to specify
    val barcodeProps = Bundle()
    barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
    profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)
    val appConfig = Bundle()
    appConfig.putString(
        "PACKAGE_NAME",
        packageName
    )      //  Associate the profile with this app
    appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
    profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))
    dataWedgeInterface.sendCommandBundle(
        this,
        DataWedgeInterface.DATAWEDGE_SEND_SET_CONFIG,
        profileConfig
    )
    //  You can only configure one plugin at a time in some versions of DW, now do the intent output
    profileConfig.remove("PLUGIN_CONFIG")
    val intentConfig = Bundle()
    intentConfig.putString("PLUGIN_NAME", "INTENT")
    intentConfig.putString("RESET_CONFIG", "true")
    val intentProps = Bundle()
    intentProps.putString("intent_output_enabled", "true")
    intentProps.putString("intent_action", PROFILE_INTENT_ACTION)
    //intentProps.putString("intent_delivery", PROFILE_INTENT_START_ACTIVITY)  //  "0"
    intentProps.putString(
        "intent_delivery",
        PROFILE_INTENT_BROADCAST_INTENT
    )  //  "broadcast intent"
    intentConfig.putBundle("PARAM_LIST", intentProps)
    profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)
    dataWedgeInterface.sendCommandBundle(
        this,
        DataWedgeInterface.DATAWEDGE_SEND_SET_CONFIG,
        profileConfig
    )
}
