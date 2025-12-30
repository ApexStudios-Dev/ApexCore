package dev.apexstudios.apexcore.core.outline;

import java.util.Arrays;
import java.util.function.Consumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class VertexExtractor {
    private final Consumer<Outline> extractor;
    private final Vector3fc[] vertices = new Vector3fc[4];
    private int vertexIndex = 0;
    private final Vector3f offset = new Vector3f();

    VertexExtractor(Consumer<Outline> extractor) {
        this.extractor = extractor;
    }

    public void setOffset(Vector3fc offset) {
        this.offset.set(offset);
    }

    public void addVertex(float pX, float pY, float pZ) {
        addVertex(new Vector3f(pX, pY, pZ));
    }

    public void addVertex(Vector3fc vertex) {
        vertices[vertexIndex++] = vertex.sub(offset, new Vector3f());

        if(vertexIndex == 4) {
            vertexIndex = 0;
            extractor.accept(Outline.create(vertices[0], vertices[1]));
            extractor.accept(Outline.create(vertices[1], vertices[2]));
            extractor.accept(Outline.create(vertices[2], vertices[3]));
            extractor.accept(Outline.create(vertices[3], vertices[0]));
            Arrays.fill(vertices, null);
        }
    }

    public void unpack(BakedQuad quad) {
        addVertex(quad.position0());
        addVertex(quad.position1());
        addVertex(quad.position2());
        addVertex(quad.position3());
    }
}
