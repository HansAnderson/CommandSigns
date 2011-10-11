package com.hans.CommandSigns;

import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class CommandSignsPlayerListener extends PlayerListener {
	private CommandSigns plugin;
	
	public CommandSignsPlayerListener(CommandSigns instance){
		plugin = instance;
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		CommandSignsSignClickEvent signClickEvent = new CommandSignsSignClickEvent(plugin);
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			BlockState state = event.getClickedBlock().getState();
			if(state instanceof Sign) {
				Sign sign = (Sign) state;
				signClickEvent.onRightClick(event, sign);
			}
		}
	}
}