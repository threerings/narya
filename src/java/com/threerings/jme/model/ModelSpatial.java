//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.jme.model;

import java.io.Externalizable;
import java.io.IOException;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import com.jme.math.Matrix4f;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;

/**
 * Contains method common to both {@link ModelNode}s and {@link ModelMesh}es.
 */
public interface ModelSpatial
{
    /**
     * Recursively expands the model bounds of any deformable meshes so that
     * they include the current vertex positions.
     */
    public void expandModelBounds ();
    
    /**
     * Recursively sets the reference transforms for any bones in the model.
     */
    public void setReferenceTransforms ();

    /**
     * Recursively compiles any static meshes to vertex buffer objects (VBOs)
     * or display lists.
     *
     * @param useVBOs if true, use VBOs if the graphics card supports them
     * @param useDisplayLists if true and not using VBOs, compile static
     * objects to display lists
     */
    public void lockStaticMeshes (
        Renderer renderer, boolean useVBOs, boolean useDisplayLists);
    
    /**
     * Recursively resolves texture references using the given provider.
     */
    public void resolveTextures (TextureProvider tprov);
    
    /**
     * Creates or populates and returns a clone of this object using the given
     * clone properties.
     *
     * @param store an instance of this class to populate, or <code>null</code>
     * to create a new instance
     */
    public Spatial putClone (Spatial store, Model.CloneCreator properties);
    
    /**
     * Recursively writes any data buffers to the output channel.
     */    
    public void writeBuffers (FileChannel out)
        throws IOException;
    
    /**
     * Recursively reads any data buffers from the input channel.
     */
    public void readBuffers (FileChannel in)
        throws IOException;
    
    /**
     * Recursively slices any data buffers from the buffer map.
     */
    public void sliceBuffers (MappedByteBuffer map);
}
