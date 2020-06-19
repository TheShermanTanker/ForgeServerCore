package mod.server.forgeservermod;

import net.minecraft.village.VillageSiege;
import net.minecraft.world.server.ServerWorld;

public class SiegeManager extends VillageSiege {
	
	@Override
	public int tick(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs) {
		return 1;
	}
	
}
