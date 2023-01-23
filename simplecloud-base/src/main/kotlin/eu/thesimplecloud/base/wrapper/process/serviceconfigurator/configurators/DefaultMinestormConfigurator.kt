package eu.thesimplecloud.base.wrapper.process.serviceconfigurator.configurators

import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.api.utils.ConfigurationFileEditor
import eu.thesimplecloud.base.wrapper.process.serviceconfigurator.IServiceConfigurator
import eu.thesimplecloud.jsonlib.JsonLib
import eu.thesimplecloud.launcher.utils.FileCopier
import java.io.File

/**
 * @created 23/01/2023 - 15:59
 * @project simplecloud-v2
 * @author  D151l
 */
class DefaultMinestormConfigurator: IServiceConfigurator {

    override fun configureService(cloudService: ICloudService, serviceTmpDirectory: File) {
        val minestormConfigFile = File(serviceTmpDirectory, "config.json")
        if (!minestormConfigFile.exists()) {
            FileCopier.copyFileOutOfJar(minestormConfigFile, "/files/config.json")
        }
        val jsonElement = JsonLib.fromJsonFile(minestormConfigFile)!!
        jsonElement.append("host", cloudService.getHost())
        jsonElement.append("port", cloudService.getPort())
        jsonElement.append("max_players", cloudService.getMaxPlayers())
        jsonElement.append("velocitySupport", true)
        jsonElement.append("velocitySecretKey", "z5r6ghru")

        jsonElement.saveAsFile(minestormConfigFile)
    }
}