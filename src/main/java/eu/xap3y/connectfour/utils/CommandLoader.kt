package eu.xap3y.connectfour.utils

import eu.xap3y.connectfour.ConnectFour
import eu.xap3y.connectfour.commands.RootCommand
import org.bukkit.command.CommandSender
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.bukkit.BukkitCommandManager
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.execution.ExecutionCoordinator.asyncCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager

class CommandLoader(private val plugin: ConnectFour) {

    fun register() {
        val commandManager: BukkitCommandManager<CommandSender> = createCommandManager()
        val annotationParser: AnnotationParser<CommandSender> = createAnnotationParser(commandManager)
        annotationParser.parse(RootCommand(plugin))
        /*annotationParser.parse(KitCommand(plugin))
        annotationParser.parse(CodesCommand(plugin))
        annotationParser.parse(PackageCommands(plugin))*/
    }

    private fun createCommandManager(): BukkitCommandManager<CommandSender> {
        val executionCoordinatorFunction: ExecutionCoordinator<CommandSender> = asyncCoordinator<CommandSender>()
        val mapperFunction: SenderMapper<CommandSender, CommandSender> = SenderMapper.identity<CommandSender>()
        val commandManager = LegacyPaperCommandManager<CommandSender>(
            plugin,
            executionCoordinatorFunction,
            mapperFunction
        )
        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier()
            commandManager.brigadierManager().setNativeNumberSuggestions(false)
        }
        if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            commandManager.registerAsynchronousCompletions()
        }

        return commandManager
    }

    private fun createAnnotationParser(commandManager: BukkitCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        return AnnotationParser(
            commandManager,
            CommandSender::class.java
        )
    }

}