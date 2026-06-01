package com.scenic.ai.rag.service;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class PgVectorFormatter {

    public String format(double[] vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(String.format(Locale.ROOT, "%.8f", vector[i]));
        }
        return builder.append(']').toString();
    }
}
