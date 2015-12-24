package latmod.ftbu.world.claims;

import com.google.gson.*;
import ftb.lib.*;
import ftb.lib.item.LMInvUtils;
import latmod.ftbu.mod.config.FTBUConfigGeneral;
import latmod.ftbu.world.*;
import latmod.lib.*;
import latmod.lib.util.EnumEnabled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.util.Map;

public class ClaimedChunks
{
	private final FastMap<Integer, FastMap<Long, ClaimedChunk>> chunks;
	
	public ClaimedChunks()
	{ chunks = new FastMap<Integer, FastMap<Long, ClaimedChunk>>(); }
	
	public void load(NBTTagCompound tag)
	{
		chunks.clear();
		
		FastMap<String, NBTTagCompound> tag1 = LMNBTUtils.toFastMapWithType(tag.getCompoundTag("ClaimedChunks"));
		
		for(Map.Entry<String, NBTTagCompound> e : tag1.entrySet())
		{
			try
			{
				int dim = Integer.parseInt(e.getKey());
				
				FastMap<Long, ClaimedChunk> map = new FastMap<Long, ClaimedChunk>();
				
				FastMap<String, NBTTagList> tag2 = LMNBTUtils.toFastMapWithType(e.getValue());
				
				for(Map.Entry<String, NBTTagList> e1 : tag2.entrySet())
				{
					NBTTagList chunksList = e1.getValue();
					
					for(int k = 0; k < chunksList.tagCount(); k++)
					{
						int[] ai = chunksList.func_150306_c(k);
						ClaimedChunk c = new ClaimedChunk(Integer.parseInt(e1.getKey()), dim, ai[0], ai[1]);
						if(ai.length >= 3 && ai[2] == 1) c.isChunkloaded = true;
						map.put(Bits.intsToLong(ai[0], ai[1]), c);
					}
				}
				
				chunks.put(Integer.valueOf(dim), map);
			}
			catch(Exception ex)
			{ ex.printStackTrace(); }
		}
	}
	
	public void load(JsonObject group)
	{
		for(Map.Entry<String, JsonElement> e : group.entrySet())
		{
			int dim = Integer.parseInt(e.getKey());
			
			FastMap<Long, ClaimedChunk> map = new FastMap<Long, ClaimedChunk>();
			
			for(Map.Entry<String, JsonElement> e1 : e.getValue().getAsJsonObject().entrySet())
			{
				try
				{
					LMPlayerServer p = LMPlayerServer.get(LMStringUtils.fromString(e1.getKey()));
					
					JsonArray chunksList = e1.getValue().getAsJsonArray();
					
					for(int k = 0; k < chunksList.size(); k++)
					{
						int[] ai = LMJsonUtils.fromArray(chunksList.get(k));
						
						if(ai != null)
						{
							ClaimedChunk c = new ClaimedChunk(p.playerID, dim, ai[0], ai[1]);
							if(ai.length >= 3 && ai[2] == 1) c.isChunkloaded = true;
							map.put(Bits.intsToLong(ai[0], ai[1]), c);
						}
					}
				}
				catch(Exception ex)
				{ ex.printStackTrace(); }
			}
			
			chunks.put(Integer.valueOf(dim), map);
		}
	}
	
	public void save(JsonObject group)
	{
		chunks.sortFromKeyNums();
		
		for(Map.Entry<Integer, FastMap<Long, ClaimedChunk>> e : chunks.entrySet())
		{
			JsonObject o1 = new JsonObject();
			
			for(ClaimedChunk c : e.getValue())
			{
				LMPlayer p = c.getOwner();
				
				if(p != null)
				{
					String id = p.getStringUUID();
					if(!o1.has(id)) o1.add(id, new JsonArray());
					
					JsonArray a = o1.get(id).getAsJsonArray();
					
					JsonArray a1 = new JsonArray();
					a1.add(new JsonPrimitive(c.pos.chunkXPos));
					a1.add(new JsonPrimitive(c.pos.chunkZPos));
					if(c.isChunkloaded) a1.add(new JsonPrimitive(1));
					a.add(a1);
				}
			}
			
			group.add(e.getKey().toString(), o1);
		}
	}
	
	public ClaimedChunk getChunk(int dim, int cx, int cz)
	{
		if(!chunks.containsKey(Integer.valueOf(dim))) return null;
		return chunks.get(Integer.valueOf(dim)).get(Long.valueOf(Bits.intsToLong(cx, cz)));
	}
	
	public FastList<ClaimedChunk> getChunks(LMPlayer p, Integer dim)
	{
		FastList<ClaimedChunk> list = new FastList<ClaimedChunk>();
		
		if(dim == null)
		{
			for(FastMap<Long, ClaimedChunk> map : chunks)
			{
				for(ClaimedChunk c : map)
				{
					if(c.ownerID == p.playerID)
						list.add(c);
				}
			}
		}
		else
		{
			for(ClaimedChunk c : chunks.get(Integer.valueOf(dim)))
			{
				if(c.ownerID == p.playerID) list.add(c);
			}
		}
		
		return list;
	}
	
	public boolean put(ClaimedChunk c)
	{
		if(c == null) return false;
		FastMap<Long, ClaimedChunk> map = chunks.get(Integer.valueOf(c.dim));
		if(map == null) chunks.put(Integer.valueOf(c.dim), map = new FastMap<Long, ClaimedChunk>());
		return map.put(Long.valueOf(Bits.intsToLong(c.pos.chunkXPos, c.pos.chunkZPos)), c) == null;
	}
	
	public boolean remove(int dim, int cx, int cz)
	{
		FastMap<Long, ClaimedChunk> map = chunks.get(Integer.valueOf(dim));
		if(map != null && map.remove(Long.valueOf(Bits.intsToLong(cx, cz))) != null)
		{
			if(map.isEmpty()) chunks.remove(Integer.valueOf(dim));
			return true;
		}
		
		return false;
	}
	
	public ChunkType getType(int dim, int cx, int cz)
	{
		World w = LMDimUtils.getWorld(dim);
		if(w == null || !w.getChunkProvider().chunkExists(cx, cz)) return ChunkType.UNLOADED;
		if(isInSpawn(dim, cx, cz)) return ChunkType.SPAWN;
		if(LMWorldServer.inst.settings.isOutsideBorder(dim, cx, cz)) return ChunkType.WORLD_BORDER;
		ClaimedChunk c = getChunk(dim, cx, cz);
		if(c == null) return ChunkType.WILDERNESS;
		return new ChunkType.PlayerClaimed(LMWorldServer.inst.getPlayer(c.ownerID));
	}
	
	public ChunkType getTypeD(int dim, double x, double z)
	{ return getType(dim, MathHelperLM.chunk(x), MathHelperLM.chunk(z)); }
	
	public static ChunkType getChunkTypeFromI(int i)
	{
		if(i <= 0) return ChunkType.UNCLAIMED_VALUES[-i];
		LMPlayer p = LMWorld.getWorld().getPlayer(i);
		return (p == null) ? ChunkType.WILDERNESS : new ChunkType.PlayerClaimed(p);
	}
	
	public static boolean isInSpawn(int dim, int cx, int cz)
	{
		if(dim != 0 || (!FTBLib.getServer().isDedicatedServer() && !FTBUConfigGeneral.spawn_area_in_sp.get())) return false;
		int radius = FTBLib.getServer().getSpawnProtectionSize();
		if(radius <= 0) return false;
		ChunkCoordinates c = LMDimUtils.getSpawnPoint(0);
		int minX = MathHelperLM.chunk(c.posX + 0.5D - radius);
		int minZ = MathHelperLM.chunk(c.posZ + 0.5D - radius);
		int maxX = MathHelperLM.chunk(c.posX + 0.5D + radius);
		int maxZ = MathHelperLM.chunk(c.posZ + 0.5D + radius);
		return cx >= minX && cx <= maxX && cz >= minZ && cz <= maxZ;
	}
	
	public static boolean isInSpawnD(int dim, double x, double z)
	{ return dim == 0 && isInSpawn(dim, MathHelperLM.chunk(x), MathHelperLM.chunk(z)); }
	
	public boolean allowExplosion(int dim, int cx, int cz)
	{
		if(dim == 0 && FTBUConfigGeneral.safe_spawn.get() && isInSpawn(dim, cx, cz))
			return false;
		else if(LMWorldServer.inst.settings.isOutsideBorder(dim, cx, cz))
			return false;
		else
		{
			ClaimedChunk c = getChunk(dim, cx, cz);
			if(c != null)
			{
				LMPlayer p = c.getOwner();
				
				if(p != null)
				{
					EnumEnabled fe = p.toPlayerMP().getRank().config.forced_explosions.get();
					if(fe == null) return p.toPlayerMP().getSettings().explosions;
					else return fe.isEnabled();
				}
			}
		}
		
		return true;
	}
	
	public static boolean canPlayerInteract(EntityPlayer ep, int x, int y, int z, boolean leftClick)
	{
		if(ep.worldObj.isRemote) return true;
		
		LMPlayerServer p = LMWorldServer.inst.getPlayer(ep);
		
		if(LMWorldServer.inst.settings.isOutsideBorderD(ep.dimension, x, z)) return false;
		else if(p.getRank().config.allowCreativeInteractSecure(ep)) return true;
		
		if(leftClick)
		{
			if(p != null && p.getRank().config.break_whitelist.get().contains(LMInvUtils.getRegName(ep.worldObj.getBlock(x, y, z))))
				return true;
		}
		
		ChunkType type = LMWorldServer.inst.claimedChunks.getTypeD(ep.dimension, x, z);
		return type.canInteract(p, leftClick);
	}
}