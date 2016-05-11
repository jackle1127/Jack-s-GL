import java.util.Arrays;

public class JacksFace {
    int vertexList[] = new int[0];
    UVCoordinate uv[] = new UVCoordinate[0];
    JacksVector normal = new JacksVector();
    JacksMaterial material = new JacksMaterial();
    JacksGeometry parent;
    
    JacksFace(JacksGeometry parent) {
        this.parent = parent;
    }
    
    JacksFace(JacksGeometry parent, int...vertices) {
        this(parent);
        setVertices(vertices);
    }
    
    void addVertex(int vertex) {
        vertexList = Arrays.copyOf(vertexList, vertexList.length + 1);
        vertexList[vertexList.length - 1] = vertex;
        if (vertexList.length >= 3) {
            calculateNormal();
        }
    }
    
    void setVertices(int[] vertices) {
        vertexList = Arrays.copyOf(vertices, vertices.length);
        if (vertexList.length >= 3) {
            calculateNormal();
        }
    }
    
    void addUV(UVCoordinate newUV) {
        uv = Arrays.copyOf(uv, uv.length + 1);
        uv[uv.length - 1] = newUV;
    }
    
    void setUV(UVCoordinate[] newUV) {
        uv = Arrays.copyOf(newUV, newUV.length);
    }
    
    void calculateNormal() {
        normal = new JacksVector(parent.vertexList[vertexList[0]],
                parent.vertexList[vertexList[1]]);
        normal.crossProduct(new JacksVector(parent.vertexList[vertexList[1]],
                parent.vertexList[vertexList[2]]));
        normal.normalize();
    }
    
    static class UVCoordinate {
        float u, v;

        public UVCoordinate() {
        }

        public UVCoordinate(float u, float v) {
            this.u = u;
            this.v = v;
        }
    }
}
