package com.abushawish.ParkNGo.Utils.Obdapi.commands.engine;


import com.abushawish.ParkNGo.Utils.Obdapi.commands.PercentageObdCommand;
import com.abushawish.ParkNGo.Utils.Obdapi.enums.AvailableCommandNames;

/**
 * Calculated Engine Load value.
 *
 * @author pires
 * @version $Id: $Id
 */
public class LoadCommand extends PercentageObdCommand {

    /**
     * <p>Constructor for LoadCommand.</p>
     */
    public LoadCommand() {
        super("01 04");
    }

    /**
     * <p>Constructor for LoadCommand.</p>
     *
     */
    public LoadCommand(LoadCommand other) {
        super(other);
    }

    /*
     * (non-Javadoc)
     *
     * @see pt.lighthouselabs.obd.commands.ObdCommand#getName()
     */
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.ENGINE_LOAD.getValue();
    }

}
