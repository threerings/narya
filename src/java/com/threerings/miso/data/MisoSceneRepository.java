//
// $Id: MisoSceneRepository.java,v 1.1 2003/02/12 05:39:15 mdb Exp $

package com.threerings.miso.data;

import com.samskivert.io.PersistenceException;

/**
 * A generic interface for an entity that provides access to {@link
 * MisoSceneModel} data.
 */
public interface MisoSceneRepository
{
    /**
     * Loads the specified scene model from the repository.
     *
     * @return the requested scene or null if no scene exists with the
     * specified scene and sector id.
     *
     * @exception PersistenceException thrown if an error occurs
     * communicating with the underlying storage mechanism.
     */
    public MisoSceneModel loadSceneModel (int sceneId, int sectorId)
        throws PersistenceException;

    /**
     * Stores the specified model in the repository, inserting it if it
     * does not already exist, updating it otherwise.
     *
     * @exception PersistenceException thrown if an error occurs
     * communicating with the underlying storage mechanism.
     */
    public void storeSceneModel (MisoSceneModel model)
        throws PersistenceException;
}
