package eu.thesimplecloud.plugin.impl.player

import eu.thesimplecloud.api.exception.NoSuchPlayerException
import eu.thesimplecloud.api.exception.NoSuchServiceException
import eu.thesimplecloud.api.exception.NoSuchWorldException
import eu.thesimplecloud.api.location.ServiceLocation
import eu.thesimplecloud.api.location.SimpleLocation
import eu.thesimplecloud.api.network.packets.player.*
import eu.thesimplecloud.api.player.ICloudPlayer
import eu.thesimplecloud.api.player.connection.ConnectionResponse
import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.clientserverapi.lib.packet.packetsender.sendQuery
import eu.thesimplecloud.clientserverapi.lib.promise.CommunicationPromise
import eu.thesimplecloud.clientserverapi.lib.promise.ICommunicationPromise
import eu.thesimplecloud.plugin.extension.syncBukkit
import eu.thesimplecloud.plugin.network.packets.PacketOutTeleportOtherService
import eu.thesimplecloud.plugin.startup.CloudPlugin
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player

/**
 * @created 23/01/2023 - 17:28
 * @project simplecloud-v2
 * @author  D151l
 */
class CloudPlayerManagerMinestom : AbstractServiceCloudPlayerManager() {

    override fun sendMessageToPlayer(cloudPlayer: ICloudPlayer, component: Component): ICommunicationPromise<Unit> {
        return CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOSendMessageToCloudPlayer(
                cloudPlayer,
                component
            )
        )
    }

    override fun connectPlayer(
        cloudPlayer: ICloudPlayer,
        cloudService: ICloudService
    ): ICommunicationPromise<ConnectionResponse> {
        return CloudPlugin.instance.connectionToManager.sendQuery(
            PacketIOConnectCloudPlayer(cloudPlayer, cloudService),
            500
        )
    }

    override fun kickPlayer(cloudPlayer: ICloudPlayer, message: String): ICommunicationPromise<Unit> {
        return CloudPlugin.instance.connectionToManager.sendUnitQuery(PacketIOKickCloudPlayer(cloudPlayer, message))
    }

    override fun sendTitle(
        cloudPlayer: ICloudPlayer,
        title: String,
        subTitle: String,
        fadeIn: Int,
        stay: Int,
        fadeOut: Int
    ) {
        CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOSendTitleToCloudPlayer(
                cloudPlayer,
                title,
                subTitle,
                fadeIn,
                stay,
                fadeOut
            )
        )
    }

    override fun forcePlayerCommandExecution(cloudPlayer: ICloudPlayer, command: String) {
        CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOCloudPlayerForceCommandExecution(
                cloudPlayer,
                command
            )
        )
    }

    override fun sendActionbar(cloudPlayer: ICloudPlayer, actionbar: String) {
        CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOSendActionbarToCloudPlayer(
                cloudPlayer,
                actionbar
            )
        )
    }

    override fun sendTablist(cloudPlayer: ICloudPlayer, headers: Array<String>, footers: Array<String>) {
        CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOSendTablistToPlayer(
                cloudPlayer.getUniqueId(),
                headers,
                footers
            )
        )
    }

    override fun teleportPlayer(cloudPlayer: ICloudPlayer, location: SimpleLocation): ICommunicationPromise<Unit> {
        if (CloudPlugin.instance.thisServiceName != cloudPlayer.getConnectedServerName()) {
            return CloudPlugin.instance.connectionToManager.sendUnitQuery(PacketIOTeleportPlayer(cloudPlayer, location))
        }

        val bukkitPlayer = getPlayerByCloudPlayer(cloudPlayer)
        bukkitPlayer
            ?: return CommunicationPromise.failed(NoSuchPlayerException("Unable to find the player on the server service"))
        val bukkitLocation = getLocationBySimpleLocation(location)
        bukkitLocation
            ?: return CommunicationPromise.failed(NoSuchWorldException("Unable to find world: ${location.worldName}"))
        syncBukkit { bukkitPlayer.teleport(bukkitLocation) }
        return CommunicationPromise.of(Unit)
    }

    override fun teleportPlayer(cloudPlayer: ICloudPlayer, location: ServiceLocation): ICommunicationPromise<Unit> {
        if (location.getService() == null) return CommunicationPromise.failed(NoSuchServiceException("Service to connect the player to cannot be found."))
        return CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketOutTeleportOtherService(cloudPlayer.getUniqueId(), location.serviceName, location as SimpleLocation),
            1000
        )
    }

    override fun hasPermission(cloudPlayer: ICloudPlayer, permission: String): ICommunicationPromise<Boolean> {
        return CloudPlugin.instance.connectionToManager.sendQuery(
            PacketIOPlayerHasPermission(
                cloudPlayer.getUniqueId(),
                permission
            ), 400
        )
    }

    override fun getLocationOfPlayer(cloudPlayer: ICloudPlayer): ICommunicationPromise<ServiceLocation> {
        if (CloudPlugin.instance.thisServiceName != cloudPlayer.getConnectedServerName()) {
            return CloudPlugin.instance.connectionToManager.sendQuery(PacketIOGetPlayerLocation(cloudPlayer))
        }

        val bukkitPlayer = getPlayerByCloudPlayer(cloudPlayer)
        bukkitPlayer ?: return CommunicationPromise.failed(NoSuchPlayerException("Unable to find bukkit player"))

        return CommunicationPromise.of(
            ServiceLocation(
                CloudPlugin.instance.thisService(),
                "",
                0.0,
                0.0,
                0.0,
                0F,
                0F
            )
        )
    }

    override fun sendPlayerToLobby(cloudPlayer: ICloudPlayer): ICommunicationPromise<Unit> {
        return CloudPlugin.instance.connectionToManager.sendQuery(PacketIOSendPlayerToLobby(cloudPlayer.getUniqueId()))
    }

    private fun getLocationBySimpleLocation(simpleLocation: SimpleLocation): Pos? {
        var pos = Pos(
            simpleLocation.x,
            simpleLocation.y,
            simpleLocation.z,
            simpleLocation.yaw,
            simpleLocation.pitch
        )

        return pos
    }

    private fun getPlayerByCloudPlayer(cloudPlayer: ICloudPlayer): Player? {
        return MinecraftServer.getConnectionManager().getPlayer(cloudPlayer.getUniqueId())
    }
}