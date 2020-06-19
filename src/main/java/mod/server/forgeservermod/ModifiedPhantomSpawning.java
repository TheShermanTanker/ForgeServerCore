package mod.server.forgeservermod;

import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.PhantomSpawner;

public class ModifiedPhantomSpawning extends PhantomSpawner {
	
	@Override
	public int tick(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs) {
		return 1;
	}

}
