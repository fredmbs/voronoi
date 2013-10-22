package triangulation;

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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * A 2D Delaunay Triangulation (DT) with incremental site insertion.
 *
 * This is not the fastest way to build a DT, but it's a reasonable way to build
 * a DT incrementally and it makes a nice interactive display. There are several
 * O(n log n) methods, but they require that the sites are all known initially.
 *
 * A Triangulation is a Set of Triangles. A Triangulation is unmodifiable as a
 * Set; the only way to change it is to add sites (via delaunayPlace).
 *
 * @author Paul Chew
 *
 * Created July 2005. Derived from an earlier, messier version.
 *
 * Modified November 2007. Rewrote to use AbstractSet as parent class and to use
 * the Graph class internally. Tried to make the DT algorithm clearer by
 * explicitly creating a cavity.  Added code needed to find a Voronoi cell.
 * 
 * Modified by Frederico Sampaio at September 2013: 
 * Remove site method for simulate distributed Voronoi algorithm.
 *
 */
public class DelaunayTriangulation extends AbstractSet<Triangle> {

    private Triangle mostRecent = null;      // Most recently "active" triangle
    private Graph<Triangle> triGraph;        // Holds triangles for navigation
    Triangle initialTriangle = null;
    //private Pnt mainSite = null;                      // The Main site 
    //private List<TriangleControl> mainControl = null;  // Main site control
    private Pnt [] nullPnt = new Pnt[0];
    

    private class TriangleControl {
        public Triangle triangle;
        public Pnt point;
        public boolean newTriangle;
        TriangleControl(Triangle triangle, Pnt point) {
            this.triangle = triangle;
            this.point = point;
            this.newTriangle = false;
        }
    }

    /**
     * All sites must fall within the initial triangle.
     * @param triangle the initial triangle
     */
    public DelaunayTriangulation (Triangle triangle) {
        triGraph = new Graph<Triangle>();
        triGraph.add(triangle);
        mostRecent = triangle;
        initialTriangle = triangle;
    }

    public void print() {
        System.out.println("--------------------------------");
        for (Triangle tri: this) {
            System.out.print("[");
            for (Pnt vertex: tri) {
                System.out.print(vertex);
                System.out.print(",");
            }
            System.out.println("]");
        }
    }
    /* The following two methods are required by AbstractSet */

    @Override
    public Iterator<Triangle> iterator () {
        return triGraph.nodeSet().iterator();
    }

    @Override
    public int size () {
        return triGraph.nodeSet().size();
    }

    @Override
    public String toString () {
        return "Triangulation with " + size() + " triangles";
    }

    /**
     * True iff triangle is a member of this triangulation.
     * This method isn't required by AbstractSet, but it improves efficiency.
     * @param triangle the object to check for membership
     */
    public boolean contains (Object triangle) {
        return triGraph.nodeSet().contains(triangle);
    }

    /**
     * Report neighbor opposite the given vertex of triangle.
     * @param site a vertex of triangle
     * @param triangle we want the neighbor of this triangle
     * @return the neighbor opposite site in triangle; null if none
     * @throws IllegalArgumentException if site is not in this triangle
     */
    public Triangle neighborOpposite (Pnt site, Triangle triangle) {
        if (!triangle.contains(site))
            return null;
            //throw new IllegalArgumentException("Bad vertex; not in triangle");
        for (Triangle neighbor: triGraph.neighbors(triangle)) {
            if (!neighbor.contains(site)) return neighbor;
        }
        return null;
    }

    /**
     * Return the set of triangles adjacent to triangle.
     * @param triangle the triangle to check
     * @return the neighbors of triangle
     */
    public Set<Triangle> neighbors(Triangle triangle) {
        return triGraph.neighbors(triangle);
    }

    /**
     * Report triangles surrounding site in order (cw or ccw).
     * @param site we want the surrounding triangles for this site
     * @param triangle a "starting" triangle that has site as a vertex
     * @return all triangles surrounding site in order (cw or ccw)
     * @throws IllegalArgumentException if site is not in triangle
     */
    public List<Triangle> surroundingTriangles (Pnt site, Triangle triangle) {
        if (!triangle.contains(site))
            throw new IllegalArgumentException("Site not in triangle");
        List<Triangle> list = new ArrayList<Triangle>();
        Triangle start = triangle;
        Pnt guide = triangle.nextVertex(site);        // Affects cw or ccw
        while (triangle != null) {
            list.add(triangle);
            Triangle previous = triangle;
            triangle = this.neighborOpposite(guide, triangle); // Next triangle
            guide = previous.nextVertex(site, guide);     // Update guide
            if (triangle == start) break;
        }
        return list;
    }

    /**
     * Report triangles and points surrounding site in order (cw or ccw).
     * @param site we want the surrounding triangles for this site
     * @param triangle a "starting" triangle that has site as a vertex
     * @return all triangles/points surrounding site in order (cw or ccw)
     * @throws IllegalArgumentException if site is not in triangle
     */
    public List<TriangleControl> surroundingTrianglesControl (Pnt site, 
            Triangle triangle) {
        if (!triangle.contains(site))
            throw new IllegalArgumentException("Site not in triangle");
        List<TriangleControl> list = new ArrayList<TriangleControl>();
        Triangle start = triangle;
        Pnt guide = triangle.nextVertex(site);        // Affects cw or ccw
        while (triangle != null) {
            list.add(new TriangleControl(triangle, guide));
            Triangle previous = triangle;
            triangle = this.neighborOpposite(guide, triangle); // Next triangle
            guide = previous.nextVertex(site, guide);     // Update guide
            if (triangle == start) break;
        }
        return list;
    }

    /**
     * Locate the triangle with point inside it or on its boundary.
     * @param point the point to locate
     * @return the triangle that holds point; null if no such triangle
     */
    public Triangle locate (Pnt point) {
        Triangle triangle = mostRecent;
        if (!this.contains(triangle)) triangle = null;

        // Try a directed walk (this works fine in 2D, but can fail in 3D)
        Set<Triangle> visited = new HashSet<Triangle>();
        while (triangle != null) {
            if (visited.contains(triangle)) { // This should never happen
                //System.out.println("Warning: Caught in a locate loop");
                break;
            }
            visited.add(triangle);
            // Corner opposite point
            Pnt corner = point.isOutside(triangle.toArray(nullPnt));
            if (corner == null) return triangle;
            triangle = this.neighborOpposite(corner, triangle);
        }
        // No luck; try brute force
        //System.out.println("Warning: Checking all triangles for " + point);
        for (Triangle tri: this) {
            if (point.isOutside(tri.toArray(nullPnt)) == null) return tri;
        }
        // No such triangle
        //System.out.println("Warning: No triangle holds " + point);
        return null;
    }

    /**
     * Locate the triangle with point as vertex
     * @param point the vertex to locate
     * @return the triangle that contains vertex; null if no such triangle
     */
    public Triangle locate_by_vertex (Pnt point) {
        Triangle triangle = mostRecent;
        if (!this.contains(triangle)) triangle = null;

        // Try a directed walk (this works fine in 2D, but can fail in 3D)
        Set<Triangle> visited = new HashSet<Triangle>();
        while (triangle != null) {
            if (triangle.contains(point)) return triangle;
            if (visited.contains(triangle)) { // This should never happen
                //System.out.println("Warning: Caught in a locate loop");
                break;
            }
            visited.add(triangle);
            // Corner opposite point
            Pnt corner = point.isOutside(triangle.toArray(nullPnt));
            if (corner == null) break;
            triangle = this.neighborOpposite(corner, triangle);
        }
        // No luck; try brute force
        //System.out.println("Warning: Checking all triangles for " + point);
        for (Triangle tri: this) {
            if (tri.contains(point)) return tri;
        }
        // No such triangle
        //System.out.println("Warning: No triangle contains " + point);
        return null;
    }

    /**
     * Place a new site into the DT.
     * Nothing happens if the site matches an existing DT vertex.
     * @param site the new Pnt
     * @return true, if site was placed
     * @throws IllegalArgumentException if site does not lie in any triangle
     */
    public boolean delaunayPlace (Pnt site) {
        // Locate containing triangle and try to place the site
        return delaunayPlace(site, locate(site));
    }

    /**
     * Check if one site affects a Voronoi cell of another site. 
     * In other words, check if it's modify the triangulation of "main site" 
     * @param mainSite an existing site
     * @param site the new Pnt
     * @return the triangle that holds point; null if no such triangle
     * @throws IllegalArgumentException if site does not lie in any triangle
     */
    public Triangle locateTriangleOf (Pnt mainSite, Pnt site) {
        Triangle mainTriangle = locate_by_vertex(mainSite);
        List<TriangleControl> mainControl = 
                surroundingTrianglesControl(mainSite, mainTriangle);
        for (TriangleControl tc: mainControl) 
            if (site.vsCircumcircle(tc.triangle.toArray(nullPnt)) == -1)
                return tc.triangle;
        return null;
    }

    /**
     * Remove all sites that not affect the triangulation of "main site" 
     * @param mainSite an existing site
     * @return number of removed sites
     */
    public ArrayList<Pnt> delaunayRemoveFarFrom (Pnt mainSite) {
        Triangle mainTriangle = locate_by_vertex(mainSite);
        List<TriangleControl> mainControl = 
                surroundingTrianglesControl(mainSite, mainTriangle);
        // transform List to HashSet to simplify and accelerate 
        HashSet<Triangle> relevantTriangles = new HashSet<Triangle>();
        HashSet<Pnt> relevantPoints = new HashSet<Pnt>();
        relevantTriangles.add(initialTriangle);
        relevantPoints.addAll(initialTriangle);
        for (TriangleControl tc: mainControl) {
            relevantTriangles.add(tc.triangle);
            relevantPoints.add(tc.point);
        }
        ArrayList<Pnt> removed = new ArrayList<>();
        for (Triangle triangle: this) {
            if (relevantTriangles.contains(triangle)) 
                    continue;
            for (Pnt site: triangle) {
                if (!relevantPoints.contains(site)) {
                    removed.add(site);
                }
            }
        }
        for (Pnt site: removed) {
            delaunayRemove(site);
        }
        return removed;
    }

    /**
     * Place a new site into the DT.
     * Nothing happens if the site matches an existing DT vertex.
     * @param site the new Pnt
     * @param triangle the triangle that holds point 
     * @return true, if site was placed
     * @throws IllegalArgumentException if site does not lie in any triangle
     */
    public boolean delaunayPlace (Pnt site, Triangle triangle) {
        // Uses straightforward scheme rather than best asymptotic time

        // Give up if no containing triangle or if site is already in DT
        if (triangle == null)
            throw new IllegalArgumentException("No containing triangle");
        if (triangle.contains(site)) return false;

        // Determine the cavity and update the triangulation
        Set<Triangle> cavity = getCavity(site, triangle);
        mostRecent = update(site, cavity);
        return true;
    }

    /**
     * Remove a site from DT.
     * Nothing happens if site don't exists.
     * @param site the Pnt to remove
     * @throws IllegalArgumentException if site does not belong to any triangle
     */
    public boolean delaunayRemove (Pnt site) {
        // Uses straightforward scheme rather than best asymptotic time

        // Locate containing triangle
        Triangle triangle = locate_by_vertex(site);
        // Give up if no containing triangle 
        if (triangle == null)
            return false;

        // Determine the cavity and update the triangulation
        List<TriangleControl> cavity = 
                this.surroundingTrianglesControl(site, triangle);
        mostRecent = remove(site, cavity);
        return true;
    }

    /**
     * Determine the cavity caused by new site.
     * @param site the site causing the cavity
     * @param triangle the triangle containing site
     * @return set of all triangles that have site in their circumcircle
     */
    private Set<Triangle> getCavity (Pnt site, Triangle triangle) {
        Set<Triangle> encroached = new HashSet<Triangle>();
        Queue<Triangle> toBeChecked = new LinkedList<Triangle>();
        Set<Triangle> marked = new HashSet<Triangle>();
        toBeChecked.add(triangle);
        marked.add(triangle);
        while (!toBeChecked.isEmpty()) {
            triangle = toBeChecked.remove();
            if (site.vsCircumcircle(triangle.toArray(nullPnt)) == 1)
                continue; // Site outside triangle => triangle not in cavity
            encroached.add(triangle);
            // Check the neighbors
            for (Triangle neighbor: triGraph.neighbors(triangle)){
                if (marked.contains(neighbor)) continue;
                marked.add(neighbor);
                toBeChecked.add(neighbor);
            }
        }
        return encroached;
    }

    /**
     * Update the triangulation by removing the cavity triangles and then
     * filling the cavity with new triangles.
     * @param site the site that created the cavity
     * @param cavity the triangles with site in their circumcircle
     * @return one of the new triangles
     */
    private Triangle update (Pnt site, Set<Triangle> cavity) {
        Set<Set<Pnt>> boundary = new HashSet<Set<Pnt>>();
        Set<Triangle> theTriangles = new HashSet<Triangle>();

        // Find boundary facets and adjacent triangles
        for (Triangle triangle: cavity) {
            theTriangles.addAll(neighbors(triangle));
            for (Pnt vertex: triangle) {
                Set<Pnt> facet = triangle.facetOpposite(vertex);
                if (boundary.contains(facet)) boundary.remove(facet);
                else boundary.add(facet);
            }
        }
        theTriangles.removeAll(cavity);        // Adj triangles only

        // Remove the cavity triangles from the triangulation
        for (Triangle triangle: cavity) triGraph.remove(triangle);

        // Build each new triangle and add it to the triangulation
        Set<Triangle> newTriangles = new HashSet<Triangle>();
        for (Set<Pnt> vertices: boundary) {
            vertices.add(site);
            Triangle tri = new Triangle(vertices);
            triGraph.add(tri);
            newTriangles.add(tri);
        }

        // Update the graph links for each new triangle
        theTriangles.addAll(newTriangles);    // Adj triangle + new triangles
        for (Triangle triangle: newTriangles)
            for (Triangle other: theTriangles)
                if (triangle.isNeighbor(other))
                    triGraph.add(triangle, other);

        // Return one of the new triangles
        return newTriangles.iterator().next();
    }

    /**
     * Update the triangulation by removing the site and 
     * filling the cavity (surrounding) with new triangles.
     * @param site the site that created the cavity (will be removed)
     * @param cavity the surrounding triangles with site in their vertices
     * @return one of the new triangles
     * @throws Exception 
     */
    private Triangle remove (Pnt site, List<TriangleControl> cavity)
    {
        int v0, v1, v2, cavitySize;
        Pnt intruder;
        double d2;
        Triangle ear, earSite;
        v0 = -1;
        cavitySize = cavity.size();
        while (cavitySize > 3) {
            v0++;
            // Is cycle completed? 
            if (v0 == cavitySize) {
                System.err.println("Invalid cavity?");
                System.exit(1);
            }
            v1 = (v0 + 1) % cavitySize;
            v2 = (v1 + 1) % cavitySize;
            ear = new Triangle(cavity.get(v0).point, 
                    cavity.get(v1).point, cavity.get(v2).point);
            earSite = new Triangle(cavity.get(v0).point, 
                    cavity.get(v2).point, site);
            d2 = earSite.direction();
            if (d2 == 0) {
                earSite = new Triangle(cavity.get(v0).point, 
                        cavity.get(v1).point, site);
                d2 = earSite.direction();
            }
            ///-------------------------------------------
            if ((ear.direction() * d2) > 0) {
                intruder = null;
                Pnt[] aear = ear.toArray(nullPnt);
                for (TriangleControl ce: cavity) 
                    if ((!ear.contains(ce.point)) &&
                        (ce.point.vsCircumcircle(aear) == -1)) {
                        intruder = ce.point;
                        break;
                    }
                if (intruder == null) {
                    TriangleControl ce = cavity.get(v0);
                    triGraph.add(ear);
                    updateCavity(site, ear, ce);
                    updateCavity(site, ear, cavity.get(v1));
                    ce.newTriangle = true;
                    ce.triangle = ear;
                    cavity.remove(v1);
                    v0 = -1;
                }
            }
            cavitySize = cavity.size();
        }
        ear = new Triangle(cavity.get(0).point, 
                cavity.get(1).point, cavity.get(2).point);
        triGraph.add(ear);
        updateCavity(site, ear, cavity.get(0));
        updateCavity(site, ear, cavity.get(1));
        updateCavity(site, ear, cavity.get(2));
        //print();
        return ear;
    }
        
    private void updateCavity(Pnt site, Triangle ear, TriangleControl ce) {
        Triangle opposite = neighborOpposite(site, ce.triangle);
        if (opposite == null) {
            if (ce.newTriangle)
                triGraph.add(ear, ce.triangle);
            else
                triGraph.remove(ce.triangle);
        } else {
            triGraph.add(ear, opposite);
            triGraph.remove(ce.triangle);
        }
    }

    /**
     * Main program; used for testing.
     */
    public static void main (String[] args) {
        Triangle tri =
            new Triangle(new Pnt(-100,-100), 
                    new Pnt(100,-100), new Pnt(0,100));
        DelaunayTriangulation dt = new DelaunayTriangulation(tri);
        System.out.println("DelaunayTriangulation created: " + dt);
        dt.delaunayPlace(new Pnt(0,0));
        dt.delaunayPlace(new Pnt(1,0));
        dt.delaunayPlace(new Pnt(0,1));
        System.out.println("After adding 4 points, we have a " + dt);
        Triangle.moreInfo = true;
        int c = 0;
        for (Triangle t: dt.triGraph.nodeSet()) {
            System.out.println(++c + ":" + t);
            for (Triangle n: dt.triGraph.neighbors(t)) {
                System.out.println("--> " + n);
            }
        }
        
    }
}