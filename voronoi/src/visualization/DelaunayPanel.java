package visualization;

/*
 * Copyright (c) 2005, 2007 by L. Paul Chew.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;

import triangulation.DelaunayTriangulation;
import triangulation.Pnt;
import triangulation.Triangle;
import algorithm.VoronoiDiagram;


/**
 * The Delauany applet.
 *
 * Creates and displays a Delaunay Triangulation (DT) or a Voronoi Diagram
 * (VoD). Has a main program so it is an application as well as an applet.
 *
 * @author Paul Chew
 *
 * Created July 2005. Derived from an earlier, messier version.
 *
 * Modified December 2007. Updated some of the Triangulation methods. Added the
 * "Colorful" checkbox. Reorganized the interface between DelaunayAp and
 * DelaunayPanel. Added code to find a Voronoi cell.
 *
 * Modified by Frederico Sampaio at September 2013: 
 * Change the application for simulate distributed Voronoi algorithm.
*/
/**
 * Graphics Panel for DelaunayAp.
 * @param <VoronoiDiagramVisualization>
 */
@SuppressWarnings("serial")
class DelaunayPanel extends JPanel {

    public static Color voronoiColor = Color.white;
    public static Color delaunayColor = Color.gray;
    public static int pointRadius = 2;
    public static int mainPointRadius = 4;
    
    private DelaunayAp controller;              // Controller for DT
    private VisualVoronoiDiagram diagram;       // Delaunay triangulation
    private Map<Object, Color> colorTable;      // Remembers colors for display
    private Graphics g;                         // Stored graphics context
    private Random random = new Random();       // Source of random numbers
    /**
     * Create and initialize the DT.
     */
    public DelaunayPanel (DelaunayAp delaunayAp) {
        this.controller = delaunayAp;
        this.diagram = null;
        colorTable = new HashMap<Object, Color>();
    }

    
    
    public VoronoiDiagram getDiagram() {
        return diagram;
    }

    public void setDiagram(VisualVoronoiDiagram diagram) {
        VisualVoronoiDiagram oldDiagram = this.diagram;
        diagram.lock();
        try {
            if (oldDiagram != null) {
                oldDiagram.lock();
                oldDiagram.setPanel(null);
            }
            diagram.setPanel(this);
            this.diagram = diagram;
            repaint();
        } finally {
            diagram.unlock();
            if (oldDiagram != null)
                oldDiagram.unlock(); 
        }
    }

    /**
     * Get the color for the spcified item; generate a new color if necessary.
     * @param item we want the color for this item
     * @return item's color
     */
    private Color getColor (Object item) {
        if (colorTable.containsKey(item)) return colorTable.get(item);
        Color color = new Color(Color.HSBtoRGB(random.nextFloat(), 1.0f, 1.0f));
        colorTable.put(item, color);
        return color;
    }

    /* Basic Drawing Methods */

    /**
     * Draw a point.
     * @param point the Pnt to draw
     */
    public void draw (Pnt point) {
        int r = pointRadius;
        int x = (int) point.coord(0);
        int y = (int) point.coord(1);
        g.fillOval(x-r, y-r, r+r, r+r);
    }

    /**
     * Draw a circle.
     * @param center the center of the circle
     * @param radius the circle's radius
     * @param fillColor null implies no fill
     */
    public void draw (Pnt center, double radius, 
            Color fillColor, Color lineColor) {
        int x = (int) center.coord(0);
        int y = (int) center.coord(1);
        int r = (int) radius;
        if (fillColor != null) {
            Color temp = g.getColor();
            g.setColor(fillColor);
            g.fillOval(x-r, y-r, r+r, r+r);
            g.setColor(temp);
        }
        if (lineColor != null) {
            Color temp = g.getColor();
            g.setColor(lineColor);
            g.drawOval(x-r, y-r, r+r, r+r);
            g.setColor(temp);
        } else 
            g.drawOval(x-r, y-r, r+r, r+r);
    }

    /**
     * Draw a polygon.
     * @param polygon an array of polygon vertices
     * @param fillColor null implies no fill
     */
    public void draw (Pnt[] polygon, Color fillColor, Color lineColor) {
        int[] x = new int[polygon.length];
        int[] y = new int[polygon.length];
        for (int i = 0; i < polygon.length; i++) {
            x[i] = (int) polygon[i].coord(0);
            y[i] = (int) polygon[i].coord(1);
        }
        if (fillColor != null) {
            Color temp = g.getColor();
            g.setColor(fillColor);
            g.fillPolygon(x, y, polygon.length);
            g.setColor(temp);
        }
        if (lineColor != null) {
            Color temp = g.getColor();
            g.setColor(lineColor);
            g.drawPolygon(x, y, polygon.length);
            g.setColor(temp);
        } else 
            g.drawPolygon(x, y, polygon.length);
    }

    /* Higher Level Drawing Methods */

    /**
     * Handles painting entire contents of DelaunayPanel.
     * Called automatically; requested via call to repaint().
     * @param g the Graphics context
     */
    public void paintComponent (Graphics g) {
        super.paintComponent(g);
        if (diagram == null) {
            Color temp = g.getColor();
            g.setColor(this.getBackground());
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.setColor(temp);
            return;
        }
        this.g = g;
        diagram.lock();  // block until condition holds
        try {
            controller.refreshDiagramControl();
            // Flood the drawing area with a "background" color
            Color temp = g.getColor();
            if (!controller.isVoronoi()) 
                g.setColor(delaunayColor);
            else if (diagram.hasInitialTriangle()) 
                g.setColor(this.getBackground());
            else 
                g.setColor(voronoiColor);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.setColor(temp);
            
            // If no colors then we can clear the color table
            if (!controller.isColorful()) colorTable.clear();
            
            // Draw the appropriate picture
            if (controller.isVoronoi())
                drawAllVoronoi(controller.isColorful(), true);
            else drawAllDelaunay(controller.isColorful());
            
            // Draw any extra info due to the mouse-entry switches
            temp = g.getColor();
            g.setColor(Color.RED);
            if (controller.showingCircles()) drawAllCircles();
            if (controller.showingDelaunay()) drawAllDelaunay(false);
            if (controller.showingVoronoi()) drawAllVoronoi(false, false);
            g.setColor(temp);
            if (diagram.getMainSite() != null)
                draw (diagram.getMainSite(), mainPointRadius, Color.RED, null);
        } finally {
            diagram.unlock();
        }
    }

    /**
     * Draw all the Delaunay triangles.
     * @param withFill true iff drawing Delaunay triangles with fill colors
     */
    private void drawAllDelaunay (boolean withFill) {
        for (Triangle triangle : diagram.getTriangulation()) {
            Pnt[] vertices = triangle.toArray(new Pnt[0]);
            draw(vertices, withFill? getColor(triangle) : null, Color.green);
        }
    }

    /**
     * Draw all the Voronoi cells.
     * @param withFill true iff drawing Voronoi cells with fill colors
     * @param withSites true iff drawing the site for each Voronoi cell
     */
    private void drawAllVoronoi (boolean withFill, boolean withSites) {
        // Keep track of sites done; no drawing for initial triangles sites
        HashSet<Pnt> done = new HashSet<Pnt>(diagram.getInitialTriangle());
        DelaunayTriangulation dt = diagram.getTriangulation();
        for (Triangle triangle : dt) {
            for (Pnt site: triangle) {
                if (done.contains(site)) continue;
                done.add(site);
                List<Triangle> list = dt.surroundingTriangles(site, triangle);
                Pnt[] vertices = new Pnt[list.size()];
                int i = 0;
                for (Triangle tri: list) {
                    vertices[i++] = tri.getCircumcenter();
                } 
                draw(vertices, withFill? getColor(site) : null, null);
                if (withSites) { 
                    if (diagram.numSitesIn(site) > 1) { 
                        draw(site, mainPointRadius + 2, Color.CYAN, Color.BLUE);
                    }
                    else 
                        draw(site);
                }
            }
        }
    }
    
    /**
     * Draw all the empty circles (one for each triangle) of the DT.
     */
    private void drawAllCircles () {
        // Loop through all triangles of the DT
        for (Triangle triangle: diagram.getTriangulation()) {
            // Skip circles involving the initial-triangle vertices
            if (triangle.containsAny(diagram.getInitialTriangle())) continue;
            Pnt c = triangle.getCircumcenter();
            double radius = c.subtract(triangle.get(0)).magnitude();
            draw(c, radius, null, Color.cyan);
        }
    }

}
