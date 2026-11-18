-- #188 : 백필이 끝난 component_type_id를 NOT NULL로 전환한다.
-- 구버전 서버가 내려간 뒤에 실행해야 한다 (구버전은 component_type_id 없이 INSERT 한다).
ALTER TABLE design_board
    MODIFY component_type_id INT NOT NULL;
