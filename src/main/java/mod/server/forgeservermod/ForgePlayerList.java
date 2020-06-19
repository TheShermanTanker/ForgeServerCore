package mod.server.forgeservermod;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.network.play.server.SSpawnPositionPacket;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.OpEntry;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ForgePlayerList extends DedicatedPlayerList {

	public ForgePlayerList(DedicatedServer server) {
		super(server);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void addOp(GameProfile profile) {
		setRank(profile, this.getServer().getOpPermissionLevel(), this.getOppedPlayers().bypassesPlayerLimit(profile));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ServerPlayerEntity createPlayerForUser(GameProfile profile) {
	    UUID uuid = PlayerEntity.getUUID(profile);
	    List<ServerPlayerEntity> list = Lists.newArrayList();
	      
	    Field uuidToPlayerMapField = ObfuscationReflectionHelper.findField(PlayerList.class, "field_177454_f");
	    uuidToPlayerMapField.setAccessible(true);
	    Map<UUID, ServerPlayerEntity> uuidToPlayerMap = null;
		try {
			uuidToPlayerMap = (Map<UUID, ServerPlayerEntity>) uuidToPlayerMapField.get(this);
		} catch (IllegalArgumentException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} catch (IllegalAccessException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	      
	      for(int i = 0; i < this.getPlayers().size(); ++i) {
	         ServerPlayerEntity serverplayerentity = this.getPlayers().get(i);
	         if (serverplayerentity.getUniqueID().equals(uuid)) {
	            list.add(serverplayerentity);
	         }
	      }
	      
	      ServerPlayerEntity serverplayerentity2 = uuidToPlayerMap.get(profile.getId());
	      if (serverplayerentity2 != null && !list.contains(serverplayerentity2)) {
	         list.add(serverplayerentity2);
	      }

	      for(ServerPlayerEntity serverplayerentity1 : list) {
	         serverplayerentity1.connection.disconnect(new TranslationTextComponent("multiplayer.disconnect.duplicate_login"));
	      }

	      PlayerInteractionManager playerinteractionmanager;
	      if (this.getServer().isDemo()) {
	         playerinteractionmanager = new PlayerInteractionManager(this.getServer().getWorld(DimensionType.OVERWORLD));
	      } else {
	         playerinteractionmanager = new PlayerInteractionManager(this.getServer().getWorld(DimensionType.OVERWORLD));
	      }

	      return new CustomServerPlayerEntity(this.getServer(), this.getServer().getWorld(DimensionType.OVERWORLD), profile, playerinteractionmanager);
	   }
	
	   @SuppressWarnings({"unchecked" })
	@Override
	   public ServerPlayerEntity recreatePlayerEntity(ServerPlayerEntity playerIn, DimensionType dimension, boolean conqueredEnd) {
		   
		   Field uuidToPlayerMapField = ObfuscationReflectionHelper.findField(PlayerList.class, "field_177454_f");
		    uuidToPlayerMapField.setAccessible(true);
		    Map<UUID, ServerPlayerEntity> uuidToPlayerMap = null;
			try {
				uuidToPlayerMap = (Map<UUID, ServerPlayerEntity>) uuidToPlayerMapField.get(this);
			} catch (IllegalArgumentException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			} catch (IllegalAccessException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
			
			this.removePlayer(playerIn);
		      playerIn.getServerWorld().removePlayer(playerIn);
		      BlockPos blockpos = playerIn.getBedLocation();
		      boolean flag = playerIn.isSpawnForced();
		      playerIn.dimension = dimension;
		      PlayerInteractionManager playerinteractionmanager;
		      if (this.getServer().isDemo()) {
		         playerinteractionmanager = new PlayerInteractionManager(this.getServer().getWorld(playerIn.dimension));
		      } else {
		         playerinteractionmanager = new PlayerInteractionManager(this.getServer().getWorld(playerIn.dimension));
		      }

		      ServerPlayerEntity serverplayerentity = new CustomServerPlayerEntity(this.getServer(), this.getServer().getWorld(playerIn.dimension), playerIn.getGameProfile(), playerinteractionmanager);
		      
		      serverplayerentity.connection = playerIn.connection/*new ServerPlayNetHandler(playerIn.getServer(), playerIn.connection.getNetworkManager(), serverplayerentity)*/;
		      serverplayerentity.connection.player = serverplayerentity;
		      
		      serverplayerentity.copyFrom(playerIn, conqueredEnd);
		      serverplayerentity.setEntityId(playerIn.getEntityId());
		      serverplayerentity.setPrimaryHand(playerIn.getPrimaryHand());

		      for(String s : playerIn.getTags()) {
		         serverplayerentity.addTag(s);
		      }

		      ServerWorld serverworld = this.getServer().getWorld(playerIn.dimension);
		      serverplayerentity.setGameType(playerIn.interactionManager.getGameType());
		      if (blockpos != null) {
		         Optional<Vec3d> optional = PlayerEntity.checkBedValidRespawnPosition(this.getServer().getWorld(playerIn.dimension), blockpos, flag);
		         if (optional.isPresent()) {
		            Vec3d vec3d = optional.get();
		            serverplayerentity.setLocationAndAngles(vec3d.x, vec3d.y, vec3d.z, 0.0F, 0.0F);
		            serverplayerentity.setRespawnPosition(blockpos, flag, false);
		         } else {
		            serverplayerentity.connection.sendPacket(new SChangeGameStatePacket(0, 0.0F));
		         }
		      }

		      while(!serverworld.hasNoCollisions(serverplayerentity) && serverplayerentity.getPosY() < 256.0D) {
		         serverplayerentity.setPosition(serverplayerentity.getPosX(), serverplayerentity.getPosY() + 1.0D, serverplayerentity.getPosZ());
		      }

		      WorldInfo worldinfo = serverplayerentity.world.getWorldInfo();
		      serverplayerentity.connection.sendPacket(new SRespawnPacket(serverplayerentity.dimension, WorldInfo.byHashing(worldinfo.getSeed()), worldinfo.getGenerator(), serverplayerentity.interactionManager.getGameType()));
		      BlockPos blockpos1 = serverworld.getSpawnPoint();
		      serverplayerentity.connection.setPlayerLocation(serverplayerentity.getPosX(), serverplayerentity.getPosY(), serverplayerentity.getPosZ(), serverplayerentity.rotationYaw, serverplayerentity.rotationPitch);
		      serverplayerentity.connection.sendPacket(new SSpawnPositionPacket(blockpos1));
		      serverplayerentity.connection.sendPacket(new SServerDifficultyPacket(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
		      serverplayerentity.connection.sendPacket(new SSetExperiencePacket(serverplayerentity.experience, serverplayerentity.experienceTotal, serverplayerentity.experienceLevel));
		      this.sendWorldInfo(serverplayerentity, serverworld);
		      this.updatePermissionLevel(serverplayerentity);
		      serverworld.addRespawnedPlayer(serverplayerentity);
		      this.addPlayer(serverplayerentity);
		      uuidToPlayerMap.put(serverplayerentity.getUniqueID(), serverplayerentity);
		      serverplayerentity.addSelfToInternalCraftingInventory();
		      serverplayerentity.setHealth(serverplayerentity.getHealth());
		      return serverplayerentity;
	   }
	
	public void setRank(GameProfile profile, int level, boolean bypassesPlayerLimit) {
		if(level <= 0 || level > 4) {
			return;
		}
		this.getOppedPlayers().addEntry(new OpEntry(profile, level, bypassesPlayerLimit));
	}
	
}
