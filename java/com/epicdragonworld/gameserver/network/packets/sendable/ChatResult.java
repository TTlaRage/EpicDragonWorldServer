package com.epicdragonworld.gameserver.network.packets.sendable;

import com.epicdragonworld.gameserver.network.SendablePacket;

/**
 * @author Pantelis Andrianakis
 */
public class ChatResult extends SendablePacket
{
	public ChatResult(byte chatType, String sender, String message)
	{
		// Send the data.
		writeShort(10); // Packet id.
		writeByte(chatType); // 0 system, 1 normal chat, 2 personal message
		writeString(sender);
		writeString(message);
	}
}
