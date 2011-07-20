/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.swing;

import java.awt.Rectangle;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.swing.event.MapPaneEvent.Type;

import org.geotools.swing.testutils.MockRenderer;
import org.geotools.swing.testutils.WaitingMapPaneListener;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for JMapPane methods that can run in a headless build.
 * 
 * @author Michael Bedward
 * @since 8.0
 * @source $URL$
 * @version $Id$
 */
public class JMapPaneHeadlessTest {
    private static final long WAIT_TIMEOUT = 500;
    
    private static final double TOL = 1.0e-6;
    
    private static final ReferencedEnvelope WORLD = 
            new ReferencedEnvelope(-10, 10, -5, 5, DefaultEngineeringCRS.CARTESIAN_2D);
    
    private static final Rectangle PANE = new Rectangle(100, 100);
    
    private JMapPane mapPane;
    private WaitingMapPaneListener listener;
    
    @Before
    public void setup() {
        mapPane = new JMapPane();
        listener = new WaitingMapPaneListener();
    }
    
    @Test
    public void defaultRenderingExecutorCreated() {
        RenderingExecutor executor = mapPane.getRenderingExecutor();
        assertNotNull(executor);
        assertTrue(executor instanceof SingleTaskRenderingExecutor);
    }
    
    @Test
    public void settingRendererLinksToMapContent() {
        MapContent mapContent = new MapContent();
        mapPane.setMapContent(mapContent);
        
        GTRenderer renderer = new MockRenderer();
        mapPane.setRenderer(renderer);
        
        assertTrue(renderer.getMapContent() == mapContent);
    }
    
    @Test
    public void settingMapContentLinksToRenderer() {
        GTRenderer renderer = new MockRenderer();
        mapPane.setRenderer(renderer);
        
        MapContent mapContent = new MapContent();
        mapPane.setMapContent(mapContent);
        
        assertTrue(renderer.getMapContent() == mapContent);
    }
    
    @Test
    public void setRendererEvent() {
        mapPane.addMapPaneListener(listener);
        listener.setExpected(Type.NEW_RENDERER);
        
        GTRenderer renderer = new MockRenderer();
        mapPane.setRenderer(renderer);
        
        assertTrue(listener.await(Type.NEW_RENDERER, WAIT_TIMEOUT));
    }
    
    @Test
    public void setMapContentEvent() {
        mapPane.addMapPaneListener(listener);
        listener.setExpected(Type.NEW_MAPCONTENT);
        
        MapContent mapContent = new MapContent();
        mapPane.setMapContent(mapContent);
        
        assertTrue(listener.await(Type.NEW_MAPCONTENT, WAIT_TIMEOUT));
    }
    
    @Test
    public void setDisplayArea_WithMapContentSet() {
        MapContent mapContent = new MapContent();
        mapPane.setMapContent(mapContent);
        mapPane.setDisplayArea(WORLD);
        assertDisplayArea(WORLD, mapPane.getDisplayArea());
    }
    
    @Test
    public void setDisplayArea_NoMapContentSet() {
        mapPane.setDisplayArea(WORLD);
        assertDisplayArea(WORLD, mapPane.getDisplayArea());
    }
    
    @Test
    public void getDisplayArea_NoAreaSet() {
        assertTrue(mapPane.getDisplayArea().isEmpty());
    }

    /**
     * Compare requested display area (world bounds) to realized display area.
     * 
     * @param requestedArea requested area
     * @param realizedArea  realized area
     */
    private void assertDisplayArea(ReferencedEnvelope requestedArea, ReferencedEnvelope realizedArea) {
        // realized area should not be empty
        assertFalse(realizedArea.isEmpty());
        
        // realized area should cover the requested area
        assertTrue(mapPane.getDisplayArea().covers(requestedArea));
        
        // realized area should have the same centre coordinates as the
        // requested area
        assertEquals(realizedArea.getMedian(0), requestedArea.getMedian(0), TOL);
        assertEquals(realizedArea.getMedian(1), requestedArea.getMedian(1), TOL);
    }
}