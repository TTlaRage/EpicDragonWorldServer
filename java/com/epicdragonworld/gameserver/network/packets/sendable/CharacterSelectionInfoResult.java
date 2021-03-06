package com.epicdragonworld.gameserver.network.packets.sendable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.epicdragonworld.gameserver.managers.DatabaseManager;
import com.epicdragonworld.gameserver.model.holders.CharacterDataHolder;
import com.epicdragonworld.gameserver.network.SendablePacket;

/**
 * @author Pantelis Andrianakis
 */
public class CharacterSelectionInfoResult extends SendablePacket
{
	private static final Logger LOGGER = Logger.getLogger(CharacterSelectionInfoResult.class.getName());
	
	private static final String CHARACTER_QUERY = "SELECT * FROM characters WHERE account=? AND access_level>'-1' ORDER BY slot ASC";
	
	public CharacterSelectionInfoResult(String accountName)
	{
		// Local data.
		final List<CharacterDataHolder> characterList = new ArrayList<>();
		
		// Get data from database.
		try (Connection con = DatabaseManager.getConnection();
			PreparedStatement ps = con.prepareStatement(CHARACTER_QUERY))
		{
			ps.setString(1, accountName);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					final CharacterDataHolder characterData = new CharacterDataHolder();
					characterData.setName(rset.getString("name"));
					characterData.setSlot(rset.getByte("slot"));
					characterData.setSelected(rset.getBoolean("selected"));
					characterData.setClassId(rset.getByte("class_id"));
					characterData.setLocationName(rset.getString("location_name"));
					characterData.setX(rset.getFloat("x"));
					characterData.setY(rset.getFloat("y"));
					characterData.setZ(rset.getFloat("z"));
					characterData.setHeading(rset.getFloat("heading"));
					characterData.setExperience(rset.getLong("experience"));
					characterData.setHp(rset.getLong("hp"));
					characterData.setMp(rset.getLong("mp"));
					characterData.setAccessLevel(rset.getByte("access_level"));
					characterList.add(characterData);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
		
		// Send the data.
		writeShort(2); // Packet id.
		writeByte(characterList.size());
		for (CharacterDataHolder characterData : characterList)
		{
			writeString(characterData.getName());
			writeByte(characterData.getSlot());
			writeByte(characterData.isSelected() ? 1 : 0);
			writeByte(characterData.getClassId());
			writeString(characterData.getLocationName());
			writeFloat(characterData.getX());
			writeFloat(characterData.getY());
			writeFloat(characterData.getZ());
			writeFloat(characterData.getHeading());
			writeLong(characterData.getExperience());
			writeLong(characterData.getHp());
			writeLong(characterData.getMp());
			writeByte(characterData.getAccessLevel());
		}
	}
}
