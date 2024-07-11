package com.example.panda_print_plugin

import android.app.Activity
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import java.lang.Exception

open class PandaPrintActivityAware: ActivityAware{
    private var activityNullable: Activity? = null
    val activity: Activity
        get() {
            if (activityNullable == null){
                throw Exception("Activity was detached")
            }
            return activityNullable!!
        }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityNullable = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
        activityNullable = null
    }
}