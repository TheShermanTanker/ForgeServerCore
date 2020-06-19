package mod.server.forgeservermod;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class EntityHuman extends PlayerEntity {

	public EntityHuman(World world, String name) {
		super(world, new GameProfile(UUID.randomUUID(), name));
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isSpectator() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCreative() {
		// TODO Auto-generated method stub
		return false;
	}

}
