package mod.server.forgeservermod;

import net.minecraft.enchantment.QuickChargeEnchantment;
import net.minecraft.inventory.EquipmentSlotType;

public class CustomEnchantmentQuickCharge extends QuickChargeEnchantment {

	public CustomEnchantmentQuickCharge(Rarity rarity, EquipmentSlotType slotType) {
		super(rarity, slotType);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int getMaxLevel() {
		return 5;
	}
	
}
