package mod.server.forgeservermod;

import net.minecraft.block.AirBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.PatrollerEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;

import sun.misc.Unsafe;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("forgeservermod")
public class ForgeServerCore {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    ForgePlayerList list = null;
    MinecraftServer server;
    public static ItemForgeCrossbow crossbow;
    public static CustomEnchantmentQuickCharge quickCharge;
    public static CustomEnchantmentPiercing piercing;
    public static ItemForgeBow bow;
    private static Unsafe unsafe;
    SiegeManager siegeManager = new SiegeManager();
    ModifiedPhantomSpawning phantomManager = new ModifiedPhantomSpawning();
    public static ForgeServerCore core;
    public static EntityType<?> villager;
    public static EntityType<?> zombieVillager;
    
    //public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = new DeferredRegister<>(ForgeRegistries.ENTITIES, "minecraft");
    //public static final RegistryObject<EntityType<EntityVillager>> VILLAGER = ENTITY_TYPES.register("villager", () -> EntityType.Builder.<EntityVillager>create(EntityVillager::new, EntityClassification.MISC).size(0.6F, 1.95F).build("villager")/*.setRegistryName("minecraft", "villager")*/);
    //public static final RegistryObject<EntityType<EntityZombieVillager>> ZOMBIE_VILLAGER = ENTITY_TYPES.register("zombie_villager", () -> EntityType.Builder.<EntityZombieVillager>create(EntityZombieVillager::new, EntityClassification.MONSTER).size(0.6F, 1.95F).immuneToFire().build("zombie_villager")/*.setRegistryName("minecraft", "zombie_villager")*/);
    
    //private static Field cooldownField = null;
    
    private static final VarHandle MODIFIERS;

    static {
        try {
            Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void makeNonFinal(Field field) {
        int mods = field.getModifiers();
        if (Modifier.isFinal(mods)) {
            MODIFIERS.set(field, mods & ~Modifier.FINAL);
        }
    }
    
    /*
    static {
    	try {
			ForgeServerCore.setFinalStatic(ObfuscationReflectionHelper.findField(Items.class, "field_222114_py"), Registry.register(Registry.ITEM, new ResourceLocation("crossbow"), new ItemForgeCrossbow((new Item.Properties()).maxStackSize(1).group(ItemGroup.COMBAT).maxDamage(326))));
			ForgeServerCore.setFinalStatic(ObfuscationReflectionHelper.findField(Enchantments.class, "field_222193_H"), Registry.register(Registry.ENCHANTMENT, "quick_charge", new CustomEnchantmentQuickCharge(Enchantment.Rarity.UNCOMMON, EquipmentSlotType.MAINHAND)));
			ForgeServerCore.setFinalStatic(ObfuscationReflectionHelper.findField(Enchantments.class, "field_222194_I"), Registry.register(Registry.ENCHANTMENT, "piercing", new CustomEnchantmentPiercing(Enchantment.Rarity.COMMON, EquipmentSlotType.MAINHAND)));
		} catch (Exception exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
    }*/
    
    public static double getHighestYAtXZ(ServerWorld world, double x, double z) {
    	for(double y = 255; y > 0; y--) {
            if(world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof AirBlock) {
    			return y;
    		}
    	}
    	return 0;
    }
    
    public static double getLowestAirAtXZ(ServerWorld world, double x, double z) {
    	return getHighestYAtXZ(world, x, z) + 1;
    }
    
    //sun.misc.Unsafe version of setFinalStatic
    public static void usetFinalStatic(Field field, Object object) throws IllegalAccessException {
    	LOGGER.info("Original field value: " + field.get(null));
        //we need a field to update
        //this is a 'base'. Usually a Class object will be returned here.
        final Object base = unsafe.staticFieldBase(field);
        //this is an 'offset'
        final long offset = unsafe.staticFieldOffset(field);
        //actual update
        unsafe.putObject(base, offset, object);
        //ensure the value was updated
        LOGGER.info( "Updated static final value: " + field.get(null));
    }
    
    public static void nsetFinalStatic(Field field, Object edit) {
    	try {
			LOGGER.info("Original field value: " + field.get(null));
		} catch (IllegalArgumentException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} catch (IllegalAccessException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
    	makeNonFinal(field);
    	field.setAccessible(true);
    	try {
			field.set(null, edit);
		} catch (IllegalArgumentException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} catch (IllegalAccessException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
    	try {
			LOGGER.info("New field value: " + field.get(null));
		} catch (IllegalArgumentException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} catch (IllegalAccessException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
    }
    
    //Never works due to the fact that the modifiers field in Field.java is no longer accessible, not even by normal reflection
    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
     }
    
    public ForgeServerCore() {
    	//ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        /*
        cooldownField = ObfuscationReflectionHelper.findField(LivingEntity.class, "field_184617_aD");
        if(cooldownField == null) {
        	LOGGER.fatal("Cooldown Field is null!");
        	System.exit(-1);
        } 
        cooldownField.setAccessible(true);*/
        
        /*
        try {
        	cooldownField = LivingEntity.class.getDeclaredFields()[26];
			cooldownField.setAccessible(true);
		} catch (SecurityException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} catch (NullPointerException exception) {
			exception.printStackTrace();
		}*/
        
        core = this;
        
        Field field = null;
		try {
			field = Unsafe.class.getDeclaredField("theUnsafe");
		} catch (NoSuchFieldException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} catch (SecurityException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} //Internal reference
		if(field == null) {
			LOGGER.fatal("Field is null");
		}
        field.setAccessible(true);
        try {
			unsafe = (Unsafe) field.get(null);
		} catch (IllegalArgumentException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} catch (IllegalAccessException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
        
        if(unsafe == null) {
			LOGGER.fatal("Unsafe is null");
		}
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
    }
    
    @SubscribeEvent
    public void onUpdate(TickEvent.WorldTickEvent event) {
    	
    }
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	/*
    	if(event.getEntityLiving() instanceof ServerPlayerEntity) {
    		ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
    		
    		try {
				
				cooldownField.setInt(player, 999999999);/*
				LOGGER.info("Disabled Cooldown for Player " + player.getName().getFormattedText() + " to: " + cooldownField.getInt(player));
				LOGGER.info(player.getName().getFormattedText() + "'s Attack Speed Attrib is " + player.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).getValue());
				LOGGER.info(player.getName().getFormattedText() + "'s getCooldownPeriod: " + player.getCooldownPeriod());
				LOGGER.info(player.getName().getFormattedText() + "'s getCooledAttack: " + player.getCooledAttackStrength(0.5F));
				
			} catch (IllegalArgumentException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			} catch (IllegalAccessException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
    	}*/
    	/*
    	if(event.getEntity() instanceof PlayerEntity) {
    		PlayerEntity player = (PlayerEntity) event.getEntity();
    		/*if(player.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue() < 2000.0D) {
    			player.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(2000.0D);
    		}*/
    		
    		/*try {
				if(cooldownField == null) {
					LOGGER.warn("Cooldown Field is empty!");
					return;
				}
				if(cooldownField.getInt(player) < 20) {
					cooldownField.set(player, 20);
				}
			} catch (IllegalArgumentException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			} catch (IllegalAccessException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
    	}*/
    }
    
    @SubscribeEvent
    public void onPlace(BlockEvent.EntityPlaceEvent event) {
    	if(event.getPlacedBlock().getBlock() instanceof BedBlock) {
    		
    	}
    }
    
    @SubscribeEvent
    public void onEnter(EntityJoinWorldEvent event) {
    	if(event.getEntity() instanceof ZombieVillagerEntity) {
			((ZombieVillagerEntity) event.getEntity()).enablePersistence();
			LOGGER.info("Zombie Villager spawned. Unsure if natural spawn or generated structure");
		}
		if(event.getEntity() instanceof PatrollerEntity) {
			((PatrollerEntity) event.getEntity()).enablePersistence();
		}
    }
    
    @SubscribeEvent
    public void onSpawn(LivingSpawnEvent.CheckSpawn event) {
    	if(event.getEntityLiving() instanceof ZombieVillagerEntity) {
    		LOGGER.info("Zombie Villager spawned!!");
    	}
    	if(event.getEntityLiving() instanceof MonsterEntity) {
    		if(event.getEntityLiving().getEntityWorld() instanceof ServerWorld) {
    			ServerWorld world = (ServerWorld) event.getEntityLiving().getEntityWorld();
    			if(world.isCloseToVillage(event.getEntityLiving().getPosition(), 2)) {
    				LOGGER.info("Denied hostile spawn within Village");
    				event.setResult(Result.DENY);
    			}
    		}
    	}
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @SuppressWarnings("resource")
	private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().map(m->m.getMessageSupplier().get()).collect(Collectors.toList()));
    }
    
    @SubscribeEvent
    public void onLoad(FMLServerAboutToStartEvent event) {
    	
    	MinecraftServer server = event.getServer();
        if(server.getPlayerList() instanceof DedicatedPlayerList) {
        	DedicatedPlayerList oldList = (DedicatedPlayerList) server.getPlayerList();
        	list = new ForgePlayerList(oldList.getServer());
        	server.setPlayerList(list);
        	LOGGER.info("Injected Custom Player List!");
        }
        this.server = server;
    }
    
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
    	CommandForgeSiegeAttackChances.register(event.getCommandDispatcher());
        LOGGER.info("HELLO from server starting");
        /*
        try {
			ForgeServerCore.nsetFinalStatic(ObfuscationReflectionHelper.findField(Items.class, "field_222114_py"), crossbow);
		} catch (Exception exception) {
			// TODO Auto-generated catch block
			LOGGER.error("Error encountered while overwriting base Items.class fields");
			exception.printStackTrace();
		}
    	
    	try {
			ForgeServerCore.nsetFinalStatic(ObfuscationReflectionHelper.findField(Enchantments.class, "field_222193_H"), quickCharge);
			ForgeServerCore.nsetFinalStatic(ObfuscationReflectionHelper.findField(Enchantments.class, "field_222194_I"), piercing);
		} catch (Exception exception) {
			// TODO Auto-generated catch block
			LOGGER.error("Error encountered while overwriting base Enchantments.class fields");
			exception.printStackTrace();
		}
    	
    	ForgeServerCore.nsetFinalStatic(ObfuscationReflectionHelper.findField(EntityType.class, "field_200756_av"), villager);*/
    }
    
    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
    	
    }
    
    @SubscribeEvent
    public void onStarted(FMLServerStartedEvent event) {
    	for (BiomeDictionary.Type type : BiomeDictionary.Type.getAll()) {
		    Set<Biome> biomes = BiomeDictionary.getBiomes(type);
		    for (Biome biome : biomes) {
		    	for(EntityClassification entityType : EntityClassification.values()) {
		    		for(Biome.SpawnListEntry entry : biome.getSpawns(entityType)) {
		    			if(entry.entityType == EntityType.ZOMBIE_VILLAGER) {
		    				biome.getSpawns(entityType).remove(entry);
		    				LOGGER.info("Spawn List Entry Removed: " + entry);
		    				break;
		    			}
		    		}
		    	}
		    }
		} 
    	
    	if(event.getServer().getPlayerList() == list) {
    		LOGGER.info("Injected Player List working correctly");
    	}
    	
    	if(event.getServer().getPlayerList() instanceof ForgePlayerList) {
    		LOGGER.info("Injected Player List working correctly");
    	}
    	
    	LOGGER.info("Custom Piercing Level: " + Enchantments.PIERCING.getMaxLevel());
    	LOGGER.info("Custom Quick Charge Level: " + Enchantments.QUICK_CHARGE.getMaxLevel());
    	LOGGER.info("Whether Crossbow is Custom: " + (Items.CROSSBOW instanceof ItemForgeCrossbow));
    	//LOGGER.info("Whether Villager is Custom: " + (EntityType.VILLAGER == villager));
    	//LOGGER.info("Whether Zombie Villager is Custom: " + (EntityType.ZOMBIE_VILLAGER == zombieVillager));
    	
    	for(ServerWorld world : server.getWorlds()) {
    		if(world.getDimension().getType() == DimensionType.OVERWORLD) {
    			OverworldChunkGenerator generator = (OverworldChunkGenerator) world.getChunkProvider().getChunkGenerator();
    			Field siegeField = ObfuscationReflectionHelper.findField(OverworldChunkGenerator.class, "field_225495_n");
    			siegeField.setAccessible(true);
    			try {
					siegeField.set(generator, siegeManager);
				} catch (IllegalArgumentException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				} catch (IllegalAccessException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}
    			Field phantomField = ObfuscationReflectionHelper.findField(OverworldChunkGenerator.class, "field_203230_r");
    			phantomField.setAccessible(true);
    			try {
					phantomField.set(generator, phantomManager);
				} catch (IllegalArgumentException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				} catch (IllegalAccessException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}
    		}
    	}
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
            
        }
        
        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> event) {
        	crossbow = new ItemForgeCrossbow((new Item.Properties()).maxStackSize(1).group(ItemGroup.COMBAT).maxDamage(397));
        	crossbow.setRegistryName("minecraft", "crossbow");
        	bow = new ItemForgeBow((new Item.Properties()).maxDamage(384).group(ItemGroup.COMBAT));
        	bow.setRegistryName("minecraft", "bow");
        	event.getRegistry().registerAll(crossbow, bow);
        	
        }
        
        @SubscribeEvent
        public static void onEntityRegistry(final RegistryEvent.Register<EntityType<?>> event) {
        	//villager = EntityType.Builder.<EntityVillager>create(EntityVillager::new, EntityClassification.MISC).size(0.6F, 1.95F).build("villager").setRegistryName("minecraft", "villager");
        	//zombieVillager = EntityType.Builder.<EntityZombieVillager>create(EntityZombieVillager::new, EntityClassification.MONSTER).size(0.6F, 1.95F).immuneToFire().build("zombie_villager").setRegistryName("minecraft", "zombie_villager");
        	event.getRegistry().registerAll(villager = EntityType.Builder.<EntityVillager>create(EntityVillager::new, EntityClassification.MISC).size(0.6F, 1.95F).build("villager").setRegistryName("minecraft", "villager"), zombieVillager = EntityType.Builder.<EntityZombieVillager>create(EntityZombieVillager::new, EntityClassification.MONSTER).size(0.6F, 1.95F).immuneToFire().build("zombie_villager").setRegistryName("minecraft", "zombie_villager"));
        }
        
        @SubscribeEvent
        public static void onEnchantRegistry(final RegistryEvent.Register<Enchantment> event) {
        	quickCharge = new CustomEnchantmentQuickCharge(Enchantment.Rarity.UNCOMMON, EquipmentSlotType.MAINHAND);
        	piercing = new CustomEnchantmentPiercing(Enchantment.Rarity.COMMON, EquipmentSlotType.MAINHAND);
        	piercing.setRegistryName("minecraft", "piercing");
        	quickCharge.setRegistryName("minecraft", "quick_charge");
        	event.getRegistry().registerAll(piercing, quickCharge);
        	
        }
    }
    
}
