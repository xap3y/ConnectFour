package eu.xap3y.connectfour.manager;

import eu.xap3y.connectfour.ConnectFour;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

public class CommandManager {

    private static AnnotationParser<CommandSender> parser;

    public CommandManager() {
        parser = createParser();
    }

    public void parse(@NotNull Object... instances) {
        parser.parse(instances);
    }

    private BukkitCommandManager<CommandSender> createCommandManager() {
        ExecutionCoordinator<CommandSender> coordinator = ExecutionCoordinator.asyncCoordinator();
        LegacyPaperCommandManager<CommandSender> manager = new LegacyPaperCommandManager<CommandSender>(
                ConnectFour.getInstance(),
                coordinator,
                SenderMapper.identity()
        );
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
            manager.brigadierManager().setNativeNumberSuggestions(false);
        }
        return manager;
    }

    private AnnotationParser<CommandSender> createParser() {
        return new AnnotationParser<>(createCommandManager(), CommandSender.class);
    }
}

