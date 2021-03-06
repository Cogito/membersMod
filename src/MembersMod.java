
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;


/**
 * @author Andrew Ardill
 *
 */
public class MembersMod extends Plugin {
	private Listener listener = new Listener(this);
	protected PropertiesFile config;
	protected final Logger log = Logger.getLogger("Minecraft");
	protected String name = "MembersMod";
	protected String version = "0.1";
	public DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
	private PluginData data;
	
	/**
	 * This must be called to setup the plug-in!
	 * @param name - The name for the config/logfile.
	 */
	public MembersMod() {
		//data = new PluginData(name, true);
		//data.specifyDataObject(MembersModData.class);
		//data.initialise();
		config = new PropertiesFile("plugins/"+name+".txt");
		reloadConfig();
	}

	private class MembersModData extends DataObject{
		private String user;
		private String lastlogin;

		public MembersModData(String user, String lastlogin){
			super();
			this.user = user;
			this.lastlogin = lastlogin;
			
		}

		public void read(SQLInput stream, String typeName) throws SQLException {
			this.user = stream.readString();
			this.lastlogin = stream.readString();
		}
		public void write(SQLOutput stream) throws SQLException {
			stream.writeString(this.user);
			stream.writeString(this.lastlogin);
			
		}
		@Override
		public String tableDescription() {
			return "user varchar(32), lastlogin varchar(32)";
		}

	}
	/**
	 * This is called when the plug-in is enabled.
	 */
	public void onEnable() {}

	/**
	 * This is called when the plug-in is disabled.
	 */
	public void onDisable() {}

	/**
	 * This is called when a reload is issued, read config here.
	 */
	public void onReload() {}

	/**
	 * Called after including a reload check for the plug-in.
	 * @param player - Player issuing the command.
	 * @param split - Array containing the command bits.
	 * @return True if the command is to be captured here.
	 */
	public boolean postReload(Player player, String[] split) { return false; }

	public void onInitialize() {}

	@Override
	public void initialize() {
		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.LOW);
		etc.getLoader().addListener(PluginLoader.Hook.DISCONNECT, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.LOGIN, listener, this, PluginListener.Priority.MEDIUM);
		onInitialize();
	}

	@Override
	public void enable() {
		onEnable();
		log.info(name+" was enabled.");
	}

	@Override
	public void disable() {
		onDisable();
		log.info(name+" was disabled.");
	}

	private void reloadConfig() {
		config.load();
		onReload();
	}

	/**
	 * Sends a message to all players!
	 * @param String - Message to send to all players.
	 */
	public void broadcast(String message) {
		for (Player p : etc.getServer().getPlayerList()) {
			p.sendMessage(message);
		}
	}

	/**
	 * Determines if a player used a command AND can use it.
	 * 
	 * @param Player - The player attempting to use the command.
	 * @param String - The command being checked.
	 * @param String - What the player is trying to use.
	 */
	public boolean isApt(Player player, String input, String command) {
		return (player.canUseCommand(command) && input.equalsIgnoreCase(command));
	}
	
	public class Listener extends PluginListener {
		MembersMod p;

		// This controls the accessibility of functions / variables from the main class.
		public Listener(MembersMod plugin) {
			p = plugin;
		}
		
		@Override
		public void onLogin(Player player) {
			player.sendMessage(Colors.Yellow + "Last login was on "+getLastLogin(player));

		}
		
		@Override
		public void onDisconnect(Player player){
			setLastLogin(player);
		}
		
		@Override
		public boolean onCommand(Player player, String[] split) {
			if (isApt(player, "/reload", split[0])) {
				reloadConfig();
			}
			return postReload(player, split);
		}
	}

	public String getLastLogin(Player player) {
		// gets the last login time, or if that doesn't exist, sets last login to now
		return config.getString(player.getName(), dateNow());
	}
	
	public void setLastLogin(Player player) {
		config.setString(player.getName(), dateNow());
		//MembersModData mydata = new MembersModData(player.getName(), dateNow());
		//data.addRow(mydata);
	}

	protected String dateNow() {
		return dateFormat.format(new Date());
	}
	
	protected Date dateFromSring(String dateString) throws ParseException{
		return dateFormat.parse(dateString);
	}
}
