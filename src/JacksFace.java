
import java.util.ArrayList;
import java.util.Arrays;

public class JacksFace {
    int vertexList[] = new int[0];
    JacksVector normal = new JacksVector();
    boolean smooth = false;
    int r = 255;
    int g = 255;
    int b = 255;
    int rS = 255;
    int gS = 255;
    int bS = 255;
    float specular = 1;
    int specularExponent = 3;
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
    
    void calculateNormal() {
        normal = new JacksVector(parent.vertexList[vertexList[0]],
                parent.vertexList[vertexList[1]]);
        normal.crossProduct(new JacksVector(parent.vertexList[vertexList[1]],
                parent.vertexList[vertexList[2]]));
        normal.normalize();
    }
}
