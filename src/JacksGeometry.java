
import java.util.Arrays;
import java.util.HashSet;
import javafx.scene.shape.Mesh;

public class JacksGeometry extends JacksObject {

    static final int GEOMETRY_TYPE_MESH = 0;
    static final int GEOMETRY_TYPE_MATH = 1;
    static final int GEOMETRY_TYPE_PATH = 2;
    int type;
    private int id = -1;
    JacksVertex[] vertexList = new JacksVertex[0];
    JacksFace[] faceList = new JacksFace[0];
    private static HashSet<Integer> usedId = new HashSet<>();

    JacksGeometry(String name) {
        type = GEOMETRY_TYPE_MESH;
        if (name == null) {
            int newId = findId();
            id = newId;
            this.name = "Object " + newId;
            usedId.add(newId);
        }
    }

    JacksGeometry(int type, String name) {
        this.type = type;
        if (name == null) {
            int newId = findId();
            id = newId;
            this.name = "Object " + newId;
            usedId.add(newId);
        }
    }

    private int findId() {
        int result = 0;
        while (usedId.contains(result)) {
            result++;
        }
        return result;
    }

    JacksVertex addVertex() {
        JacksVertex newVertex = new JacksVertex();
        vertexList = Arrays.copyOf(vertexList, vertexList.length + 1);
        vertexList[vertexList.length - 1] = newVertex;
        return newVertex;
    }

    JacksVertex addVertex(float x, float y, float z) {
        JacksVertex newVertex = new JacksVertex(x, y, z);
        vertexList = Arrays.copyOf(vertexList, vertexList.length + 1);
        vertexList[vertexList.length - 1] = newVertex;
        return newVertex;
    }

    JacksFace addFace() {
        JacksFace newFace = new JacksFace(this);
        faceList = Arrays.copyOf(faceList, faceList.length + 1);
        faceList[faceList.length - 1] = newFace;
        return newFace;
    }

    JacksFace addFace(int... vertexIndices) {
        JacksFace newFace = new JacksFace(this, vertexIndices);
        faceList = Arrays.copyOf(faceList, faceList.length + 1);
        faceList[faceList.length - 1] = newFace;
        return newFace;
    }

    protected JacksGeometry clone() {
        JacksGeometry newObject = new JacksGeometry(this.type, this.name);
        newObject.x = x;
        newObject.y = y;
        newObject.z = z;
        newObject.rotateX = rotateX;
        newObject.rotateY = rotateY;
        newObject.rotateZ = rotateZ;
        newObject.scaleX = scaleX;
        newObject.scaleY = scaleY;
        newObject.scaleZ = scaleZ;

        for (JacksVertex current : vertexList) {
            newObject.addVertex(current.x, current.y, current.z).normal
                    = current.normal.clone();
        }
        for (JacksFace face : faceList) {
            JacksFace newFace = newObject.addFace();
            newFace.setVertices(Arrays.copyOf(face.vertexList, face.vertexList.length));
            newFace.material = face.material;
            newFace.normal = face.normal;
            newFace.smooth = face.smooth;
            newFace.uv = face.uv.clone();
        }
        return newObject;
    }

    void destroy() {
        if (id >= 0) {
            usedId.remove((Integer) id);
        }
        for (JacksFace face : faceList) {
            face.parent = null;
        }
        faceList = null;
    }
}
