package madscience.furnace;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import madscience.mod.ModLoader;
import net.minecraft.item.ItemStack;


public class FurnaceRecipeComponent
{
    @Expose
    @SerializedName("ItemName")
    private String itemName;

    @Expose
    @SerializedName("ItemAmount")
    private int itemAmount;

    @Expose
    @SerializedName("ParentModID")
    private String parentModID;

    @Expose
    @SerializedName("MetaDamage")
    private int metaDamage;

    private boolean hasLoaded = false;

    private ItemStack associatedItemStack = null;

    FurnaceRecipeComponent(String parentModID, String internalName, int amount, int metaDamage)
    {
        super();

        this.parentModID = parentModID;
        this.itemName = internalName;
        this.itemAmount = amount;
        this.metaDamage = metaDamage;
    }

    public String getInternalName()
    {
        return this.itemName;
    }

    public int getAmount()
    {
        return this.itemAmount;
    }

    public String getNameWithModID()
    {
        return this.parentModID + ":" + this.itemName;
    }

    public int getMetaDamage()
    {
        return this.metaDamage;
    }

    public boolean isLoaded()
    {
        return this.hasLoaded;
    }

    public ItemStack getAssociatedItemStack()
    {
        if (! this.hasLoaded)
        {
            ModLoader.log().warning( "[FurnaceRecipeComponent]Cannot return associated itemstack for recipe since it was never loaded!" );
            return null;
        }

        return this.associatedItemStack;
    }

    public void associateItemStackToRecipeComponent(ItemStack associatedItemStack)
    {
        // Prevent double-loading!
        if (hasLoaded)
        {
            ModLoader.log().warning( "[FurnaceRecipeComponent]Already loaded and verified this recipe with GameRegistry!" );
            return;
        }

        if (this.associatedItemStack != null)
        {
            ModLoader.log().warning( "[FurnaceRecipeComponent]Associated item stack is not null! How can this be?!" );
            return;
        }

        // Make sure this cannot happen twice.
        hasLoaded = true;

        // Take a copy of the inputed parameter item for future reference.
        this.associatedItemStack = associatedItemStack;
    }

    public String getModID()
    {
        return this.parentModID;
    }
}
