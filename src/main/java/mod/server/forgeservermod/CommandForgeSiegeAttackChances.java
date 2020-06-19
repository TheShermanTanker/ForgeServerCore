package mod.server.forgeservermod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CommandForgeSiegeAttackChances {
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("siege_chance").then(Commands.argument("percentage", IntegerArgumentType.integer())
				        .executes(context -> { 
				            int chance = IntegerArgumentType.getInteger(context, "percentage");
				            //ForgeServerCore.core.siegeManager.siegeChance = chance;
				            ForgeServerCore.LOGGER.info("Set percentage chance for Siege to occur to " + chance + "%");
				            return 1;
				        })));
	}
	
}
