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
package visualization;


import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;

import triangulation.Pnt;
import algorithm.Location2D;
import algorithm.Site;
import algorithm.VoronoiDiagram;

/**
 * Voroni diagram control for multi-thread application
 */
public class VisualVoronoiDiagram extends VoronoiDiagram {
    
    // CUSTON: To avoid race condition caused by 
    // simultaneous method invocation originated by other applications.
    private final ReentrantLock lock = new ReentrantLock();
    
    private JPanel delaunayPanel;
    static private DelaunayAp app = null;
    
    public static void start(boolean mainApp) {
        if (app != null) {
            app.disposable();
        }
        app = new DelaunayAp();
        app.configure(mainApp);
        app.run();
    }
    
    /**
     * Create and initialize the DT.
     */
    public VisualVoronoiDiagram(String id) {
        super(id);
        delaunayPanel = null;
        app.addDiagram(this);
    }
    
    public void lock() {
        lock.lock();
    }
    
    public void unlock() {
        lock.unlock();
    }
    
    public JPanel getPanel() {
        return delaunayPanel;
    }
    
    public void setPanel(JPanel delaunayPanel) {
        this.delaunayPanel = delaunayPanel;
    }
    
    /**
     * CUSTON: add new Voronoi site.
     */
    public boolean addSite(Site site) {
        lock.lock();  // block until condition holds
        try {
            if (super.addSite(site)) {
                if (delaunayPanel != null) 
                    delaunayPanel.repaint();
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * CUSTON: add new Voronoi site.
     */
    public boolean addRelevantSite(Site site) {
        lock.lock();  // block until condition holds
        try {
            if (super.addRelevantSite(site)) {
                if (delaunayPanel != null) 
                    delaunayPanel.repaint();
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * CUSTON: del Voronoi site.
     */
    public boolean delSite(Site site, Location2D pos) {
        lock.lock();  // block until condition holds
        try {
            if (super.delSite(site, pos)) {
                if (delaunayPanel != null) 
                    delaunayPanel.repaint();
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * CUSTON: del all Voronoi sites far form this point.
     */
    public ArrayList<Pnt> delFarFromMainSite() {
        lock.lock();  // block until condition holds
        try {
            ArrayList<Pnt> deleted = super.delFarFromMainSite();
            if (!deleted.isEmpty() && delaunayPanel != null)
                delaunayPanel.repaint();
            return deleted;
        } finally {
            lock.unlock();
        }
    }

}
