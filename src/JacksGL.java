
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    boolean objectListLock = false;
    Robot robot;
    LinkedList<JacksObject> clipboard = new LinkedList<>();
    ArrayList<JacksMaterial> editingMaterial = new ArrayList<>();
    JacksMaterial activeMaterial = null;
    BufferedImage currentTexture = null;
    BufferedImage currentNormalMap = null;

    JFileChooser chooser = new JFileChooser();

    Timer timer = new Timer(100, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            thisFrame.setTitle(glPanel.getWidth() + "x" + glPanel.getHeight()
                    + " - " + (1000000000.0 / (float) glPanel.deltaTime) + " FPS"
                    + " - " + glPanel.polygonDrawn + " polygons drawn");
        }
    });

    public JacksGL() {
        try {
            robot = new Robot();
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
                } else if (sourceLabel == lblSpecExp) {
                    source = spnSpecExp;
                    deltaX *= 100;
                } else if (sourceLabel == lblNormalAmount) {
                    source = spnNormalAmount;
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
        pnlMaterialEdit.setVisible(false);
        pnlMaterialControl.setVisible(false);
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
                0, Double.POSITIVE_INFINITY, .01));
        spnSpecExp.setModel(new SpinnerNumberModel(0,
                0, Double.POSITIVE_INFINITY, 1));
        spnNormalAmount.setModel(new SpinnerNumberModel(0,
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
        lblSpecExp.addMouseMotionListener(spinnerMouseMotion);
        lblNormalAmount.addMouseMotionListener(spinnerMouseMotion);

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
        lblSpecExp.addMouseListener(spinnerMouseInput);
        lblNormalAmount.addMouseListener(spinnerMouseInput);

        this.setExtendedState(MAXIMIZED_BOTH);
        JacksGeometry cube = new JacksGeometry("Doodster");
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
//        glPanel.addGeometry(cube);
        JacksGeometry clone = cube.clone();
        clone.x = .2f;
        clone.y = .2f;
        clone.z = .2f;
//        glPanel.addGeometry(clone);
//        glPanel.addLight(new JacksLight(0, 2, 128, 255, 0,
//                1.5f, -.8f, 1.5f, true));
//        glPanel.addLight(new JacksLight(0, 2, 255, 0, 128,
//                -1.5f, -.8f, 1.5f, true));
//        JacksLight sun = new JacksLight(1, .5f, 128, 128, 255,
//                0, 0, 0, true);
//        glPanel.addLight(sun);
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
        cbxShowLight = new javax.swing.JCheckBox();
        btnRotateBGndXUp = new javax.swing.JButton();
        btnRotateBGndXDown = new javax.swing.JButton();
        btnRotateBGndYLeft = new javax.swing.JButton();
        btnRotateBGndYRight = new javax.swing.JButton();
        btnResetBGnd = new javax.swing.JButton();
        btnAddLight = new javax.swing.JButton();
        sldAmbient = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        btnAddObject = new javax.swing.JButton();
        cbxShowBackFaces = new javax.swing.JCheckBox();
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
        cbxOthogonal = new javax.swing.JCheckBox();
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
        jLabel6 = new javax.swing.JLabel();
        sldAngle = new javax.swing.JSlider();
        lblAmbientLight = new javax.swing.JLabel();
        lblCameraAngle = new javax.swing.JLabel();
        cbxHdri = new javax.swing.JCheckBox();
        cbxSmooth = new javax.swing.JCheckBox();
        cbxForceSmooth = new javax.swing.JCheckBox();
        cbxTexture = new javax.swing.JCheckBox();
        cbxFreeLook = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstObjects = new javax.swing.JList<>(new DefaultListModel());
        cbxForceRes = new javax.swing.JCheckBox();
        spnWidth = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        spnHeight = new javax.swing.JSpinner();
        cbxShowNormal = new javax.swing.JCheckBox();
        pnlMaterialEdit = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        lstMaterials = new javax.swing.JList<>(new DefaultListModel());
        pnlMaterialControl = new javax.swing.JPanel();
        glMaterialReview = new JacksGLPanel();
        jLabel8 = new javax.swing.JLabel();
        pnlDiffuseColor = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        pnlSpecularColor = new javax.swing.JPanel();
        lblSpecExp = new javax.swing.JLabel();
        spnSpecExp = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        pnlTexture = new javax.swing.JPanel() {
            public void paint(Graphics g) {
                super.paint(g);
                if (currentTexture != null)
                g.drawImage(currentTexture, 0, 0, this);
            }
        };
        jLabel11 = new javax.swing.JLabel();
        pnlNormalMap = new javax.swing.JPanel() {
            public void paint(Graphics g) {
                super.paint(g);
                if (currentNormalMap != null)
                g.drawImage(currentNormalMap, 0, 0, this);
            }
        };
        lblNormalAmount = new javax.swing.JLabel();
        spnNormalAmount = new javax.swing.JSpinner();
        lblSpecExp1 = new javax.swing.JLabel();
        sldAlpha = new javax.swing.JSlider();
        jLabel15 = new javax.swing.JLabel();
        sldEnvReflection = new javax.swing.JSlider();
        jLabel16 = new javax.swing.JLabel();
        cbxZSort = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();

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
            .addGap(0, 0, Short.MAX_VALUE)
        );
        glPanelLayout.setVerticalGroup(
            glPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        cbxShowLight.setSelected(true);
        cbxShowLight.setText("Show light sources");
        cbxShowLight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxShowLightActionPerformed(evt);
            }
        });

        btnRotateBGndXUp.setText("Rotate Background X ^");
        btnRotateBGndXUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRotateBGndXUpActionPerformed(evt);
            }
        });

        btnRotateBGndXDown.setText("Rotate Background X v");
        btnRotateBGndXDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRotateBGndXDownActionPerformed(evt);
            }
        });

        btnRotateBGndYLeft.setText("Rotate Background Y <");
        btnRotateBGndYLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRotateBGndYLeftActionPerformed(evt);
            }
        });

        btnRotateBGndYRight.setText("Rotate Background Y >");
        btnRotateBGndYRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRotateBGndYRightActionPerformed(evt);
            }
        });

        btnResetBGnd.setText("Reset background rotation");
        btnResetBGnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetBGndActionPerformed(evt);
            }
        });

        btnAddLight.setText("Add light");
        btnAddLight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddLightActionPerformed(evt);
            }
        });

        sldAmbient.setValue(0);
        sldAmbient.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldAmbientStateChanged(evt);
            }
        });

        jLabel1.setText("Ambient light:");

        btnAddObject.setText("Add object");
        btnAddObject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddObjectActionPerformed(evt);
            }
        });

        cbxShowBackFaces.setText("Show back faces");
        cbxShowBackFaces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxShowBackFacesActionPerformed(evt);
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
                    .addGroup(pnlObjectEditLayout.createSequentialGroup()
                        .addGroup(pnlObjectEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14))
                        .addGap(0, 0, Short.MAX_VALUE))
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
                        .addComponent(spnScaleZ, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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

        cbxOthogonal.setText("Othogonal");
        cbxOthogonal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxOthogonalActionPerformed(evt);
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

        jLabel6.setText("Camera angle");

        sldAngle.setMaximum(180);
        sldAngle.setMinimum(2);
        sldAngle.setToolTipText("");
        sldAngle.setValue(60);
        sldAngle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldAngleStateChanged(evt);
            }
        });

        lblAmbientLight.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAmbientLight.setText("0");

        lblCameraAngle.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCameraAngle.setText("60");

        cbxHdri.setSelected(true);
        cbxHdri.setText("HDRI Background");
        cbxHdri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxHdriActionPerformed(evt);
            }
        });

        cbxSmooth.setSelected(true);
        cbxSmooth.setText("Smooth");
        cbxSmooth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSmoothActionPerformed(evt);
            }
        });

        cbxForceSmooth.setText("Force Smooth");
        cbxForceSmooth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxForceSmoothActionPerformed(evt);
            }
        });

        cbxTexture.setSelected(true);
        cbxTexture.setText("Texture");
        cbxTexture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxTextureActionPerformed(evt);
            }
        });

        cbxFreeLook.setText("Free look");
        cbxFreeLook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxFreeLookActionPerformed(evt);
            }
        });

        lstObjects.setMaximumSize(new java.awt.Dimension(278, 146));
        lstObjects.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstObjectsValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(lstObjects);

        cbxForceRes.setText("Force Resolution");
        cbxForceRes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxForceResActionPerformed(evt);
            }
        });

        spnWidth.setModel(new javax.swing.SpinnerNumberModel(1366, 100, 3840, 1));
        spnWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnWidthStateChanged(evt);
            }
        });

        jLabel7.setText("x");

        spnHeight.setModel(new javax.swing.SpinnerNumberModel(768, 100, 2160, 1));
        spnHeight.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnHeightStateChanged(evt);
            }
        });

        cbxShowNormal.setText("Show Normal");
        cbxShowNormal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxShowNormalActionPerformed(evt);
            }
        });

        lstMaterials.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstMaterialsValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(lstMaterials);

        glMaterialReview.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout glMaterialReviewLayout = new javax.swing.GroupLayout(glMaterialReview);
        glMaterialReview.setLayout(glMaterialReviewLayout);
        glMaterialReviewLayout.setHorizontalGroup(
            glMaterialReviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        glMaterialReviewLayout.setVerticalGroup(
            glMaterialReviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 128, Short.MAX_VALUE)
        );

        jLabel8.setText("Diffuse");

        pnlDiffuseColor.setBackground(new java.awt.Color(0, 0, 0));
        pnlDiffuseColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                pnlDiffuseColorMousePressed(evt);
            }
        });

        javax.swing.GroupLayout pnlDiffuseColorLayout = new javax.swing.GroupLayout(pnlDiffuseColor);
        pnlDiffuseColor.setLayout(pnlDiffuseColorLayout);
        pnlDiffuseColorLayout.setHorizontalGroup(
            pnlDiffuseColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlDiffuseColorLayout.setVerticalGroup(
            pnlDiffuseColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 14, Short.MAX_VALUE)
        );

        jLabel9.setText("Specular");

        pnlSpecularColor.setBackground(new java.awt.Color(0, 0, 0));
        pnlSpecularColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                pnlSpecularColorMousePressed(evt);
            }
        });

        javax.swing.GroupLayout pnlSpecularColorLayout = new javax.swing.GroupLayout(pnlSpecularColor);
        pnlSpecularColor.setLayout(pnlSpecularColorLayout);
        pnlSpecularColorLayout.setHorizontalGroup(
            pnlSpecularColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlSpecularColorLayout.setVerticalGroup(
            pnlSpecularColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 14, Short.MAX_VALUE)
        );

        lblSpecExp.setText("Specular Exponent");
        lblSpecExp.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        spnSpecExp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnSpecExpStateChanged(evt);
            }
        });

        jLabel10.setText("Texture");

        pnlTexture.setBackground(new java.awt.Color(0, 0, 0));
        pnlTexture.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlTextureMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnlTextureLayout = new javax.swing.GroupLayout(pnlTexture);
        pnlTexture.setLayout(pnlTextureLayout);
        pnlTextureLayout.setHorizontalGroup(
            pnlTextureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlTextureLayout.setVerticalGroup(
            pnlTextureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 66, Short.MAX_VALUE)
        );

        jLabel11.setText("Normal");

        pnlNormalMap.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout pnlNormalMapLayout = new javax.swing.GroupLayout(pnlNormalMap);
        pnlNormalMap.setLayout(pnlNormalMapLayout);
        pnlNormalMapLayout.setHorizontalGroup(
            pnlNormalMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlNormalMapLayout.setVerticalGroup(
            pnlNormalMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 66, Short.MAX_VALUE)
        );

        lblNormalAmount.setText("Amount");
        lblNormalAmount.setCursor(new java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR));

        spnNormalAmount.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnNormalAmountStateChanged(evt);
            }
        });

        lblSpecExp1.setText("Alpha");

        sldAlpha.setValue(200);
        sldAlpha.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldAlphaStateChanged(evt);
            }
        });

        jLabel15.setText("Environment");

        sldEnvReflection.setValue(0);
        sldEnvReflection.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldEnvReflectionStateChanged(evt);
            }
        });

        jLabel16.setText("Reflection");

        javax.swing.GroupLayout pnlMaterialControlLayout = new javax.swing.GroupLayout(pnlMaterialControl);
        pnlMaterialControl.setLayout(pnlMaterialControlLayout);
        pnlMaterialControlLayout.setHorizontalGroup(
            pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMaterialControlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(glMaterialReview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlMaterialControlLayout.createSequentialGroup()
                        .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlDiffuseColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlSpecularColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlMaterialControlLayout.createSequentialGroup()
                        .addComponent(lblSpecExp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnSpecExp))
                    .addGroup(pnlMaterialControlLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlTexture, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlMaterialControlLayout.createSequentialGroup()
                        .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(lblNormalAmount))
                        .addGap(5, 5, 5)
                        .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spnNormalAmount)
                            .addComponent(pnlNormalMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlMaterialControlLayout.createSequentialGroup()
                        .addComponent(lblSpecExp1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sldAlpha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlMaterialControlLayout.createSequentialGroup()
                        .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sldEnvReflection, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlMaterialControlLayout.setVerticalGroup(
            pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMaterialControlLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(glMaterialReview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDiffuseColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSpecularColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSpecExp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spnSpecExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSpecExp1, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                    .addComponent(sldAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlTexture, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlNormalMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNormalAmount)
                    .addComponent(spnNormalAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlMaterialControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMaterialControlLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel16))
                    .addComponent(sldEnvReflection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlMaterialEditLayout = new javax.swing.GroupLayout(pnlMaterialEdit);
        pnlMaterialEdit.setLayout(pnlMaterialEditLayout);
        pnlMaterialEditLayout.setHorizontalGroup(
            pnlMaterialEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMaterialEditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMaterialEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4)
                    .addComponent(pnlMaterialControl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlMaterialEditLayout.setVerticalGroup(
            pnlMaterialEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMaterialEditLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMaterialControl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cbxZSort.setText("Z Sort");
        cbxZSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxZSortActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(cbxForceRes)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(spnWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(spnHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(cbxFreeLook)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cbxShowNormal)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cbxZSort))
                        .addComponent(pnlObjectEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAddLight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAddObject, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnResetBGnd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRotateBGndYLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRotateBGndYRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRotateBGndXDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRotateBGndXUp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pnlLightEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel6)
                                .addComponent(jLabel1))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(sldAmbient, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(sldAngle, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                            .addGap(4, 4, 4)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(lblCameraAngle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblAmbientLight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(cbxTexture)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxSmooth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxForceSmooth))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbxShowLight)
                                    .addComponent(cbxShowBackFaces))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbxHdri)
                                    .addComponent(cbxOthogonal))))
                        .addComponent(pnlMaterialEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxShowLight)
                    .addComponent(cbxOthogonal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxShowBackFaces)
                    .addComponent(cbxHdri))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxSmooth)
                    .addComponent(cbxForceSmooth)
                    .addComponent(cbxTexture))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxFreeLook)
                    .addComponent(cbxShowNormal)
                    .addComponent(cbxZSort))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxForceRes)
                    .addComponent(spnWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(spnHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRotateBGndXUp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRotateBGndXDown)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRotateBGndYRight)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRotateBGndYLeft)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnResetBGnd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAddObject)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAddLight)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sldAmbient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAmbientLight, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(sldAngle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblCameraAngle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlObjectEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlLightEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMaterialEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem7.setText("Select All");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem7);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        jMenuItem5.setText("Delete");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Copy");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem6.setText("Paste");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem6);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(glPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(0, 242, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
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
                JacksVector right = new JacksVector((float) deltaX / 400.0f, 0, 0);
                JacksVector up = new JacksVector(0, -(float) deltaY / 400.0f, 0);
                right.rotateX(-xRot);
                right.rotateY(yRot);
                up.rotateX(-xRot);
                up.rotateY(yRot);
                xPos -= right.x + up.x;
                zPos -= right.z + up.z;
                yPos -= right.y + up.y;
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

    private void cbxShowLightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxShowLightActionPerformed
        glPanel.showLightSources = cbxShowLight.isSelected();
    }//GEN-LAST:event_cbxShowLightActionPerformed

    private void btnRotateBGndXUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRotateBGndXUpActionPerformed
        glPanel.backgroundRotationX += Math.PI / 18;
        glPanel.backgroundRotationX = rotate(glPanel.backgroundRotationX,
                (float) Math.PI * 2);
    }//GEN-LAST:event_btnRotateBGndXUpActionPerformed

    private void btnResetBGndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetBGndActionPerformed
        glPanel.backgroundRotationX = 0;
        glPanel.backgroundRotationY = 0;
        glPanel.backgroundRotationZ = 0;
    }//GEN-LAST:event_btnResetBGndActionPerformed

    private void btnRotateBGndXDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRotateBGndXDownActionPerformed
        glPanel.backgroundRotationX -= Math.PI / 18;
        glPanel.backgroundRotationX = rotate(glPanel.backgroundRotationX,
                (float) Math.PI * 2);
    }//GEN-LAST:event_btnRotateBGndXDownActionPerformed

    private void btnRotateBGndYRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRotateBGndYRightActionPerformed
        glPanel.backgroundRotationY -= Math.PI / 18;
        glPanel.backgroundRotationY = rotate(glPanel.backgroundRotationY,
                (float) Math.PI * 2);
    }//GEN-LAST:event_btnRotateBGndYRightActionPerformed

    private void btnRotateBGndYLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRotateBGndYLeftActionPerformed
        glPanel.backgroundRotationY += Math.PI / 18;
        glPanel.backgroundRotationY = rotate(glPanel.backgroundRotationY,
                (float) Math.PI * 2);
    }//GEN-LAST:event_btnRotateBGndYLeftActionPerformed

    private void btnAddLightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddLightActionPerformed
        glPanel.addLight(new JacksLight(0, 1.5f, 255, 255, 255, 0, 0, 0, true));
        refreshObjectList();
    }//GEN-LAST:event_btnAddLightActionPerformed

    private void sldAmbientStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldAmbientStateChanged
        glPanel.ambient = (float) sldAmbient.getValue()
                / (float) sldAmbient.getMaximum();
        lblAmbientLight.setText(sldAmbient.getValue() + "");
    }//GEN-LAST:event_sldAmbientStateChanged

    private void btnAddObjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddObjectActionPerformed
        chooser.setFileFilter(
                new FileNameExtensionFilter("Wavefront OBJ", "obj"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            long fileLength = chooser.getSelectedFile().length();
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    long progress = 0;
                    JacksGeometry readingObject = null;
                    int vertexIndexOffset = 0;
                    BufferedReader reader = null;
                    boolean smoothMode = false;
                    boolean vertexNormalSpecified = false;
                    try {
                        reader = new BufferedReader(
                                new FileReader(chooser.getSelectedFile()));
                        String directory = chooser.getSelectedFile().getParent();
                        String line;
                        ArrayList<JacksFace.UVCoordinate> uvList
                                = new ArrayList<>();
                        HashMap<String, JacksMaterial> materialMap
                                = new HashMap<>();
                        JacksMaterial defaultMaterial = new JacksMaterial("Default");
                        JacksMaterial currentMaterial = null;
                        while ((line = reader.readLine()) != null) {
                            progress += line.length() + 2;
                            jProgressBar1.setValue((int) (100 * progress / fileLength));
                            if (line.startsWith("mtllib ")) {
                                String materialFileName = line.substring("mtllib ".length());
                                File materialFile = new File(directory + File.separator + materialFileName);
                                try {
                                    BufferedReader materialReader
                                            = new BufferedReader(
                                                    new FileReader(materialFile));
                                    String materialLine = "";
                                    JacksMaterial readingMaterial = null;
                                    while ((materialLine = materialReader.readLine()) != null) {
                                        if (materialLine.startsWith("newmtl ")) {
                                            if (readingMaterial != null) {
                                                materialMap.put(readingMaterial.name,
                                                        readingMaterial);
                                            }
                                            String materialName
                                                    = materialLine.substring(
                                                            "newmtl ".length());
                                            readingMaterial = new JacksMaterial(materialName);
                                        } else if (materialLine.startsWith("Ka ")) {
                                            String[] att = materialLine.split("\\s+");
                                            readingMaterial.rA = Float.parseFloat(att[1]);
                                            readingMaterial.gA = Float.parseFloat(att[2]);
                                            readingMaterial.bA = Float.parseFloat(att[3]);
                                        } else if (materialLine.startsWith("Kd ")) {
                                            String[] att = materialLine.split("\\s+");
                                            readingMaterial.r = Float.parseFloat(att[1]);
                                            readingMaterial.g = Float.parseFloat(att[2]);
                                            readingMaterial.b = Float.parseFloat(att[3]);
                                        } else if (materialLine.startsWith("Ks ")) {
                                            String[] att = materialLine.split("\\s+");
                                            readingMaterial.rS = Float.parseFloat(att[1]);
                                            readingMaterial.gS = Float.parseFloat(att[2]);
                                            readingMaterial.bS = Float.parseFloat(att[3]);
                                        } else if (materialLine.startsWith("d ")) {
                                            String[] att = materialLine.split("\\s+");
                                            readingMaterial.a = Float.parseFloat(att[1]);
                                        } else if (materialLine.startsWith("Tr ")) {
                                            String[] att = materialLine.split("\\s+");
                                            readingMaterial.a = 1 - Float.parseFloat(att[1]);
                                        } else if (materialLine.startsWith("Ns ")) {
                                            String[] att = materialLine.split("\\s+");
                                            readingMaterial.specularExponent = (int) Float.parseFloat(att[1]);
                                        } else if (materialLine.toLowerCase().startsWith("map_kd ")) {
                                            String texture
                                                    = materialLine.substring(
                                                            "map_Kd ".length()).trim();
                                            if (!texture.isEmpty()) {
                                                System.out.println(directory
                                                        + File.separator + texture);
                                                try {
                                                    readingMaterial.setTexture(
                                                            ImageIO.read(new File(
                                                                    directory + File.separator + texture)));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        } else if (materialLine.toLowerCase().startsWith("map_bump ")) {
                                            String texture
                                                    = materialLine.substring(
                                                            "map_Bump ".length()).trim();
                                            if (!texture.isEmpty()) {
                                                if (texture.startsWith("-bm ")) {
                                                    texture = texture.substring(
                                                            "-bm ".length()).trim();
                                                    readingMaterial.n = Float
                                                            .parseFloat(texture.substring(0, texture.indexOf(" ")));
                                                    texture = texture.substring(
                                                            texture.indexOf(" ")).trim();
                                                }
                                                System.out.println(directory
                                                        + File.separator + texture);
                                                try {
                                                    readingMaterial.setNormalMap(
                                                            ImageIO.read(new File(
                                                                    directory + File.separator + texture)));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                    if (readingMaterial != null) {
                                        materialMap.put(readingMaterial.name,
                                                readingMaterial);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (line.startsWith("o")) {
                                if (readingObject != null) {
                                    for (JacksFace face : readingObject.faceList) {
                                        if (face.vertexList.length >= 3) {
                                            face.calculateNormalAndCenter();
                                            if (!vertexNormalSpecified || true) {
                                                for (int i : face.vertexList) {
                                                    face.parent.vertexList[i].normal.add(face.normal);
                                                }
                                            }
                                        }
                                    }
                                    for (int i = 0; i < readingObject.vertexList.length; i++) {
                                        readingObject.vertexList[i].normal.normalize();
                                    }
                                    vertexIndexOffset += readingObject.vertexList.length;
                                    glPanel.addGeometry(readingObject);
                                }
                                String name = line.substring("o".length()).trim();
                                System.out.println(name);
                                if (name.isEmpty()) {
                                    name = null;
                                }
                                readingObject = new JacksGeometry(name);
                            } else if (line.startsWith("v ")) {
                                if (readingObject == null) {
                                    readingObject = new JacksGeometry(null);
                                }
                                String[] att = line.split("\\s+");
                                readingObject.addVertex(Float.parseFloat(att[1]),
                                        Float.parseFloat(att[2]),
                                        Float.parseFloat(att[3]));
                            } else if (line.startsWith("vt ")) {
                                String[] att = line.split("\\s+");
                                uvList.add(new JacksFace.UVCoordinate(
                                        Float.parseFloat(att[1]),
                                        Float.parseFloat(att[2])));
                            } else if (line.startsWith("vn ")) {
//                                String[] att = line.split("\\s+");
//                                readingObject.vertexList[currentVertex].normal
//                                        .setXYZ(Float.parseFloat(att[1]),
//                                                Float.parseFloat(att[2]),
//                                                Float.parseFloat(att[3]));
//                                vertexNormalSpecified = true;
                            } else if (line.startsWith("s ")) {
                                smoothMode = line.trim().equals("s 1");
                            } else if (line.startsWith("usemtl ")) {
                                currentMaterial = materialMap.get(
                                        line.substring("usemtl ".length()));
                            } else if (line.startsWith("f ")) {
                                if (readingObject == null) {
                                    readingObject = new JacksGeometry(null);
                                }
                                String[] att = line.replaceAll("\\s+", " ").split(" ");
                                int[] newFace = new int[att.length - 1];
                                JacksFace.UVCoordinate[] newFaceTexture = null;
                                for (int i = 0; i < att.length - 1; i++) {
                                    if (att[i + 1].contains("/")) {
                                        if (newFaceTexture == null) {
                                            newFaceTexture = new JacksFace.UVCoordinate[att.length - 1];
                                        }
                                        String[] arguments = att[i + 1].split("/");
                                        newFace[i] = Integer.parseInt(arguments[0]) - 1 - vertexIndexOffset;
                                        if (!arguments[1].isEmpty()) {
                                            newFaceTexture[i] = uvList.get(
                                                    Integer.parseInt(arguments[1]) - 1);
                                        }
                                    } else {
                                        newFace[i] = Integer.parseInt(att[i + 1]) - 1 - vertexIndexOffset;
                                    }
                                }
                                JacksFace newJacksFace = readingObject.addFace(newFace);
                                newJacksFace.smooth = smoothMode;
                                if (newFaceTexture != null) {
                                    newJacksFace.setUV(newFaceTexture);
                                }
                                if (currentMaterial != null) {
                                    newJacksFace.material = currentMaterial;
                                } else {
                                    newJacksFace.material = defaultMaterial;
                                }
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
                    for (JacksFace face : readingObject.faceList) {
                        if (face.vertexList.length >= 3) {
                            face.calculateNormalAndCenter();
                            if (!vertexNormalSpecified || true) {
                                for (int i : face.vertexList) {
                                    face.parent.vertexList[i].normal.add(face.normal);
                                }
                            }
                        }
                    }
                    for (int i = 0; i < readingObject.vertexList.length; i++) {
                        readingObject.vertexList[i].normal.normalize();
                    }
                    jProgressBar1.setValue(0);
                    glPanel.addGeometry(readingObject);
                    refreshObjectList();
                    System.out.println(readingObject.name);
                }
            };

            Thread thread = new Thread(run);

            thread.start();

        }
    }//GEN-LAST:event_btnAddObjectActionPerformed

    private void cbxShowBackFacesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxShowBackFacesActionPerformed
        glPanel.showBackFace = cbxShowBackFaces.isSelected();
    }//GEN-LAST:event_cbxShowBackFacesActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        LinkedList<JacksObject> toRemove = new LinkedList<>();
        for (JacksObject object : glPanel.selectedObjects) {
            toRemove.add(object);
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
                yRot = -(float) Math.PI / 2;
                xRot = 0;
                updateView();
                break;
            case KeyEvent.VK_NUMPAD3:
                yRot = 0;
                xRot = 0;
                updateView();
                break;
            case KeyEvent.VK_DECIMAL:
                if (glPanel.activeObject != null) {
                    xPos = glPanel.activeObject.x;
                    yPos = glPanel.activeObject.y;
                    zPos = glPanel.activeObject.z;
                    distance = 4;
                    updateView();
                }
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
            JacksObject selectedObject
                    = glPanel.selectOnScreen(new Point(evt.getX(), evt.getY()));
            glPanel.activeObject = selectedObject;
            if (!shift) {
                lstObjects.clearSelection();
                glPanel.selectedObjects.clear();
            }
            ArrayList<JacksGeometry> geometryList = glPanel.getGeometryList();
            for (int i = 0; i < geometryList.size(); i++) {
                if (geometryList.get(i) == selectedObject) {
                    if (!shift) {
                        lstObjects.setSelectedIndex(i);
                        return;
                    } else {
                        lstObjects.addSelectionInterval(i, i);
                    }
                }
            }
            ArrayList<JacksLight> lightList = glPanel.getLightList();
            for (int i = 0; i < lightList.size(); i++) {
                if (lightList.get(i) == selectedObject) {
                    if (!shift) {
                        lstObjects.setSelectedIndex(i + geometryList.size());
                        return;
                    } else {
                        lstObjects.addSelectionInterval(i + geometryList.size(), i + geometryList.size());
                    }
                }
            }
        }
    }//GEN-LAST:event_glPanelMousePressed

    private void cbxOthogonalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxOthogonalActionPerformed
        glPanel.othogonal = cbxOthogonal.isSelected();
    }//GEN-LAST:event_cbxOthogonalActionPerformed

    private void sldAngleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldAngleStateChanged
        glPanel.cameraAngle = (float) (sldAngle.getValue() * Math.PI / 180);
        lblCameraAngle.setText(sldAngle.getValue() + "");
        glPanel.recalculateCamera();
    }//GEN-LAST:event_sldAngleStateChanged

    private void cbxHdriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxHdriActionPerformed
        glPanel.hdriMode = cbxHdri.isSelected();
    }//GEN-LAST:event_cbxHdriActionPerformed

    private void cbxSmoothActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSmoothActionPerformed
        glPanel.smooth = cbxSmooth.isSelected();
    }//GEN-LAST:event_cbxSmoothActionPerformed

    private void cbxForceSmoothActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxForceSmoothActionPerformed
        glPanel.forceSmooth = cbxForceSmooth.isSelected();
    }//GEN-LAST:event_cbxForceSmoothActionPerformed

    private void cbxTextureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxTextureActionPerformed
        glPanel.texture = cbxTexture.isSelected();
    }//GEN-LAST:event_cbxTextureActionPerformed

    private void cbxFreeLookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxFreeLookActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxFreeLookActionPerformed

    private void lstObjectsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstObjectsValueChanged
        if (!objectListLock) {
            glPanel.selectedObjects.clear();
            if (lstObjects.getSelectedIndex() >= 0) {
                if (lstObjects.getSelectedIndex() < glPanel.getGeometryList().size()) {
                    glPanel.activeObject = glPanel.getGeometryList().get(
                            lstObjects.getSelectedIndex());
                } else {
                    glPanel.activeObject = glPanel.getLightList().get(
                            lstObjects.getSelectedIndex() - glPanel.getGeometryList().size());
                }
                if (lstObjects.getSelectedIndices().length > 0) {
                    for (int i : lstObjects.getSelectedIndices()) {
                        if (i < glPanel.getGeometryList().size()) {
                            glPanel.selectedObjects.add(glPanel.getGeometryList().get(i));
                        } else {
                            glPanel.selectedObjects.add(glPanel.getLightList().get(
                                    i - glPanel.getGeometryList().size()));
                        }
                    }
                }
                pnlObjectEdit.setVisible(true);
                populateControls();
                jPanel2.repaint();
            } else if (!shift) {
                glPanel.activeObject = null;
                pnlObjectEdit.setVisible(false);
                pnlLightEdit.setVisible(false);
                pnlMaterialEdit.setVisible(false);
                pnlMaterialControl.setVisible(false);
            }
        }
    }//GEN-LAST:event_lstObjectsValueChanged

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        clipboard.clear();
        for (JacksObject object : glPanel.selectedObjects) {
            clipboard.add(object);
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        if (clipboard.size() > 0) {

            glPanel.selectedObjects.clear();
            for (JacksObject object : clipboard) {
                JacksObject newObject = object.clone();
                newObject.name = object.name + " Copy";
                glPanel.addObject(newObject);
                glPanel.selectedObjects.add(newObject);
                glPanel.activeObject = newObject;
//                System.out.println(object.name + " --> " + readingObject.name);
            }
            System.out.println("dootster");
            for (JacksObject object : glPanel.selectedObjects) {
                System.out.println(object.name);
            }
            objectListLock = true;
            lstObjects.clearSelection();
            refreshObjectList();
            populateControls();
            objectListLock = false;
        }
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void cbxForceResActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxForceResActionPerformed
        applyResolution();
    }//GEN-LAST:event_cbxForceResActionPerformed

    private void spnWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnWidthStateChanged
        applyResolution();
    }//GEN-LAST:event_spnWidthStateChanged

    private void spnHeightStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnHeightStateChanged
        applyResolution();
    }//GEN-LAST:event_spnHeightStateChanged

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

    private void cbxShowNormalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxShowNormalActionPerformed
        glPanel.showNormal = cbxShowNormal.isSelected();
    }//GEN-LAST:event_cbxShowNormalActionPerformed

    private void lstMaterialsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstMaterialsValueChanged
        if (lstMaterials.getSelectedIndex() >= 0) {
            activeMaterial = editingMaterial.get(lstMaterials.getSelectedIndex());
            pnlMaterialControl.setVisible(true);
            populateMaterialControl(activeMaterial);
        }
    }//GEN-LAST:event_lstMaterialsValueChanged

    private void pnlTextureMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlTextureMouseClicked
        pnlTexture.getGraphics().drawImage(currentTexture, 0, 0, this);
    }//GEN-LAST:event_pnlTextureMouseClicked

    private void pnlDiffuseColorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlDiffuseColorMousePressed
        JColorChooser colorChooser = new JColorChooser();
        Color newColor = colorChooser.showDialog(this, "Pick a color",
                pnlDiffuseColor.getBackground());
        if (newColor != null) {
            pnlDiffuseColor.setBackground(newColor);
            applyMaterialControl();
        }
    }//GEN-LAST:event_pnlDiffuseColorMousePressed

    private void pnlSpecularColorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlSpecularColorMousePressed
        JColorChooser colorChooser = new JColorChooser();
        Color newColor = colorChooser.showDialog(this, "Pick a color",
                pnlSpecularColor.getBackground());
        if (newColor != null) {
            pnlSpecularColor.setBackground(newColor);
            applyMaterialControl();
        }
    }//GEN-LAST:event_pnlSpecularColorMousePressed

    private void spnSpecExpStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnSpecExpStateChanged
        if (getFloatFromSpinner(spnSpecExp) < 0) {
            spnSpecExp.setValue(0.0f);
        }
        applyMaterialControl();
    }//GEN-LAST:event_spnSpecExpStateChanged

    private void sldAlphaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldAlphaStateChanged
        applyMaterialControl();
    }//GEN-LAST:event_sldAlphaStateChanged

    private void spnNormalAmountStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnNormalAmountStateChanged
        applyMaterialControl();
    }//GEN-LAST:event_spnNormalAmountStateChanged

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        lstObjects.setSelectionInterval(0, lstObjects.getModel().getSize());
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void sldEnvReflectionStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldEnvReflectionStateChanged
        applyMaterialControl();
    }//GEN-LAST:event_sldEnvReflectionStateChanged

    private void cbxZSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxZSortActionPerformed
        glPanel.zSort = cbxZSort.isSelected();
    }//GEN-LAST:event_cbxZSortActionPerformed

    void applyResolution() {
        glPanel.forceResolution = cbxForceRes.isSelected();
        glPanel.resolutionWidth = (Integer) spnWidth.getValue();
        glPanel.resolutionHeight = (Integer) spnHeight.getValue();
        glPanel.resize();
    }

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
        DefaultListModel model = (DefaultListModel) lstObjects.getModel();
        model.removeAllElements();
        for (JacksGeometry object : glPanel.getGeometryList()) {
            model.addElement(object.name);
            if (glPanel.selectedObjects.contains(object)) {
                lstObjects.addSelectionInterval(model.getSize() - 1, model.getSize() - 1);
            }
        }
        for (JacksLight light : glPanel.getLightList()) {
            model.addElement(light.name);
            if (glPanel.selectedObjects.contains(light)) {
                lstObjects.addSelectionInterval(model.getSize() - 1, model.getSize() - 1);
            }
        }
        refreshStats();
    }

    void populateControls() {
        populateObjectControl(glPanel.activeObject);
        if (glPanel.activeObject instanceof JacksLight) {
            pnlMaterialEdit.setVisible(false);
            pnlLightEdit.setVisible(true);
            populateLightControl((JacksLight) glPanel.activeObject);
        } else if (glPanel.activeObject instanceof JacksGeometry) {
            pnlLightEdit.setVisible(false);
            pnlMaterialEdit.setVisible(true);
            populateMaterialList((JacksGeometry) glPanel.activeObject);
        } else {
            pnlLightEdit.setVisible(false);
            pnlMaterialEdit.setVisible(false);
        }
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

    void populateMaterialControl(JacksMaterial material) {
        objectLock = true;
        pnlDiffuseColor.setBackground(new Color(material.r, material.g, material.b));
        pnlSpecularColor.setBackground(new Color(material.rS, material.gS, material.bS));
        spnSpecExp.setValue((float) material.specularExponent);
        sldAlpha.setValue((int) (material.a * sldAlpha.getMaximum()));
        currentTexture = new BufferedImage(pnlTexture.getWidth(),
                pnlTexture.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        currentNormalMap = new BufferedImage(pnlNormalMap.getWidth(),
                pnlNormalMap.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        currentTexture.getGraphics().drawImage(material.
                getTexture(pnlTexture.getSize()), 0, 0, pnlTexture);
        currentNormalMap.getGraphics().drawImage(material.
                getNormalMap(pnlNormalMap.getSize()), 0, 0, pnlNormalMap);
        pnlTexture.paint(pnlTexture.getGraphics());
        pnlNormalMap.paint(pnlNormalMap.getGraphics());
        spnNormalAmount.setValue(material.n);
        sldEnvReflection.setValue((int) (material.environmentReflection
                * sldEnvReflection.getMaximum()));
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

    void applyMaterialControl() {
        if (!objectLock) {
            activeMaterial.r = (float) pnlDiffuseColor.getBackground().getRed() / 255.0f;
            activeMaterial.g = (float) pnlDiffuseColor.getBackground().getGreen() / 255.0f;
            activeMaterial.b = (float) pnlDiffuseColor.getBackground().getBlue() / 255.0f;
            activeMaterial.rS = (float) pnlSpecularColor.getBackground().getRed() / 255.0f;
            activeMaterial.gS = (float) pnlSpecularColor.getBackground().getGreen() / 255.0f;
            activeMaterial.bS = (float) pnlSpecularColor.getBackground().getBlue() / 255.0f;
            activeMaterial.specularExponent = (int) getFloatFromSpinner(spnSpecExp);
            activeMaterial.a = (float) sldAlpha.getValue() / sldAlpha.getMaximum();
            activeMaterial.n = getFloatFromSpinner(spnNormalAmount);
            activeMaterial.environmentReflection
                    = (float) sldEnvReflection.getValue()
                    / sldEnvReflection.getMaximum();;
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

    void populateMaterialList(JacksGeometry object) {
        DefaultListModel model = (DefaultListModel) lstMaterials.getModel();
        editingMaterial.clear();
        model.clear();
        for (JacksFace face : object.faceList) {
            if (!editingMaterial.contains(face.material)) {
                editingMaterial.add(face.material);
                model.addElement(face.material.name);
            }
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
    private javax.swing.JButton btnAddLight;
    private javax.swing.JButton btnAddObject;
    private javax.swing.JButton btnResetBGnd;
    private javax.swing.JButton btnRotateBGndXDown;
    private javax.swing.JButton btnRotateBGndXUp;
    private javax.swing.JButton btnRotateBGndYLeft;
    private javax.swing.JButton btnRotateBGndYRight;
    private javax.swing.JComboBox<String> cboLightType;
    private javax.swing.JCheckBox cbxForceRes;
    private javax.swing.JCheckBox cbxForceSmooth;
    private javax.swing.JCheckBox cbxFreeLook;
    private javax.swing.JCheckBox cbxHdri;
    private javax.swing.JCheckBox cbxOthogonal;
    private javax.swing.JCheckBox cbxShowBackFaces;
    private javax.swing.JCheckBox cbxShowLight;
    private javax.swing.JCheckBox cbxShowNormal;
    private javax.swing.JCheckBox cbxSmooth;
    private javax.swing.JCheckBox cbxTexture;
    private javax.swing.JCheckBox cbxZSort;
    private JacksGLPanel glMaterialReview;
    private JacksGLPanel glPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblAmbientLight;
    private javax.swing.JLabel lblCameraAngle;
    private javax.swing.JLabel lblEnergy;
    private javax.swing.JLabel lblLightType;
    private javax.swing.JLabel lblLocX;
    private javax.swing.JLabel lblLocY;
    private javax.swing.JLabel lblLocZ;
    private javax.swing.JLabel lblNormalAmount;
    private javax.swing.JLabel lblRotX;
    private javax.swing.JLabel lblRotY;
    private javax.swing.JLabel lblRotZ;
    private javax.swing.JLabel lblScaleX;
    private javax.swing.JLabel lblScaleY;
    private javax.swing.JLabel lblScaleZ;
    private javax.swing.JLabel lblSpecExp;
    private javax.swing.JLabel lblSpecExp1;
    private javax.swing.JList<String> lstMaterials;
    private javax.swing.JList<String> lstObjects;
    private javax.swing.JPanel pnlDiffuseColor;
    private javax.swing.JPanel pnlLightColor;
    private javax.swing.JPanel pnlLightEdit;
    private javax.swing.JPanel pnlMaterialControl;
    private javax.swing.JPanel pnlMaterialEdit;
    private javax.swing.JPanel pnlNormalMap;
    private javax.swing.JPanel pnlObjectEdit;
    private javax.swing.JPanel pnlSpecularColor;
    private javax.swing.JPanel pnlTexture;
    private javax.swing.JSlider sldAlpha;
    private javax.swing.JSlider sldAmbient;
    private javax.swing.JSlider sldAngle;
    private javax.swing.JSlider sldEnvReflection;
    private javax.swing.JSlider sldLightBlue;
    private javax.swing.JSlider sldLightGreen;
    private javax.swing.JSlider sldLightRed;
    private javax.swing.JSpinner spnEnergy;
    private javax.swing.JSpinner spnHeight;
    private javax.swing.JSpinner spnLocX;
    private javax.swing.JSpinner spnLocY;
    private javax.swing.JSpinner spnLocZ;
    private javax.swing.JSpinner spnNormalAmount;
    private javax.swing.JSpinner spnRotX;
    private javax.swing.JSpinner spnRotY;
    private javax.swing.JSpinner spnRotZ;
    private javax.swing.JSpinner spnScaleX;
    private javax.swing.JSpinner spnScaleY;
    private javax.swing.JSpinner spnScaleZ;
    private javax.swing.JSpinner spnSpecExp;
    private javax.swing.JSpinner spnWidth;
    // End of variables declaration//GEN-END:variables
}
