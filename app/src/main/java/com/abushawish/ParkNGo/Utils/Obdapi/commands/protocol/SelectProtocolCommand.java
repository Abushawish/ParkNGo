package com.abushawish.ParkNGo.Utils.Obdapi.commands.protocol;


import com.abushawish.ParkNGo.Utils.Obdapi.enums.ObdProtocols;

/**
 * Select the protocol to use.
 *
 * @author pires
 * @version $Id: $Id
 */
public class SelectProtocolCommand extends ObdProtocolCommand {

    private final ObdProtocols protocol;

    /**
     * <p>Constructor for SelectProtocolCommand.</p>
     *
     */
    public SelectProtocolCommand(final ObdProtocols protocol) {
        super("AT SP " + protocol.getValue());
        this.protocol = protocol;
    }

    /** {@inheritDoc} */
    @Override
    public String getFormattedResult() {
        return getResult();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Select Protocol " + protocol.name();
    }

}
