package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.project.NotificationDonate
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.optimizers.OptimizerSponsor
import com.dzen.campfire.server.tables.TSupport
import com.sup.dev.java.libs.http_server.HttpServer
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

object ControllerDonates {

    private val server = HttpServer(4051)
    private val secretDonates = App.secrets.getJson("donates")?: Json()
    private val secretField = secretDonates.getString("field")
    private val secretValue = secretDonates.getString("value")

    fun start(){
        ToolsThreads.thread {
            server.start {
                val message = it.read().message
                parseMessage(message.split("\n").toTypedArray())
                it.sendOK()
                it.close()
            }
        }

    }

    private fun parseMessage(array:Array<String>){

        var sum:Long? = null
        var accountId:Long? = null
        var donateId = 0L

        var secretFieldFound = false

        for (s in array) {
            if (s.contains("notification_type=")){
                val split = s.split("&")
                for(ss in split){
                    if(ss.contains("$secretField=") && ss.contains(secretValue)){
                        secretFieldFound = true
                    }
                    if(ss.contains("withdraw_amount=")){
                        val sp = ss.split("=")
                        if(sp.size>1) sum = (sp[1].toDouble() * 100).toLong()
                    }
                    if(ss.contains("label=") && !ss.contains("operation_label=")){
                        val sp = ss.split("=")
                        if(sp.size>1){
                            val spp = sp[1].split("-")
                            if(spp.isNotEmpty()) accountId = spp[0].toLong()
                            if(spp.size > 1) donateId = spp[1].toLong()

                        }
                    }
                }
            }
        }

        if(secretFieldFound && accountId != null && sum != null && donateId != 0L) {
            var text = if(array.isEmpty()) "" else array[0]
            for (s in array) text += "\n" + s
            addDonate(accountId, sum, text, donateId)
        }

    }

    fun insertDonateDraft(accountId:Long, comment:String, sum:Long):Long{
        val date = ToolsDate.getStartOfMonth()

        return Database.insert("ControllerDonates insertDonateDraft", TSupport.NAME,
                TSupport.date, date,
                TSupport.user_id, accountId,
                TSupport.status, API.STATUS_DRAFT,
                TSupport.comment, comment,
                TSupport.count, sum,
                TSupport.donate_info, "",
                TSupport.date_create, System.currentTimeMillis()
        )
    }

    fun addDonate(accountId:Long, sum:Long, info:String, donateId:Long){
        val date = ToolsDate.getStartOfMonth()
        var donateRecordId = 0L

        val v = Database.select("ControllerDonates addDonate select 1", SqlQuerySelect(TSupport.NAME, TSupport.id, TSupport.status)
                .where(TSupport.id, "=", donateId))

        if(v.isEmpty){

            val vv = Database.select("ControllerDonates addDonate select 2", SqlQuerySelect(TSupport.NAME, TSupport.id)
                    .where(TSupport.status, "=", API.STATUS_DRAFT)
                    .where(TSupport.user_id, "=",accountId)
                    .sort(TSupport.date_create, false)
            )

            if(vv.isEmpty){
                donateRecordId = insertDonateDraft(accountId, "", sum)
            }else{
                donateRecordId = vv.next()
            }

        }else{
            donateRecordId = v.next()
            val status:Long = v.next()
            if(status != API.STATUS_DRAFT) return
        }

        Database.update("ControllerDonates addDonate update", SqlQueryUpdate(TSupport.NAME)
                .where(TSupport.id, "=", donateRecordId)
                .update(TSupport.count, sum)
                .update(TSupport.date, date)
                .update(TSupport.status, API.STATUS_PUBLIC)
                .updateValue(TSupport.donate_info, info)

        )

        OptimizerSponsor.setSponsor(accountId, sum)

        ControllerNotifications.push(accountId, NotificationDonate(sum))

    }

}