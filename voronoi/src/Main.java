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
// ----------------------------------------------------------------------
import java.util.Random;

import javax.swing.JDialog;

import visualization.VisualVoronoiDiagram;
import visualization.VisualVoronoiDiagramFactory;

import algorithm.Prog;
import algorithm.Site;
import algorithm.VoronoiDiagramFactory;
import daj.Application;
import daj.Node;
// 
// 
// ----------------------------------------------------------------------

// ------------------------------------------------------------------------
//
//
// ------------------------------------------------------------------------
@SuppressWarnings("serial")
public class Main extends Application {
    private int id;
    private Configuration cfg;
    private VoronoiDiagramFactory factory;
    private static int width = 600;
    private static int height = 400; 
    // ----------------------------------------------------------------------
    // main function of application
    // ----------------------------------------------------------------------
    public static void main(String[] args) {
        Application app = new Main();
        app.nodeRadius = 7;
        app.channelRadius = 4;
        app.channelWidth = 2;
        app.nodeNormalFont = app.nodeSmallFont;
        app.run();
    }
    
    // ----------------------------------------------------------------------
    // constructor for application
    // ----------------------------------------------------------------------
    public Main () {
        super("Distributed Voronoi Diagram", Main.width, Main.height);
        factory = new VisualVoronoiDiagramFactory();
        cfg = new Configuration();
        cfg.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        id = 0;
    }
    
    private Node newNode(int x, int y)
    {
        int newId = id++;
        Site newSite = new Site(newId, x, y);
        Prog newProg = new Prog(factory, newSite, cfg.ignoreIrrelevantSites);
        newProg.setCleanupIrrelevantSitesPeriodically(cfg.cleanupIrrelevantSitesPeriodically);
        newProg.setForwardPresenceOnAdd(cfg.forwardPresenceOnAdd);
        newProg.setFloodOnForwardFail(cfg.floodOnForwardFail);
        newProg.setAnnouncePresencePeriodically(cfg.announcePresencePeriodically);
        newProg.setCleanupDelay(cfg.cleanupDelay);
        newProg.setfPresenceDelay(cfg.presenceDelay);
        newProg.setMovementDelay(cfg.movementDelay);
        newProg.setMessageDeep(cfg.messageDeep);
        // convert from space (x, y) to canvas (x, y), if necessary.
        return node(newProg, Integer.toString(newId), x, y);
    }
    
    private void edge(Node a, Node b) {
        link(a, b);
        link(b, a);
    }
    
    // ----------------------------------------------------------------------
    // construction of network
    // ----------------------------------------------------------------------
    public void construct() {

        cfg.setVisible(true);
        if (!cfg.ok)
            System.exit(0);
        
        VisualVoronoiDiagram.start(false);
        switch(cfg.testCase) {
        case 0:
            gridTest(5, 50);
            break;
        case 1:
            basicTest();
            break;
        case 2:
            linearTest();
            break;
        case 3:
            geometricTest();
            break;
        case 4:
            randomTest(35);
            break;
        default:
            System.exit(1);
        }
    }
    
    private void gridTest(int n, int sep) {
        Node nodes[][] = new Node[n][n];
        for (int i = 0; i < n; i++) 
            for (int j = 0; j < n; j++) 
                nodes[i][j] = newNode(sep * (i+1), sep * (j + 1));
        for (int i = 0; i < n; i++) 
            for (int j = 0; j < n; j++) {
                if (i > 0)
                    edge(nodes[i-1][j], nodes[i][j]);
                if (j > 0)
                    edge(nodes[i][j-1], nodes[i][j]);
            }
    }

    private void randomTest(int n) {
        Random random = new Random();
        Integer x, y, w, h;
        w = Main.width - 2*this.nodeRadius;
        h = Main.height - 2*this.nodeRadius;
        x = random.nextInt(w) + this.nodeRadius;
        y = random.nextInt(h) + this.nodeRadius;
        Node node0 = newNode(x, y);
        n--;
        for (int i = 0; i < n; i++) { 
            x = random.nextInt(w) + this.nodeRadius;
            y = random.nextInt(h) + this.nodeRadius;
            edge(node0, newNode(x, y));
        }
    }

    private void basicTest() {
        Node node0 = newNode(100, 100);
        Node node1 = newNode(150, 200);
        Node node2 = newNode(300, 150);
        Node node3 = newNode(250, 200);
        Node node4 = newNode(250, 300);
        edge(node0, node1);
        edge(node1, node2);
        edge(node2, node0);
        edge(node3, node2);
        edge(node3, node4);
    }

    private void linearTest() {
        Node node0 = newNode(150, 100);
        Node node1 = newNode(150, 150);
        Node node2 = newNode(150, 200);
        Node node3 = newNode(150, 250);
        edge(node0, node1);
        edge(node1, node2);
        edge(node2, node3);
    }
    
    private void geometricTest() {
        Node node0 = newNode(100, 100); 
        edge(node0, newNode(50, 75));  
        edge(node0, newNode(75, 50));  
        edge(node0, newNode(125,50));  
        edge(node0, newNode(150, 75)); 
        edge(node0, newNode(150, 125));
        edge(node0, newNode(125, 150));
        edge(node0, newNode(75, 150)); 
        edge(node0, newNode(50, 125)); 
        edge(node0, newNode(10, 100)); 
        edge(node0, newNode(190, 100));
        edge(node0, newNode(100, 10)); 
        edge(node0, newNode(100, 190));
        edge(node0, newNode(30, 30));  
        edge(node0, newNode(170, 170));
        edge(node0, newNode(170, 30)); 
        edge(node0, newNode(30, 170)); 
        
    }

    // ----------------------------------------------------------------------
    // informative message
    // ----------------------------------------------------------------------
    public String getText() {
        return  "Distributed Voronoi Diagram \n" + 
                "\n------------------------------------------------------\n" +
                "\n Test case: " + cfg.testCases[cfg.testCase] +
                "\n ignoreIrrelevantSites = " + cfg.ignoreIrrelevantSites +
                "\n cleanupIrrelevantSitesPeriodically = " + cfg.cleanupIrrelevantSitesPeriodically +
                "\n forwardPresenceOnAdd = " + cfg.forwardPresenceOnAdd +
                "\n floodOnForwardFail = " + cfg.floodOnForwardFail + 
                "\n announcePresencePeriodically = " + cfg.announcePresencePeriodically + 
                "\n  presenceDelay = " + cfg.presenceDelay +
                "\n movementDelay = " + cfg.movementDelay+ 
                "\n cleanupDelay = " + cfg.cleanupDelay; 
    }
    
    @Override
    public void resetStatistics() {
        // TODO Auto-generated method stub
        
    }
}

