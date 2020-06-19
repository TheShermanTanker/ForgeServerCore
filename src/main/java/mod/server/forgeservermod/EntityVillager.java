package mod.server.forgeservermod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.villager.IVillagerType;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class EntityVillager extends VillagerEntity {
	
	public EntityVillager(EntityType<? extends VillagerEntity> type, World worldIn) {
	      super(type, worldIn);
	}
	
	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(1.0D);
	}
	
	@Override
	public void livingTick() {
		if(this.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
			if (this.getHealth() < this.getMaxHealth() && this.ticksExisted % 20 == 0) {
	            this.heal(2.0F);
	         }
		}
		super.livingTick();
	}
	
	public EntityVillager(EntityType<? extends VillagerEntity> type, World worldIn, IVillagerType villagerType) {
		super(type, worldIn, villagerType);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onStruckByLightning(LightningBoltEntity lightning) {
		return;
	}
	
	@Override
	public boolean preventDespawn() {
		return true;
	}
	
}
