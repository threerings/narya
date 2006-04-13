//
// $Id: SceneParser.java 3749 2005-11-09 04:00:16Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.jme.tools.xml;

import java.io.FileInputStream;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;

import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.jme.tools.ModelDef;

/**
 * Parses XML files containing 3D models.
 */
public class ModelParser
{
    public ModelParser ()
    {
        // create and configure our digester
        _digester = new Digester();
        
        // add the rules
        String model = "model";
        _digester.addObjectCreate(model, ModelDef.class.getName());
        _digester.addSetNext(model, "setModel", ModelDef.class.getName());
        
        String tmesh = model + "/triMesh";
        _digester.addObjectCreate(tmesh, ModelDef.TriMeshDef.class.getName());
        _digester.addRule(tmesh, new SetPropertyFieldsRule());
        _digester.addSetNext(tmesh, "addSpatial",
            ModelDef.SpatialDef.class.getName());
        
        String smesh = model + "/skinMesh";
        _digester.addObjectCreate(smesh,
            ModelDef.SkinMeshDef.class.getName());
        _digester.addRule(smesh, new SetPropertyFieldsRule());
        _digester.addSetNext(smesh, "addSpatial",
            ModelDef.SpatialDef.class.getName());
        
        String node = model + "/node";
        _digester.addObjectCreate(node, ModelDef.NodeDef.class.getName());
        _digester.addRule(node, new SetPropertyFieldsRule());
        _digester.addSetNext(node, "addSpatial",
            ModelDef.SpatialDef.class.getName());
        
        String bnode = model + "/boneNode";
        _digester.addObjectCreate(bnode, ModelDef.BoneNodeDef.class.getName());
        _digester.addRule(bnode, new SetPropertyFieldsRule());
        _digester.addSetNext(bnode, "addSpatial",
            ModelDef.SpatialDef.class.getName());
        
        String vertex = tmesh + "/vertex", svertex = smesh + "/vertex";
        _digester.addObjectCreate(vertex, ModelDef.Vertex.class.getName());
        _digester.addObjectCreate(svertex,
            ModelDef.SkinVertex.class.getName());
        _digester.addRule(vertex, new SetPropertyFieldsRule());
        _digester.addRule(svertex, new SetPropertyFieldsRule());
        _digester.addSetNext(vertex, "addVertex",
            ModelDef.Vertex.class.getName());
        _digester.addSetNext(svertex, "addVertex",
            ModelDef.Vertex.class.getName());
            
        String bweight = smesh + "/vertex/boneWeight";
        _digester.addObjectCreate(bweight,
            ModelDef.BoneWeight.class.getName());
        _digester.addRule(bweight, new SetPropertyFieldsRule());
        _digester.addSetNext(bweight, "addBoneWeight",
            ModelDef.BoneWeight.class.getName());
    }
    
    /**
     * Parses the XML file at the specified path into a model definition.
     */
    public ModelDef parseModel (String path)
        throws IOException, SAXException
    {
        _model = null;
        _digester.push(this);
        _digester.parse(new FileInputStream(path));
        return _model;
    }
    
    /**
     * Called by the parser once the model is parsed.
     */
    public void setModel (ModelDef model)
    {
        _model = model;
    }
    
    protected Digester _digester;
    protected ModelDef _model;
}
