// @java.file.header

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.examples.cache.loaddata.dataloader;

import org.gridgain.examples.cache.loaddata.storeloader.*;
import org.gridgain.grid.*;
import org.gridgain.grid.editions.*;
import org.gridgain.grid.typedef.*;

import javax.swing.*;

/**
 * Starts up an empty node with cache configuration that {@link GridCacheLoaderStore} configured.
 *
 * @author @java.author
 * @version @java.version
 */
@GridNotAvailableIn(GridEdition.COMPUTE_GRID)
public class GridCacheDataLoaderNodeStartup {
    /**
     * Start up an empty node with specified cache configuration.
     *
     * @param args Command line arguments, none required.
     * @throws GridException If example execution failed.
     */
    public static void main(String[] args) throws GridException {
        G.start("examples/config/spring-cache-dataloader.xml");

        try {
            // Wait until Ok is pressed.
            JOptionPane.showMessageDialog(
                null,
                new JComponent[] {
                    new JLabel("GridGain started."),
                    new JLabel("Press OK to stop GridGain.")
                },
                "GridGain",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
        finally {
            G.stop(true);
        }
    }
}
