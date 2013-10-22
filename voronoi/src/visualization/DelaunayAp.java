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

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
@SuppressWarnings("serial")
public class DelaunayAp extends Applet
        implements Runnable, ActionListener, MouseListener, 
                   ListSelectionListener, MouseMotionListener 
{

    private boolean debug = false;             // Used for debugging
    private Component currentSwitch = null;    // Entry-switch that mouse is in

    private static String windowTitle = "Voronoi/Delaunay Window";
    private JRadioButton voronoiButton = new JRadioButton("Voronoi Diagram");
    private JRadioButton delaunayButton =
                                    new JRadioButton("Delaunay Triangulation");
    //private JButton clearButton = new JButton("Clear");
    private JCheckBox colorfulBox = new JCheckBox("More Colorful");
    private DelaunayPanel delaunayPanel = new DelaunayPanel(this);
    private JLabel circleSwitch = new JLabel("Show Empty Circles");
    private JLabel delaunaySwitch = new JLabel("Show Delaunay Edges");
    private JLabel voronoiSwitch = new JLabel("Show Voronoi Edges");
    private JLabel mousePos = new JLabel("(x,y)");
    private JFrame dWindow;
    private DefaultListModel<VisualVoronoiDiagram> listModel;
    private JList<VisualVoronoiDiagram> list;
    private JScrollPane scrollPane = new JScrollPane(list);

    /**
     * Main program (used when run as application instead of applet).
     */
    public void configure(boolean mainWindow) {
        //this.init();                    // Applet initialization
        dWindow = new JFrame();         // Create window
        dWindow.setSize(750, 550);      // Set window size
        dWindow.setTitle(windowTitle);  // Set window title
        dWindow.setLayout(new BorderLayout());   // Specify layout manager
        if (mainWindow) 
            dWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        else
            dWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                                                 // Specify closing behavior
        dWindow.add(this, "Center");           // Place applet into window
        dWindow.setFocusable(true);
        dWindow.setVisible(true);                // Show the window
    }

    /**
     * Initialize the applet.
     * As recommended, the actual use of Swing components takes place in the
     * event-dispatching thread.
     */
    public void init () {
        try {SwingUtilities.invokeAndWait(this);}
        catch (Exception e) {System.err.println("Initialization failure");}
    }

    private void refresh(JComponent c) {
        c.revalidate();
        c.repaint();
        JComponent pane = c.getRootPane();
        if (pane != null) {
            pane.revalidate();
            pane.repaint();
        }
    }
    
    public void disposable() {
        dWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    public void addDiagram(VisualVoronoiDiagram diagram) {
        listModel.addElement(diagram);
        list.setSelectedValue(diagram, true);
        delaunayPanel.setDiagram(diagram);
        refresh(scrollPane);
    }
    
    public void delDiagram(VisualVoronoiDiagram diagram) {
        listModel.removeElement(diagram);
        refresh(scrollPane);
    }
    
    public void refreshDiagramControl() {
        refresh(scrollPane);
    }
    
    /**
     * Set up the applet's GUI.
     * As recommended, the init method executes this in the event-dispatching
     * thread.
     */
    public void run () {
        setLayout(new BorderLayout());

        // Add the button controls
        ButtonGroup group = new ButtonGroup();
        group.add(voronoiButton);
        group.add(delaunayButton);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(voronoiButton);
        buttonPanel.add(delaunayButton);
        //buttonPanel.add(clearButton);
        buttonPanel.add(new JLabel("          "));      // Spacing
        buttonPanel.add(colorfulBox);
        buttonPanel.add(mousePos);
        this.add(buttonPanel, "North");

        // Add the mouse-entry switches
        JPanel switchPanel = new JPanel();
        switchPanel.add(circleSwitch);
        switchPanel.add(new Label("     "));            // Spacing
        switchPanel.add(delaunaySwitch);
        switchPanel.add(new Label("     "));            // Spacing
        switchPanel.add(voronoiSwitch);
        this.add(switchPanel, "South");

        //
        listModel = new DefaultListModel<VisualVoronoiDiagram>();
        list = new JList<VisualVoronoiDiagram>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        scrollPane = new JScrollPane(list);
        this.add(scrollPane, "West");
        
        // Build the delaunay panel
        //delaunayPanel.setBackground(Color.gray);
        delaunayPanel.addMouseMotionListener(this);
        this.add(delaunayPanel, "Center");

        // Register the listeners
        voronoiButton.addActionListener(this);
        delaunayButton.addActionListener(this);
        //clearButton.addActionListener(this);
        colorfulBox.addActionListener(this);
        delaunayPanel.addMouseListener(this);
        circleSwitch.addMouseListener(this);
        delaunaySwitch.addMouseListener(this);
        voronoiSwitch.addMouseListener(this);

        // Initialize the radio buttons
        voronoiButton.doClick();
    }

    /**
     * A button has been pressed; redraw the picture.
     */
    public void actionPerformed(ActionEvent e) {
        if (debug)
            System.out.println(((AbstractButton)e.getSource()).getText());
       // if (e.getSource() == clearButton) delaunayPanel.clear();
        delaunayPanel.repaint();
    }

    /**
     * If entering a mouse-entry switch then redraw the picture.
     */
    public void mouseEntered(MouseEvent e) {
        currentSwitch = e.getComponent();
        if (currentSwitch instanceof JLabel) delaunayPanel.repaint();
        else currentSwitch = null;
    }

    /**
     * If exiting a mouse-entry switch then redraw the picture.
     */
    public void mouseExited(MouseEvent e) {
        currentSwitch = null;
        if (e.getComponent() instanceof JLabel) delaunayPanel.repaint();
    }

    /**
     * If mouse has been pressed inside the delaunayPanel then add a new site.
     */
    public void mousePressed(MouseEvent e) {
        /* CUSTON: Suppress this functionality!
        if (e.getSource() != delaunayPanel) return;
        newSite(e.getX(), e.getY());
        if (debug ) 
            System.out.println("Click (" +
                    e.getX() + "," + e.getY() + ")");
         */
    }

    /**
     * Not used, but needed for MouseListener.
     */
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}

    /**
     * @return true iff the "colorful" box is selected
     */
    public boolean isColorful() {
        return colorfulBox.isSelected();
    }

    /**
     * @return true iff doing Voronoi diagram.
     */
    public boolean isVoronoi() {
        return voronoiButton.isSelected();
    }

    /**
     * @return true iff within circle switch
     */
    public boolean showingCircles() {
        return currentSwitch == circleSwitch;
    }

    /**
     * @return true iff within delaunay switch
     */
    public boolean showingDelaunay() {
        return currentSwitch == delaunaySwitch;
    }

    /**
     * @return true iff within voronoi switch
     */
    public boolean showingVoronoi() {
        return currentSwitch == voronoiSwitch;
    }

    // CUSTON: Pause the execution of this application (for test purpose).
    public static void sleep(int msec) {
        try {
            Thread.sleep(msec);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            VisualVoronoiDiagram diagram = list.getSelectedValue();
            delaunayPanel.setDiagram(diagram);
        }
    }

    @Override
    public void mouseDragged(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getSource() != delaunayPanel) return;
        mousePos.setText("(" + e.getX() + "," + e.getY() + ")");
        mousePos.repaint();
    }
    
}
