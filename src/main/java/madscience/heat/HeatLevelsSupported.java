package madscience.heat;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class HeatLevelsSupported
{
    /**
     * Starting amount of heat that the machine will start off with.
     */
    @Expose
    @SerializedName("HeatLevelValue")
    private int heatLevelValue;

    /**
     * Amount of heat the machine will need to gather before it will be considered 'heated'.
     */
    @Expose
    @SerializedName("HeatLevelTriggerValue")
    private int heatLevelTriggerValue;

    /**
     * Maximum amount of heat that a given machine can handle in total.
     */
    @Expose
    @SerializedName("HeatLevelMaximumValue")
    private int heatLevelMaximumValue;

    @SuppressWarnings("ucd")
    public HeatLevelsSupported(int heatStartValue, int heatTriggerValue, int heatMaximumValue)
    {
        super();

        this.heatLevelValue = heatStartValue;
        this.heatLevelTriggerValue = heatTriggerValue;
        this.heatLevelMaximumValue = heatMaximumValue;
    }

    public int getHeatLevelValue()
    {
        return heatLevelValue;
    }

    public int getHeatLevelTriggerValue()
    {
        return heatLevelTriggerValue;
    }

    public int getHeatLevelMaximumValue()
    {
        return heatLevelMaximumValue;
    }
}
