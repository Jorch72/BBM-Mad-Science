package madscience.factory.slotcontainers;

import madscience.factory.MadTileEntityFactory;
import madscience.factory.MadTileEntityFactoryProduct;
import madscience.factory.recipes.MadRecipe;
import madscience.factory.recipes.MadRecipeComponent;
import madscience.factory.tileentity.prefab.MadTileEntityPrefab;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;

public class MadInputSlot extends Slot
{
    private String machineName = null;
    private MadTileEntityFactoryProduct registeredMachine = null;

    public MadInputSlot(MadTileEntityPrefab tileEntity, int index, int x, int y)
    {
        super(tileEntity, index, x, y);
        this.machineName = tileEntity.getMachineInternalName();
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        // Grab the recipe archive object array.
        MadTileEntityFactoryProduct possibleMachine = MadTileEntityFactory.getMachineInfo(machineName);

        // Prevent multiple queries.
        if (registeredMachine == null)
        {
            this.registeredMachine = possibleMachine;
        }

        if (this.registeredMachine != null)
        {
            // Check if this slot is for buckets (filled or unfilled).
            MadSlotContainer[] containerSlots = this.registeredMachine.getContainerTemplate();
            for (MadSlotContainer machineSlot : containerSlots)
            {
                if (machineSlot.getSlotType().name().toLowerCase().contains("bucket") &&
                    stack.getItem() instanceof ItemBucket)
                {
                    return true;
                }
            }
            
            // Loop through all of the machines internal recipes and look for matches to item for any input slot.
            MadRecipe[] recipeArchiveArray = this.registeredMachine.getRecipeArchive();
            for (MadRecipe machineRecipe : recipeArchiveArray)
            {
                MadRecipeComponent[] inputIngredients = machineRecipe.getInputIngredientsArray();
                for (MadRecipeComponent recipeResult : inputIngredients)
                {
                    if (!recipeResult.isLoaded())
                    {
                        continue;
                    }

                    // Determine if this recipe result matches anything in ingredient list.
                    if (recipeResult.getItemStack().getItem().equals(stack.getItem()))
                    {
                        return true;
                    }
                }
            }
        }

        // Ensure that we return false in the event something goes wrong before this.
        return false;
    }
}