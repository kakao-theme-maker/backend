-- design_board가 component_type을 직접 참조하도록 컬럼과 unique key를 확장한다.
-- post_id FK는 별도 인덱스 없이 unique_design_board를 인덱스로 사용하므로,
-- unique key는 반드시 같은 ALTER 문 안에서 교체한다 (분리하면 ERROR 1553).
-- component_type_id는 백필 이후 NOT NULL로 전환한다 (V202611151702).
ALTER TABLE design_board
    ADD COLUMN component_type_id INT NULL,
    ADD CONSTRAINT fk_design_board_component_type
        FOREIGN KEY (component_type_id) REFERENCES component_type (component_type_id),
    DROP INDEX unique_design_board,
    ADD CONSTRAINT unique_design_board UNIQUE (post_id, design_component_id, component_type_id);
