package mod.server.forgeservermod;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class ItemForgeBow extends BowItem {

	public ItemForgeBow(Properties builder) {
		super(builder);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
	      if (entityLiving instanceof PlayerEntity) {
	         PlayerEntity playerentity = (PlayerEntity)entityLiving;
	         boolean flag = playerentity.abilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
	         ItemStack itemstack = playerentity.findAmmo(stack);
	         if (!itemstack.isEmpty() || flag) {
	            if (itemstack.isEmpty()) {
	               itemstack = new ItemStack(Items.ARROW);
	            }

	            int i = this.getUseDuration(stack) - timeLeft;
	            float f = getArrowVelocity(i);
	            if (!((double)f < 0.1D)) {
	               boolean flag1 = flag && itemstack.getItem() == Items.ARROW;
	               if (!worldIn.isRemote) {
	                  ArrowItem arrowitem = (ArrowItem)(itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
	                  AbstractArrowEntity abstractarrowentity = arrowitem.createArrow(worldIn, itemstack, playerentity);
	                  if(abstractarrowentity instanceof ArrowEntity) {
	        	    	  ArrowEntity newArrow = new EntityTippedArrow(worldIn, playerentity);
	        	    	  newArrow.setPotionEffect(itemstack);
	        	    	  abstractarrowentity = newArrow;
	        	      } else if(abstractarrowentity instanceof SpectralArrowEntity) {
	        	    	  abstractarrowentity = new EntitySpectralArrow(worldIn, playerentity);
	        	      }
	                  abstractarrowentity.shoot(playerentity, playerentity.rotationPitch, playerentity.rotationYaw, 0.0F, f * 3.0F, 1.0F);
	                  if (f == 1.0F) {
	                     abstractarrowentity.setIsCritical(true);
	                  }

	                  int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
	                  if (j > 0) {
	                     abstractarrowentity.setDamage(abstractarrowentity.getDamage() + (double)j * 0.5D + 0.5D);
	                  }

	                  int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
	                  if (k > 0) {
	                     abstractarrowentity.setKnockbackStrength(k);
	                  }

	                  if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
	                     abstractarrowentity.setFire(100);
	                  }

	                  stack.damageItem(1, playerentity, (p_220009_1_) -> {
	                     p_220009_1_.sendBreakAnimation(playerentity.getActiveHand());
	                  });
	                  if (flag1 || playerentity.abilities.isCreativeMode && (itemstack.getItem() == Items.SPECTRAL_ARROW || itemstack.getItem() == Items.TIPPED_ARROW)) {
	                     abstractarrowentity.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
	                  }

	                  worldIn.addEntity(abstractarrowentity);
	               }

	               worldIn.playSound((PlayerEntity)null, playerentity.getPosX(), playerentity.getPosY(), playerentity.getPosZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
	               if (!flag1 && !playerentity.abilities.isCreativeMode) {
	                  itemstack.shrink(1);
	                  if (itemstack.isEmpty()) {
	                     playerentity.inventory.deleteStack(itemstack);
	                  }
	               }

	               playerentity.addStat(Stats.ITEM_USED.get(this));
	            }
	         }
	      }
	   }

}
