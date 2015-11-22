package latmod.ftbu.mod;

import cpw.mods.fml.relauncher.*;
import ftb.lib.api.gui.LMGuiHandler;
import ftb.lib.gui.ContainerEmpty;
import ftb.lib.item.ItemDisplay;
import latmod.ftbu.mod.client.gui.GuiDisplayItem;
import latmod.ftbu.mod.client.gui.claims.GuiClaimChunks;
import latmod.ftbu.mod.client.gui.friends.GuiFriends;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;

public class FTBUGuiHandler extends LMGuiHandler
{
	public static final FTBUGuiHandler instance = new FTBUGuiHandler(FTBUFinals.MOD_ID);
	
	public static final int FRIENDS = 1;
	public static final int SECURITY = 2;
	public static final int DISPLAY_ITEM = 3;
	public static final int ADMIN_CLAIMS = 4;
	
	public FTBUGuiHandler(String s)
	{ super(s); }
	
	public Container getContainer(EntityPlayer ep, int id, NBTTagCompound data)
	{
		return new ContainerEmpty(ep, null);
	}
	
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer ep, int id, NBTTagCompound data)
	{
		if(id == FRIENDS) return new GuiFriends(null);
		else if(id == DISPLAY_ITEM)
			return new GuiDisplayItem(ItemDisplay.readFromNBT(data));
		else if(id == ADMIN_CLAIMS)
			return new GuiClaimChunks(data.getLong("T"));
		return null;
	}
}