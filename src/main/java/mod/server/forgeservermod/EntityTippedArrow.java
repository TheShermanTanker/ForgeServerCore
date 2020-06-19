package mod.server.forgeservermod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.block.BedrockBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class EntityTippedArrow extends ArrowEntity {

	Field inBlockStateField;
	Field ticksInGroundField;
	Field ticksInAirField;
	Method spawnPotionParticles;
	
	public EntityTippedArrow(World world, LivingEntity living) {
		super(world, living);
		if(living instanceof IllusionerEntity) {
			this.setPierceLevel((byte) 2);
		}
		// TODO Auto-generated constructor stub
		ticksInGroundField = ObfuscationReflectionHelper.findField(AbstractArrowEntity.class, "field_70252_j");
        ticksInAirField = ObfuscationReflectionHelper.findField(AbstractArrowEntity.class, "field_70257_an");
        inBlockStateField = ObfuscationReflectionHelper.findField(AbstractArrowEntity.class, "field_195056_av");
        ticksInGroundField.setAccessible(true);
        ticksInAirField.setAccessible(true);
        inBlockStateField.setAccessible(true);
        spawnPotionParticles = ObfuscationReflectionHelper.findMethod(ArrowEntity.class, "func_184556_b", int.class);
        spawnPotionParticles.setAccessible(true);
	}
	
	@Override
	public boolean getNoClip() {
		return ((this.getShotFromCrossbow() || this.getShooter() instanceof IllusionerEntity) && this.getPierceLevel() > 0);
	}
	
	@Override
	protected void onHit(RayTraceResult raytraceResultIn) {
	      RayTraceResult.Type raytraceresult$type = raytraceResultIn.getType();
	      if (raytraceresult$type == RayTraceResult.Type.ENTITY) {
	         this.onEntityHit((EntityRayTraceResult)raytraceResultIn);
	      } else if (raytraceresult$type == RayTraceResult.Type.BLOCK) {
	         if(this.getNoClip()) {
	        	 this.setPierceLevel((byte) (this.getPierceLevel() - 1));
	        	 BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult) raytraceResultIn;
	        	 if(this.world.getBlockState(blockraytraceresult.getPos()).getBlock() instanceof BedrockBlock || this.world.getBlockState(blockraytraceresult.getPos()).getBlock() instanceof EndPortalFrameBlock || this.world.getBlockState(blockraytraceresult.getPos()).getBlock() instanceof CommandBlockBlock) {
	        		 super.onHit(raytraceResultIn);
	        	 } else {
	        		 world.destroyBlock(blockraytraceresult.getPos(), true);
	        		 world.setBlockState(blockraytraceresult.getPos(), Blocks.AIR.getDefaultState());
	        	 }
	        	 
	         } else {
	        	 super.onHit(raytraceResultIn);
	         }
	      }

	   }
	
	@Override
	public void tick() {
		if (!this.world.isRemote) {
	        this.setFlag(6, this.isGlowing());
	    }

	    this.baseTick();
	      
	    boolean flag = this.getNoClip();
	      Vec3d vec3d = this.getMotion();
	      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
	         float f = MathHelper.sqrt(horizontalMag(vec3d));
	         this.rotationYaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * (double)(180F / (float)Math.PI));
	         this.rotationPitch = (float)(MathHelper.atan2(vec3d.y, (double)f) * (double)(180F / (float)Math.PI));
	         this.prevRotationYaw = this.rotationYaw;
	         this.prevRotationPitch = this.rotationPitch;
	      }

	      BlockPos blockpos = new BlockPos(this);
	      BlockState blockstate = this.world.getBlockState(blockpos);
	      if (!blockstate.isAir() && !flag) {
	         VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
	         if (!voxelshape.isEmpty()) {
	            Vec3d vec3d1 = this.getPositionVec();

	            for(AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
	               if (axisalignedbb.offset(blockpos).contains(vec3d1)) {
	                  this.inGround = true;
	                  break;
	               }
	            }
	         }
	      }

	      if (this.arrowShake > 0) {
	         --this.arrowShake;
	      }

	      if (this.isWet()) {
	         this.extinguish();
	      }

	      if (this.inGround && !flag) {
	         try {
				if (inBlockStateField.get(this) != blockstate && this.world.hasNoCollisions(this.getBoundingBox().grow(0.06D))) {
				    this.inGround = false;
				    this.setMotion(vec3d.mul((double)(this.rand.nextFloat() * 0.2F), (double)(this.rand.nextFloat() * 0.2F), (double)(this.rand.nextFloat() * 0.2F)));
				    
				    try {
						ticksInAirField.setInt(this, 0);
						ticksInGroundField.setInt(this, 0);
					} catch (IllegalArgumentException exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					} catch (IllegalAccessException exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					}
				    //this.ticksInGround = 0;
				    //this.ticksInAir = 0;
				 } else if (!this.world.isRemote) {
				    this.func_225516_i_();
				 }
			} catch (IllegalArgumentException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			} catch (IllegalAccessException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}

	         ++this.timeInGround;
	      } else {
	         this.timeInGround = 0;
	         try {
	        	 ticksInAirField.setInt(this, ticksInAirField.getInt(this) + 1);
	         } catch (IllegalArgumentException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
			 } catch (IllegalAccessException exception) {
					// TODO Auto-generated catch block
			 }		
	         
	         //++this.ticksInAir;
	         Vec3d vec3d2 = this.getPositionVec();
	         Vec3d vec3d3 = vec3d2.add(vec3d);
	         RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vec3d2, vec3d3, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
	         if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
	            vec3d3 = raytraceresult.getHitVec();
	         }

	         while(!this.removed) {
	            EntityRayTraceResult entityraytraceresult = this.rayTraceEntities(vec3d2, vec3d3);
	            if (entityraytraceresult != null) {
	               raytraceresult = entityraytraceresult;
	            }

	            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
	               Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
	               Entity entity1 = this.getShooter();
	               if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
	                  raytraceresult = null;
	                  entityraytraceresult = null;
	               }
	            }

	            if (raytraceresult != null) {
	               this.onHit(raytraceresult);
	               this.isAirBorne = true;
	            }

	            if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
	               break;
	            }

	            raytraceresult = null;
	         }

	         vec3d = this.getMotion();
	         double d3 = vec3d.x;
	         double d4 = vec3d.y;
	         double d0 = vec3d.z;
	         if (this.getIsCritical()) {
	            for(int i = 0; i < 4; ++i) {
	               this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + d3 * (double)i / 4.0D, this.getPosY() + d4 * (double)i / 4.0D, this.getPosZ() + d0 * (double)i / 4.0D, -d3, -d4 + 0.2D, -d0);
	            }
	         }
	         
	         if (this.isBurning()) {
	        	 this.world.addParticle(ParticleTypes.FLAME, this.getPosX(), this.getPosY(), this.getPosZ(), 0.0D, 0.0D, 0.0D);
		     }

	         double d5 = this.getPosX() + d3;
	         double d1 = this.getPosY() + d4;
	         double d2 = this.getPosZ() + d0;
	         float f1 = MathHelper.sqrt(horizontalMag(vec3d));
	         if (flag) {
	            this.rotationYaw = (float)(MathHelper.atan2(-d3, -d0) * (double)(180F / (float)Math.PI));
	         } else {
	            this.rotationYaw = (float)(MathHelper.atan2(d3, d0) * (double)(180F / (float)Math.PI));
	         }

	         for(this.rotationPitch = (float)(MathHelper.atan2(d4, (double)f1) * (double)(180F / (float)Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
	            ;
	         }

	         while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
	            this.prevRotationPitch += 360.0F;
	         }

	         while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
	            this.prevRotationYaw -= 360.0F;
	         }

	         while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
	            this.prevRotationYaw += 360.0F;
	         }

	         this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
	         this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
	         float f2 = 1.0F;
	         float f3 = 0.07F;
	         if(this.getShooter() instanceof PlayerEntity && !this.getShotFromCrossbow()) {
	        	 f3 = 0.02F;
	         }
	         if (this.isInWater()) {
	            for(int j = 0; j < 4; ++j) {
	               float f4 = 0.25F;
	               this.world.addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
	            }

	            f2 = this.getWaterDrag();
	         }

	         this.setMotion(vec3d.scale((double)f2));
	         if (!this.hasNoGravity() && !flag) {
	            Vec3d vec3d4 = this.getMotion();
	            this.setMotion(vec3d4.x, vec3d4.y - (double) f3, vec3d4.z);
	         }

	         this.setPosition(d5, d1, d2);
	         this.doBlockCollisions();
	      }
	      
	      if (this.world.isRemote) {
	    	 
	          if (this.inGround) {
	             if (this.timeInGround % 5 == 0) {
	            	 try {
						spawnPotionParticles.invoke(this, 1);
					} catch (IllegalAccessException exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					} catch (IllegalArgumentException exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					} catch (InvocationTargetException exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					}
	                //this.spawnPotionParticles(1);
	             }
	          } else {
	        	  try {
						spawnPotionParticles.invoke(this, 2);
					} catch (IllegalAccessException exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					} catch (IllegalArgumentException exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					} catch (InvocationTargetException exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					}
	             //this.spawnPotionParticles(2);
	          }
	       } 
	}

}
