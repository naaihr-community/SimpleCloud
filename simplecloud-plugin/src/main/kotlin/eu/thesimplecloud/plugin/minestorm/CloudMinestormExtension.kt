package eu.thesimplecloud.plugin.minestorm

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.player.ICloudPlayerManager
import eu.thesimplecloud.plugin.impl.player.CloudPlayerManagerMinestorm
import eu.thesimplecloud.plugin.listener.CloudListener
import eu.thesimplecloud.plugin.minestorm.listener.MinestomListener
import eu.thesimplecloud.plugin.startup.CloudPlugin
import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension
import net.minestom.server.timer.Scheduler
import net.minestom.server.timer.TaskSchedule
import kotlin.reflect.KClass


/**
 * @created 23/01/2023 - 16:14
 * @project simplecloud-v2
 * @author  D151l
 */
class CloudMinestormExtension : Extension(), ICloudMinestomExtension {

    companion object {
        @JvmStatic
        lateinit var instance: CloudMinestormExtension
    }

    init {
        instance = this
    }

    override fun preInitialize() {
        CloudPlugin(this)
    }

    override fun initialize() {
        CloudPlugin.instance.onEnable()
        CloudAPI.instance.getEventManager().registerListener(CloudPlugin.instance, CloudListener())
        MinestomListener()
        //synchronizeOnlineCountTask()
    }


    override fun terminate() {
        CloudPlugin.instance.onDisable()
    }

    override fun getCloudPlayerManagerClass(): KClass<out ICloudPlayerManager> {
        return CloudPlayerManagerMinestorm::class
    }

    override fun shutdown() {
        MinecraftServer.stopCleanly()
    }

    override fun onBeforeFirstUpdate() {
        CloudPlugin.instance.thisService().setMOTD(MinecraftServer.getBrandName())
    }

    private fun synchronizeOnlineCountTask() {
        val scheduler: Scheduler = MinecraftServer.getSchedulerManager()
        scheduler.scheduleNextTick {
            val service = CloudPlugin.instance.thisService()
            val onlinePlayers = MinecraftServer.getConnectionManager().onlinePlayers.size
            if (service.getOnlineCount() != onlinePlayers) {
                service.setOnlineCount(onlinePlayers)
                service.update()
            }
        }
        scheduler.submitTask {
            TaskSchedule.seconds(3)
        }
    }
}