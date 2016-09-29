package com.abushawish.ParkNGo.Utils.Obdapi.commands.engine;


import com.abushawish.ParkNGo.Utils.Obdapi.commands.temperature.TemperatureCommand;
import com.abushawish.ParkNGo.Utils.Obdapi.enums.AvailableCommandNames;

/**
 * Displays the current engine Oil temperature.
 *
 * @author pires
 * @version $Id: $Id
 */
public class OilTempCommand extends TemperatureCommand {

    /**
     * Default ctor.
     */
    public OilTempCommand() {
        super("01 5C");
    }

    /**
     * Copy ctor.
     *
     */
    public OilTempCommand(OilTempCommand other) {
        super(other);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.ENGINE_OIL_TEMP.getValue();
    }

}
