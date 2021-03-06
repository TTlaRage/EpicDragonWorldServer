package com.epicdragonworld.gameserver.network.packets.receivable;

import com.epicdragonworld.gameserver.managers.WorldManager;
import com.epicdragonworld.gameserver.model.WorldObject;
import com.epicdragonworld.gameserver.model.actor.Player;
import com.epicdragonworld.gameserver.network.GameClient;
import com.epicdragonworld.gameserver.network.ReceivablePacket;
import com.epicdragonworld.gameserver.network.packets.sendable.PlayerInformation;

/**
 * @author Pantelis Andrianakis
 */
public class ObjectInfoRequest
{
	public ObjectInfoRequest(GameClient client, ReceivablePacket packet)
	{
		// Read data.
		final long objectId = packet.readLong();
		
		// Get the acting player.
		final Player player = client.getActiveChar();
		// Send the information.
		for (WorldObject object : WorldManager.getVisibleObjects(player))
		{
			if (object.getObjectId() == objectId)
			{
				if (object.isPlayer())
				{
					client.channelSend(new PlayerInformation(object.asPlayer()));
				}
				// TODO: Other objects - NpcInformation?
				break;
			}
		}
	}
}
