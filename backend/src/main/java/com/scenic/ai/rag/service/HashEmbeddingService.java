package com.scenic.ai.rag.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class HashEmbeddingService {

    private static final int DIMENSION = 1536;

    public double[] embed(String text) {
        double[] vector = new double[DIMENSION];
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            int index = Math.floorMod(bytes[i] * 31 + i * 17, DIMENSION);
            vector[index] += 1.0;
        }
        byte[] digest = sha256(text);
        for (int i = 0; i < digest.length; i++) {
            int index = Math.floorMod(Byte.toUnsignedInt(digest[i]) * 13 + i, DIMENSION);
            vector[index] += 0.5;
        }
        normalize(vector);
        return vector;
    }

    private byte[] sha256(String text) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void normalize(double[] vector) {
        double sum = 0;
        for (double value : vector) {
            sum += value * value;
        }
        double norm = Math.sqrt(sum);
        if (norm == 0) {
            return;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
    }
}
