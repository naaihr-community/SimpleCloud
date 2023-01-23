package eu.thesimplecloud.plugin.minestorm.listener

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.plugin.startup.CloudPlugin
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerLoginEvent

/**
 * @created 23/01/2023 - 17:19
 * @project simplecloud-v2
 * @author  D151l
 */
class MinestomListener {

    private val UNKNOWN_ADRESS = "§cYou are connected from an unknown address!"
    private val NOT_REGISTERED = "§cYou are not registered on the network!"
    init {
        val globalEventHandler = MinecraftServer.getGlobalEventHandler()

        globalEventHandler.addListener(PlayerLoginEvent::class.java) {
            val player = it.player

            val hostAddress = player.playerConnection.serverAddress
            if (hostAddress != "127.0.0.1" && !CloudAPI.instance.getWrapperManager().getAllCachedObjects()
                    .any { it.getHost() == hostAddress }
            ) {
                player.kick(UNKNOWN_ADRESS)
                return@addListener
            }

            if (CloudAPI.instance.getCloudPlayerManager().getCachedCloudPlayer(player.uuid) == null) {
                player.kick(NOT_REGISTERED)
            }

            this.updateCurrentOnlineCountTo(MinecraftServer.getConnectionManager().onlinePlayers.size)
        }
        globalEventHandler.addListener(PlayerDisconnectEvent::class.java) {
            this.onPlayerDisconnected(it.player)
        }
    }

    private fun onPlayerDisconnected(player: Player) {
        val playerManager = CloudAPI.instance.getCloudPlayerManager()
        val cloudPlayer = playerManager.getCachedCloudPlayer(player.uuid)

        if (cloudPlayer != null && !cloudPlayer.isUpdatesEnabled()) {
            playerManager.delete(cloudPlayer)
        }
        updateCurrentOnlineCountTo(MinecraftServer.getConnectionManager().onlinePlayers.size)
    }

    private fun updateCurrentOnlineCountTo(count: Int) {
        val thisService = CloudPlugin.instance.thisService()
        thisService.setOnlineCount(count)
        thisService.update()
    }
}