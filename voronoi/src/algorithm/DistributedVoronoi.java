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

import triangulation.Pnt;


public class DistributedVoronoi {
    
    private VoronoiDiagram diagram;
    // Controls each site and it's location 
    private HashMap<Site, Location2D> sites;
    // Local site
    private Site theSite;
    // Local control of relevant sites
    private boolean onlyRelevantSites;
    
    public DistributedVoronoi(VoronoiDiagramFactory factory,
            Site site, boolean ignoreIrrelevantSites) {
        this.theSite = site;
        sites = new HashMap<Site, Location2D>();
        sites.put(this.theSite, this.theSite.getPos());
        diagram = factory.construct(Integer.toString(site.getId()));
        diagram.addSite(site);
        diagram.setMainSite(site);
        onlyRelevantSites = ignoreIrrelevantSites;
    }
    
    // move site local
    public void moveLocal(int x, int y) {
        diagram.delSite(theSite, theSite.getPos());
        theSite.setPos(x, y);
        diagram.addSite(theSite);
        diagram.setMainSite(theSite);
    }
    
    // check if site is in diagram
    public boolean hasSite(Site site) {
        return sites.containsKey(site);
    }
    
    // safe add new site to diagram
    public boolean addRemote(Site site) {
        boolean newSite; 
        // ensure that the site do not exist in its position
        if (sites.containsKey(site)) {
            Location2D oldPos = sites.get(site);
            // if site was not moved, do nothing
            if (oldPos.equals(site.getPos()))
                return false;
            // otherwise, remove site from its old position before add
            sites.remove(site);
            diagram.delSite(site, oldPos);
            newSite = false;
        } else 
            newSite = true;
        // put a point representing the site in the diagram
        boolean added;
        if (onlyRelevantSites) 
            added = diagram.addRelevantSite(site);
        else
            added = diagram.addSite(site);
        if (added) 
            sites.put(site, site.getPos());
        return added && newSite;
    }

    // safe remove the site form its position
    public boolean delRemote(Site site) {
        // if site exist in diagram
        if (sites.containsKey(site)) {
            // remove from site control
            sites.remove(site);
            // get its old position
            Location2D oldPos = sites.get(site);
            Location2D pos = site.getPos();
            // if site was not moved...
            if (oldPos.equals(pos))
                return diagram.delSite(site, oldPos);
            else {
                // otherwise, probably we have control problem...
                // anyway, is it safe to try remove from both positions?
                System.err.println("Remove in weird situation.");
                boolean removed = false;
                removed = removed || diagram.delSite(site, pos);
                removed = removed || diagram.delSite(site, oldPos);
                return removed;
            }
        }
        return false;
    }

    // delete irrelevant sites from triangulation and from the controls
    public ArrayList<Site> delIrrelevantSites() {
        ArrayList<Pnt> farPoints = diagram.delFarFromMainSite();
        ArrayList<Site> farSites = new ArrayList<Site>();
        for (Pnt p: farPoints) {
            for (Site site: sites.keySet()) 
                if (site.inPosition(p)) 
                    farSites.add(site);
        }
        for (Site site: farSites)
            sites.remove(site);
        return farSites;
    }
    
}
