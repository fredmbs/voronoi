/**
 * Distributed Voronoi Diagram
 *
 *  @author Frederico Martins Biber Sampaio
 *
 * The MIT License (MIT)
 * 
 * Copyright (C) 2013  Frederico Martins Biber Sampaio
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE. 
*/
package algorithm;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import triangulation.DelaunayTriangulation;
import triangulation.Pnt;
import triangulation.Triangle;

/**
 * Voroni diagram control for multi-thread application
 */
public class VoronoiDiagram {

    private DelaunayTriangulation dt;           // Delaunay triangulation
    private Triangle initialTriangle;           // Initial triangle
    private static int initialSize = 10000;     // Size of initial triangle
    private Pnt mainSite = null;
    // Control site in same location
    // In physical system, site with same position can happen 
    // due to sensor imprecision.
    // However, in the the Delaunay/Voronoi model this can't happen.
    // To overcome this limitation, this class controls sites per position.
    // Besides make a interface (facade) with diagram application,
    // this control is the main purpose of this class.
    private HashMap<Pnt, HashSet<Site>> points;
    private String id;

    /**
     * Create and initialize the DT.
     */
    public VoronoiDiagram(String id) {
        initialTriangle = new Triangle(
                new Pnt(-initialSize, -initialSize),
                new Pnt( initialSize, -initialSize),
                new Pnt(           0,  initialSize));
        points = new HashMap<Pnt, HashSet<Site>>();
        dt = new DelaunayTriangulation(initialTriangle);
        this.id = id;
    }

    @Override
    public String toString() {
        return "Process " + id + " @ " + mainSite;
    }

    public DelaunayTriangulation getTriangulation() {
        return dt;
    }
    
    public void setMainSite(Site main) {
        Pnt point = new Pnt(main.getPos().getX(), main.getPos().getY());
        mainSite = point;
    }

    public void setMainSite(Pnt point) {
        mainSite = point;
    }

    public Pnt getMainSite() {
        return mainSite;
    }

    public Triangle getInitialTriangle() {
        return initialTriangle;
    }

    public boolean hasInitialTriangle() {
        return dt.contains(initialTriangle);
    }
    
    /**
     * CUSTON: add new Voronoi site.
     */
    public boolean addSite(Site site) {
        Pnt point = new Pnt(site.getPos().getX(), site.getPos().getY());
        if (newPoint(point, site)) 
            return dt.delaunayPlace(point);;
        return true;
    }

    /**
     * CUSTON: add new Voronoi site.
     */
    public boolean addRelevantSite(Site site) {
        Pnt point = new Pnt(site.getPos().getX(), site.getPos().getY());
        Triangle triangle = dt.locateTriangleOf(mainSite, point);
        if (triangle != null) {
            if (newPoint(point, site)) 
                return dt.delaunayPlace(point, triangle);
            return true;
        }
        return false;
    }

    /**
     * CUSTON: del Voronoi site.
     */
    public boolean delSite(Site site, Location2D pos) {
        Pnt point = new Pnt(pos.getX(), pos.getY());
        if (delPoint(point, site)) 
            return dt.delaunayRemove(point);
        return true;
    }

    /**
     * CUSTON: del all Voronoi sites far form this point.
     */
    public ArrayList<Pnt> delFarFromMainSite() {
        return delFarFrom(mainSite);
    }

    // try to add new site to diagram by his position
    private boolean newPoint(Pnt point, Site site) {
        if (points.containsKey(point)) {
            points.get(point).add(site);
            return false; //points.get(site.getPos()).size() == 1;
        }
        else {
            HashSet<Site> inThisPos = new HashSet<Site>(); 
            inThisPos.add(site);
            points.put(point, inThisPos);
            return true;
        }
    }
    
    // try to remove site from specified position
    private boolean delPoint(Pnt point, Site site) {
        if (points.containsKey(point)) {
            points.get(point).remove(site);
            if (points.get(point).isEmpty()) { 
                points.remove(point);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all sites far from main site
     * @param main the main site 
     */
    private ArrayList<Pnt> delFarFrom(Pnt main) {
        mainSite = main;
        ArrayList<Pnt> deleted = dt.delaunayRemoveFarFrom(main);
        for (Pnt point: deleted)
            points.remove(point);
        return deleted;
    }
    
    /**
     * 
     */
    public int numSites() {
        return points.size();
    }

    /**
     * 
     */
    public int numSitesIn(Pnt point) {
        if (points.containsKey(point)) 
            return points.get(point).size();
        return 0;
    }

}
