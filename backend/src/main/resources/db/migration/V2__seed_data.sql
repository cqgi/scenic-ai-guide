INSERT INTO admin_user (username, password_hash, display_name, role)
VALUES (
    'admin',
    '$2a$10$6EmNcmMjpi541ZLD2H8OD.sIf3xyATI2Q2v3CZIdMktxf7/HUJc6m',
    '景区管理员',
    'ADMIN'
);

INSERT INTO scenic_area (id, name, summary, location, opening_hours, contact_phone)
VALUES (
    1,
    '栖霞山示范景区',
    '栖霞山示范景区以山水景观、历史遗迹和秋季红叶闻名，适合历史文化游、自然风光游和亲子研学游。',
    '江苏省南京市栖霞区',
    '08:00-17:00',
    '025-00000000'
);

INSERT INTO scenic_spot (id, scenic_area_id, name, alias, summary, tags, recommended_minutes, sort_order)
VALUES
    (1, 1, '游客中心', '服务中心,入口大厅', '游客中心提供咨询、路线推荐、休息和导览服务，是进入景区后的第一站。', 'service,intro', 10, 1),
    (2, 1, '明镜湖', '湖心景观,镜湖', '明镜湖水面开阔，倒映山色，是适合拍照和轻松散步的自然景观点。', 'nature,photo', 20, 2),
    (3, 1, '古寺遗址', '栖霞古寺,寺庙遗址', '古寺遗址承载景区重要的佛教文化和历史记忆，适合历史文化讲解。', 'history,culture', 30, 3),
    (4, 1, '红叶谷', '枫叶谷,秋景谷', '红叶谷以秋季红叶景观著称，是景区自然风光游的核心节点。', 'nature,photo,family', 35, 4),
    (5, 1, '观景台', '山顶平台,远眺台', '观景台视野开阔，可俯瞰山体和城市风貌，适合作为路线终点。', 'nature,photo', 20, 5),
    (6, 1, '碑刻长廊', '石刻长廊,碑廊', '碑刻长廊展示历代题刻和文化印记，适合人文深度游。', 'history,culture', 25, 6);

INSERT INTO route_plan (scenic_area_id, name, interest_tags, duration_minutes, spot_ids, guide_text)
VALUES
    (1, '历史文化精讲线', 'history,culture', 120, '1,3,6,5', '从游客中心出发，重点参观古寺遗址和碑刻长廊，最后到观景台回顾景区历史脉络。'),
    (1, '自然风光轻松线', 'nature,photo', 100, '1,2,4,5', '从游客中心进入后游览明镜湖、红叶谷和观景台，适合拍照和轻松游览。'),
    (1, '亲子研学体验线', 'family,culture,nature', 90, '1,2,4,6', '路线兼顾自然观察和文化认知，适合亲子家庭边走边学。');

INSERT INTO knowledge_document (id, scenic_area_id, title, file_name, doc_type, status, created_by)
VALUES
    (1, 1, '栖霞山示范景区基础讲解词', 'seed-guide.md', 'markdown', 'active', 1);

INSERT INTO knowledge_chunk (document_id, scenic_spot_id, title, content, summary, keywords, aliases, spot_names, tags, search_text, metadata_json)
VALUES
    (1, 3, '古寺遗址讲解', '古寺遗址是栖霞山示范景区的重要历史文化节点，承载佛教文化、古代建筑和地方历史记忆。游客在这里可以了解景区从古代寺院到现代文旅空间的演变。', '古寺遗址历史文化讲解。', '古寺,佛教文化,历史,遗址', '栖霞古寺,寺庙遗址', '古寺遗址', 'history,culture', '古寺遗址 栖霞古寺 寺庙遗址 佛教文化 历史 建筑 地方记忆', '{"source":"seed"}'),
    (1, 4, '红叶谷讲解', '红叶谷是景区最具代表性的自然景观点之一，秋季红叶层次丰富，适合摄影打卡和自然风光游览。非秋季也可欣赏山谷植被和步道景观。', '红叶谷自然风光讲解。', '红叶,秋景,摄影,自然', '枫叶谷,秋景谷', '红叶谷', 'nature,photo,family', '红叶谷 枫叶谷 秋景谷 红叶 秋季 摄影 自然风光 山谷 植被 步道', '{"source":"seed"}'),
    (1, 6, '碑刻长廊讲解', '碑刻长廊集中展示历代题刻、书法和地方文化记忆，是了解景区人文积淀的重要地点。对书法、历史和传统文化感兴趣的游客建议重点参观。', '碑刻长廊人文讲解。', '碑刻,书法,历史,人文', '石刻长廊,碑廊', '碑刻长廊', 'history,culture', '碑刻长廊 石刻长廊 碑廊 题刻 书法 地方文化 人文 历史', '{"source":"seed"}');

INSERT INTO digital_human_profile (scenic_area_id, name, avatar_url, model_url, voice_code, clothing_style, welcome_text, persona_prompt)
VALUES (
    1,
    '小栖',
    '/assets/digital-human/default-avatar.png',
    '/assets/digital-human/default-model.json',
    'female_warm_01',
    '宋韵汉服',
    '你好，我是你的景区 AI 导游小栖，可以为你讲解景点、推荐路线，也可以回答游览问题。',
    '你是一位亲切、专业、熟悉栖霞山示范景区历史文化和自然风光的 AI 数字人导游。'
);
