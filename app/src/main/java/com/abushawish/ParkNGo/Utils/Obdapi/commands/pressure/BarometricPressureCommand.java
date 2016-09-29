package com.abushawish.ParkNGo.Utils.Obdapi.commands.pressure;


import com.abushawish.ParkNGo.Utils.Obdapi.enums.AvailableCommandNames;

/**
 * Barometric pressure.
 *
 * @author pires
 * @version $Id: $Id
 */
public class BarometricPressureCommand extends PressureCommand {

    /**
     * <p>Constructor for BarometricPressureCommand.</p>
     */
    public BarometricPressureCommand() {
        super("01 33");
    }

    /**
     * <p>Constructor for BarometricPressureCommand.</p>
     *
     */
    public BarometricPressureCommand(PressureCommand other) {
        super(other);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.BAROMETRIC_PRESSURE.getValue();
    }

}
