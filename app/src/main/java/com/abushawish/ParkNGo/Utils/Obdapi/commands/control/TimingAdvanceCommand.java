package com.abushawish.ParkNGo.Utils.Obdapi.commands.control;


import com.abushawish.ParkNGo.Utils.Obdapi.commands.PercentageObdCommand;
import com.abushawish.ParkNGo.Utils.Obdapi.enums.AvailableCommandNames;

/**
 * Timing Advance
 *
 * @author pires
 * @version $Id: $Id
 */
public class TimingAdvanceCommand extends PercentageObdCommand {

    /**
     * <p>Constructor for TimingAdvanceCommand.</p>
     */
    public TimingAdvanceCommand() {
        super("01 0E");
    }

    /**
     * <p>Constructor for TimingAdvanceCommand.</p>
     *
     */
    public TimingAdvanceCommand(TimingAdvanceCommand other) {
        super(other);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.TIMING_ADVANCE.getValue();
    }

}
