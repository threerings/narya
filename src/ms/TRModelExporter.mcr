--
-- $Id$
--
-- Narya library - tools for developing networked games
-- Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
-- http://www.threerings.net/code/narya/
--
-- This library is free software; you can redistribute it and/or modify it
-- under the terms of the GNU Lesser General Public License as published
-- by the Free Software Foundation; either version 2.1 of the License, or
-- (at your option) any later version.
--
-- This library is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
-- Lesser General Public License for more details.
--
-- You should have received a copy of the GNU Lesser General Public
-- License along with this library; if not, write to the Free Software
-- Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

macroScript TRModelExporter category:"File" \
    buttonText:"Export Model as XML..." toolTip:"Export Model as XML" (
    
    -- Writes a point3-valued attribute to the file
    fn writePoint3Attr attr p outFile =
    (
        format "%=\"%, %, %\"" attr p.x p.y p.z to:outFile
    )
    
    -- Writes a quat-valued attribute to the file
    fn writeQuatAttr attr q outFile =
    (
        format "%=\"%, %, %, %\"" attr q.x q.y q.z q.w to:outFile
    )
    
    -- Writes a skinned vertex's bone weights to the file
    fn writeSkinBoneWeights skin vidx outFile =
    (
        weightCount = skinOps.getVertexWeightCount skin vidx
        for i = 1 to weightCount do (
            bone = skinOps.getBoneName skin (skinOps.getVertexWeightBoneID \
                skin vidx i) 0
            weight = skinOps.getVertexWeight skin vidx i
            format "      <boneWeight bone=\"%\" weight=\"%\"/>\n" bone \
                weight to:outFile
        )
    )
    
    -- Writes a physiqued vertex's bone weights to the file
    fn writePhysiqueBoneWeights node vidx outFile =
    (
        boneCount = physiqueOps.getVertexBoneCount node vidx
        for i = 1 to boneCount do (
            bone = physiqueOps.getVertexBone node vidx i
            weight = physiqueOps.getVertexWeight node vidx i
            format "      <boneWeight bone=\"%\" weight=\"%\"/>\n" bone.name \
                weight to:outFile
        )
    )
    
    -- Writes a mesh's vertices to the file
    fn writeVertices mesh outFile =
    (
        for i = 1 to mesh.numfaces do (
            face = getFace mesh i
            local tvface
            if mesh.numtverts > 0 do (
                tvface = getTVFace mesh i
            )
            for i = 1 to 3 do in coordsys local (
                format "    <vertex" to:outFile
                writePoint3Attr " location" (getVert mesh face[i]) outFile
                writePoint3Attr " normal" (getNormal mesh face[i]) outFile
                if tvface != undefined do (
                    tvert = getTVert mesh tvface[i]
                    format " tcoords=\"%, %\"" tvert[1] tvert[2] to:outFile
                )
                if isProperty mesh #skin or isProperty mesh #physique then (
                    format ">\n" to:outFile
                    if isProperty mesh #skin then (
                        writeSkinBoneWeights mesh.skin face[i] outFile
                    ) else (
                        writePhysiqueBoneWeights mesh face[i] outFile
                    )
                    format "    </vertex>\n" to:outFile
                ) else (
                    format "/>\n" to:outFile
                )
            )
        )
    )
    
    -- Writes a node to the file
    fn writeNode node outFile =
    (
        local kind
        isMesh = false
        if isKindOf node Editable_Mesh then (
            isMesh = true
            if isProperty node #skin or isProperty node #physique then (
                kind = "skinMesh"
            ) else (
                kind = "triMesh"
            )
        ) else (
            kind = "node"
        )
        format "  <% name=\"%\"" kind node.name to:outFile
        xform = node.transform
        if node.parent != undefined do (
            xform = xform * (inverse node.parent.transform)
            format " parent=\"%\"" node.parent.name to:outFile
        )
        if isMesh do (
            xform = preTranslate xform node.objectOffsetPos
            xform = preRotate xform node.objectOffsetRot
            xform = preScale xform node.objectOffsetScale
        )
        writePoint3Attr " translation" xform.translationPart \
            outFile
        writeQuatAttr " rotation" (inverse xform.rotationPart) \
            outFile
        writePoint3Attr " scale" xform.scalePart outFile
        if isMesh then (
            format ">\n" to:outFile
            if isProperty node #skin do (
                setCommandPanelTaskMode mode:#modify
                modPanel.setCurrentObject node.skin node:node
            )
            writeVertices node outFile
            format "  </%>\n\n" kind to:outFile
            
        ) else (
            format "/>\n\n" to:outFile
        )
    )
    
    -- Writes the model to the named file
    fn writeModel fileName =
    (
        outFile = createfile fileName
        format "<?xml version=\"1.0\" standalone=\"yes\"?>\n\n" to:outFile
        format "<model>\n\n" to:outFile
        local nodes
        oldsel = selection as array
        if selection.count > 0 then (
            nodes = selection
        ) else (
            nodes = objects
        )
        for node in nodes do (
            writeNode node outFile
        )
        select oldsel
        format "</model>\n" to:outFile
        close outFile
    )
    
    --
    -- Main entry point
    --
    
    -- Get the target filename
    persistent global xmlModelFileName
    local fileName
    if (xmlModelFileName == undefined) then (
        fileName = maxFilePath + (getFilenameFile maxFileName) + ".xml"
    ) else (
        fileName = xmlModelFileName
    )
    fileName = getSaveFileName caption:"Select File to Export" \
        filename:fileName types:"XML Models (*.XML)|*.xml|All|*.*"
    if fileName != undefined do (
        xmlModelFileName = fileName
        writeModel fileName
    )
)
