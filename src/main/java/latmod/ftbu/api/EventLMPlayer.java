package latmod.ftbu.api;

import cpw.mods.fml.relauncher.Side;
import latmod.ftbu.world.LMPlayer;

public abstract class EventLMPlayer extends EventLM
{
	public abstract LMPlayer getPlayer();
	public abstract Side getSide();
}