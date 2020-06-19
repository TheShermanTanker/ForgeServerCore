package mod.server.forgeservermod;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.server.ServerWorld;

public class CustomServerPlayerEntity extends ServerPlayerEntity {

	public CustomServerPlayerEntity(MinecraftServer server, ServerWorld worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn) {
		super(server, worldIn, profile, interactionManagerIn);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public float getCooledAttackStrength(float adjustTicks) {
		return 1.0F;
	}
	
	@Override
	public float getCooldownPeriod() {
		server.logInfo("Requested Cooldown Period Modified");
		return 0.00998003992F;
	}
	
	@Override
	public void resetCooldown() {
		return;
	}

}
