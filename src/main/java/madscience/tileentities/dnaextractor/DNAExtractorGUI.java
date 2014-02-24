package madscience.tileentities.dnaextractor;

import madscience.MadFluids;
import madscience.MadFurnaces;
import madscience.MadScience;
import madscience.util.GUIContainerBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DNAExtractorGUI extends GUIContainerBase
{
    private DNAExtractorEntity ENTITY;

    public DNAExtractorGUI(InventoryPlayer par1InventoryPlayer, DNAExtractorEntity par2TileEntityFurnace)
    {
        super(new DNAExtractorContainer(par1InventoryPlayer, par2TileEntityFurnace));
        this.ENTITY = par2TileEntityFurnace;
        this.TEXTURE = new ResourceLocation(MadScience.ID, "textures/gui/" + MadFurnaces.DNAEXTRACTOR_INTERNALNAME + ".png");
    }

    private void displayGauge(int screenX, int screenY, int line, int col, int squaled)
    {
        // Variable to keep track of block texture segments.
        int start = 0;

        // Grab the icon of the liquid by looking at fluid properties in
        // internal tank.
        Icon liquidIcon = MadFluids.LIQUIDDNA_MUTANT.getStillIcon();

        // Bind the texture we grabbed so we can use it in rendering.
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        // We can only display the water icon if it is not null.
        if (liquidIcon == null)
        {
            return;
        }

        while (true)
        {
            int x;
            if (squaled > 16)
            {
                x = 16;
                squaled -= 16;
            }
            else
            {
                x = squaled;
                squaled = 0;
            }

            // ------------
            // PROGRESS BAR
            // ------------
            // Screen Coords: 131x19
            // Filler Coords: 176x31
            // Image Size WH: 16x58
            drawTexturedModelRectFromIcon(screenX + col, screenY + line + 58 - x - start, liquidIcon, 16, 16 - (16 - x));
            start = start + 16;

            if (x == 0 || squaled == 0)
            {
                break;
            }
        }

        // Re-draws gauge lines ontop of scaled fluid amount to make it look
        // like the fluid is behind the gauge lines.
        mc.renderEngine.bindTexture(TEXTURE);
        drawTexturedModalRect(screenX + col, screenY + line, 176, 31, 16, 58);
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        super.drawGuiContainerBackgroundLayer(par1, par2, par3);
        
        // -----------
        // POWER LEVEL
        int powerRemianingPercentage = this.ENTITY.getPowerRemainingScaled(14);
        // Screen Coords: 10x49
        // Filler Coords: 176x0
        // Image Size WH: 14x14
        this.drawTexturedModalRect(screenX + 10, screenY + 49 + 14 - powerRemianingPercentage, 176, 14 - powerRemianingPercentage, 14, powerRemianingPercentage + 2);

        // ---------------------
        // ITEM COOKING PROGRESS
        // ---------------------
        int powerCookPercentage = this.ENTITY.getItemCookTimeScaled(31);
        // Screen Coords: 32x31
        // Filler Coords: 176x14
        // Image Size WH: 31x17
        this.drawTexturedModalRect(screenX + 32, screenY + 31, 176, 14, powerCookPercentage + 1, 17);

        // ------------
        // PROGRESS BAR
        // ------------
        // Screen Coords: 131x19
        // Filler Coords: 176x31
        // Image Size WH: 16x58
        displayGauge(screenX, screenY, 19, 131, this.ENTITY.getWaterRemainingScaled(58));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        
        // Input slot help.
        if (this.isPointInRegion(9, 32, 18, 18, mouseX, mouseY))
        {
                if (this.ENTITY.internalLiquidDNAMutantTank.getFluidAmount() > 0 && this.ENTITY.getStackInSlot(0) == null)
                        this.drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop + 10, "Input slot");
        }
        
        // Water bucket input help.
        if (this.isPointInRegion(152, 61, 18, 18, mouseX, mouseY))
        {
                if (this.ENTITY.getStackInSlot(1) == null)
                {
                    this.drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop + 10, "Place empty bucket.");
                }
        }
        
        // Mutant DNA tank help.
        if (this.isPointInRegion(131, 19, 16, 58, mouseX, mouseY) && this.ENTITY.internalLiquidDNAMutantTank.getFluid() != null)
        {
                if (this.ENTITY.internalLiquidDNAMutantTank.getFluid() != null)
                        this.drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop + 10, this.ENTITY.internalLiquidDNAMutantTank.getFluid().getFluid().getLocalizedName(), this.ENTITY.internalLiquidDNAMutantTank.getFluid().amount + " L");
        }
        
        // Name displayed above the GUI, typically name of the furnace.
        String s = MadFurnaces.DNAEXTRACTOR_TILEENTITY.getLocalizedName();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);

        // Text that labels player inventory area as "Inventory".
        String x = I18n.getString("container.inventory");
        this.fontRenderer.drawString(x, 8, this.ySize - 96 + 2, 4210752);
    }
}
