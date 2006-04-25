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

macroScript TRAnimationExporter category:"File" \
    buttonText:"Export Animation as XML..." toolTip:"Export Animation as XML" (
    
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
    
    -- Writes a single node transform
    fn writeTransform node outFile = (
        format "    <transform name=\"%\"" node.name to:outFile
        xform = node.transform
        if node.parent != undefined do (
            xform = xform * (inverse node.parent.transform)
        )
        writePoint3Attr " translation" xform.translationPart outFile
        writeQuatAttr " rotation" (inverse xform.rotationPart) outFile
        writePoint3Attr " scale" xform.scalePart outFile
        format "/>\n" to:outFile
    )
    
    -- Writes a single animation frame
    fn writeFrame nodes outFile = (
        format "  <frame>\n" to:outFile
        for node in nodes do (
            writeTransform node outFile
        )
        format "  </frame>\n\n" to:outFile
    )
    
    -- Writes animation to the named file
    fn writeAnimation fileName =
    (
        outFile = createfile fileName
        format "<?xml version=\"1.0\" standalone=\"yes\"?>\n\n" to:outFile
        format "<animation frameRate=\"%\">\n\n" frameRate to:outFile
        local nodes
        if selection.count > 0 then (
            nodes = selection
        ) else (
            nodes = objects
        )
        for i = animationRange.start to animationRange.end do at time i (
            writeFrame nodes outFile
        )
        format "</animation>\n" to:outFile
        close outFile
    )
    
    --
    -- Main entry point
    --
    
    -- Get the target filename
    persistent global xmlAnimFileName
    local fileName
    if (xmlAnimFileName == undefined) then (
        fileName = maxFilePath + (getFilenameFile maxFileName) + ".xml"
    ) else (
        fileName = xmlAnimFileName
    )
    fileName = getSaveFileName caption:"Select File to Export" \
        filename:fileName types:"XML Animations (*.XML)|*.xml|All|*.*"
    if fileName != undefined do (
        xmlAnimFileName = fileName
        writeAnimation fileName
    )
)
