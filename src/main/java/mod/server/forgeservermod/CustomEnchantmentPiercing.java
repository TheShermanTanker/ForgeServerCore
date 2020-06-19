package mod.server.forgeservermod;

import net.minecraft.enchantment.PiercingEnchantment;
import net.minecraft.inventory.EquipmentSlotType;

public class CustomEnchantmentPiercing extends PiercingEnchantment {

	public CustomEnchantmentPiercing(Rarity rarity, EquipmentSlotType slotType) {
		super(rarity, slotType);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int getMaxLevel() {
		return 7;
	}

}
