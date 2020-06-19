package mod.server.forgeservermod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.world.World;

public class EntityZombieVillager extends ZombieVillagerEntity {

	public EntityZombieVillager(EntityType<? extends ZombieVillagerEntity> type, World world) {
		super(type, world);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean preventDespawn() {
		return true;
	}
	
	@Override
	public boolean canDespawn(double distanceToClosestPlayer) {
		return false;
	}

}
