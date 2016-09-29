package com.abushawish.ParkNGo.Utils.Obdapi.commands.fuel;


import com.abushawish.ParkNGo.Utils.Obdapi.commands.PercentageObdCommand;
import com.abushawish.ParkNGo.Utils.Obdapi.enums.AvailableCommandNames;

/**
 * Get fuel level in percentage
 *
 * @author pires
 * @version $Id: $Id
 */
public class FuelLevelCommand extends PercentageObdCommand {

    /**
     * <p>Constructor for FuelLevelCommand.</p>
     */
    public FuelLevelCommand() {
        super("01 2F");
    }

    /** {@inheritDoc} */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        percentage = 100.0f * buffer.get(2) / 255.0f;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.FUEL_LEVEL.getValue();
    }

    /**
     * <p>getFuelLevel.</p>
     *
     * @return a float.
     */
    public float getFuelLevel() {
        return percentage;
    }

}
