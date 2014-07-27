package madscience.factory.model;

import com.google.gson.annotations.Expose;

public class MadModelWorldRender
{
    @Expose
    private MadModelPosition modelWorldPosition;
    
    @Expose
    private MadModelScale modelWorldScale;
    
    public MadModelWorldRender(
            MadModelPosition modelWorldPosition,
            MadModelScale modelWorldScale)
    {
        super();
        
        this.modelWorldPosition = modelWorldPosition;
        this.modelWorldScale = modelWorldScale;
    }

    public MadModelPosition getModelWorldPosition()
    {
        return modelWorldPosition;
    }

    public MadModelScale getModelWorldScale()
    {
        return modelWorldScale;
    }
}