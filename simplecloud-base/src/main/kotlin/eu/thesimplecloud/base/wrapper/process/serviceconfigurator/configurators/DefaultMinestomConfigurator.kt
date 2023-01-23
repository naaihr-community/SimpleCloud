package eu.thesimplecloud.base.wrapper.process.serviceconfigurator.configurators

import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.base.wrapper.process.serviceconfigurator.IServiceConfigurator
import eu.thesimplecloud.jsonlib.JsonLib
import eu.thesimplecloud.launcher.utils.FileCopier
import java.io.File

/**
 * @created 23/01/2023 - 15:59
 * @project simplecloud-v2
 * @author  D151l
 */
class DefaultMinestomConfigurator: IServiceConfigurator {

    override fun configureService(cloudService: ICloudService, serviceTmpDirectory: File) {
        val minestormConfigFile = File(serviceTmpDirectory, "config.json")
        if (!minestormConfigFile.exists()) {
            FileCopier.copyFileOutOfJar(minestormConfigFile, "/files/config.json")
        }
        val jsonElement = JsonLib.fromJsonFile(minestormConfigFile)!!
        jsonElement.append("host", cloudService.getHost())
        jsonElement.append("port", cloudService.getPort())
        jsonElement.append("maxPlayers", cloudService.getMaxPlayers())
        jsonElement.append("mojangAuth", false)
        jsonElement.append("bungeeCordSupport", true)
        jsonElement.append("velocitySupport", false)

        jsonElement.saveAsFile(minestormConfigFile)
    }
}