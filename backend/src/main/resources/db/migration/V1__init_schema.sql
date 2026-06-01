CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE admin_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'ADMIN',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE scenic_area (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    summary TEXT NOT NULL,
    location VARCHAR(255),
    opening_hours VARCHAR(255),
    contact_phone VARCHAR(64),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE scenic_spot (
    id BIGSERIAL PRIMARY KEY,
    scenic_area_id BIGINT NOT NULL REFERENCES scenic_area(id),
    name VARCHAR(128) NOT NULL,
    alias VARCHAR(255),
    summary TEXT NOT NULL,
    tags VARCHAR(255),
    recommended_minutes INTEGER NOT NULL DEFAULT 20,
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    sort_order INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE route_plan (
    id BIGSERIAL PRIMARY KEY,
    scenic_area_id BIGINT NOT NULL REFERENCES scenic_area(id),
    name VARCHAR(128) NOT NULL,
    interest_tags VARCHAR(255) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    spot_ids VARCHAR(255) NOT NULL,
    guide_text TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    scenic_area_id BIGINT NOT NULL REFERENCES scenic_area(id),
    title VARCHAR(255) NOT NULL,
    file_name VARCHAR(255),
    file_url VARCHAR(512),
    doc_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'active',
    created_by BIGINT REFERENCES admin_user(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE knowledge_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES knowledge_document(id) ON DELETE CASCADE,
    scenic_spot_id BIGINT REFERENCES scenic_spot(id),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    keywords VARCHAR(512),
    aliases VARCHAR(512),
    spot_names VARCHAR(512),
    tags VARCHAR(255),
    search_text TEXT NOT NULL,
    embedding vector(1536),
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE digital_human_profile (
    id BIGSERIAL PRIMARY KEY,
    scenic_area_id BIGINT NOT NULL REFERENCES scenic_area(id),
    name VARCHAR(64) NOT NULL,
    avatar_url VARCHAR(512),
    model_url VARCHAR(512),
    voice_code VARCHAR(128),
    clothing_style VARCHAR(128),
    welcome_text VARCHAR(512) NOT NULL,
    persona_prompt TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE visitor_session (
    id BIGSERIAL PRIMARY KEY,
    scenic_area_id BIGINT NOT NULL REFERENCES scenic_area(id),
    session_token VARCHAR(128) NOT NULL UNIQUE,
    interests VARCHAR(255),
    device_type VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_active_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE interaction_log (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES visitor_session(id),
    input_type VARCHAR(32) NOT NULL,
    user_query TEXT NOT NULL,
    asr_text TEXT,
    intent VARCHAR(64),
    answer TEXT,
    emotion VARCHAR(64),
    source_chunk_ids VARCHAR(255),
    latency_ms INTEGER,
    satisfaction INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE feedback (
    id BIGSERIAL PRIMARY KEY,
    interaction_id BIGINT NOT NULL REFERENCES interaction_log(id) ON DELETE CASCADE,
    score INTEGER NOT NULL,
    comment VARCHAR(512),
    sentiment VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE retrieval_trace (
    id BIGSERIAL PRIMARY KEY,
    interaction_id BIGINT REFERENCES interaction_log(id) ON DELETE SET NULL,
    query TEXT NOT NULL,
    bm25_candidates JSONB NOT NULL DEFAULT '[]'::jsonb,
    keyword_candidates JSONB NOT NULL DEFAULT '[]'::jsonb,
    vector_candidates JSONB NOT NULL DEFAULT '[]'::jsonb,
    final_candidates JSONB NOT NULL DEFAULT '[]'::jsonb,
    rejected BOOLEAN NOT NULL DEFAULT FALSE,
    reject_reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_scenic_spot_area ON scenic_spot(scenic_area_id);
CREATE INDEX idx_route_plan_area ON route_plan(scenic_area_id);
CREATE INDEX idx_knowledge_document_area ON knowledge_document(scenic_area_id);
CREATE INDEX idx_knowledge_chunk_document ON knowledge_chunk(document_id);
CREATE INDEX idx_knowledge_chunk_search_text ON knowledge_chunk USING gin (to_tsvector('simple', search_text));
CREATE INDEX idx_knowledge_chunk_title_trgm ON knowledge_chunk USING gin (title gin_trgm_ops);
CREATE INDEX idx_visitor_session_area ON visitor_session(scenic_area_id);
CREATE INDEX idx_interaction_session ON interaction_log(session_id);
CREATE INDEX idx_retrieval_trace_interaction ON retrieval_trace(interaction_id);
