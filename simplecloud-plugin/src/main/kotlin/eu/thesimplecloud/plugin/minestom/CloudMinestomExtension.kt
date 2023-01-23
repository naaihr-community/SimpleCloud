package eu.thesimplecloud.plugin.minestom

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.player.ICloudPlayerManager
import eu.thesimplecloud.plugin.impl.player.CloudPlayerManagerMinestom
import eu.thesimplecloud.plugin.listener.CloudListener
import eu.thesimplecloud.plugin.minestom.listener.MinestomListener
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
class CloudMinestomExtension : Extension(), ICloudMinestomExtension {

    companion object {
        @JvmStatic
        lateinit var instance: CloudMinestomExtension
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
        return CloudPlayerManagerMinestom::class
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