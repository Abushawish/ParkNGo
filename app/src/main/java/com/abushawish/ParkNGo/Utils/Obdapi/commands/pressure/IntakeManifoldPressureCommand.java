package com.abushawish.ParkNGo.Utils.Obdapi.commands.pressure;


import com.abushawish.ParkNGo.Utils.Obdapi.enums.AvailableCommandNames;

/**
 * Intake Manifold Pressure
 *
 * @author pires
 * @version $Id: $Id
 */
public class IntakeManifoldPressureCommand extends PressureCommand {

    /**
     * Default ctor.
     */
    public IntakeManifoldPressureCommand() {
        super("01 0B");
    }

    /**
     * Copy ctor.
     *
     */
    public IntakeManifoldPressureCommand(IntakeManifoldPressureCommand other) {
        super(other);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.INTAKE_MANIFOLD_PRESSURE.getValue();
    }

}
