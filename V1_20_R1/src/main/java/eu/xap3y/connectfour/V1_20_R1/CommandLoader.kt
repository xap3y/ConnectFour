package eu.xap3y.connectfour.V1_20_R1

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator.asyncCoordinator
import org.incendo.cloud.paper.PaperCommandManager

class CommandLoader(private val plugin: Plugin) {

    fun getParser(): AnnotationParser<CommandSourceStack> {
        val commandManager: PaperCommandManager<CommandSourceStack> = createPaperCommandManager()
        val annotationParser: AnnotationParser<CommandSourceStack> = createAnnotationParser(commandManager)
        return annotationParser
    }


    private fun createPaperCommandManager(): PaperCommandManager<CommandSourceStack> {
        val executionCoordinatorFunction =  asyncCoordinator<CommandSourceStack>()
        //val mapperFunction: SenderMapper<CommandSourceStack, CommandSender> = SenderMapper.identity<CommandSender, C>()
        val commandManager = PaperCommandManager.builder()
            .executionCoordinator(executionCoordinatorFunction)
            .buildOnEnable(plugin)

        return commandManager
    }

    private fun createAnnotationParser(commandManager: PaperCommandManager<CommandSourceStack>): AnnotationParser<CommandSourceStack> {
        val test =  AnnotationParser<CommandSourceStack>(
            commandManager,
            CommandSourceStack::class.java,
        )
        return test
    }

}