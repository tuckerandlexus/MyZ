/**
 * 
 */
package jordan.sicherman.commands;

import jordan.sicherman.utilities.configuration.ConfigEntries;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Jordan
 * 
 */
public enum CommandHandler {

	MANAGE_SPAWNS(new String[] { "manage", "spawns" }, CommandPermission.SPAWN_MANAGER, "Manage spawnpoints", "manage spawns",
			new SpawnManager(), true), CONFIG_RELOAD(new String[] { "reload", "config" }, CommandPermission.CONFIG_MANAGER,
			"Reload the configurations", "reload config", new ReloadConfig(), true), SPAWN(new String[] { "spawn" },
			CommandPermission.PLAY, "Spawn in the world", "spawn", new Spawn(), true), MANAGE_ENGINEER(
			new String[] { "manage", "engineer" }, CommandPermission.ENGINEER_MANAGER, "Manage engineering recipes", "manage engineer",
			new EngineerManager(), ConfigEntries.USE_ENHANCED_ANVILS.<Boolean> getValue()), RELOAD_USERDATA(new String[] { "reload",
			"userdata" }, CommandPermission.CONFIG_MANAGER, "Reload userdata files", "reload userdata", new ReloadUserdata(), true), TRANSCRIBE(
			new String[] { "transcribe" }, CommandPermission.CONFIG_MANAGER, "Flush locale YAMLs to a MyZ-readable format", "transcribe",
			new Transcribe(), true), MANAGE_CHESTS(new String[] { "manage", "chests" }, CommandPermission.CHEST_MANAGER, "Manage chests",
			"manage chests", new ChestManager(), ConfigEntries.CHESTS.<Boolean> getValue()), MANAGE_SPAWN_KIT(new String[] { "manage",
			"spawn", "kit" }, CommandPermission.SPAWN_KIT_MANAGER, "Manage spawn kits", "manage spawn kit", new SpawnKitManager(), true), RESPAWN_CHESTS(
			new String[] { "respawn", "chests" }, CommandPermission.CHEST_MANAGER, "Respawn MyZ chests", "respawn chests",
			new RespawnChests(), ConfigEntries.CHESTS.<Boolean> getValue()), VERSION(new String[] { "version" }, CommandPermission.OP,
			"Check the version of MyZ on your server", "version", new Version(), true), MYZ_ITEM(new String[] { "item", "get", "$item" },
			CommandPermission.CHEST_MANAGER, "Get a MyZ item", "item <TAB>", new MyZItem(), true), MYZ_ITEM_STATE(new String[] { "item",
			"apply", "$state" }, CommandPermission.CHEST_MANAGER, "Apply a MyZ state to the item in your hand", "item <TAB>",
			new MyZItemState(), true);

	private final String[] args;
	private final CommandPermission perm;
	private final boolean isEnabled;
	private final String desc, usage;
	private final SimpleCommandExecutor executor;

	private CommandHandler(String[] args, CommandPermission perm, String desc, String usage, SimpleCommandExecutor executor,
			boolean isEnabled) {
		this.args = args;
		this.perm = perm;
		this.isEnabled = isEnabled;
		this.desc = desc;
		this.usage = usage;
		this.executor = executor;
	}

	public String[] getArgs() {
		return args;
	}

	public String getPermissionNode() {
		return perm.getNode();
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public String getDescription() {
		return desc;
	}

	public String getUsage() {
		return usage;
	}

	public SimpleCommandExecutor getExecutor() {
		return executor;
	}

	private boolean matches(String[] args) {
		if (args.length < getArgs().length) { return false; }

		if ((this == MYZ_ITEM || this == MYZ_ITEM_STATE) && args[0].equalsIgnoreCase(getArgs()[0])
				&& args[1].equalsIgnoreCase(getArgs()[1])) { return true; }

		for (int i = 0; i < args.length; i++) {
			if (!args[i].equalsIgnoreCase(getArgs()[i])) { return false; }
		}

		return true;
	}

	public boolean execute(CommandSender sender, String[] args) {
		if (canExecute(sender) && matches(args)) {
			getExecutor().execute(sender, args, this);
			return true;
		}
		return false;
	}

	public boolean canExecute(CommandSender sender) {
		return isEnabled() && sender.hasPermission(getPermissionNode());
	}

	@Override
	public String toString() {
		return ChatColor.BLUE + "/MyZ " + getUsage() + ": " + ChatColor.WHITE + getDescription();
	}

	public static enum CommandPermission {
		SPAWN_MANAGER("MyZ.manager.spawns"), CONFIG_MANAGER("MyZ.manager.config"), PLAY("MyZ.play"), ENGINEER_MANAGER(
				"MyZ.manager.engineer"), CHEST_MANAGER("MyZ.manager.chests"), SPAWN_KIT_MANAGER("MyZ.manager.spawn_kits"), OP("MyZ.*");

		private final String node;

		private CommandPermission(String node) {
			this.node = node;
		}

		public String getNode() {
			return node;
		}
	}
}
