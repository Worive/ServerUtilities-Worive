package latmod.ftbu.mod.cmd.admin;

import latmod.core.util.LMStringUtils;
import latmod.ftbu.cmd.*;
import latmod.ftbu.mod.config.*;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.*;

public class CmdAdminSpawnArea extends CommandLM //TODO: Remove
{
	public CmdAdminSpawnArea(String s)
	{ super(s, CommandLevel.OP); }

	public String[] getTabStrings(ICommandSender ics, String[] args, int i)
	{ return (i == 0) ? new String[] { "safe", "pvp" } : null; }
	
	public IChatComponent onCommand(ICommandSender ics, String[] args)
	{
		checkArgs(args, 2);
		
		if(args[0].equals("safe"))
		{
			FTBUConfig.general.safeSpawn = args[1].equals("true");
			ConfigGeneral.save();
			return new ChatComponentText("SafeSpawn set to: " + FTBUConfig.general.safeSpawn);
		}
		else if(args[0].equals("pvp"))
		{
			FTBUConfig.general.spawnPVP = args[1].equals("true");
			ConfigGeneral.save();
			return new ChatComponentText("SpawnPVP set to: " + FTBUConfig.general.spawnPVP);
		}
		
		return new ChatComponentText(LMStringUtils.strip(getTabStrings(ics, args, 0)));
	}
}