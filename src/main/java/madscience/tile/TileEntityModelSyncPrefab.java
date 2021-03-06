package madscience.tile;


import madscience.mod.ModLoader;
import madscience.model.ModelArchive;
import madscience.model.ModelData;
import madscience.model.ModelPosition;
import madscience.model.ModelScale;
import madscience.product.TileEntityFactoryProduct;
import madscience.rendering.WorldRenderInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;


public class TileEntityModelSyncPrefab extends TileEntityDamagePrefab
{
    /**
     * Path to current texture that should be displayed on our model.
     */
    private String entityTexture;

    /**
     * Snapshot of current model configuration for this entity.
     */
    private ModelData[] entityModelReference;

    /**
     * Snapshot of current world rendering information.
     */
    private WorldRenderInfo entityWorldRenderInformation;

    public TileEntityModelSyncPrefab()
    {
        super();
    }

    public TileEntityModelSyncPrefab(TileEntityFactoryProduct registeredMachine)
    {
        super( registeredMachine );

        // Default model and texture information.
        ModelArchive renderingInformation = registeredMachine.getModelArchive();
        if (renderingInformation != null)
        {
            // Load default texture.
            if (this.entityTexture == null)
            {
                this.entityTexture = renderingInformation.getMachineTexture();
            }

            // Load the default model configuration.
            if (this.entityModelReference == null)
            {
                this.entityModelReference = renderingInformation.getMachineModelsDataClone();
            }

            // Load world rendering reference which we can manipulate over the network.
            if (this.entityWorldRenderInformation == null)
            {
                this.entityWorldRenderInformation = renderingInformation.getWorldRenderInfoClone();
            }
        }
    }

    /**
     * Updates all server side model reference to be invisible on next update packet sent.
     */
    public void hideAllModelPieces()
    {
        // Hide all the model pieces that makeup this machine.
        for (ModelData modelReference : this.getEntityModelData())
        {
            this.setModelWorldRenderVisibilityByName( modelReference.getModelPieceName(),
                                                      false );
        }
    }

    /**
     * Updates server side world rendering information for position offset and scaling which is applied to all models in the collection on update.
     */
    public void setWorldRenderInformation(ModelPosition modelPosition, ModelScale modelScale)
    {
        // Create new world renderer position.
        ModelPosition modelWorldPosition = new ModelPosition( modelPosition.getModelTranslateX(),
                                                              modelPosition.getModelTranslateY(),
                                                              modelPosition.getModelTranslateZ() );

        // Create new world renderer scale.
        ModelScale modelWorldScale = new ModelScale( modelScale.getModelScaleX(),
                                                     modelScale.getModelScaleY(),
                                                     modelScale.getModelScaleZ() );

        // Create new world render object from the above two components.
        WorldRenderInfo updatedWorldRender = new WorldRenderInfo( modelWorldPosition,
                                                                  modelWorldScale );
        this.entityWorldRenderInformation = updatedWorldRender;
    }

    /**
     * Updates server side list of model visibility which is then transmitted to clients over the network.
     */
    public void setModelWorldRenderVisibilityByName(String modelName, boolean visible)
    {
        int x = 0;
        for (ModelData modelData : this.entityModelReference)
        {
            if (modelData.getModelPieceName().equals( modelName ))
            {
                if (modelData.isModelVisible() != visible)
                {
                    this.entityModelReference[x] = new ModelData( visible,
                                                                  modelName );
                    break;
                }
                break;
            }
            x++;
        }
    }

    /**
     * Sets the texture resource path to whatever is specified.
     */
    public void setTextureRenderedOnModel(String entityTexture)
    {
        this.entityTexture = entityTexture;
    }

    public String getEntityTexture()
    {
        return entityTexture;
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
    }

    @Override
    public void initiate()
    {
        super.initiate();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT( nbt );

        // Model instance reload.
        NBTTagList modelTagList = nbt.getTagList( "ModelDataList" );
        this.entityModelReference = new ModelData[modelTagList.tagCount()];

        for (int i = 0; i < modelTagList.tagCount(); ++ i)
        {
            NBTTagCompound modelPart = (NBTTagCompound) modelTagList.tagAt( i );
            byte partNumber = modelPart.getByte( "ModelData" );

            if (partNumber >= 0 && partNumber < this.entityModelReference.length)
            {
                this.setModelDataPartContents( partNumber,
                                               ModelData.loadModelDataFromNBT( modelPart ) );
            }
        }

        try
        {
            // World position render info.
            float renderWorldPosX;
            float renderWorldPosY;
            float renderWorldPosZ;
            renderWorldPosX = nbt.getFloat( "RenderWorldPosX" );
            renderWorldPosY = nbt.getFloat( "RenderWorldPosY" );
            renderWorldPosZ = nbt.getFloat( "RenderWorldPosZ" );

            // World scale render info.
            float renderWorldScaleX;
            float renderWorldScaleY;
            float renderWorldScaleZ;
            renderWorldScaleX = nbt.getFloat( "RenderWorldScaleX" );
            renderWorldScaleY = nbt.getFloat( "RenderWorldScaleY" );
            renderWorldScaleZ = nbt.getFloat( "RenderWorldScaleZ" );

            // Create world rendering objects from reloaded data.
            ModelPosition modelWorldPosition = new ModelPosition( renderWorldPosX,
                                                                  renderWorldPosY,
                                                                  renderWorldPosZ );
            ModelScale modelWorldScale = new ModelScale( renderWorldScaleX,
                                                         renderWorldScaleY,
                                                         renderWorldScaleZ );

            // Create new world rendering information from NBT data.
            WorldRenderInfo worldRenderFromNBT = new WorldRenderInfo( modelWorldPosition,
                                                                      modelWorldScale );
            this.entityWorldRenderInformation = worldRenderFromNBT;
        }
        catch (Exception err)
        {
            // Print the exception to the log so users know something bad happened.
            ModLoader.log().warning( err.getMessage() );
            ModLoader.log().info( "[" + this.getMachineInternalName() +
                                  "]Unable to load world render information from NBT, using defaults!" );

            // Use default world rendering data since saved stuff is corrupted.
            this.entityWorldRenderInformation = ModelArchive.defaultWorldRenderInfo();
        }

        // Path to current texture what should be loaded onto the model.
        this.entityTexture = nbt.getString( "TexturePath" );
    }

    private void setModelDataPartContents(int partNumber, ModelData modelData)
    {
        this.entityModelReference[partNumber] = modelData;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT( nbt );

        // Model instance saving.
        NBTTagList modelTagList = new NBTTagList();
        for (int i = 0; i < this.entityModelReference.length; ++ i)
        {
            NBTTagCompound modelTag = new NBTTagCompound();
            modelTag.setByte( "ModelData",
                              (byte) i );
            this.entityModelReference[i].writeToNBT( modelTag );
            modelTagList.appendTag( modelTag );
        }
        nbt.setTag( "ModelDataList",
                    modelTagList );

        // Model world position rendering.
        if (this.entityWorldRenderInformation != null)
        {
            nbt.setFloat( "RenderWorldPosX",
                          this.entityWorldRenderInformation.getModelWorldPosition().getModelTranslateX() );
            nbt.setFloat( "RenderWorldPosY",
                          this.entityWorldRenderInformation.getModelWorldPosition().getModelTranslateY() );
            nbt.setFloat( "RenderWorldPosZ",
                          this.entityWorldRenderInformation.getModelWorldPosition().getModelTranslateZ() );

            // Model world scale rendering.
            nbt.setFloat( "RenderWorldScaleX",
                          this.entityWorldRenderInformation.getModelWorldScale().getModelScaleX() );
            nbt.setFloat( "RenderWorldScaleY",
                          this.entityWorldRenderInformation.getModelWorldScale().getModelScaleY() );
            nbt.setFloat( "RenderWorldScaleZ",
                          this.entityWorldRenderInformation.getModelWorldScale().getModelScaleZ() );
        }

        // Path to current texture that should be loaded onto the model.
        nbt.setString( "TexturePath",
                       this.entityTexture );
    }

    public ModelData[] getEntityModelData()
    {
        return entityModelReference;
    }

    public WorldRenderInfo getEntityWorldRenderInformation()
    {
        return entityWorldRenderInformation;
    }
}
