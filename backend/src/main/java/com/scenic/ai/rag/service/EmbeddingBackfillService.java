package com.scenic.ai.rag.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class EmbeddingBackfillService implements ApplicationRunner {

    private final EntityManager entityManager;
    private final EmbeddingService embeddingService;
    private final PgVectorFormatter pgVectorFormatter;

    public EmbeddingBackfillService(EntityManager entityManager, EmbeddingService embeddingService, PgVectorFormatter pgVectorFormatter) {
        this.entityManager = entityManager;
        this.embeddingService = embeddingService;
        this.pgVectorFormatter = pgVectorFormatter;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                        SELECT id, search_text
                        FROM knowledge_chunk
                        WHERE enabled = true
                          AND embedding IS NULL
                        LIMIT 200
                        """)
                .getResultList();
        for (Object[] row : rows) {
            Long id = ((Number) row[0]).longValue();
            String searchText = (String) row[1];
            entityManager.createNativeQuery("UPDATE knowledge_chunk SET embedding = CAST(:embedding AS vector) WHERE id = :id")
                    .setParameter("embedding", pgVectorFormatter.format(embeddingService.embed(searchText)))
                    .setParameter("id", id)
                    .executeUpdate();
        }
    }
}
