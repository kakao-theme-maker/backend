-- #188 : 디자인 컴포넌트는 더 이상 component type을 갖지 않으므로 조인 테이블을 제거한다.
-- 보드에 연결되지 않은 컴포넌트의 타입 매핑은 마이그레이션 전 백업(mysqldump)에만 남는다.
DROP TABLE design_component_component_type;
