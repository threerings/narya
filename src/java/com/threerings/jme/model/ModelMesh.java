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
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import java.util.Properties;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Controller;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.VBOInfo;
import com.jme.scene.batch.TriangleBatch;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.geom.BufferUtils;

import com.samskivert.util.StringUtil;

import com.threerings.jme.Log;

/**
 * A {@link TriMesh} with a serialization mechanism tailored to stored models.
 */
public class ModelMesh extends TriMesh
    implements Externalizable, ModelSpatial
{
    /**
     * No-arg constructor for deserialization.
     */
    public ModelMesh ()
    {
        super("mesh");
    }
    
    /**
     * Creates a mesh with no vertex data.
     */
    public ModelMesh (String name)
    {
        super(name);
    }
    
    /**
     * Configures this mesh based on the given parameters and (sub-)properties.
     *
     * @param texture the texture specified in the model export, if any (can be
     * overridden by textures specified in the properties)
     * @param solid whether or not the mesh allows back face culling
     * @param transparent whether or not the mesh is (partially) transparent
     */
    public void configure (
        boolean solid, String texture, boolean transparent, Properties props)
    {
        _textures = (texture == null) ? null : StringUtil.parseStringArray(
            props.getProperty(texture, texture));
        _solid = solid;
        _transparent = transparent;
    }
    
    /**
     * Sets the buffers as {@link ByteBuffer}s, because we can't create byte
     * views of non-byte buffers.  This method is where the model is
     * initialized after loading.
     */
    public void reconstruct (
        ByteBuffer vertices, ByteBuffer normals, ByteBuffer colors,
        ByteBuffer textures, ByteBuffer indices)
    {
       reconstruct(
            vertices == null ? null : vertices.asFloatBuffer(),
            normals == null ? null : normals.asFloatBuffer(),
            colors == null ? null : colors.asFloatBuffer(),
            textures == null ? null : textures.asFloatBuffer(),
            indices == null ? null : indices.asIntBuffer());
        _vertexByteBuffer = vertices;
        _normalByteBuffer = normals;
        _colorByteBuffer = colors;
        _textureByteBuffer = textures;
        _indexByteBuffer = indices;
        
        // initialize the model if we're displaying
        if (DisplaySystem.getDisplaySystem() == null) {
            return;
        }
        if (_backCull == null) {
            initSharedStates();
        }
        if (_solid) {
            setRenderState(_backCull);
        }
        if (_transparent) {
            setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
            setRenderState(_blendAlpha);
            setRenderState(_overlayZBuffer);
        }
    }
    
    /**
     * Adjusts the vertices and the transform of the mesh so that the mesh's
     * position lies at the center of its bounding volume.
     */
    public void centerVertices ()
    {
        Vector3f offset = getBatch(0).getModelBound().getCenter().negate();
        if (!offset.equals(Vector3f.ZERO)) {
            getLocalTranslation().subtractLocal(offset);
            getBatch(0).getModelBound().getCenter().set(Vector3f.ZERO);
            getBatch(0).translatePoints(offset);
        }
    }
    
    @Override // documentation inherited
    public void reconstruct (
        FloatBuffer vertices, FloatBuffer normals, FloatBuffer colors,
        FloatBuffer textures, IntBuffer indices)
    {
        super.reconstruct(vertices, normals, colors, textures, indices);
        
        _vertexBufferSize = (vertices == null) ? 0 : vertices.capacity();
        _normalBufferSize = (normals == null) ? 0 : normals.capacity();
        _colorBufferSize = (colors == null) ? 0 : colors.capacity();
        _textureBufferSize = (textures == null) ? 0 : textures.capacity();
        _indexBufferSize = (indices == null) ? 0 : indices.capacity();
    }
    
    @Override // documentation inherited
    public void reconstruct (
        FloatBuffer vertices, FloatBuffer normals, FloatBuffer colors,
        FloatBuffer textures)
    {
        super.reconstruct(vertices, normals, colors, textures);
        
        _vertexBufferSize = (vertices == null) ? 0 : vertices.capacity();
        _normalBufferSize = (normals == null) ? 0 : normals.capacity();
        _colorBufferSize = (colors == null) ? 0 : colors.capacity();
        _textureBufferSize = (textures == null) ? 0 : textures.capacity();
    }
    
    // documentation inherited from interface ModelSpatial
    public Spatial putClone (Spatial store, Model.CloneCreator properties)
    {
        ModelMesh mstore = (ModelMesh)properties.originalToCopy.get(this);
        if (mstore != null) {
            return mstore;
        } else if (store == null) {
            mstore = new ModelMesh(getName());
        } else {
            mstore = (ModelMesh)store;
        }
        properties.originalToCopy.put(this, mstore);
        mstore.normalsMode = normalsMode;
        mstore.cullMode = cullMode;
        for (int ii = 0; ii < RenderState.RS_MAX_STATE; ii++) {
            RenderState rstate = getRenderState(ii);
            if (rstate != null) {
                mstore.setRenderState(rstate);
            }
        }
        mstore.renderQueueMode = renderQueueMode;
        mstore.lockedMode = lockedMode;
        mstore.lightCombineMode = lightCombineMode;
        mstore.textureCombineMode = textureCombineMode;
        mstore.name = name;
        mstore.isCollidable = isCollidable;
        mstore.localRotation.set(localRotation);
        mstore.localTranslation.set(localTranslation);
        mstore.localScale.set(localScale);
        for (Object controller : getControllers()) {
            if (controller instanceof ModelController) {
                mstore.addController(
                    ((ModelController)controller).putClone(null, properties));
            }
        }
        TriangleBatch batch = (TriangleBatch)getBatch(0),
            mbatch = (TriangleBatch)mstore.getBatch(0);
        mbatch.setVertexBuffer(properties.isSet("vertices") ?
            batch.getVertexBuffer() :
                BufferUtils.clone(batch.getVertexBuffer()));
        mbatch.setColorBuffer(properties.isSet("colors") ?
            batch.getColorBuffer() :
                BufferUtils.clone(batch.getColorBuffer()));
        mbatch.setNormalBuffer(properties.isSet("normals") ?
            batch.getNormalBuffer() :
                BufferUtils.clone(batch.getNormalBuffer()));
        FloatBuffer texcoords;
        for (int ii = 0; (texcoords = batch.getTextureBuffer(ii)) != null;
            ii++) {
            mbatch.setTextureBuffer(properties.isSet("texcoords") ?
                texcoords : BufferUtils.clone(texcoords), ii);
        }
        mbatch.setIndexBuffer(properties.isSet("indices") ?
            batch.getIndexBuffer() :
                BufferUtils.clone(batch.getIndexBuffer()));
        if (properties.isSet("vboinfo")) {
            mbatch.setVBOInfo(batch.getVBOInfo());
        }
        if (properties.isSet("obbtree")) {
            mbatch.setCollisionTree(batch.getCollisionTree());
        }
        if (properties.isSet("displaylistid")) {
            mbatch.setDisplayListID(batch.getDisplayListID());
        }
        if (batch.getModelBound() != null) {
            mbatch.setModelBound(properties.isSet("bound") ?
                batch.getModelBound() : batch.getModelBound().clone(null));
        }
        if (_textures != null && _textures.length > 1) {
            int tidx = properties.random % _textures.length;
            mstore._textures = new String[] { _textures[tidx] };
            mstore._tstates = new TextureState[] { _tstates[tidx] };
            mstore.setRenderState(_tstates[tidx]);
        } else {
            mstore._textures = _textures;
            mstore._tstates = _tstates;
        }
        return mstore;
    }
    
    @Override // documentation inherited
    public void updateWorldVectors ()
    {
        if (!_transformLocked) {
            super.updateWorldVectors();
        }
    }
    
    // documentation inherited from interface Externalizable
    public void writeExternal (ObjectOutput out)
        throws IOException
    {
        out.writeUTF(getName());
        out.writeObject(getLocalTranslation());
        out.writeObject(getLocalRotation());
        out.writeObject(getLocalScale());
        out.writeObject(getBatch(0).getModelBound());
        out.writeInt(_vertexBufferSize);
        out.writeInt(_normalBufferSize);
        out.writeInt(_colorBufferSize);
        out.writeInt(_textureBufferSize);
        out.writeInt(_indexBufferSize);
        out.writeObject(_textures);
        out.writeBoolean(_solid);
        out.writeBoolean(_transparent);
    }
    
    // documentation inherited from interface Externalizable
    public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        setName(in.readUTF());
        setLocalTranslation((Vector3f)in.readObject());
        setLocalRotation((Quaternion)in.readObject());
        setLocalScale((Vector3f)in.readObject());
        setModelBound((BoundingVolume)in.readObject());
        _vertexBufferSize = in.readInt();
        _normalBufferSize = in.readInt();
        _colorBufferSize = in.readInt();
        _textureBufferSize = in.readInt();
        _indexBufferSize = in.readInt();
        _textures = (String[])in.readObject();
        _solid = in.readBoolean();
        _transparent = in.readBoolean();
    }
    
    // documentation inherited from interface ModelSpatial
    public void expandModelBounds ()
    {
        // no-op
    }
    
    // documentation inherited from interface ModelSpatial
    public void setReferenceTransforms ()
    {
        // no-op
    }
    
    // documentation inherited from interface ModelSpatial
    public void lockStaticMeshes (
        Renderer renderer, boolean useVBOs, boolean useDisplayLists)
    {
        if (useVBOs && renderer.supportsVBO()) {
            VBOInfo vboinfo = new VBOInfo(true);
            vboinfo.setVBOIndexEnabled(true);
            setVBOInfo(vboinfo);
            
        } else if (useDisplayLists) {
            lockMeshes(renderer);
        }
    }
    
    // documentation inherited from interface ModelSpatial
    public void resolveTextures (TextureProvider tprov)
    {
        if (_textures == null) {
            return;
        }
        _tstates = new TextureState[_textures.length];
        for (int ii = 0; ii < _textures.length; ii++) {
            _tstates[ii] = tprov.getTexture(_textures[ii]);
        }
        if (_tstates[0] != null) {
            setRenderState(_tstates[0]);
        }
    }
    
    // documentation inherited from interface ModelSpatial
    public void writeBuffers (FileChannel out)
        throws IOException
    {
        if (_vertexBufferSize > 0) {
            _vertexByteBuffer.rewind();
            convertOrder(_vertexByteBuffer, ByteOrder.LITTLE_ENDIAN);
            out.write(_vertexByteBuffer);
            convertOrder(_vertexByteBuffer, ByteOrder.nativeOrder());
        }
        if (_normalBufferSize > 0) {
            _normalByteBuffer.rewind();
            convertOrder(_normalByteBuffer, ByteOrder.LITTLE_ENDIAN);
            out.write(_normalByteBuffer);
            convertOrder(_normalByteBuffer, ByteOrder.nativeOrder());
        }
        if (_colorBufferSize > 0) {
            _colorByteBuffer.rewind();
            convertOrder(_colorByteBuffer, ByteOrder.LITTLE_ENDIAN);
            out.write(_colorByteBuffer);
            convertOrder(_colorByteBuffer, ByteOrder.nativeOrder());
        }
        if (_textureBufferSize > 0) {
            _textureByteBuffer.rewind();
            convertOrder(_textureByteBuffer, ByteOrder.LITTLE_ENDIAN);
            out.write(_textureByteBuffer);
            convertOrder(_textureByteBuffer, ByteOrder.nativeOrder());
        }
        if (_indexBufferSize > 0) {
            _indexByteBuffer.rewind();
            convertOrder(_indexByteBuffer, ByteOrder.LITTLE_ENDIAN);
            out.write(_indexByteBuffer);
            convertOrder(_indexByteBuffer, ByteOrder.nativeOrder());
        }
    }
  
    // documentation inherited from interface ModelSpatial
    public void readBuffers (FileChannel in)
        throws IOException
    {
        ByteBuffer vbbuf = null, nbbuf = null, cbbuf = null, tbbuf = null,
            ibbuf = null;
        ByteOrder le = ByteOrder.LITTLE_ENDIAN;
        if (_vertexBufferSize > 0) {
            vbbuf = ByteBuffer.allocateDirect(_vertexBufferSize*4).order(le);
            in.read(vbbuf);
            vbbuf.rewind();
            convertOrder(vbbuf, ByteOrder.nativeOrder());
        }
        if (_normalBufferSize > 0) {
            nbbuf = ByteBuffer.allocateDirect(_normalBufferSize*4).order(le);
            in.read(nbbuf);
            nbbuf.rewind();
            convertOrder(nbbuf, ByteOrder.nativeOrder());
        }
        if (_colorBufferSize > 0) {
            cbbuf = ByteBuffer.allocateDirect(_colorBufferSize*4).order(le);
            in.read(cbbuf);
            cbbuf.rewind();
            convertOrder(cbbuf, ByteOrder.nativeOrder());
        }
        if (_textureBufferSize > 0) {
            tbbuf = ByteBuffer.allocateDirect(_textureBufferSize*4).order(le);
            in.read(tbbuf);
            tbbuf.rewind();
            convertOrder(tbbuf, ByteOrder.nativeOrder());
        }
        if (_indexBufferSize > 0) {
            ibbuf = ByteBuffer.allocateDirect(_indexBufferSize*4).order(le);
            in.read(ibbuf);
            ibbuf.rewind();
            convertOrder(ibbuf, ByteOrder.nativeOrder());
        }
        reconstruct(vbbuf, nbbuf, cbbuf, tbbuf, ibbuf);
    }
    
    // documentation inherited from interface ModelSpatial
    public void sliceBuffers (MappedByteBuffer map)
    {
        ByteBuffer vbbuf = null, nbbuf = null, cbbuf = null, tbbuf = null,
            ibbuf = null;
        int total = 0;
        if (_vertexBufferSize > 0) {
            int npos = map.position() + _vertexBufferSize*4;
            map.limit(npos);
            vbbuf = map.slice().order(ByteOrder.LITTLE_ENDIAN);
            map.position(npos);
        }
        if (_normalBufferSize > 0) {
            int npos = map.position() + _normalBufferSize*4;
            map.limit(npos);
            nbbuf = map.slice().order(ByteOrder.LITTLE_ENDIAN);
            map.position(npos);
        }
        if (_colorBufferSize > 0) {
            int npos = map.position() + _colorBufferSize*4;
            map.limit(npos);
            cbbuf = map.slice().order(ByteOrder.LITTLE_ENDIAN);
            map.position(npos);
        }
        if (_textureBufferSize > 0) {
            int npos = map.position() + _textureBufferSize*4;
            map.limit(npos);
            tbbuf = map.slice().order(ByteOrder.LITTLE_ENDIAN);
            map.position(npos);
        }
        if (_indexBufferSize > 0) {
            int npos = map.position() + _indexBufferSize*4;
            map.limit(npos);
            ibbuf = map.slice().order(ByteOrder.LITTLE_ENDIAN);
            map.position(npos);
        }
        reconstruct(vbbuf, nbbuf, cbbuf, tbbuf, ibbuf);
    }
    
    /**
     * Locks the transform and bounds of this mesh on the assumption that its
     * position will not change.
     */
    protected void lockInstance ()
    {
        lockBounds();
        _transformLocked = true;
    }
    
    /**
     * Imposes the specified order on the given buffer of 32 bit values.
     */
    protected static void convertOrder (ByteBuffer buf, ByteOrder order)
    {
        if (buf.order() == order) {
            return;
        }
        IntBuffer obuf = buf.asIntBuffer(),
            nbuf = buf.order(order).asIntBuffer();
        while (obuf.hasRemaining()) {
            nbuf.put(obuf.get());
        }
    }
    
    /**
     * Initializes the states shared between all models.  Requires an active
     * display.
     */
    protected static void initSharedStates ()
    {
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        _backCull = renderer.createCullState();
        _backCull.setCullMode(CullState.CS_BACK);
        _blendAlpha = renderer.createAlphaState();
        _blendAlpha.setBlendEnabled(true);
        _overlayZBuffer = renderer.createZBufferState();
        _overlayZBuffer.setFunction(ZBufferState.CF_LEQUAL);
        _overlayZBuffer.setWritable(false);
    }
    
    /** The sizes of the various buffers (zero for <code>null</code>). */
    protected int _vertexBufferSize, _normalBufferSize, _colorBufferSize,
        _textureBufferSize, _indexBufferSize;
    
    /** The backing byte buffers for the various buffers. */
    protected ByteBuffer _vertexByteBuffer, _normalByteBuffer,
        _colorByteBuffer, _textureByteBuffer, _indexByteBuffer;
    
    /** The type of bounding volume that this mesh should use. */
    protected int _boundingType;
    
    /** The name of this model's textures, or <code>null</code> for none. */
    protected String[] _textures;
    
    /** Whether or not this mesh can enable back-face culling. */
    protected boolean _solid;
    
    /** Whether or not this mesh must be rendered as transparent. */
    protected boolean _transparent;
    
    /** For prototype meshes, the resolved texture states. */
    protected TextureState[] _tstates;
    
    /** Whether or not the transform has been locked.  This operates in a
     * slightly different way than JME's locking, in that it allows applying
     * transformations to display lists. */
    protected boolean _transformLocked;
    
    /** The shared state for back face culling. */
    protected static CullState _backCull;
    
    /** The shared state for alpha blending. */
    protected static AlphaState _blendAlpha;
    
    /** The shared state for checking, but not writing to, the z buffer. */
    protected static ZBufferState _overlayZBuffer;
    
    private static final long serialVersionUID = 1;
}
