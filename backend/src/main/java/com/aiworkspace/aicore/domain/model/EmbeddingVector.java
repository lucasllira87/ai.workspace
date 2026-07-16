package com.aiworkspace.aicore.domain.model;

import java.util.Arrays;

public final class EmbeddingVector {

    private final float[] values;
    private final int dimensions;
    private final ModelId model;

    private EmbeddingVector(float[] values, ModelId model) {
        this.values = Arrays.copyOf(values, values.length);
        this.dimensions = values.length;
        this.model = model;
    }

    public static EmbeddingVector of(float[] values, ModelId model) {
        return new EmbeddingVector(values, model);
    }

    public float[] values() {
        return Arrays.copyOf(values, values.length);
    }

    public int dimensions() {
        return dimensions;
    }

    public ModelId model() {
        return model;
    }

    // Returns the pgvector-compatible string format: [v1,v2,...,vN]
    public String toPgVectorString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(values[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmbeddingVector that)) return false;
        return dimensions == that.dimensions
                && Arrays.equals(values, that.values)
                && model.equals(that.model);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(values);
        result = 31 * result + dimensions;
        result = 31 * result + model.hashCode();
        return result;
    }
}
