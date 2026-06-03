CREATE INDEX IF NOT EXISTS idx_interaction_created_at ON interaction_log(created_at);
CREATE INDEX IF NOT EXISTS idx_interaction_intent ON interaction_log(intent);
CREATE INDEX IF NOT EXISTS idx_interaction_emotion ON interaction_log(emotion);
CREATE INDEX IF NOT EXISTS idx_interaction_satisfaction ON interaction_log(satisfaction);
CREATE INDEX IF NOT EXISTS idx_feedback_created_at ON feedback(created_at);
CREATE INDEX IF NOT EXISTS idx_retrieval_trace_created_at ON retrieval_trace(created_at);
CREATE INDEX IF NOT EXISTS idx_knowledge_document_status ON knowledge_document(status);
