CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_embedding
ON knowledge_chunk
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 16)
WHERE embedding IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_keywords_trgm
ON knowledge_chunk
USING gin (keywords gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_spot_names_trgm
ON knowledge_chunk
USING gin (spot_names gin_trgm_ops);
