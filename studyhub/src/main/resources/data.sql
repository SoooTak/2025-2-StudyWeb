INSERT INTO study(title, description, category, capacity, status, image_url, created_at)
SELECT '알고리즘 스터디(DB)', '기본기 강화', 'CS', 5, 'OPEN', NULL, NOW()
WHERE NOT EXISTS (SELECT 1 FROM study);
UPDATE study SET apply_deadline = DATE_ADD(NOW(), INTERVAL 7 DAY) WHERE id = 1;