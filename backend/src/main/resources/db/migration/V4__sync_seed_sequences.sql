SELECT setval('admin_user_id_seq', COALESCE((SELECT MAX(id) FROM admin_user), 1), true);
SELECT setval('scenic_area_id_seq', COALESCE((SELECT MAX(id) FROM scenic_area), 1), true);
SELECT setval('scenic_spot_id_seq', COALESCE((SELECT MAX(id) FROM scenic_spot), 1), true);
SELECT setval('knowledge_document_id_seq', COALESCE((SELECT MAX(id) FROM knowledge_document), 1), true);
