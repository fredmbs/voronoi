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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


@SuppressWarnings("serial")
public class Configuration extends JDialog {
    
    private final JPanel contentPanel = new JPanel();
    public int testCase = 0;
    public boolean ignoreIrrelevantSites = false;
    public boolean cleanupIrrelevantSitesPeriodically = false;
    public boolean forwardPresenceOnAdd = true;
    public boolean floodOnForwardFail = true;
    public boolean announcePresencePeriodically = true;
    public int presenceDelay = 10000;
    public int movementDelay = 1000;
    public int cleanupDelay = 15000;
    public boolean ok = false;
    public String[] testCases = {"Grid", "Basic", "Linear", "Geometric", "Random"};
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            Configuration dialog = new Configuration();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create the dialog.
     */
    public Configuration() {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setModal(true);
        setAlwaysOnTop(true);
        setTitle("Distributed Voronoi Diagram");
        setBounds(100, 100, 412, 329);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {189, 73, 15};
        gridBagLayout.rowHeights = new int[] {34, 20, 23, 23, 23, 23, 23, 23, 0, 33, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);
        contentPanel.setLayout(new FlowLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc_contentPanel = new GridBagConstraints();
        gbc_contentPanel.anchor = GridBagConstraints.NORTH;
        gbc_contentPanel.insets = new Insets(0, 0, 5, 5);
        gbc_contentPanel.gridwidth = 2;
        gbc_contentPanel.gridx = 0;
        gbc_contentPanel.gridy = 0;
        getContentPane().add(contentPanel, gbc_contentPanel);
        {
            JLabel lblNewLabel = new JLabel("Configure the simulation");
            contentPanel.add(lblNewLabel);
        }
        {
            JLabel lblChooseScenario = new JLabel("Choose scenario");
            GridBagConstraints gbc_lblChooseScenario = new GridBagConstraints();
            gbc_lblChooseScenario.fill = GridBagConstraints.HORIZONTAL;
            gbc_lblChooseScenario.insets = new Insets(0, 0, 5, 5);
            gbc_lblChooseScenario.gridx = 0;
            gbc_lblChooseScenario.gridy = 1;
            getContentPane().add(lblChooseScenario, gbc_lblChooseScenario);
        }
        {
            final JComboBox<String> comboBox = new JComboBox<String>();
            comboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    testCase = comboBox.getSelectedIndex();
                }
            });
            GridBagConstraints gbc_comboBox = new GridBagConstraints();
            gbc_comboBox.anchor = GridBagConstraints.NORTHWEST;
            gbc_comboBox.insets = new Insets(0, 0, 5, 5);
            gbc_comboBox.gridx = 1;
            gbc_comboBox.gridy = 1;
            getContentPane().add(comboBox, gbc_comboBox);
            comboBox.setModel(new DefaultComboBoxModel<String>(testCases));
        }
        {
            JCheckBox chckbxIgnoreirrelevantsites = new JCheckBox("IgnoreIrrelevantSites");
            chckbxIgnoreirrelevantsites.setSelected(ignoreIrrelevantSites);
            chckbxIgnoreirrelevantsites.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    ignoreIrrelevantSites = 
                            e.getStateChange() == ItemEvent.SELECTED;
                }
            });
            GridBagConstraints gbc_chckbxIgnoreirrelevantsites = new GridBagConstraints();
            gbc_chckbxIgnoreirrelevantsites.gridwidth = 2;
            gbc_chckbxIgnoreirrelevantsites.anchor = GridBagConstraints.NORTH;
            gbc_chckbxIgnoreirrelevantsites.fill = GridBagConstraints.HORIZONTAL;
            gbc_chckbxIgnoreirrelevantsites.insets = new Insets(0, 0, 5, 5);
            gbc_chckbxIgnoreirrelevantsites.gridx = 0;
            gbc_chckbxIgnoreirrelevantsites.gridy = 2;
            getContentPane().add(chckbxIgnoreirrelevantsites, gbc_chckbxIgnoreirrelevantsites);
        }
        {
            JCheckBox chckbxCleanupirrelevantsitesperiodically = new JCheckBox("CleanupIrrelevantSitesPeriodically");
            chckbxCleanupirrelevantsitesperiodically.setSelected(cleanupIrrelevantSitesPeriodically);
            chckbxCleanupirrelevantsitesperiodically.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    cleanupIrrelevantSitesPeriodically = 
                            e.getStateChange() == ItemEvent.SELECTED;
                }
            });
            GridBagConstraints gbc_chckbxCleanupirrelevantsitesperiodically = new GridBagConstraints();
            gbc_chckbxCleanupirrelevantsitesperiodically.anchor = GridBagConstraints.NORTHWEST;
            gbc_chckbxCleanupirrelevantsitesperiodically.insets = new Insets(0, 0, 5, 5);
            gbc_chckbxCleanupirrelevantsitesperiodically.gridx = 0;
            gbc_chckbxCleanupirrelevantsitesperiodically.gridy = 3;
            getContentPane().add(chckbxCleanupirrelevantsitesperiodically, gbc_chckbxCleanupirrelevantsitesperiodically);
        }
        {
            JCheckBox chckbxForwardpresenceonadd = new JCheckBox("ForwardPresenceOnAddToDiagram");
            chckbxForwardpresenceonadd.setSelected(forwardPresenceOnAdd);
            chckbxForwardpresenceonadd.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    forwardPresenceOnAdd =
                            e.getStateChange() == ItemEvent.SELECTED;
                }
            });
            {
                final JSpinner spinnerCleanupDelay = new JSpinner();
                spinnerCleanupDelay.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        cleanupDelay = (Integer)spinnerCleanupDelay.getValue();
                    }
                });
                spinnerCleanupDelay.setModel(new SpinnerNumberModel(cleanupDelay, new Integer(0), null, new Integer(100)));
                GridBagConstraints gbc_spinnerCleanupDelay = new GridBagConstraints();
                gbc_spinnerCleanupDelay.fill = GridBagConstraints.HORIZONTAL;
                gbc_spinnerCleanupDelay.insets = new Insets(0, 0, 5, 5);
                gbc_spinnerCleanupDelay.gridx = 1;
                gbc_spinnerCleanupDelay.gridy = 3;
                getContentPane().add(spinnerCleanupDelay, gbc_spinnerCleanupDelay);
            }
            {
                JLabel lblMs = new JLabel("ms");
                GridBagConstraints gbc_lblMs = new GridBagConstraints();
                gbc_lblMs.insets = new Insets(0, 0, 5, 0);
                gbc_lblMs.gridx = 2;
                gbc_lblMs.gridy = 3;
                getContentPane().add(lblMs, gbc_lblMs);
            }
            GridBagConstraints gbc_chckbxForwardpresenceonadd = new GridBagConstraints();
            gbc_chckbxForwardpresenceonadd.gridwidth = 2;
            gbc_chckbxForwardpresenceonadd.anchor = GridBagConstraints.NORTH;
            gbc_chckbxForwardpresenceonadd.fill = GridBagConstraints.HORIZONTAL;
            gbc_chckbxForwardpresenceonadd.insets = new Insets(0, 0, 5, 5);
            gbc_chckbxForwardpresenceonadd.gridx = 0;
            gbc_chckbxForwardpresenceonadd.gridy = 4;
            getContentPane().add(chckbxForwardpresenceonadd, gbc_chckbxForwardpresenceonadd);
        }
        {
            JCheckBox chckbxFloodonforwardfail = new JCheckBox("FloodOnForwardFail");
            chckbxFloodonforwardfail.setSelected(floodOnForwardFail);
            chckbxFloodonforwardfail.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    floodOnForwardFail = 
                            e.getStateChange() == ItemEvent.SELECTED;
                }
            });
            GridBagConstraints gbc_chckbxFloodonforwardfail = new GridBagConstraints();
            gbc_chckbxFloodonforwardfail.anchor = GridBagConstraints.NORTH;
            gbc_chckbxFloodonforwardfail.fill = GridBagConstraints.HORIZONTAL;
            gbc_chckbxFloodonforwardfail.insets = new Insets(0, 0, 5, 5);
            gbc_chckbxFloodonforwardfail.gridx = 0;
            gbc_chckbxFloodonforwardfail.gridy = 5;
            getContentPane().add(chckbxFloodonforwardfail, gbc_chckbxFloodonforwardfail);
        }
        {
            JCheckBox chckbxAnnouncepresenceperiodically = new JCheckBox("AnnouncePresencePeriodically");
            chckbxAnnouncepresenceperiodically.setSelected(announcePresencePeriodically);
            chckbxAnnouncepresenceperiodically.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    announcePresencePeriodically =
                            e.getStateChange() == ItemEvent.SELECTED;
                }
            });
            GridBagConstraints gbc_chckbxAnnouncepresenceperiodically = new GridBagConstraints();
            gbc_chckbxAnnouncepresenceperiodically.anchor = GridBagConstraints.NORTH;
            gbc_chckbxAnnouncepresenceperiodically.fill = GridBagConstraints.HORIZONTAL;
            gbc_chckbxAnnouncepresenceperiodically.insets = new Insets(0, 0, 5, 5);
            gbc_chckbxAnnouncepresenceperiodically.gridx = 0;
            gbc_chckbxAnnouncepresenceperiodically.gridy = 6;
            getContentPane().add(chckbxAnnouncepresenceperiodically, gbc_chckbxAnnouncepresenceperiodically);
        }
        {
            final JSpinner spinnerPresenceDelay = new JSpinner();
            spinnerPresenceDelay.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    presenceDelay = (Integer)spinnerPresenceDelay.getValue();
                }
            });
            spinnerPresenceDelay.setModel(new SpinnerNumberModel(presenceDelay, null, null, new Integer(100)));
            GridBagConstraints gbc_spinnerPresenceDelay = new GridBagConstraints();
            gbc_spinnerPresenceDelay.fill = GridBagConstraints.HORIZONTAL;
            gbc_spinnerPresenceDelay.insets = new Insets(0, 0, 5, 5);
            gbc_spinnerPresenceDelay.gridx = 1;
            gbc_spinnerPresenceDelay.gridy = 6;
            getContentPane().add(spinnerPresenceDelay, gbc_spinnerPresenceDelay);
        }
        {
            JLabel lblMs_1 = new JLabel("ms");
            GridBagConstraints gbc_lblMs_1 = new GridBagConstraints();
            gbc_lblMs_1.insets = new Insets(0, 0, 5, 0);
            gbc_lblMs_1.gridx = 2;
            gbc_lblMs_1.gridy = 6;
            getContentPane().add(lblMs_1, gbc_lblMs_1);
        }
        {
            JCheckBox chckbxMovementnotificationdelay = new JCheckBox("MovementNotificationDelay");
            chckbxMovementnotificationdelay.setSelected(true);
            chckbxMovementnotificationdelay.setEnabled(false);
            GridBagConstraints gbc_chckbxMovementnotificationdelay = new GridBagConstraints();
            gbc_chckbxMovementnotificationdelay.anchor = GridBagConstraints.NORTHWEST;
            gbc_chckbxMovementnotificationdelay.insets = new Insets(0, 0, 5, 5);
            gbc_chckbxMovementnotificationdelay.gridx = 0;
            gbc_chckbxMovementnotificationdelay.gridy = 7;
            getContentPane().add(chckbxMovementnotificationdelay, gbc_chckbxMovementnotificationdelay);
        }
        {
            final JSpinner spinnerMovimentDelay = new JSpinner();
            spinnerMovimentDelay.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    movementDelay = (Integer)spinnerMovimentDelay.getValue();
                }
            });
            spinnerMovimentDelay.setModel(new SpinnerNumberModel(movementDelay, null, null, new Integer(100)));
            GridBagConstraints gbc_spinnerMovimentDelay = new GridBagConstraints();
            gbc_spinnerMovimentDelay.fill = GridBagConstraints.HORIZONTAL;
            gbc_spinnerMovimentDelay.insets = new Insets(0, 0, 5, 5);
            gbc_spinnerMovimentDelay.gridx = 1;
            gbc_spinnerMovimentDelay.gridy = 7;
            getContentPane().add(spinnerMovimentDelay, gbc_spinnerMovimentDelay);
        }
        {
            JLabel lblMs_2 = new JLabel("ms");
            GridBagConstraints gbc_lblMs_2 = new GridBagConstraints();
            gbc_lblMs_2.insets = new Insets(0, 0, 5, 0);
            gbc_lblMs_2.gridx = 2;
            gbc_lblMs_2.gridy = 7;
            getContentPane().add(lblMs_2, gbc_lblMs_2);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            GridBagConstraints gbc_buttonPane = new GridBagConstraints();
            gbc_buttonPane.insets = new Insets(0, 0, 5, 5);
            gbc_buttonPane.anchor = GridBagConstraints.NORTH;
            gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
            gbc_buttonPane.gridwidth = 2;
            gbc_buttonPane.gridx = 0;
            gbc_buttonPane.gridy = 8;
            getContentPane().add(buttonPane, gbc_buttonPane);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        ok = true;
                        setVisible(false);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ok = false;
                        setVisible(false);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
    }
    
}
