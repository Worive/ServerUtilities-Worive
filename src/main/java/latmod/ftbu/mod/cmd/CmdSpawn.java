package latmod.ftbu.mod.cmd;

import latmod.ftbu.cmd.*;
import latmod.ftbu.util.LMDimUtils;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;

public class CmdSpawn extends CommandLM
{
	public CmdSpawn()
	{ super("spawn", CommandLevel.ALL); }
	
	public IChatComponent onCommand(ICommandSender ics, String[] args)
	{
		EntityPlayerMP ep = getCommandSenderAsPlayer(ics);
		if(LMDimUtils.teleportPlayer(ep, LMDimUtils.getEntitySpawnPoint(0)))
			return new ChatComponentText("Teleported to spawn"); //LANG
		return error(new ChatComponentText("Failed to teleport!"));
	}
}