
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class JacksGL extends javax.swing.JFrame {

    JFrame thisFrame = this;
    float yRot = 0;
    float xRot = 0;
    float xPos = 0;
    float yPos = 0;
    float zPos = 0;
    float distance = 8;
    int prevX = -1;
    int prevY = -1;
    boolean ctrl = false;
    boolean shift = false;
    boolean alt = false;
    boolean objectLock = true;
    Robot robot;

    JFileChooser chooser = new JFileChooser();

    Timer timer = new Timer(100, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            thisFrame.setTitle(glPanel.getWidth() + "x" + glPanel.getHeight()
                    + " - " + (1000000000.0 / (float) glPanel.deltaTime) + " FPS");
        }
    });

    public JacksGL() {
        try {
            this.robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(JacksGL.class.getName()).log(Level.SEVERE, null, ex);
        }
        initComponents();
        MouseMotionListener spinnerMouseMotion = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int deltaX = 0;
                if (prevX >= 0) {
                    deltaX = e.getXOnScreen() - prevX;
                }
                JLabel sourceLabel = (JLabel) e.getSource();
                JSpinner source = spnLocX;
                if (sourceLabel == lblLocX) {
                    source = spnLocX;
                } else if (sourceLabel == lblLocY) {
                    source = spnLocY;
                } else if (sourceLabel == lblLocZ) {
                    source = spnLocZ;
                } else if (sourceLabel == lblRotX) {
                    source = spnRotX;
                    deltaX *= 40;
                } else if (sourceLabel == lblRotY) {
                    source = spnRotY;
                    deltaX *= 40;
                } else if (sourceLabel == lblRotZ) {
                    source = spnRotZ;
                    deltaX *= 40;
                } else if (sourceLabel == lblScaleX) {
                    source = spnScaleX;
                } else if (sourceLabel == lblScaleY) {
                    source = spnScaleY;
                } else if (sourceLabel == lblScaleZ) {
                    source = spnScaleZ;
                } else if (sourceLabel == lblEnergy) {
                    source = spnEnergy;
                }
                source.setValue(getFloatFromSpinner(source) + (float) deltaX / 100);
                prevX = e.getXOnScreen();
                warpMouse(e.getXOnScreen(), e.getYOnScreen());
            }
        };

        MouseInputListener spinnerMouseInput = new MouseInputAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                prevX = -1;
            }
        };

        jScrollPane1.getVerticalScrollBar().setUnitIncrement(20);
        pnlObjectEdit.setVisible(false);
        pnlLightEdit.setVisible(false);
        spnLocX.setModel(new SpinnerNumberModel(0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01));
        spnLocY.setModel(new SpinnerNumberModel(0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01));
        spnLocZ.setModel(new SpinnerNumberModel(0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01));
        spnRotX.setModel(new SpinnerNumberModel(0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01));
        spnRotY.setModel(new SpinnerNumberModel(0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01));
        spnRotZ.setModel(new SpinnerNumberModel(0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01));
        spnScaleX.setModel(new SpinnerNumberModel(0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01));
        spnScaleY.setModel(new SpinnerNumberModel(0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01));
        spnScaleZ.setModel(new SpinnerNumberModel(0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01));
        spnEnergy.setModel(new SpinnerNumberModel(0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01));
        ChangeListener objectSpinnerListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                applyObjectControl();
            }
        };
        ChangeListener lightChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                applyLightControl();
            }
        };
        
        spnLocX.addChangeListener(objectSpinnerListener);
        spnLocY.addChangeListener(objectSpinnerListener);
        spnLocZ.addChangeListener(objectSpinnerListener);
        spnRotX.addChangeListener(objectSpinnerListener);
        spnRotY.addChangeListener(objectSpinnerListener);
        spnRotZ.addChangeListener(objectSpinnerListener);
        spnScaleX.addChangeListener(objectSpinnerListener);
        spnScaleY.addChangeListener(objectSpinnerListener);
        spnScaleZ.addChangeListener(objectSpinnerListener);
        
        sldLightRed.addChangeListener(lightChangeListener);
        sldLightGreen.addChangeListener(lightChangeListener);
        sldLightBlue.addChangeListener(lightChangeListener);
        spnEnergy.addChangeListener(lightChangeListener);

        lblLocX.addMouseMotionListener(spinnerMouseMotion);
        lblLocY.addMouseMotionListener(spinnerMouseMotion);
        lblLocZ.addMouseMotionListener(spinnerMouseMotion);
        lblRotX.addMouseMotionListener(spinnerMouseMotion);
        lblRotY.addMouseMotionListener(spinnerMouseMotion);
        lblRotZ.addMouseMotionListener(spinnerMouseMotion);
        lblScaleX.addMouseMotionListener(spinnerMouseMotion);
        lblScaleY.addMouseMotionListener(spinnerMouseMotion);
        lblScaleZ.addMouseMotionListener(spinnerMouseMotion);
        lblEnergy.addMouseMotionListener(spinnerMouseMotion);

        lblLocX.addMouseListener(spinnerMouseInput);
        lblLocY.addMouseListener(spinnerMouseInput);
        lblLocZ.addMouseListener(spinnerMouseInput);
        lblRotX.addMouseListener(spinnerMouseInput);
        lblRotY.addMouseListener(spinnerMouseInput);
        lblRotZ.addMouseListener(spinnerMouseInput);
        lblScaleX.addMouseListener(spinnerMouseInput);
        lblScaleY.addMouseListener(spinnerMouseInput);
        lblScaleZ.addMouseListener(spinnerMouseInput);
        lblEnergy.addMouseListener(spinnerMouseInput);

        this.setExtendedState(MAXIMIZED_BOTH);
        JacksGeometry cube = new JacksGeometry();
        cube.addVertex(1, 1, 1);
        cube.addVertex(1, 1, -1);
        cube.addVertex(-1, 1, -1);
        cube.addVertex(-1, 1, 1);
        cube.addVertex(1, -1, 1);
        cube.addVertex(1, -1, -1);
        cube.addVertex(-1, -1, -1);
        cube.addVertex(-1, -1, 1);
        cube.addFace(0, 1, 2, 3);
        cube.addFace(0, 4, 5, 1);
        cube.addFace(3, 2, 6, 7);
        cube.addFace(0, 3, 7, 4);
        cube.addFace(1, 5, 6, 2);
        cube.addFace(4, 7, 6, 5);
        glPanel.addGeometry(cube);
        JacksGeometry clone = cube.clone();
        clone.x = .2f;
        clone.y = .2f;
        clone.z = .2f;
        glPanel.addGeometry(clone);
        glPanel.addLight(new JacksLight(0, 2, 128, 255, 0,
                1.5f, -.8f, 1.5f, true));
        glPanel.addLight(new JacksLight(0, 2, 255, 0, 128,
                -1.5f, -.8f, 1.5f, true));
        JacksLight sun = new JacksLight(1, .5f, 128, 128, 255,
                0, 0, 0, true);
        glPanel.addLight(sun);
        updateView();
        glPanel.startUpdating();
        timer.start();
        refreshObjectList();
    }

    void updateView() {
        glPanel.cameraY = distance * (float) Math.sin(xRot) + yPos;
        glPanel.cameraX = distance * (float) (Math.cos(xRot) * Math.sin(yRot)) + xPos;
        glPanel.cameraZ = distance * (float) (Math.cos(xRot) * Math.cos(yRot)) + zPos;
        glPanel.cameraRotationX = -xRot;
        glPanel.cameraRotationY = yRot;
        glPanel.updateOrigin();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem4 = new javax.swing.JMenuItem();
        glPanel = new JacksGLPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jSlider1 = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jCheckBox2 = new javax.swing.JCheckBox();
        pnlObjectEdit = new javax.swing.JPanel();
        spnLocX = new javax.swing.JSpinner();
        lblLocX = new javax.swing.JLabel();
        lblLocY = new javax.swing.JLabel();
        spnLocY = new javax.swing.JSpinner();
        lblLocZ = new javax.swing.JLabel();
        spnLocZ = new javax.swing.JSpinner();
        lblRotX = new javax.swing.JLabel();
        spnRotX = new javax.swing.JSpinner();
        lblRotY = new javax.swing.JLabel();
        spnRotY = new javax.swing.JSpinner();
        lblRotZ = new javax.swing.JLabel();
        spnRotZ = new javax.swing.JSpinner();
        lblScaleX = new javax.swing.JLabel();
        spnScaleX = new javax.swing.JSpinner();
        lblScaleY = new javax.swing.JLabel();
        spnScaleY = new javax.swing.JSpinner();
        lblScaleZ = new javax.swing.JLabel();
        spnScaleZ = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        pnlLightEdit = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        sldLightRed = new javax.swing.JSlider();
        sldLightGreen = new javax.swing.JSlider();
        sldLightBlue = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        pnlLightColor = new javax.swing.JPanel();
        lblEnergy = new javax.swing.JLabel();
        spnEnergy = new javax.swing.JSpinner();
        cboLightType = new javax.swing.JComboBox<>();
        lblLightType = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();

        jMenuItem4.setText("jMenuItem4");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        glPanel.setBackground(new java.awt.Color(0, 0, 0));
        glPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                glPanelMouseDragged(evt);
            }
        });
        glPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                glPanelFocusLost(evt);
            }
        });
        glPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                glPanelMouseWheelMoved(evt);
            }
        });
        glPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                glPanelMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                glPanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                glPanelMouseReleased(evt);
            }
        });
        glPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                glPanelKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                glPanelKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout glPanelLayout = new javax.swing.GroupLayout(glPanel);
        glPanel.setLayout(glPanelLayout);
        glPanelLayout.setHorizontalGroup(
            glPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 356, Short.MAX_VALUE)
        );
        glPanelLayout.setVerticalGroup(
            glPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Show light sources");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jButton1.setText("Rotate Background X ^");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Rotate Background X v");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Rotate Background Y <");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Rotate Background Y >");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Reset background rotation");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Add light");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jSlider1.setValue(0);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jLabel1.setText("Ambient light:");

        jButton7.setText("Add object");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jList1.setModel(new DefaultListModel());
        jList1.setPreferredSize(new java.awt.Dimension(264, 0));
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList1);

        jCheckBox2.setText("Show back faces");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        spnLocX.setMinimumSize(new java.awt.Dimension(46, 20));

        lblLocX.setText("X");
        lblLocX.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        lblLocY.setText("Y");
        lblLocY.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        spnLocY.setMinimumSize(new java.awt.Dimension(46, 20));

        lblLocZ.setText("Z");
        lblLocZ.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        spnLocZ.setMinimumSize(new java.awt.Dimension(46, 20));

        lblRotX.setText("X");
        lblRotX.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        spnRotX.setMinimumSize(new java.awt.Dimension(46, 20));

        lblRotY.setText("Y");
        lblRotY.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        spnRotY.setMinimumSize(new java.awt.Dimension(46, 20));

        lblRotZ.setText("Z");
        lblRotZ.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        spnRotZ.setMinimumSize(new java.awt.Dimension(46, 20));

        lblScaleX.setText("X");
        lblScaleX.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        spnScaleX.setMinimumSize(new java.awt.Dimension(46, 20));

        lblScaleY.setText("Y");
        lblScaleY.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        spnScaleY.setMinimumSize(new java.awt.Dimension(46, 20));

        lblScaleZ.setText("Z");
        lblScaleZ.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        spnScaleZ.setMinimumSize(new java.awt.Dimension(46, 20));

        jLabel12.setText("Location:");

        jLabel13.setText("Rotation:");

        jLabel14.setText("Scale:");

        javax.swing.GroupLayout pnlObjectEditLayout = new javax.swing.GroupLayout(pnlObjectEdit);
        pnlObjectEdit.setLayout(pnlObjectEditLayout);
        pnlObjectEditLayout.setHorizontalGroup(
            pnlObjectEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlObjectEditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlObjectEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlObjectEditLayout.createSequentialGroup()
                        .addGroup(pnlObjectEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlObjectEditLayout.createSequentialGroup()
                                .addComponent(lblRotX)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnRotX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlObjectEditLayout.createSequentialGroup()
                                .addComponent(lblLocX)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnLocX, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlObjectEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlObjectEditLayout.createSequentialGroup()
                                .addComponent(lblLocY)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnLocY, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlObjectEditLayout.createSequentialGroup()
                                .addComponent(lblRotY)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnRotY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlObjectEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlObjectEditLayout.createSequentialGroup()
                                .addComponent(lblLocZ)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnLocZ, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(pnlObjectEditLayout.createSequentialGroup()
                                .addComponent(lblRotZ)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnRotZ, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addComponent(jLabel12)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addGroup(pnlObjectEditLayout.createSequentialGroup()
                        .addComponent(lblScaleX)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnScaleX, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblScaleY)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnScaleY, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblScaleZ)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnScaleZ, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlObjectEditLayout.setVerticalGroup(
            pnlObjectEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlObjectEditLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlObjectEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnLocX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLocX)
                    .addComponent(spnLocY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLocY)
                    .addComponent(spnLocZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLocZ))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlObjectEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnRotX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRotX)
                    .addComponent(spnRotY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRotY)
                    .addComponent(spnRotZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRotZ))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlObjectEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnScaleX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblScaleX)
                    .addComponent(spnScaleY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblScaleY)
                    .addComponent(spnScaleZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblScaleZ))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jCheckBox3.setText("Othogonal");
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });

        jLabel3.setText("Red");

        sldLightRed.setMaximum(255);
        sldLightRed.setValue(0);

        sldLightGreen.setMaximum(255);
        sldLightGreen.setValue(0);

        sldLightBlue.setMaximum(255);
        sldLightBlue.setValue(0);

        jLabel4.setText("Green");

        jLabel5.setText("Blue");

        pnlLightColor.setBackground(new java.awt.Color(0, 0, 0));
        pnlLightColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                pnlLightColorMousePressed(evt);
            }
        });

        javax.swing.GroupLayout pnlLightColorLayout = new javax.swing.GroupLayout(pnlLightColor);
        pnlLightColor.setLayout(pnlLightColorLayout);
        pnlLightColorLayout.setHorizontalGroup(
            pnlLightColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlLightColorLayout.setVerticalGroup(
            pnlLightColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 62, Short.MAX_VALUE)
        );

        lblEnergy.setText("Energy");
        lblEnergy.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        cboLightType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Point light", "Directional light" }));
        cboLightType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboLightTypeActionPerformed(evt);
            }
        });

        lblLightType.setText("Light type");
        lblLightType.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        javax.swing.GroupLayout pnlLightEditLayout = new javax.swing.GroupLayout(pnlLightEdit);
        pnlLightEdit.setLayout(pnlLightEditLayout);
        pnlLightEditLayout.setHorizontalGroup(
            pnlLightEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLightEditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlLightEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlLightColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlLightEditLayout.createSequentialGroup()
                        .addGroup(pnlLightEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlLightEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sldLightRed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sldLightBlue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sldLightGreen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlLightEditLayout.createSequentialGroup()
                        .addComponent(lblEnergy)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnEnergy))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLightEditLayout.createSequentialGroup()
                        .addComponent(lblLightType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboLightType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlLightEditLayout.setVerticalGroup(
            pnlLightEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLightEditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlLightEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sldLightRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlLightEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sldLightGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlLightEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sldLightBlue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlLightColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlLightEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboLightType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLightType))
                .addGap(9, 9, 9)
                .addGroup(pnlLightEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEnergy)
                    .addComponent(spnEnergy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlObjectEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(jCheckBox2)
                    .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox3))
                    .addComponent(jScrollPane2)
                    .addComponent(pnlLightEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlObjectEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlLightEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        jLabel2.setText("0 vertices - 0 faces");

        jMenu1.setText("File");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Load Background");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItem3.setText("Remove Background");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        jMenuItem5.setText("Delete");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(glPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(glPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void glPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_glPanelMouseDragged
        int deltaX = 0;
        int deltaY = 0;
        if (prevX >= 0) {
            deltaX = evt.getXOnScreen() - prevX;
        }
        if (prevY >= 0) {
            deltaY = evt.getYOnScreen() - prevY;
        }
        if (SwingUtilities.isMiddleMouseButton(evt)) {
            if (!shift && !ctrl && !alt) {
                yRot -= (float) deltaX / 40.0;
                xRot += (float) deltaY / 40.0;
                yRot = rotate(yRot, (float) Math.PI * 2);
                xRot = rotate(xRot, (float) Math.PI * 2);
            } else if (shift && !ctrl && !alt) {
                deltaX *= (distance + 1);
                deltaY *= (distance + 1);
                JacksVector right = new JacksVector((float) deltaX / 200.0f, 0, 0);
                JacksVector up = new JacksVector(0, -(float) deltaY / 200.0f, 0);
                JacksOrigin temp = new JacksOrigin();
                right.rotateX(-xRot);
                right.rotateY(yRot);
                up.rotateX(-xRot);
                up.rotateY(yRot);
                xPos -= (right.dotProduct(temp.x) + up.dotProduct(temp.x));
                zPos -= (right.dotProduct(temp.z) + up.dotProduct(temp.z));
                yPos -= (right.dotProduct(temp.y) + up.dotProduct(temp.y));
            }
        }
        updateView();
        prevX = evt.getXOnScreen();
        prevY = evt.getYOnScreen();
        warpMouse(evt.getXOnScreen(), evt.getYOnScreen());
    }//GEN-LAST:event_glPanelMouseDragged

    private void glPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_glPanelMouseReleased
        prevX = -1;
        prevY = -1;
    }//GEN-LAST:event_glPanelMouseReleased

    private void glPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_glPanelMouseWheelMoved
        distance += evt.getWheelRotation() / 2.0;
        if (distance < 0) {
            distance = 0;
        }
        if (distance > 380) {
            distance = 380;
        }
        updateView();
    }//GEN-LAST:event_glPanelMouseWheelMoved

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        chooser.setFileFilter(
                new FileNameExtensionFilter("Image", "jpg", "png", "bmp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage temp = ImageIO.read(chooser.getSelectedFile());
                BufferedImage hdri = new BufferedImage(temp.getWidth(), temp.getHeight(),
                        BufferedImage.TYPE_3BYTE_BGR);
                hdri.getGraphics().drawImage(temp, 0, 0, null);
                glPanel.addHdri(hdri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        glPanel.removeHdri();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        glPanel.showLightSources = jCheckBox1.isSelected();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        glPanel.backgroundRotationX += Math.PI / 18;
        glPanel.backgroundRotationX = rotate(glPanel.backgroundRotationX,
                (float) Math.PI * 2);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        glPanel.backgroundRotationX = 0;
        glPanel.backgroundRotationY = 0;
        glPanel.backgroundRotationZ = 0;
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        glPanel.backgroundRotationX -= Math.PI / 18;
        glPanel.backgroundRotationX = rotate(glPanel.backgroundRotationX,
                (float) Math.PI * 2);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        glPanel.backgroundRotationY -= Math.PI / 18;
        glPanel.backgroundRotationY = rotate(glPanel.backgroundRotationY,
                (float) Math.PI * 2);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        glPanel.backgroundRotationY += Math.PI / 18;
        glPanel.backgroundRotationY = rotate(glPanel.backgroundRotationY,
                (float) Math.PI * 2);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        glPanel.addLight(new JacksLight(0, 4, 255, 255, 255, 0, 0, 0, true));
        refreshObjectList();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        glPanel.ambient = (float) jSlider1.getValue() / (float) jSlider1.getMaximum();
    }//GEN-LAST:event_jSlider1StateChanged

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        chooser.setFileFilter(
                new FileNameExtensionFilter("Wavefront OBJ", "obj"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            long fileLength = chooser.getSelectedFile().length();
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    long progress = 0;
                    JacksGeometry newObject = new JacksGeometry();

                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(
                                new FileReader(chooser.getSelectedFile()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            progress += line.length() + 2;
                            jProgressBar1.setValue((int) (100 * progress / fileLength));
                            if (line.startsWith("v ")) {
                                float[] newVertex = new float[3];
                                String[] att = line.split("\\s+");
                                newVertex[0] = Float.parseFloat(att[1]);
                                newVertex[1] = Float.parseFloat(att[2]);
                                newVertex[2] = Float.parseFloat(att[3]);
                                newObject.addVertex(newVertex[0], newVertex[1], newVertex[2]);
                            } else if (line.startsWith("f ")) {
                                String[] att = line.replaceAll("\\s+", " ").split(" ");
                                int[] newFace = new int[att.length - 1];
                                for (int i = 0; i < att.length - 1; i++) {
                                    if (att[i + 1].contains("/")) {
                                        att[i + 1]
                                                = att[i + 1].substring(0, att[i + 1].indexOf("/"));
                                    }
                                    newFace[i] = Integer.parseInt(att[i + 1]) - 1;
                                }
                                JacksFace newJacksFace = newObject.addFace(newFace);
                                newJacksFace.specularExponent = 16;
                                newJacksFace.specular = .6f;
                            }
                        }

                        reader.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException ex) {
                            }
                        }
                    }
                    jProgressBar1.setValue(0);
                    glPanel.addGeometry(newObject);
                    refreshObjectList();
                }
            };

            Thread thread = new Thread(run);
            thread.start();

        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        glPanel.showBackFace = jCheckBox2.isSelected();
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        glPanel.selectedObjects.clear();
        if (jList1.getSelectedIndex() >= 0) {
            if (jList1.getSelectedIndex() < glPanel.getGeometryList().size()) {
                glPanel.activeObject = glPanel.getGeometryList().get(
                        jList1.getSelectedIndex());
            } else {
                glPanel.activeObject = glPanel.getLightList().get(
                        jList1.getSelectedIndex() - glPanel.getGeometryList().size());
            }
            if (jList1.getSelectedIndices().length > 0) {
                for (int i : jList1.getSelectedIndices()) {
                    if (i < glPanel.getGeometryList().size()) {
                        glPanel.selectedObjects.add(glPanel.getGeometryList().get(i));
                    } else {
                        glPanel.selectedObjects.add(glPanel.getLightList().get(
                                i - glPanel.getGeometryList().size()));
                    }
                }
            }
            populateObjectControl(glPanel.activeObject);
            pnlObjectEdit.setVisible(true);
            if (glPanel.activeObject instanceof JacksLight) {
                pnlLightEdit.setVisible(true);
                populateLightControl((JacksLight) glPanel.activeObject);
            } else {
                pnlLightEdit.setVisible(false);
            }
            jPanel2.repaint();
        } else {
            glPanel.activeObject = null;
            pnlObjectEdit.setVisible(false);
            pnlLightEdit.setVisible(false);
        }
    }//GEN-LAST:event_jList1ValueChanged

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        LinkedList<JacksObject> toRemove = new LinkedList<>();
        for (int i : jList1.getSelectedIndices()) {
            if (i < glPanel.getGeometryList().size()) {
                toRemove.add(glPanel.getGeometryList().get(i));
            } else {
                toRemove.add(glPanel.getLightList().get(
                        i - glPanel.getGeometryList().size()));
            }
        }
        for (JacksObject object : toRemove) {
            glPanel.removeObject(object);
        }
        refreshObjectList();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void glPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_glPanelMouseEntered
        glPanel.requestFocus();
    }//GEN-LAST:event_glPanelMouseEntered

    private void glPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_glPanelKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_CONTROL:
                ctrl = true;
                break;
            case KeyEvent.VK_SHIFT:
                shift = true;
                break;
            case KeyEvent.VK_ALT:
                alt = true;
                break;
            case KeyEvent.VK_NUMPAD7:
                yRot = 0;
                xRot = (float) Math.PI / 2;
                updateView();
                break;
            case KeyEvent.VK_NUMPAD1:
                yRot = - (float) Math.PI / 2;
                xRot = 0;
                updateView();
                break;
            case KeyEvent.VK_NUMPAD3:
                yRot = 0;
                xRot = 0;
                updateView();
                break;
        }
    }//GEN-LAST:event_glPanelKeyPressed

    private void glPanelKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_glPanelKeyReleased
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_CONTROL:
                ctrl = false;
                break;
            case KeyEvent.VK_SHIFT:
                shift = false;
                break;
            case KeyEvent.VK_ALT:
                alt = false;
                break;
        }
    }//GEN-LAST:event_glPanelKeyReleased

    private void glPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_glPanelFocusLost
        ctrl = false;
        shift = false;
        alt = false;
    }//GEN-LAST:event_glPanelFocusLost

    private void glPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_glPanelMousePressed
        if (SwingUtilities.isLeftMouseButton(evt)) {
            JacksObject selectedObject = 
                    glPanel.selectOnScreen(new Point(evt.getX(), evt.getY()));
            if (!shift) {
                jList1.clearSelection();
                glPanel.activeObject = selectedObject;
                ArrayList<JacksGeometry> geometryList = glPanel.getGeometryList();
                for (int i = 0; i < geometryList.size(); i++) {
                    if (geometryList.get(i) == selectedObject) {
                        jList1.setSelectedIndex(i);
                        return;
                    }
                }
                ArrayList<JacksLight> lightList = glPanel.getLightList();
                for (int i = 0; i < lightList.size(); i++) {
                    if (lightList.get(i) == selectedObject) {
                        jList1.setSelectedIndex(i + geometryList.size());
                        return;
                    }
                }
            }
        }
    }//GEN-LAST:event_glPanelMousePressed

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
        glPanel.othogonal = jCheckBox3.isSelected();
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    private void cboLightTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboLightTypeActionPerformed
        applyLightControl();
    }//GEN-LAST:event_cboLightTypeActionPerformed

    private void pnlLightColorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlLightColorMousePressed
        JColorChooser colorChooser = new JColorChooser();
        Color newColor = colorChooser.showDialog(this, "Pick a color",
                new Color(sldLightRed.getValue(), sldLightGreen.getValue(),
                        sldLightBlue.getValue()));
        if (newColor != null) {
            sldLightRed.setValue(newColor.getRed());
            sldLightGreen.setValue(newColor.getGreen());
            sldLightBlue.setValue(newColor.getBlue());
            applyLightControl();
        }
    }//GEN-LAST:event_pnlLightColorMousePressed

    float rotate(float number, float cap) {
        number %= cap;
        if (number < 0) {
            number += cap;
        }
        return number;
    }

    void refreshStats() {
        int vertexCount = 0;
        int faceCount = 0;
        for (JacksGeometry geometry : glPanel.getGeometryList()) {
            vertexCount += geometry.vertexList.length;
            faceCount += geometry.faceList.length;
        }
        jLabel2.setText(vertexCount + " vertices; " + faceCount + " faces");
    }

    void refreshObjectList() {
        DefaultListModel model = (DefaultListModel) jList1.getModel();
        model.removeAllElements();
        for (JacksGeometry object : glPanel.getGeometryList()) {
            model.addElement(object.name);
        }
        for (JacksLight light : glPanel.getLightList()) {
            model.addElement(light.name);
        }
        refreshStats();
    }

    void populateObjectControl(JacksObject object) {
        objectLock = true;
        spnLocX.setValue(object.x);
        spnLocY.setValue(object.y);
        spnLocZ.setValue(object.z);
        spnRotX.setValue(object.rotateX * 180 / Math.PI);
        spnRotY.setValue(object.rotateY * 180 / Math.PI);
        spnRotZ.setValue(object.rotateZ * 180 / Math.PI);
        spnScaleX.setValue(object.scaleX);
        spnScaleY.setValue(object.scaleY);
        spnScaleZ.setValue(object.scaleZ);
        objectLock = false;
    }

    void populateLightControl(JacksLight light) {
        objectLock = true;
        spnEnergy.setValue(light.energy);
        sldLightRed.setValue(light.r);
        sldLightGreen.setValue(light.g);
        sldLightBlue.setValue(light.b);
        cboLightType.setSelectedIndex(light.lightType);
        pnlLightColor.setBackground(new Color(sldLightRed.getValue(),
                sldLightGreen.getValue(), sldLightBlue.getValue()));
        objectLock = false;
    }
    
    void applyObjectControl() {
        if (!objectLock) {
            glPanel.activeObject.x = getFloatFromSpinner(spnLocX);
            glPanel.activeObject.y = getFloatFromSpinner(spnLocY);
            glPanel.activeObject.z = getFloatFromSpinner(spnLocZ);
            glPanel.activeObject.rotateX = (float) (getFloatFromSpinner(spnRotX) * Math.PI / 180);
            glPanel.activeObject.rotateY = (float) (getFloatFromSpinner(spnRotY) * Math.PI / 180);
            glPanel.activeObject.rotateZ = (float) (getFloatFromSpinner(spnRotZ) * Math.PI / 180);
            glPanel.activeObject.scaleX = getFloatFromSpinner(spnScaleX);
            glPanel.activeObject.scaleY = getFloatFromSpinner(spnScaleY);
            glPanel.activeObject.scaleZ = getFloatFromSpinner(spnScaleZ);
        }
    }

    void applyLightControl() {
        if (!objectLock) {
            JacksLight light = (JacksLight) glPanel.activeObject;
            pnlLightColor.setBackground(new Color(sldLightRed.getValue(),
                sldLightGreen.getValue(), sldLightBlue.getValue()));
            light.energy = getFloatFromSpinner(spnEnergy);
            light.r = sldLightRed.getValue();
            light.g = sldLightGreen.getValue();
            light.b = sldLightBlue.getValue();
            light.lightType = cboLightType.getSelectedIndex();
        }
    }

    void warpMouse(int x, int y) {
        if (robot != null) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            if (x >= screenSize.width - 1) {
                x = 1;
                prevX = 0;
            }
            if (y >= screenSize.height - 1) {
                y = 1;
                prevY = 0;
            }
            if (x <= 0) {
                x = screenSize.width - 2;
                prevX = screenSize.width - 1;
            }
            if (y <= 0) {
                y = screenSize.height - 2;
                prevY = screenSize.height - 1;
            }
            robot.mouseMove(x, y);
        }
    }

    private float getFloatFromSpinner(JSpinner spinner) {
        Object value = spinner.getValue();
        if (value instanceof Double) {
            return (float) (double) (Double) value;
        } else if (value instanceof Float) {
            return (float) value;
        }
        return -1;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
        * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JacksGL.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JacksGL.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JacksGL.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JacksGL.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JacksGL().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cboLightType;
    private JacksGLPanel glPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JList<String> jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JLabel lblEnergy;
    private javax.swing.JLabel lblLightType;
    private javax.swing.JLabel lblLocX;
    private javax.swing.JLabel lblLocY;
    private javax.swing.JLabel lblLocZ;
    private javax.swing.JLabel lblRotX;
    private javax.swing.JLabel lblRotY;
    private javax.swing.JLabel lblRotZ;
    private javax.swing.JLabel lblScaleX;
    private javax.swing.JLabel lblScaleY;
    private javax.swing.JLabel lblScaleZ;
    private javax.swing.JPanel pnlLightColor;
    private javax.swing.JPanel pnlLightEdit;
    private javax.swing.JPanel pnlObjectEdit;
    private javax.swing.JSlider sldLightBlue;
    private javax.swing.JSlider sldLightGreen;
    private javax.swing.JSlider sldLightRed;
    private javax.swing.JSpinner spnEnergy;
    private javax.swing.JSpinner spnLocX;
    private javax.swing.JSpinner spnLocY;
    private javax.swing.JSpinner spnLocZ;
    private javax.swing.JSpinner spnRotX;
    private javax.swing.JSpinner spnRotY;
    private javax.swing.JSpinner spnRotZ;
    private javax.swing.JSpinner spnScaleX;
    private javax.swing.JSpinner spnScaleY;
    private javax.swing.JSpinner spnScaleZ;
    // End of variables declaration//GEN-END:variables
}
