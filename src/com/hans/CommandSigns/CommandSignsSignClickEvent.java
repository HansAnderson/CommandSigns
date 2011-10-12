package com.hans.CommandSigns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.permissions.PermissionAttachment;

public class CommandSignsSignClickEvent {
	
	private static CommandSigns plugin;
	private static String[] delimiters = {"/","\\\\","@"};
	
	public CommandSignsSignClickEvent(CommandSigns instance){
		plugin = instance;
	}
	
	public void onRightClick(PlayerInteractEvent event, Sign sign)
	{
		Player player = event.getPlayer();
		CommandSignsLocation location = new CommandSignsLocation(sign.getX(), sign.getY(), sign.getZ());
		CommandSignsPlayerState state = plugin.playerStates.get(player.getName());
		if(state != null) {
			if(state.equals(CommandSignsPlayerState.ENABLE)) {
				enableSign(player, location);
			} else if(state.equals(CommandSignsPlayerState.DISABLE)) {
				disableSign(player, location);
			} else if(state.equals(CommandSignsPlayerState.READ)) {
				readSign(player, location);
			}
			return;
		}
		if(!plugin.activeSigns.containsKey(location)) {
			return;
		}
		List<String> commandList = parseSignText(player, plugin.activeSigns.get(location).toString());
		if(plugin.hasPermission(player,"CommandSigns.use.regular")) {				
			String groupFilter = null;
			
			for(String command : commandList)
			{
				if(command.indexOf("@") != 0 && groupFilter != null && !inGroup(player, groupFilter)) {
					continue;
				}
				
				if(command.startsWith("@")) {
					if(command.length() <= 1) {
						groupFilter = null;
					} else {
						groupFilter = command.substring(1).trim();
					}
					continue;
				}
				
				if(command.startsWith("/")) {
					PermissionAttachment newPermission = null;
					if(command.length() <= 1) {
						player.sendMessage("Error, SignCommand /command is of length 0.");
						continue;
					}
					if(command.startsWith("/*")) {
						command = command.substring(2);
						
						if(player.hasPermission("CommandSigns.use.super")) {
							if(!player.hasPermission("CommandSigns.permissions")) {
								newPermission = player.addAttachment(plugin, "CommandSigns.permissions", true);
							}
						} else {
							player.sendMessage("You may not use this type of sign.");
							continue;
						}
					}else{
						command = command.substring(1);
					}
					Bukkit.dispatchCommand(player, command);
					//player.performCommand(command);
					if(newPermission != null) {
						newPermission.remove();
					}
					continue;
				}
				
				if(command.indexOf("\\\\") == 0) {
					String msg = command.substring(2);
					player.sendMessage(msg);
					continue;
				}
			}
		}
	}
	
	private boolean inGroup(Player player, String group) {
		String permissionsNode = ("CommandSigns.group." + group);
		if(player.hasPermission(permissionsNode)) {
			return true;
		}
		player.sendMessage("You do not have that group permission");
		return false;
	}

	public static List<String> parseSignText(Player player, String text)
	{
		text = text.replace("<X>", ""+ player.getLocation().getBlockX());
		text = text.replace("<Y>", ""+ player.getLocation().getBlockY());
		text = text.replace("<Z>", ""+ player.getLocation().getBlockZ());
		text = text.replace("<NAME>", ""+ player.getName());
		List<String> commandList = new ArrayList<String>();
		//commandList.add(text);
		for(String delimiter : text.split("\r"))
		{
			commandList.add(delimiter);
		}
		return commandList;
	}
	
	public void enableSign(Player player, CommandSignsLocation location) {
		if(plugin.activeSigns.containsKey(location)) {
			player.sendMessage("Sign is already enabled!");
			return;
		}
		CommandSignsText text = plugin.playerText.get(player.getName());
		plugin.activeSigns.put(location, text);
		plugin.playerStates.remove(player.getName());
		plugin.playerText.remove(player.getName());
		player.sendMessage("CommandSign enabled");
	}
	
	public void readSign(Player player, CommandSignsLocation location) {
		CommandSignsText text = plugin.activeSigns.get(location);
		if(text == null) {
			player.sendMessage("Sign is not a CommandSign.");
		}
		String[] lines = text.getText();
		for(int i = 0; i < lines.length; i++) {
			if(lines[i] != null) {
				player.sendMessage("Line" + i + ": " + lines[i]);
			}
		}
		plugin.playerStates.remove(player.getName());
	}
	
	public void disableSign(Player player, CommandSignsLocation location) {
		if(!plugin.activeSigns.containsKey(location)) {
			player.sendMessage("Sign is not enabled!");
			plugin.playerStates.remove(player.getName());
			return;
		}
		plugin.activeSigns.remove(location);
		plugin.playerStates.remove(player.getName());
		player.sendMessage("Sign disabled.");
	}
}
