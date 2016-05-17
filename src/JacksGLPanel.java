
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class JacksGLPanel extends javax.swing.JPanel {

    private final float INTERPOLATION_CLAMP = .0001f;
    int lightOnScreenSize = 20;
    int numberOfThreads = 4;
    float deltaTime = 0;
    float cameraX = 0;
    float cameraY = 0;
    float cameraZ = 0;

    float cameraRotationX = 0;
    float cameraRotationY = 0;
    float cameraRotationZ = 0;

    float backgroundRotationX = 0;
    float backgroundRotationY = 0;
    float backgroundRotationZ = 0;
    float ambient = 0;

    boolean showLightSources = true;
    boolean showBackFace = false;
    boolean othogonal = false;
    boolean hdriMode = true;
    boolean smooth = true;
    boolean forceSmooth = false;
    boolean texture = true;
    boolean forceResolution = false;
    boolean showNormal = false;
    int resolutionWidth = -1;
    int resolutionHeight = -1;
    float othogonalHeight = 10;

    float cameraAngle = (float) Math.PI * 60.0f / 180.0f;
    float lowClipping = 0f;
    private BufferedImage rendered;
    private BufferedImage hdri;
    private BufferedImage tempImage;
    private Raster renderedRaster;
    private byte[] renderedBytes;
    private byte[] hdriBytes;
    private byte[] tempBytes;
    private JacksVertex projectedVertexMap[] = new JacksVertex[3];
    private JacksIllumination faceIllumination = new JacksIllumination();

    private float tempFloat, vYLength, vXLength, coefficient1, coefficient2;
    private JacksVector v = new JacksVector();
    private JacksVector vX = new JacksVector();
    private JacksVector vY = new JacksVector();
    private JacksVector incrementX = new JacksVector();
    private JacksVector incrementY = new JacksVector();

    private JacksGeometry object;
    private int currentVertexIndex;
    private float fromZ;
    private float toZ;
    private float fromX;
    private float toX;
    private float fromU;
    private float toU;
    private float fromV;
    private float toV;
    private JacksIllumination currentIllumination = new JacksIllumination();
    private JacksVertex fromVertex = new JacksVertex();
    private JacksVertex toVertex = new JacksVertex();
    private JacksVertex currentVertex = new JacksVertex();
    private JacksVector fromUp = new JacksVector();
    private JacksVector toUp = new JacksVector();
    private JacksVector fromRight = new JacksVector();
    private JacksVector toRight = new JacksVector();
    private JacksVector currentUp = new JacksVector();
    private JacksVector currentRight = new JacksVector();
    private float tempU;
    private float tempV;
    private float alpha;
    private float currentZ;
    private int fromXOnScreen;
    private int toXOnScreen;
    private int increment;
    float luminance;
    float luminanceS;
    private float r, g, b;
    private Point point[] = new Point[3];
    private JacksVertex projectedVertex[] = new JacksVertex[3];
    private JacksFace.UVCoordinate uv[] = new JacksFace.UVCoordinate[3];
    private JacksVector up[] = new JacksVector[3];
    private JacksVector right[] = new JacksVector[3];
    private int order[] = new int[3];
    private int temp;
    private JacksLight lights[];
    private JacksVector normal = new JacksVector();
    private JacksVector tempVector = new JacksVector();
    private JacksVertex center = new JacksVertex();
    private JacksVector toCenter = new JacksVector();
    private JacksVertex lightVertex = new JacksVertex();
    private JacksVector lightVector = new JacksVector();
    private JacksVector specularVector = new JacksVector();
    private float lightDistance;
    private int panelWidth;
    private int panelHeight;
    private long startTime;
    private long stopTime;
    private ArrayList<JacksFace> transparentFaceList = new ArrayList<>();

    float cameraHeight;
    float cameraWidth;
    JacksOrigin tempOrigin = new JacksOrigin();
    JacksOrigin tempLightOrigin = new JacksOrigin();
    int tempX;
    int tempY;
    int tempZ;

    private float[][] depthBuffer;
    private JacksOrigin origin = new JacksOrigin();

    private ArrayList<JacksGeometry> geometryList = new ArrayList<>();
    private ArrayList<JacksLight> lightList = new ArrayList<>();
    ArrayList<JacksObject> selectedObjects = new ArrayList<>();
    JacksObject activeObject = null;

    private Thread updateThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                deltaTime = update();
            }
        }
    });

    public JacksGLPanel() {
        for (int i = 0; i < 3; i++) {
            up[i] = new JacksVector();
            right[i] = new JacksVector();
        }
        initComponents();
        resize();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resize();
            }
        });
    }

    void addHdri(BufferedImage hdri) {
        hdriBytes = ((DataBufferByte) hdri.getData().getDataBuffer())
                .getData();
        this.hdri = hdri;
        resize();
    }

    void removeHdri() {
        hdri = null;
    }

    void resize() {
        if (this.getWidth() > 0 && this.getHeight() > 0) {
            if (forceResolution) {
                panelWidth = resolutionWidth;
                panelHeight = resolutionHeight;
            } else {
                panelWidth = this.getWidth();
                panelHeight = this.getHeight();
            }
            depthBuffer = new float[panelHeight][panelWidth];
            rendered = new BufferedImage(panelWidth, panelHeight,
                    BufferedImage.TYPE_3BYTE_BGR);
            tempImage = new BufferedImage(panelWidth, panelHeight,
                    BufferedImage.TYPE_3BYTE_BGR);
            if (hdri != null) {
                tempImage.getGraphics().drawImage(hdri, 0, 0,
                        tempImage.getWidth(), tempImage.getHeight(), null);
                tempBytes = ((DataBufferByte) tempImage
                        .getData().getDataBuffer()).getData();
            }
            renderedBytes = ((DataBufferByte) rendered.getData().getDataBuffer())
                    .getData();
            renderedRaster = Raster.createRaster(rendered.getSampleModel(), new DataBufferByte(renderedBytes, renderedBytes.length), null);
            recalculateCamera();
        }
    }

    void recalculateCamera() {
        cameraHeight = 2 * (float) Math.tan(cameraAngle / 2);
        cameraWidth = cameraHeight * panelWidth / panelHeight;

    }

    long update() {
        startTime = System.nanoTime();
        if (rendered != null && renderedBytes != null) {
            try {
                clearDepthBuffer();
                if (hdri != null) {
                    if (hdriMode) {
                        renderBackground();
                    } else {
                        for (int i = 0; i < tempBytes.length; i++) {
                            renderedBytes[i] = tempBytes[i];
                        }
                    }
                } else {
                    Arrays.fill(renderedBytes, (byte) 0);
                }
                updateTempLightList();
                updateOnScreenVertices();
                currentVertexIndex = 0;

                for (int objectIndex = 0; objectIndex < geometryList.size(); objectIndex++) {
                    object = geometryList.get(objectIndex);
                    drawObject(object, false);
                    currentVertexIndex += object.vertexList.length;
                }
                rendered.setData(renderedRaster);

                // Draw light sources
                Graphics g = rendered.getGraphics();
                if (showLightSources) {
                    for (JacksLight light : lightList) {
                        tempLightOrigin.copyAttribute(origin);
                        transformOrigin(tempLightOrigin, light);
                        lightVertex.setXYZ(0, 0, 0);
                        lightVertex.project(tempLightOrigin);
                        if (lightVertex.z > 0) {
                            continue;
                        }
                        Point lightPoint = xyzToOnScreenXY(lightVertex);

                        g.setColor(new Color(light.r, light.g, light.b));
                        if (activeObject == light) {
                            g.setColor(new Color(255, 128, 0));
                        }
                        g.drawOval(lightPoint.x - lightOnScreenSize / 2,
                                lightPoint.y - lightOnScreenSize / 2,
                                lightOnScreenSize, lightOnScreenSize);
                        if (light.lightType == 1) {
                            tempVector.copyXYZ(light.direction);
                            tempVector.project(tempLightOrigin);
                            tempVector.add(tempLightOrigin.translate);
                            Point secondPoint = xyzToOnScreenXY(tempVector.x,
                                    tempVector.y, tempVector.z);
                            g.drawLine(lightPoint.x, lightPoint.y,
                                    secondPoint.x, secondPoint.y);
                        }
                    }
                    if (showNormal) {
                        int i = 0;
                        g.setColor(Color.CYAN);
                        for (JacksVertex vertex : projectedVertexMap) {
                            if (vertex.z > 0) {
                                continue;
                            }
                            Point vertexPoint = xyzToOnScreenXY(vertex);
                            JacksVertex tail = vertex.clone();
                            tail.x += vertex.normal.x / 3;
                            tail.y += vertex.normal.y / 3;
                            tail.z += vertex.normal.z / 3;
                            Point tailPoint = xyzToOnScreenXY(tail);

                            g.drawLine(vertexPoint.x, vertexPoint.y,
                                    tailPoint.x, tailPoint.y);
                        }
                    }
                }
                this.getGraphics().drawImage(rendered, 0, 0, this.getWidth(),
                        this.getHeight(), this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        stopTime = System.nanoTime();
        return stopTime - startTime;
    }

    private void drawObject(JacksGeometry object, boolean alwaysOnTop) {
        tempOrigin.copyAttribute(origin);
        transformOrigin(tempOrigin, object);
        transparentFaceList.clear();
        for (JacksFace face : object.faceList) {
            if (face.material.a == 1) {
                drawFace(face);
            } else {
                transparentFaceList.add(face);
            }
        }
        for (JacksFace face : transparentFaceList) {
            drawFace(face);
        }
    }

    private void drawFace(JacksFace face) {
        if (face.vertexList.length >= 3) {
            center.setXYZ(0, 0, 0);
            normal.copyXYZ(face.normal);
            normal.project(tempOrigin);
            normal.normalize();
            for (Integer index : face.vertexList) {
                center.x += face.parent.vertexList[index].x;
                center.y += face.parent.vertexList[index].y;
                center.z += face.parent.vertexList[index].z;
            }
            center.x /= face.vertexList.length;
            center.y /= face.vertexList.length;
            center.z /= face.vertexList.length;
            center.project(tempOrigin);
            toCenter.setXYZ(center.x, center.y, center.z);
            if (!showBackFace && toCenter.dotProduct(normal) > 0) {
                return; // Skip back face.
            }

            for (int i = 1; i < face.vertexList.length - 1; i++) {
                // Rasterization
                projectedVertex[0] = projectedVertexMap[face.vertexList[0]
                        + currentVertexIndex];
                projectedVertex[1] = projectedVertexMap[face.vertexList[i]
                        + currentVertexIndex];
                projectedVertex[2] = projectedVertexMap[face.vertexList[i + 1]
                        + currentVertexIndex];

                if (-projectedVertex[0].z <= lowClipping
                        && -projectedVertex[1].z <= lowClipping
                        && -projectedVertex[2].z <= lowClipping) {
                    continue;
                }

                point[0] = xyzToOnScreenXY(projectedVertex[0]);
                point[1] = xyzToOnScreenXY(projectedVertex[1]);
                point[2] = xyzToOnScreenXY(projectedVertex[2]);

                if (!(face.smooth && smooth || forceSmooth)) {
                    calculateLuminance(faceIllumination,
                            center.x, center.y,
                            center.z, normal, face.material);
                }
                if (face.uv.length > 0) {
                    uv[0] = face.uv[0];
                    uv[1] = face.uv[i];
                    uv[2] = face.uv[i + 1];
                }

                if (face.material.normalMap != null) {
                    calculateUp(up[0], projectedVertex[0], projectedVertex[1],
                            projectedVertex[2], uv[0], uv[1], uv[2]);
                    calculateUp(up[1], projectedVertex[1], projectedVertex[2],
                            projectedVertex[0], uv[1], uv[2], uv[0]);
                    calculateUp(up[2], projectedVertex[2], projectedVertex[0],
                            projectedVertex[1], uv[2], uv[0], uv[1]);
                    calculateRight(right[0], projectedVertex[0], projectedVertex[1],
                            projectedVertex[2], uv[0], uv[1], uv[2]);
                    calculateRight(right[1], projectedVertex[1], projectedVertex[2],
                            projectedVertex[0], uv[1], uv[2], uv[0]);
                    calculateRight(right[2], projectedVertex[2], projectedVertex[0],
                            projectedVertex[1], uv[2], uv[0], uv[1]);
                }

                if (Math.abs((float) (point[0].x * (point[2].y - point[1].y)
                        + point[1].x * (point[2].y - point[0].y)
                        + point[2].x * (point[0].y - point[1].y)) / 2) > panelWidth * panelHeight) {
                    continue;
                }

                if ((point[0].x < 0 && point[1].x < 0 && point[2].x < 0)
                        || (point[0].y < 0 && point[1].y < 0 && point[2].y < 0)
                        || (point[0].x > panelWidth && point[1].x > panelWidth
                        && point[2].x > panelWidth)
                        || (point[0].y > panelHeight && point[1].y > panelHeight
                        && point[2].y > panelHeight)) {
                    continue;
                }
                // If face is closer than clipping distance

                order[0] = 0;
                order[1] = 1;
                order[2] = 2;

                // Sort the points by y coordinate. (bubble sort...derps)
                if (point[order[0]].y > point[order[1]].y) {
                    temp = order[0];
                    order[0] = order[1];
                    order[1] = temp;
                }
                if (point[order[1]].y > point[order[2]].y) {
                    temp = order[1];
                    order[1] = order[2];
                    order[2] = temp;
                }
                if (point[order[0]].y > point[order[1]].y) {
                    temp = order[0];
                    order[0] = order[1];
                    order[1] = temp;
                }

                for (int y = point[order[0]].y; y <= point[order[2]].y; y++) {
                    toZ = interpolateZbyY(projectedVertex[order[0]].y,
                            projectedVertex[order[0]].z,
                            projectedVertex[order[2]].y,
                            projectedVertex[order[2]].z,
                            y, panelHeight, cameraHeight);
                    toXOnScreen = (int) linear(point[order[0]].y, point[order[2]].y,
                            y, point[order[0]].x, point[order[2]].x);
                    if (!testDifference(projectedVertex[order[0]].z,
                            projectedVertex[order[2]].z)) {
                        alpha = (toZ - projectedVertex[order[0]].z)
                                / (projectedVertex[order[2]].z
                                - projectedVertex[order[0]].z);
                    } else {
                        alpha = point[order[0]].y == point[order[2]].y
                                ? 1
                                : (float) (y - point[order[0]].y)
                                / (float) (point[order[2]].y
                                - point[order[0]].y);
                    }
                    toX = linear(alpha, projectedVertex[order[0]].x,
                            projectedVertex[order[2]].x);
                    if (face.smooth && smooth || forceSmooth) {
                        linear(toVertex, alpha, projectedVertex[order[0]],
                                projectedVertex[order[2]]);
                    }
                    if (face.uv.length > 0 && uv[0] != null) {
                        toU = linear(alpha, uv[order[0]].u, uv[order[2]].u);
                        toV = linear(alpha, uv[order[0]].v, uv[order[2]].v);
                    }
                    if (face.material.normalMap != null) {
                        linear(toUp, alpha, up[order[0]], up[order[2]]);
                        linear(toRight, alpha, right[order[0]], right[order[2]]);
                    }
                    if (y <= point[order[1]].y) {
                        // First half

                        fromZ = interpolateZbyY(projectedVertex[order[0]].y,
                                projectedVertex[order[0]].z,
                                projectedVertex[order[1]].y,
                                projectedVertex[order[1]].z,
                                y, panelHeight, cameraHeight);

                        if (!testDifference(projectedVertex[order[0]].z,
                                projectedVertex[order[1]].z)) {
                            alpha = (fromZ - projectedVertex[order[0]].z)
                                    / (projectedVertex[order[1]].z
                                    - projectedVertex[order[0]].z);
                        } else {
                            alpha = point[order[0]].y == point[order[1]].y
                                    ? 0
                                    : (float) (y - point[order[0]].y)
                                    / (float) (point[order[1]].y
                                    - point[order[0]].y);
                        }

                        fromX = linear(alpha, projectedVertex[order[0]].x,
                                projectedVertex[order[1]].x);
                        if (face.uv.length > 0 && uv[0] != null) {
                            fromU = linear(alpha, uv[order[0]].u,
                                    uv[order[1]].u);
                            fromV = linear(alpha, uv[order[0]].v,
                                    uv[order[1]].v);
                        }
                        fromXOnScreen = (int) linear(point[order[0]].y,
                                point[order[1]].y, y,
                                point[order[0]].x, point[order[1]].x);
                        if (face.smooth && smooth || forceSmooth) {
                            linear(fromVertex, alpha, projectedVertex[order[0]],
                                    projectedVertex[order[1]]);
                        }
                        if (face.material.normalMap != null) {
                            linear(fromUp, alpha, up[order[0]], up[order[1]]);
                            linear(fromRight, alpha, right[order[0]], right[order[1]]);
                        }
                    } else {// Second half
                        fromZ = interpolateZbyY(projectedVertex[order[1]].y,
                                projectedVertex[order[1]].z,
                                projectedVertex[order[2]].y,
                                projectedVertex[order[2]].z,
                                y, panelHeight, cameraHeight);

                        if (!testDifference(projectedVertex[order[1]].z, projectedVertex[order[2]].z)) {
                            alpha = (fromZ - projectedVertex[order[1]].z)
                                    / (projectedVertex[order[2]].z
                                    - projectedVertex[order[1]].z);
                        } else {
                            alpha = point[order[1]].y == point[order[2]].y
                                    ? 0
                                    : (float) (y - point[order[1]].y)
                                    / (float) (point[order[2]].y
                                    - point[order[1]].y);
                        }

                        fromX = linear(alpha, projectedVertex[order[1]].x,
                                projectedVertex[order[2]].x);
                        if (face.uv.length > 0 && uv[0] != null) {
                            fromU = linear(alpha, uv[order[1]].u,
                                    uv[order[2]].u);
                            fromV = linear(alpha, uv[order[1]].v,
                                    uv[order[2]].v);
                        }

                        fromXOnScreen = (int) linear(point[order[1]].y,
                                point[order[2]].y, y,
                                point[order[1]].x, point[order[2]].x);
                        if (face.smooth && smooth || forceSmooth) {
                            linear(fromVertex, alpha, projectedVertex[order[1]],
                                    projectedVertex[order[2]]);
                        }
                        if (face.material.normalMap != null) {
                            linear(fromUp, alpha, up[order[1]], up[order[2]]);
                            linear(fromRight, alpha, right[order[1]], right[order[2]]);
                        }
                    }
                    increment = fromXOnScreen > toXOnScreen ? -1 : 1;
                    if (fromXOnScreen != toXOnScreen) {
                        for (int x = fromXOnScreen; x != toXOnScreen; x += increment) {
                            drawPixel(face, x, y, i, false);
                        }
                    }
                    drawPixel(face, toXOnScreen, y, i, false);
                }
            }
        }
    }

    private void drawPixel(JacksFace face, int x, int y, int i, boolean alwaysOnTop) {
        if (x > 0 && x < rendered.getWidth()
                && y > 0 && y < rendered.getHeight()) {
            currentZ = interpolateZbyX(
                    fromX, fromZ, toX, toZ,
                    x, panelWidth, cameraWidth);
            if (!testDifference(fromZ, toZ)) {
                alpha = (currentZ - fromZ) / (toZ - fromZ);
            } else {
                alpha = fromXOnScreen == toXOnScreen
                        ? 0
                        : (float) (x - fromXOnScreen)
                        / (float) (toXOnScreen - fromXOnScreen);
            }
            if (-currentZ > 0 && -currentZ < depthBuffer[y][x] || alwaysOnTop) {
                linear(currentVertex, alpha, fromVertex, toVertex);
                if (face.uv[0] != null) {
                    tempU = linear(alpha, fromU, toU);
                    tempV = linear(alpha, fromV, toV);
                } else {
                    tempU = 0;
                    tempV = 0;
                }
                if (face.material.texture == null || !texture) {
                    r = face.material.r;
                    g = face.material.g;
                    b = face.material.b;
                } else {
                    r = face.material.getR(tempU, tempV);
                    g = face.material.getG(tempU, tempV);
                    b = face.material.getB(tempU, tempV);
                }
                if (face.material.normalMap != null) {
                    linear(currentUp, alpha, fromUp, toUp);
                    linear(currentRight, alpha, fromRight, toRight);
                    currentUp.normalize();
                    currentRight.normalize();
                    currentUp.multiply((face.material.getNormalY(tempU, tempV) - .5f) * face.material.n);
                    currentRight.multiply((face.material.getNormalX(tempU, tempV) - .5f) * face.material.n);
                    currentVertex.normal.multiply(face.material.getNormalZ(tempU, tempV) - .5f);
                    currentVertex.normal.add(currentUp, currentRight);
                    currentVertex.normal.normalize();
                }
                if (face.smooth && smooth || forceSmooth) {
                    calculateLuminance(currentIllumination,
                            currentVertex.x, currentVertex.y,
                            currentVertex.z, currentVertex.normal, face.material);
                } else {
                    currentIllumination.copyAttribute(faceIllumination);
                }

                r = r * currentIllumination.dR;
                g = g * currentIllumination.dG;
                b = b * currentIllumination.dB;

                if (face.material.a < 1) {
                    r = (1 - face.material.a)
                            * byteToFloat(renderedBytes[(y * panelWidth + x) * 3 + 2]) / 255
                            + face.material.a * r;
                    g = (1 - face.material.a)
                            * byteToFloat(renderedBytes[(y * panelWidth + x) * 3 + 1]) / 255
                            + face.material.a * g;
                    b = (1 - face.material.a)
                            * byteToFloat(renderedBytes[(y * panelWidth + x) * 3]) / 255
                            + face.material.a * b;
                }

                r += currentIllumination.sR;
                g += currentIllumination.sG;
                b += currentIllumination.sB;

                if (hdri != null) {
                    int rgb = getRGBByVector(currentVertex.normal);
                    float red = (float) ((rgb >> 16) & 0xFF) / 255;
                    float green = (float) ((rgb >> 8) & 0xFF) / 255;
                    float blue = (float) (rgb & 0xFF) / 255;
                    r = .5f * r + .5f * red;
                    g = .5f * g + .5f * green;
                    b = .5f * b + .5f * blue;
                }
                if (r > 1) {
                    r = 1;
                }
                if (g > 1) {
                    g = 1;
                }
                if (b > 1) {
                    b = 1;
                }
                renderedBytes[(y * panelWidth + x) * 3]
                        = (byte) (b * 255);
                renderedBytes[(y * panelWidth + x) * 3 + 1]
                        = (byte) (g * 255);
                renderedBytes[(y * panelWidth + x) * 3 + 2]
                        = (byte) (r * 255);

                depthBuffer[y][x] = -currentZ;
            }
        }
    }

    void calculateUp(JacksVector upVector, JacksVertex v1, JacksVertex v2,
            JacksVertex v3,
            JacksFace.UVCoordinate uv1, JacksFace.UVCoordinate uv2,
            JacksFace.UVCoordinate uv3) {
        if (uv1.u == uv2.u) {
            coefficient1 = 0;
            coefficient2 = 1.0f / (uv1.v - uv2.v);
        } else if (uv1.u == uv3.u) {
            coefficient1 = 1.0f / (uv1.v - uv3.v);
            coefficient2 = 0;
        } else {
            coefficient1 = 1.0f / ((uv1.v - uv3.v) - (uv1.u - uv3.u) * (uv1.v - uv2.v) / (uv1.u - uv2.u));
            coefficient2 = (-coefficient1 * (uv1.u - uv3.u)) / (uv1.u - uv2.u);
        }
        upVector.x = coefficient1 * (v1.x - v3.x) + coefficient2 * (v1.x - v2.x);
        upVector.y = coefficient1 * (v1.y - v3.y) + coefficient2 * (v1.y - v2.y);
        upVector.z = coefficient1 * (v1.z - v3.z) + coefficient2 * (v1.z - v2.z);
//        System.out.println(coefficient1 + "-" + coefficient2 + "-" + v1 + "-" + v2 + "-" + v3 + "-" + uv1 + "-" + uv2 + "-" + uv3);
    }

    void calculateRight(JacksVector upVector, JacksVertex v1, JacksVertex v2,
            JacksVertex v3,
            JacksFace.UVCoordinate uv1, JacksFace.UVCoordinate uv2,
            JacksFace.UVCoordinate uv3) {
        if (uv1.v == uv2.v) {
            coefficient1 = 0;
            coefficient2 = 1.0f / (uv1.u - uv2.u);
        } else if (uv1.v == uv3.v) {
            coefficient1 = 1.0f / (uv1.u - uv3.u);
            coefficient2 = 0;
        } else {
            coefficient1 = 1.0f / ((uv1.u - uv3.u) - (uv1.v - uv3.v) * (uv1.u - uv2.u) / (uv1.v - uv2.v));
            coefficient2 = (-coefficient1 * (uv1.v - uv3.v)) / (uv1.v - uv2.v);
        }
        upVector.x = coefficient1 * (v1.x - v3.x) + coefficient2 * (v1.x - v2.x);
        upVector.y = coefficient1 * (v1.y - v3.y) + coefficient2 * (v1.y - v2.y);
        upVector.z = coefficient1 * (v1.z - v3.z) + coefficient2 * (v1.z - v2.z);
    }

    float interpolateZbyX(float x1, float z1, float x2, float z2, int xS, int wS,
            float w) {
        if (testDifference(z1, z2)) {
//            System.out.println("boop " + z1 + " - " + System.currentTimeMillis());
            return z1;
        }
        tempFloat = (x1 - z1 * (x2 - x1) / (z2 - z1))
                / (-(x2 - x1) / (z2 - z1)
                + (.5f - (float) xS / (float) wS) * w);
        if ((tempFloat - z1) / (z2 - z1) > 1) {
            return z2;
        }
        if ((tempFloat - z1) / (z2 - z1) < 0) {
            return z1;
        }
        return tempFloat;

    }

    float interpolateZbyY(float y1, float z1, float y2, float z2, int yS, int hS,
            float h) {
        if (testDifference(z1, z2)) {
            return z1;
        }
        tempFloat = (z1 * (y2 - y1) / (z2 - z1) - y1)
                / ((y2 - y1) / (z2 - z1)
                + ((.5f - (float) yS / (float) hS) * h));
        if ((tempFloat - z1) / (z2 - z1) > 1) {
            return z2;
        }
        if ((tempFloat - z1) / (z2 - z1) < 0) {
            return z1;
        }
        return tempFloat;
    }

    private void calculateLuminance(JacksIllumination illumination,
            float x, float y, float z, JacksVector n, JacksMaterial material) {
        n.normalize();
        luminance = 0;
        luminanceS = 0;
        illumination.dR = ambient;
        illumination.dG = ambient;
        illumination.dB = ambient;
        illumination.sR = 0;
        illumination.sG = 0;
        illumination.sB = 0;
        toCenter.setXYZ(x, y, z);
        toCenter.normalize();
        if (lightList.size() > 0) {
            for (JacksLight light : lights) {
                if (light.lightType == JacksLight.TYPE_POINT) {
                    lightVector.setXYZ(x - light.x, y - light.y, z - light.z);
                    specularVector.copyXYZ(lightVector);
                    lightDistance = lightVector.x * lightVector.x
                            + lightVector.y * lightVector.y
                            + lightVector.z * lightVector.z;

                    luminance = -lightVector.dotProduct(n)
                            / lightDistance;
                    if (luminance > 0) {
                        illumination.dR += light.energy * (float) light.r / 255.0f * luminance;
                        illumination.dG += light.energy * (float) light.g / 255.0f * luminance;
                        illumination.dB += light.energy * (float) light.b / 255.0f * luminance;
                    }
                    // If light shines on face, not behind.
                    if (lightVector.dotProduct(n) < 0) {
                        lightVector.normalize();
                        tempVector.copyXYZ(n);
                        tempVector.multiply(-2 * lightVector.dotProduct(n));
                        lightVector.add(tempVector);

                        luminanceS = -lightVector.dotProduct(toCenter);
                        if (showBackFace) {
                            luminanceS = Math.abs(luminanceS);
                        }
                        if (luminanceS > 0) {
                            luminanceS = (float) Math.pow(luminanceS, material.specularExponent);
                            illumination.sR += material.specular
                                    * material.rS * luminanceS;
                            illumination.sG += material.specular
                                    * material.gS * luminanceS;
                            illumination.sB += material.specular
                                    * material.bS * luminanceS;
                        }
                    }
                } else if (light.lightType == JacksLight.TYPE_DIRECTIONAL) {
                    lightVector.copyXYZ(light.direction);
                    lightVector.normalize();
                    luminance = -lightVector.dotProduct(n);
                    if (luminance > 0) {
                        illumination.dR += light.energy * (float) light.r / 255.0f * luminance;
                        illumination.dG += light.energy * (float) light.g / 255.0f * luminance;
                        illumination.dB += light.energy * (float) light.b / 255.0f * luminance;
                    }
                    if (lightVector.dotProduct(n) < 0) {
                        tempVector.copyXYZ(n);
                        tempVector.multiply(-2 * lightVector.dotProduct(n));
                        lightVector.add(tempVector);

                        luminanceS = -lightVector.dotProduct(toCenter);
                        if (luminanceS > 0) {
                            tempFloat = luminanceS;
                            for (int i = 1; i <= material.specularExponent; i++) {
                                luminanceS *= tempFloat;
                            }
                            illumination.sR += material.specular
                                    * material.rS * luminanceS;
                            illumination.sG += material.specular
                                    * material.gS * luminanceS;
                            illumination.sB += material.specular
                                    * material.bS * luminanceS;
                        }
                    }
                }
            }
        } else {
            illumination.dR += -toCenter.dotProduct(n);
            illumination.dG += -toCenter.dotProduct(n);
            illumination.dB += -toCenter.dotProduct(n);
        }

        if (illumination.dR < 0) {
            illumination.dR = 0;
        }
        if (illumination.dG < 0) {
            illumination.dG = 0;
        }
        if (illumination.dB < 0) {
            illumination.dB = 0;
        }

        if (illumination.sR < 0) {
            illumination.sR = 0;
        }
        if (illumination.sG < 0) {
            illumination.sG = 0;
        }
        if (illumination.sB < 0) {
            illumination.sB = 0;
        }

        if (selectedObjects.contains(object)) {
            illumination.dR = .7f;
            illumination.dG = .4f;
            illumination.sR = .7f;
            illumination.sG = .4f;
        }
        if (activeObject == object) {
            illumination.dR = 1;
            illumination.dG = .5f;
            illumination.sR = 1;
            illumination.sG = .5f;
        }
    }

    private boolean testDifference(float n1, float n2) {
        return n1 - n2 > -INTERPOLATION_CLAMP
                && n1 - n2 < INTERPOLATION_CLAMP;
    }

    private void transformOrigin(JacksOrigin origin, JacksObject object) {
        origin.translate(object.x, object.y, object.z);
        origin.rotate(cameraRotationX, 0, 0);
        origin.rotate(0, 0, cameraRotationZ);
        origin.rotate(0, cameraRotationY, 0);
        origin.rotate(object.rotateX, object.rotateY, object.rotateZ);
        origin.rotate(0, -cameraRotationY, 0);
        origin.rotate(0, 0, -cameraRotationZ);
        origin.rotate(-cameraRotationX, 0, 0);
        origin.x.multiply(object.scaleX);
        origin.y.multiply(object.scaleY);
        origin.z.multiply(object.scaleZ);
    }

    private Point xyzToOnScreenXY(float x, float y, float z) {
        return new Point((int) (panelWidth * (.5 - x / (-Math.abs(z) * cameraWidth))),
                (int) (panelHeight * (.5 + y / (-Math.abs(z) * cameraHeight))));
    }

    private Point xyzToOnScreenXY(JacksVertex vertex) {
        if (!othogonal) {
            if (vertex.z == 0) {
                return new Point((int) (panelWidth * vertex.x / Math.abs(vertex.x)),
                        (int) (-panelHeight * vertex.y / Math.abs(vertex.y)));
            }
            return new Point((int) (panelWidth * (.5 - vertex.x
                    / (-Math.abs(vertex.z) * cameraWidth))),
                    (int) (panelHeight * (.5 + vertex.y
                    / (-Math.abs(vertex.z) * cameraHeight))));
        } else {
            return new Point((int) (panelWidth * (.5 + vertex.x
                    / (othogonalHeight * panelWidth / panelHeight))),
                    (int) (panelHeight * (.5 - vertex.y
                    / othogonalHeight)));
        }
    }

    private void updateTempLightList() {
        int i = 0;
        for (JacksLight light : lightList) {
            tempLightOrigin.copyAttribute(origin);
            transformOrigin(tempLightOrigin, light);
            lights[i].copyAttribute(light);
            lights[i].x = 0;
            lights[i].y = 0;
            lights[i].z = 0;
            lights[i++].project(tempLightOrigin);
        }
    }

    private void updateTempLightListSize() {
        lights = new JacksLight[lightList.size()];
        int i = 0;
        for (JacksLight light : lightList) {
            JacksLight newLight = light.clone();
            lights[i++] = newLight;
        }
    }

    private void updateOnScreenVertices() {
        int i = 0;
        for (JacksGeometry object : geometryList) {
            tempOrigin.copyAttribute(origin);
            transformOrigin(tempOrigin, object);

            for (JacksVertex vertex : object.vertexList) {
                projectedVertexMap[i].setXYZ(vertex.x, vertex.y, vertex.z);
                projectedVertexMap[i].normal.copyXYZ(vertex.normal);
                projectedVertexMap[i].project(tempOrigin);
                projectedVertexMap[i].normal.normalize();
//                locationMap[i] = xyzToOnScreenXY(projectedVertexMap[i]);
                i++;
            }
        }
    }

    private void clearDepthBuffer() {
        for (int i = 0; i < depthBuffer.length; i++) {
            for (int j = 0; j < depthBuffer[0].length; j++) {
                depthBuffer[i][j] = Float.POSITIVE_INFINITY;
            }
        }
    }

    private void updateVertexListSize() {
        int numberOfVertex = 0;
        for (JacksGeometry object : geometryList) {
            numberOfVertex += object.vertexList.length;
        }
//        locationMap = new Point[numberOfVertex];
        projectedVertexMap = new JacksVertex[numberOfVertex];
        for (int i = 0; i < projectedVertexMap.length; i++) {
            projectedVertexMap[i] = new JacksVertex();
        }
    }

    void updateOrigin() {
        origin.reset();
        origin.rotate(0, -cameraRotationY, 0);
        origin.rotate(0, 0, -cameraRotationZ);
        origin.rotate(-cameraRotationX, 0, 0);
        origin.translate(-cameraX, -cameraY, -cameraZ);
    }

    ArrayList<JacksGeometry> getGeometryList() {
        ArrayList<JacksGeometry> newList = new ArrayList<>();
        for (JacksGeometry geometry : geometryList) {
            newList.add(geometry);
        }
        return newList;
    }

    ArrayList<JacksLight> getLightList() {
        ArrayList<JacksLight> newList = new ArrayList<>();
        for (JacksLight light : lightList) {
            newList.add(light);
        }
        return newList;
    }

    void addGeometry(JacksGeometry newGeometry) {
        geometryList.add(newGeometry);
        updateVertexListSize();
    }

    void addLight(JacksLight newLight) {
        lightList.add(newLight);
        updateTempLightListSize();
    }

    void removeGeometry(JacksGeometry geometry) {
        geometry.destroy();
        geometryList.remove(geometry);
        updateVertexListSize();
        System.gc();
    }

    void removeLight(JacksLight light) {
        light.destroy();
        lightList.remove(light);
        updateTempLightListSize();
        System.gc();
    }

    void removeObject(JacksObject object) {
        if (object instanceof JacksGeometry) {
            removeGeometry((JacksGeometry) object);
        } else if (object instanceof JacksLight) {
            removeLight((JacksLight) object);
        }
        System.gc();
    }

    void addObject(JacksObject object) {
        if (object instanceof JacksGeometry) {
            addGeometry((JacksGeometry) object);
        } else if (object instanceof JacksLight) {
            addLight((JacksLight) object);
        }
    }

    void startUpdating() {
        updateThread.start();
    }

    void stopUpdating() {
        updateThread.stop();
    }

    private void renderBackground() {
        if (hdri != null) {
            final int hdriHeight = hdri.getHeight();
            final int hdriWidth = hdri.getWidth();

            vYLength = (float) Math.tan(cameraAngle / 2);
            vXLength = vYLength * (float) panelWidth / (float) panelHeight;

            v.setXYZ(0, 0, -1);

            vX.setXYZ(-vXLength, 0, 0);

            v.rotateX(cameraRotationX);
            v.rotateZ(cameraRotationZ);
            v.rotateY(cameraRotationY);
            vX.rotateX(cameraRotationX);
            vX.rotateZ(cameraRotationZ);
            vX.rotateY(cameraRotationY);

            v.rotateX(backgroundRotationX);
            v.rotateZ(backgroundRotationZ);
            v.rotateY(backgroundRotationY);
            vX.rotateX(backgroundRotationX);
            vX.rotateZ(backgroundRotationZ);
            vX.rotateY(backgroundRotationY);

            vY.copyXYZ(JacksVector.crossProduct(v, vX));
            vY.normalize();
            vY.multiply(vYLength);
            incrementX.copyXYZ(vX);
            incrementY.copyXYZ(vY);
            incrementX.multiply(-2.0f / (float) panelWidth);
            incrementY.multiply(-2.0f / (float) panelHeight);
            LinkedList<Thread> threadList = new LinkedList();
            int indexIncrement = (renderedBytes.length / 3) / numberOfThreads;
            for (int i = 0; i < renderedBytes.length / 3;
                    i += indexIncrement) {
                final int startIndex = i;
                int candidateStopIndex = i + indexIncrement - 1;
                if (candidateStopIndex >= panelWidth * panelHeight - 1) {
                    candidateStopIndex = panelWidth * panelHeight - 1;
                }
                final int stopIndex = candidateStopIndex;
                final int startX = i % panelWidth;
                final int startY = i / panelWidth;
                final JacksVector threadVector = JacksVector.addNewVector(v, vX, vY);
                threadVector.add(JacksVector.multiply(startX, incrementX));
                threadVector.add(JacksVector.multiply(startY, incrementY));

                threadList.add(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JacksVector currentVector = threadVector.clone();
                        for (int j = startIndex; j <= stopIndex; j++) {
                            int newX = (int) (hdriWidth * (Math.PI
                                    - Math.atan2(currentVector.x, currentVector.z))
                                    / (2 * Math.PI));
                            int newY = (int) (hdriHeight * Math.acos(currentVector.y
                                    / currentVector.length()) / Math.PI);

                            newX = rotateNumber(newX, hdriWidth);
                            newY = rotateNumber(newY, hdriHeight);

                            renderedBytes[j * 3]
                                    = hdriBytes[(newY * hdriWidth + newX) * 3];
                            renderedBytes[j * 3 + 1]
                                    = hdriBytes[(newY * hdriWidth + newX) * 3 + 1];
                            renderedBytes[j * 3 + 2]
                                    = hdriBytes[(newY * hdriWidth + newX) * 3 + 2];
                            currentVector.add(incrementX);
                            if (j % panelWidth == 0 && j != startIndex) {
                                currentVector.add(incrementY);
                                currentVector.add(vX);
                                currentVector.add(vX);
                            }
                        }

                    }
                }));
                threadList.getLast().run();
            }
            for (Thread thread : threadList) {
                try {
                    thread.join();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private float linear(float startProgress, float endProgress, float progress,
            float start, float end) {
        return testDifference(startProgress, endProgress)
                ? start
                : start + (end - start) * (progress - startProgress)
                / (endProgress - startProgress);
    }

    private float linear(float alpha, float start, float end) {
        if (alpha < 0) {
            return start;
        }
        if (alpha > 1) {
            return end;
        }
        return start + (end - start) * alpha;
    }

    private void linear(JacksIllumination newIllumination,
            float alpha, JacksIllumination start, JacksIllumination end) {
        newIllumination.dR = linear(alpha, start.dR, end.dR);
        newIllumination.dG = linear(alpha, start.dG, end.dG);
        newIllumination.dB = linear(alpha, start.dB, end.dB);
        newIllumination.sR = linear(alpha, start.sR, end.sR);
        newIllumination.sG = linear(alpha, start.sG, end.sG);
        newIllumination.sB = linear(alpha, start.sB, end.sB);
    }

    private void linear(JacksVertex newVertex,
            float alpha, JacksVertex start, JacksVertex end) {
        newVertex.x = linear(alpha, start.x, end.x);
        newVertex.y = linear(alpha, start.y, end.y);
        newVertex.z = linear(alpha, start.z, end.z);
        newVertex.normal.x = linear(alpha, start.normal.x, end.normal.x);
        newVertex.normal.y = linear(alpha, start.normal.y, end.normal.y);
        newVertex.normal.z = linear(alpha, start.normal.z, end.normal.z);
    }

    private void linear(JacksVector newVector,
            float alpha, JacksVector start, JacksVector end) {
        newVector.x = linear(alpha, start.x, end.x);
        newVector.y = linear(alpha, start.y, end.y);
        newVector.z = linear(alpha, start.z, end.z);
    }

    private float linear(int startProgress, int endProgress, int progress, int start, int end) {
        return startProgress == endProgress
                ? start
                : start + (end - start) * (progress - startProgress)
                / (endProgress - startProgress);
    }

    private int getRGBByVector(JacksVector vector) {
        int hdriWidth = hdri.getWidth();
        int hdriHeight = hdri.getHeight();

        int newX = (int) (hdriWidth
                * ((Math.PI - Math.atan2(vector.x, vector.z)) / (2 * Math.PI)));
        int newY = (int) (hdriHeight
                * (Math.acos(vector.y / vector.length()) / Math.PI));

        newX = rotateNumber(newX, hdriWidth);
        newY = rotateNumber(newY, hdriHeight);
        return hdri.getRGB(newX, newY);
    }

    private int rotateNumber(int x, int max) {
        x = x % max;
        if (x < 0) {
            x = max + x;
        }
        return x;
    }

    private double rotateNumber(double x, double max) {
        x = x % max;
        if (x < 0) {
            x = max + x;
        }
        return x;
    }

    private boolean pointBelongsToPlane(Point point, Point... planePoints) {
        if (planePoints.length <= 2) {
            return false;
        }
        int intersections = 0;
        for (int i = 0; i < planePoints.length; i++) {
            if (point.y >= planePoints[i].y
                    && point.y <= planePoints[(i + 1) % planePoints.length].y
                    || point.y <= planePoints[i].y
                    && point.y >= planePoints[(i + 1) % planePoints.length].y) {
                if (linear(planePoints[i].y,
                        planePoints[(i + 1) % planePoints.length].y,
                        point.y, planePoints[i].x,
                        planePoints[(i + 1) % planePoints.length].x) >= point.x) {
                    intersections++;
                }
            }
        }
        if (intersections == 1) {
            return true;
        } else {
            return false;
        }
    }

    private float byteToFloat(byte number) {
        return number >= 0
                ? number
                : number + 256;
    }

    JacksObject selectOnScreen(Point mouseLocation) {
        for (JacksLight light : lightList) {
            JacksOrigin tempOrigin = origin.clone();
            transformOrigin(tempOrigin, light);

            Point lightLocation = xyzToOnScreenXY(tempOrigin.translate.x,
                    tempOrigin.translate.y, tempOrigin.translate.z);
            if ((lightLocation.x - mouseLocation.x)
                    * (lightLocation.x - mouseLocation.x)
                    + (lightLocation.y - mouseLocation.y)
                    * (lightLocation.y - mouseLocation.y)
                    <= lightOnScreenSize * lightOnScreenSize / 4) {
                return light;
            }
        }
        int selectVertexIndex = 0;
        for (JacksGeometry geometry : geometryList) {
            for (JacksFace face : geometry.faceList) {
                Point[] facePoints = new Point[face.vertexList.length];
                int j = 0;
                for (int i : face.vertexList) {
                    facePoints[j++] = xyzToOnScreenXY(projectedVertexMap[i
                            + selectVertexIndex]);
                }
                if (pointBelongsToPlane(mouseLocation, facePoints)) {
                    return geometry;
                }
            }
            selectVertexIndex += geometry.vertexList.length;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
