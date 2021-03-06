/**
 * 
 */
package jordan.sicherman.nms.v1_7_R4.mobs;

import jordan.sicherman.MyZ;
import jordan.sicherman.items.ItemTag;
import jordan.sicherman.items.ItemUtilities;
import jordan.sicherman.nms.v1_7_R4.mobs.pathfinders.CustomPathfinderGoalHurtByTarget;
import jordan.sicherman.nms.v1_7_R4.mobs.pathfinders.CustomPathfinderGoalLookAtPlayer;
import jordan.sicherman.nms.v1_7_R4.mobs.pathfinders.CustomPathfinderGoalMeleeAttack;
import jordan.sicherman.nms.v1_7_R4.mobs.pathfinders.CustomPathfinderGoalMoveToLocation;
import jordan.sicherman.nms.v1_7_R4.mobs.pathfinders.CustomPathfinderGoalNearestAttackableTarget;
import jordan.sicherman.nms.v1_7_R4.mobs.pathfinders.CustomPathfinderGoalRandomLookaround;
import jordan.sicherman.nms.v1_7_R4.mobs.pathfinders.CustomPathfinderGoalRandomStroll;
import jordan.sicherman.utilities.configuration.ConfigEntries;
import net.minecraft.server.v1_7_R4.EntityCreature;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.EntityZombie;
import net.minecraft.server.v1_7_R4.GenericAttributes;
import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.Items;
import net.minecraft.server.v1_7_R4.MathHelper;
import net.minecraft.server.v1_7_R4.MobEffectList;
import net.minecraft.server.v1_7_R4.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R4.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_7_R4.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_7_R4.util.UnsafeList;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Jordan
 * 
 */
public class CustomEntityZombie extends EntityZombie implements SmartEntity {

	private Location smartTarget;

	@Override
	public void setSmartTarget(Location inLoc, long duration) {
		smartTarget = inLoc;

		new BukkitRunnable() {
			@Override
			public void run() {
				smartTarget = null;
			}
		}.runTaskLater(MyZ.instance, duration);
	}

	@Override
	public Location getSmartTarget() {
		return smartTarget;
	}

	@Override
	public EntityCreature getEntity() {
		return this;
	}

	private final ZombieType type;
	private final boolean crawler;

	private static enum ZombieType {
		NORMAL(ConfigEntries.ZOMBIE_NORMAL_CHANCE.<Integer> getValue()), LEATHER(ConfigEntries.ZOMBIE_LEATHER_CHANCE.<Integer> getValue()), CHAIN(
				ConfigEntries.ZOMBIE_CHAIN_CHANCE.<Integer> getValue()), GOLD(ConfigEntries.ZOMBIE_GOLD_CHANCE.<Integer> getValue()), IRON(
				ConfigEntries.ZOMBIE_IRON_CHANCE.<Integer> getValue());

		private final int chance;

		private ZombieType(int chance) {
			this.chance = chance;
		}
	}

	@Override
	protected void bj() {
		motY = 0.46D * ConfigEntries.ZOMBIE_JUMP_MULTIPLIER.<Double> getValue();
		if (hasEffect(MobEffectList.JUMP)) {
			motY += (getEffect(MobEffectList.JUMP).getAmplifier() + 1) * 0.1F;
		}
		if (isSprinting()) {
			float f = yaw * 0.01745329F;

			motX -= MathHelper.sin(f) * 0.2F;
			motZ += MathHelper.cos(f) * 0.2F;
		}
		al = true;
	}

	@SuppressWarnings("unchecked")
	public CustomEntityZombie(World world) {
		super(world);

		int choices = ZombieType.values().length;
		while (true) {
			ZombieType option = ZombieType.values()[random.nextInt(choices)];
			if (random.nextInt(100) + 1 <= option.chance) {
				type = option;
				break;
			}
		}

		crawler = random.nextInt(100) + 1 <= ConfigEntries.ZOMBIE_CRAWLER_CHANCE.<Integer> getValue();

		try {
			CommonMobUtilities.bField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			CommonMobUtilities.bField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
			CommonMobUtilities.cField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
			CommonMobUtilities.cField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		goalSelector.a(0, new PathfinderGoalFloat(this));
		goalSelector.a(2, new CustomPathfinderGoalMeleeAttack(this, EntityHuman.class, crawler ? isBaby() ? 0.25D : 0.5D
				: ConfigEntries.ZOMBIE_SPEED_TARGET.<Double> getValue() * (isBaby() ? 0.5D : 1.0D), false));
		goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		goalSelector.a(7, new CustomPathfinderGoalRandomStroll(this, 1.0D));
		goalSelector.a(8, new CustomPathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		goalSelector.a(8, new CustomPathfinderGoalLookAtPlayer(this, CustomEntityGuard.class, 8.0F));
		goalSelector.a(8, new CustomPathfinderGoalRandomLookaround(this));
		goalSelector.a(4, new CustomPathfinderGoalMoveToLocation(this, 1.2D));
		goalSelector.a(4, new CustomPathfinderGoalMeleeAttack(this, CustomEntityGuard.class, crawler ? isBaby() ? 0.25D : 0.5D
				: ConfigEntries.ZOMBIE_SPEED_TARGET.<Double> getValue() * (isBaby() ? 0.5D : 1.0D), true));
		targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, true, new Class[] { EntityHuman.class, CustomEntityGuard.class }));
		targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 0, true));
		targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget(this, CustomEntityGuard.class, 0, false));
	}

	@Override
	public boolean canSpawn() {
		return world.a(boundingBox, this) && world.getCubes(this, boundingBox).isEmpty() && !world.containsLiquid(boundingBox);
	}

	@Override
	protected void aD() {
		super.aD();
		getAttributeInstance(GenericAttributes.maxHealth).setValue(ConfigEntries.ZOMBIE_HEALTH.<Double> getValue());
		getAttributeInstance(GenericAttributes.c).setValue(ConfigEntries.ZOMBIE_KNOCKBACK_RESIST.<Double> getValue());
		getAttributeInstance(GenericAttributes.d).setValue(ConfigEntries.ZOMBIE_SPEED.<Double> getValue());
		getAttributeInstance(GenericAttributes.e).setValue(ConfigEntries.ZOMBIE_DAMAGE.<Double> getValue());
	}

	@Override
	protected void bC() {
		switch (type) {
		case CHAIN:
			setEquipment(1, new ItemStack(Items.CHAINMAIL_BOOTS));
			setEquipment(2, new ItemStack(Items.CHAINMAIL_LEGGINGS));
			setEquipment(3, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
			setEquipment(4, new ItemStack(Items.CHAINMAIL_HELMET));
			break;
		case GOLD:
			setEquipment(1, new ItemStack(Items.GOLD_BOOTS));
			setEquipment(2, new ItemStack(Items.GOLD_LEGGINGS));
			setEquipment(3, new ItemStack(Items.GOLD_CHESTPLATE));
			setEquipment(4, new ItemStack(Items.GOLD_HELMET));
			break;
		case IRON:
			setEquipment(1, new ItemStack(Items.IRON_BOOTS));
			setEquipment(2, new ItemStack(Items.IRON_LEGGINGS));
			setEquipment(3, new ItemStack(Items.IRON_CHESTPLATE));
			setEquipment(4, new ItemStack(Items.IRON_HELMET));
			break;
		case LEATHER:
			setEquipment(1, new ItemStack(Items.LEATHER_BOOTS));
			setEquipment(2, new ItemStack(Items.LEATHER_LEGGINGS));
			setEquipment(3, new ItemStack(Items.LEATHER_CHESTPLATE));
			setEquipment(4, new ItemStack(Items.LEATHER_HELMET));
			break;
		default:
			break;
		}
	}

	@Override
	protected Item getLoot() {
		return null;
	}

	@Override
	protected void dropDeathLoot(boolean killedByPlayer, int enchantmentLevel) {
		int amount = random.nextInt(4) == 0 ? random.nextInt(2) + 1 : 0;
		for (int dropped = 0; dropped <= amount; dropped++) {
			a(Items.ROTTEN_FLESH, 1);
		}
	}

	@Override
	protected void getRareDrop(int i) {
		switch (random.nextInt(2)) {
		case 0:
			a(CraftItemStack.asNMSCopy(ItemUtilities.getInstance().getTagItem(ItemTag.MURKY_WATER, 1)), 0.0F);
			break;
		case 1:
			a(CraftItemStack.asNMSCopy(ItemUtilities.getInstance().getTagItem(ItemTag.CHAIN, 1)), 0.0F);
			break;
		}
	}
}
