/*
 * MIT License
 *
 * Copyright (C) 2020-2022 The SimpleCloud authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.thesimplecloud.plugin.extension

import eu.thesimplecloud.plugin.minestom.ICloudMinestomExtension
import eu.thesimplecloud.plugin.proxy.ICloudProxyPlugin
import eu.thesimplecloud.plugin.proxy.bungee.CloudBungeePlugin
import eu.thesimplecloud.plugin.server.CloudSpigotPlugin
import eu.thesimplecloud.plugin.startup.CloudPlugin
import net.md_5.bungee.api.ProxyServer
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.Scheduler
import net.minestom.server.timer.Task
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

fun syncBukkit(function: () -> Unit) = Bukkit.getScheduler().runTask(CloudSpigotPlugin.instance, function)

fun syncService(function: () -> Unit) {
    if (CloudPlugin.instance.cloudServicePlugin is ICloudProxyPlugin) {
        ProxyServer.getInstance().scheduler.schedule(CloudBungeePlugin.instance, function, 0, TimeUnit.MILLISECONDS)
    } else if(CloudPlugin.instance.cloudServicePlugin is ICloudMinestomExtension) {
        val scheduler: Scheduler = MinecraftServer.getSchedulerManager();
        val task: Task = scheduler.scheduleNextTick(function)
        task.cancel()
    } else {
        Bukkit.getScheduler().runTask(CloudSpigotPlugin.instance, function)
    }
}