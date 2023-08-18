package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.project.NotificationAlive

object ControllerAlive {

    private var lastSend = 0L

    fun sendIfNeed(){
        if(lastSend > System.currentTimeMillis() - (1000L * 60 * 3)) return
        lastSend = System.currentTimeMillis()
        ControllerSubThread.inSub("ControllerAlive"){
            for(id in API.PROTOADMINS) ControllerNotifications.push(id, NotificationAlive())
        }
    }


}