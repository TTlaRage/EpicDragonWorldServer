package com.epicdragonworld.gameserver.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.epicdragonworld.Config;
import com.epicdragonworld.gameserver.model.WorldObject;
import com.epicdragonworld.gameserver.model.actor.Player;
import com.epicdragonworld.gameserver.network.GameClient;
import com.epicdragonworld.gameserver.network.packets.sendable.DeleteObject;

/**
 * @author Pantelis Andrianakis
 */
public class WorldManager
{
	private static final Logger LOGGER_WORLD_ACCESS = Logger.getLogger("world");
	
	private static final List<GameClient> ONLINE_CLIENTS = new CopyOnWriteArrayList<>();
	private static final Map<Long, Player> PLAYER_OBJECTS = new ConcurrentHashMap<>();
	private static final Map<Long, WorldObject> GAME_OBJECTS = new ConcurrentHashMap<>();
	private static final int VISIBILITY_RADIUS = 10050; // Needs to be slightly bigger than the client limit.
	
	public static void addObject(WorldObject object)
	{
		if (object.isPlayer())
		{
			if (!PLAYER_OBJECTS.values().contains(object))
			{
				ONLINE_CLIENTS.add(object.asPlayer().getClient());
				PLAYER_OBJECTS.put(object.getObjectId(), object.asPlayer());
				
				// Log world access.
				if (Config.LOG_WORLD)
				{
					LOGGER_WORLD_ACCESS.info("Player [" + object.asPlayer().getName() + "] Account [" + object.asPlayer().getClient().getAccountName() + "] Entered the world.");
				}
			}
		}
		else if (!GAME_OBJECTS.values().contains(object))
		{
			GAME_OBJECTS.put(object.getObjectId(), object);
		}
	}
	
	public static void removeObject(WorldObject object)
	{
		// Broadcast deletion to nearby players.
		for (Player player : getVisiblePlayers(object))
		{
			player.channelSend(new DeleteObject(object));
		}
		
		// Remove from list and take necessary actions.
		if (object.isPlayer())
		{
			PLAYER_OBJECTS.remove(object.getObjectId());
			// Store player.
			object.asPlayer().storeMe();
			
			// Log world access.
			if (Config.LOG_WORLD)
			{
				LOGGER_WORLD_ACCESS.info("Player [" + object.asPlayer().getName() + "] Account [" + object.asPlayer().getClient().getAccountName() + "] Left the world.");
			}
		}
		else
		{
			GAME_OBJECTS.remove(object.getObjectId());
		}
	}
	
	public static List<WorldObject> getVisibleObjects(WorldObject object)
	{
		final List<WorldObject> result = new ArrayList<>();
		for (Player player : PLAYER_OBJECTS.values())
		{
			if (player == object)
			{
				continue;
			}
			if (object.calculateDistance(player) < VISIBILITY_RADIUS)
			{
				result.add(player);
			}
		}
		for (WorldObject obj : GAME_OBJECTS.values())
		{
			if (obj == object)
			{
				continue;
			}
			if (object.calculateDistance(obj) < VISIBILITY_RADIUS)
			{
				result.add(obj);
			}
		}
		return result;
	}
	
	public static List<Player> getVisiblePlayers(WorldObject object)
	{
		final List<Player> result = new ArrayList<>();
		for (Player player : PLAYER_OBJECTS.values())
		{
			if (player == object)
			{
				continue;
			}
			if (object.calculateDistance(player) < VISIBILITY_RADIUS)
			{
				result.add(player);
			}
		}
		return result;
	}
	
	public static WorldObject getObject(long objectId)
	{
		if (PLAYER_OBJECTS.containsKey(objectId))
		{
			return PLAYER_OBJECTS.get(objectId);
		}
		return GAME_OBJECTS.get(objectId);
	}
	
	public static Player getPlayerByName(String name)
	{
		for (Player player : PLAYER_OBJECTS.values())
		{
			if (player.getName().equalsIgnoreCase(name))
			{
				return player;
			}
		}
		return null;
	}
	
	public static int getOnlineCount()
	{
		return ONLINE_CLIENTS.size();
	}
	
	public static void addClient(GameClient client)
	{
		if (!ONLINE_CLIENTS.contains(client))
		{
			ONLINE_CLIENTS.add(client);
		}
	}
	
	public static void removeClient(GameClient client)
	{
		// Store and remove player.
		final Player player = client.getActiveChar();
		if (player != null)
		{
			removeObject(player);
		}
		
		// Remove from list.
		ONLINE_CLIENTS.remove(client);
	}
	
	public static void removeClientByAccountName(String accountName)
	{
		for (GameClient client : ONLINE_CLIENTS)
		{
			if (client.getAccountName().equals(accountName))
			{
				removeClient(client);
				break;
			}
		}
	}
	
	public static GameClient getClientByAccountName(String accountName)
	{
		for (GameClient client : ONLINE_CLIENTS)
		{
			if (client.getAccountName().equals(accountName))
			{
				return client;
			}
		}
		return null;
	}
}
