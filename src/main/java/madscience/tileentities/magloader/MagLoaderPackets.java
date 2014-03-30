package madscience.tileentities.magloader;

import madscience.network.MadPackets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class MagLoaderPackets extends MadPackets
{
    // Tile entity reference upon execute.
    private MagLoaderEntity ENTITY;
    
    // Last known maximum cook time.
    private int lastItemCookTimeMaximum;
    
    // Last known current cook time.
    private int lastItemCookTimeValue;

    // Last known amount of energy stored.
    private long lastItemStoredEnergy;

    // Last known maximum amount of energy.
    private long lastItemStoredMaxEnergy;
    
    // Does the loader have bullets loaded into it.
    private int bulletCount;
    
    // Does the loader have magazines inserted into it.
    private int magazineCount;

    // Last known texture displayed on the tile entity.
    private String texturePath;

    // XYZ coordinates of tile entity source.
    private int tilePosX;
    private int tilePosY;
    private int tilePosZ;

    public MagLoaderPackets()
    {
        // Required for reflection.
    }

    public MagLoaderPackets(int posX, int posY, int posZ, int cookTime, int cookTimeMax,
            long energyStored, long energyMax, String tileTexture, int storageItemsCount, int inputItemsCount)
    {
        // World position information.
        tilePosX = posX;
        tilePosY = posY;
        tilePosZ = posZ;

        // Tile entity specific.
        lastItemCookTimeValue = cookTime;
        lastItemCookTimeMaximum = cookTimeMax;
        lastItemStoredMaxEnergy = energyMax;
        lastItemStoredEnergy = energyStored;
        texturePath = tileTexture;
        
        // Magazine loader specific.
        bulletCount = storageItemsCount;
        magazineCount = inputItemsCount;
    }

    @Override
    public void execute(EntityPlayer player, Side side) throws ProtocolException
    {
        // ------
        // CLIENT
        // ------

        // Packet received by client, executing payload.
        if (side.isClient())
        {
            ENTITY = (MagLoaderEntity) player.worldObj.getBlockTileEntity(tilePosX, tilePosY, tilePosZ);
            if (ENTITY == null)
                return;

            this.ENTITY.currentItemCookingValue = lastItemCookTimeValue;
            this.ENTITY.currentItemCookingMaximum = lastItemCookTimeMaximum;
            this.ENTITY.setEnergyCapacity(lastItemStoredMaxEnergy);
            this.ENTITY.setEnergy(ForgeDirection.UNKNOWN, lastItemStoredEnergy);
            this.ENTITY.TEXTURE = texturePath;
            this.ENTITY.clientBulletCount = bulletCount;
            this.ENTITY.clientMagazineCount = magazineCount;
        }
        else
        {
            throw new ProtocolException("Cannot send this packet to the server!");
        }
    }

    @Override
    public void read(ByteArrayDataInput in) throws ProtocolException
    {
        // ------
        // CLIENT
        // ------

        // World coordinate information.
        this.tilePosX = in.readInt();
        this.tilePosY = in.readInt();
        this.tilePosZ = in.readInt();

        // Packet being read by client, verifying integrity.
        this.lastItemCookTimeValue = in.readInt();
        this.lastItemCookTimeMaximum = in.readInt();
        this.lastItemStoredMaxEnergy = in.readLong();
        this.lastItemStoredEnergy = in.readLong();
        this.texturePath = in.readUTF();
        
        // Magazine loader packet for client.
        this.bulletCount = in.readInt();
        this.magazineCount = in.readInt();
    }

    @Override
    public void write(ByteArrayDataOutput out)
    {
        // ------
        // SERVER
        // ------

        // World coordinate information.
        out.writeInt(tilePosX);
        out.writeInt(tilePosY);
        out.writeInt(tilePosZ);

        // Packet being written by server.
        out.writeInt(lastItemCookTimeValue);
        out.writeInt(lastItemCookTimeMaximum);
        out.writeLong(lastItemStoredMaxEnergy);
        out.writeLong(lastItemStoredEnergy);
        out.writeUTF(texturePath);
        
        // Magazine loader server information.
        out.writeInt(bulletCount);
        out.writeInt(magazineCount);
    }
}