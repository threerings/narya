//
// $Id: SceneRepository.java,v 1.2 2001/08/15 00:00:51 mdb Exp $

package com.threerings.miso.scene;

import java.io.IOException;

/**
 * The SceneRepository interface manages persistent scene storage.
 */
public interface SceneRepository
{
    /**
     * Loads and returns the scene object with the specified id.
     *
     * @param fname the full pathname to the file.
     *
     * @return the scene object.
     */
    public Scene loadScene (String fname) throws IOException;
}
