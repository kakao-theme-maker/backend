-- #188 : design_component_component_type 매핑을 design_board.component_type_id로 옮긴다.
-- DML만 포함하므로 실패 시 파일 전체가 롤백되며, 모든 단계는 재실행해도 결과가 같다.

-- 1) 기존 보드 행 : 컴포넌트에 매핑된 타입 중 가장 작은 component_type_id를 갖는다.
UPDATE design_board db
    JOIN (SELECT design_component_id, MIN(component_type_id) AS component_type_id
          FROM design_component_component_type
          GROUP BY design_component_id) m
    ON m.design_component_id = db.design_component_id
SET db.component_type_id = m.component_type_id
WHERE db.component_type_id IS NULL;

-- 2) 타입이 여러 개인 컴포넌트 : 나머지 타입마다 보드 행을 추가한다 (행 분할).
INSERT INTO design_board (post_id, design_component_id, component_type_id)
SELECT DISTINCT db.post_id, db.design_component_id, m.component_type_id
FROM design_board db
         JOIN design_component_component_type m
              ON m.design_component_id = db.design_component_id
WHERE NOT EXISTS (SELECT 1
                  FROM design_board x
                  WHERE x.post_id = db.post_id
                    AND x.design_component_id = db.design_component_id
                    AND x.component_type_id = m.component_type_id);

-- 3) 매핑이 없던 컴포넌트 : 기본 타입(component_type_id = 1)을 갖는다.
--    pk=1이 없으면 FK 위반으로 파일 전체가 롤백된다.
UPDATE design_board
SET component_type_id = 1
WHERE component_type_id IS NULL;
