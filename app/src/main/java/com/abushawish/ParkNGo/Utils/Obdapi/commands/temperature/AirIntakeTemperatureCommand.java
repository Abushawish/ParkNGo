package com.abushawish.ParkNGo.Utils.Obdapi.commands.temperature;


import com.abushawish.ParkNGo.Utils.Obdapi.enums.AvailableCommandNames;

/**
 * Temperature of intake air.
 *
 * @author pires
 * @version $Id: $Id
 */
public class AirIntakeTemperatureCommand extends TemperatureCommand {

    /**
     * <p>Constructor for AirIntakeTemperatureCommand.</p>
     */
    public AirIntakeTemperatureCommand() {
        super("01 0F");
    }

    /**
     * <p>Constructor for AirIntakeTemperatureCommand.</p>
     *
     */
    public AirIntakeTemperatureCommand(AirIntakeTemperatureCommand other) {
        super(other);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.AIR_INTAKE_TEMP.getValue();
    }

}
