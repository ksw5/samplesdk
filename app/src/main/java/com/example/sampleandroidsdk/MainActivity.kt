package com.example.sampleandroidsdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony.Carriers.SERVER
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.nanorep.convesationui.structure.controller.ChatController
import com.nanorep.convesationui.structure.controller.ChatLoadResponse
import com.nanorep.convesationui.structure.controller.ChatLoadedListener
import com.nanorep.nanoengine.bot.BotAccount
import com.nanorep.sdkcore.utils.runMain


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }
}