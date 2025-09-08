package com.komentum.theme.component;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.component.service.ColorStyleService;
import com.komentum.theme.component.service.ComponentTypeService;
import com.komentum.theme.component.service.DesignComponentService;
import com.komentum.theme.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({ColorStyleService.class, ComponentTypeService.class, DesignComponentService.class})
class ThemeComponentWorkflowTest {

    @Autowired
    private ComponentTypeService componentTypeService;

    @Autowired
    private DesignComponentService designComponentService;

    @Autowired
    private ColorStyleService colorStyleService;

    @Autowired
    private ComponentTypeRepository componentTypeRepository;

    @Autowired
    private DesignComponentRepository designComponentRepository;

    @Autowired
    private ColorStyleRepository colorStyleRepository;

    @Test
    void completeWorkflow_CreateComponentTypeToDesignComponentToColorStyle() {
        // === 1단계: ComponentType 생성 ===
        ComponentType componentType = ComponentType.builder()
                .explain("통합테스트 버튼 컴포넌트")
                .platform(Platform.IOS)
                .componentPath("/ios/integration-button")
                .componentName("IntegrationButton")
                .sizeX(120)
                .sizeY(60)
                .build();

        ComponentType savedComponentType = componentTypeService.createComponentType(componentType);
        
        assertThat(savedComponentType.getComponentTypeId()).isNotNull();
        assertThat(savedComponentType.getExplain()).isEqualTo("통합테스트 버튼 컴포넌트");
        assertThat(savedComponentType.getSizeX()).isEqualTo(120);
        assertThat(savedComponentType.getSizeY()).isEqualTo(60);

        // === 2단계: DesignComponent 생성 (ComponentType 연관관계) ===
        CreateDesignComponentRequest designRequest = new CreateDesignComponentRequest();
        designRequest.setUserEmail("integration-test@example.com");
        designRequest.setImageUrl("https://example.com/integration-button.png");
        designRequest.setComponentTypeId(savedComponentType.getComponentTypeId());
        designRequest.setIsPublic(true);

        DesignComponent savedDesignComponent = designComponentService.createDesignComponent(designRequest);
        
        assertThat(savedDesignComponent.getDesignComponentId()).isNotNull();
        assertThat(savedDesignComponent.getUserEmail()).isEqualTo("integration-test@example.com");
        assertThat(savedDesignComponent.getComponentType()).isNotNull();
        assertThat(savedDesignComponent.getComponentType().getComponentTypeId())
                .isEqualTo(savedComponentType.getComponentTypeId());
        assertThat(savedDesignComponent.getIsPublic()).isTrue();
        assertThat(savedDesignComponent.getCreatedAt()).isNotNull();

        // === 3단계: ColorStyle 생성 ===
        ColorStyle colorStyle = ColorStyle.builder()
                .explain("통합테스트 버튼 배경색")
                .platform(Platform.IOS)
                .styleSheetPath("styles/ios.css")
                .styleElementName(".integration-button")
                .stylePropsName("background-color")
                .build();

        ColorStyle savedColorStyle = colorStyleService.createColorStyle(colorStyle);
        
        assertThat(savedColorStyle.getColorTypeId()).isNotNull();
        assertThat(savedColorStyle.getExplain()).isEqualTo("통합테스트 버튼 배경색");
        assertThat(savedColorStyle.getPlatform()).isEqualTo(Platform.IOS);
        assertThat(savedColorStyle.getStyleElementName()).isEqualTo(".integration-button");
        assertThat(savedColorStyle.getStylePropsName()).isEqualTo("background-color");

        // === 4단계: 연관관계 및 데이터 무결성 검증 ===
        // Repository 직접 조회로 연관관계 확인 (지연 로딩 문제 회피)
        List<DesignComponent> relatedDesigns = designComponentRepository.findAll().stream()
                .filter(d -> d.getComponentType().getComponentTypeId().equals(savedComponentType.getComponentTypeId()))
                .toList();
        assertThat(relatedDesigns).hasSize(1);
        assertThat(relatedDesigns.get(0).getUserEmail()).isEqualTo("integration-test@example.com");

        // Repository 직접 조회로 데이터 일관성 확인
        assertThat(componentTypeRepository.count()).isGreaterThan(0);
        assertThat(designComponentRepository.count()).isGreaterThan(0);
        assertThat(colorStyleRepository.count()).isGreaterThan(0);
    }

    @Test
    void updateWorkflow_PartialUpdatesWithRelationshipPreservation() {
        // === 1단계: 초기 데이터 생성 ===
        ComponentType originalType = componentTypeService.createComponentType(ComponentType.builder()
                .explain("수정 테스트 컴포넌트")
                .platform(Platform.IOS)
                .componentPath("/ios/update-test")
                .componentName("UpdateTestComponent")
                .sizeX(100)
                .sizeY(50)
                .build());

        CreateDesignComponentRequest designRequest = new CreateDesignComponentRequest();
        designRequest.setUserEmail("update-test@example.com");
        designRequest.setImageUrl("https://example.com/update-test.png");
        designRequest.setComponentTypeId(originalType.getComponentTypeId());
        designRequest.setIsPublic(false);

        DesignComponent originalDesign = designComponentService.createDesignComponent(designRequest);

        ColorStyle originalStyle = colorStyleService.createColorStyle(ColorStyle.builder()
                .explain("원본 색상 설명")
                .platform(Platform.IOS)
                .styleSheetPath("styles/ios.css")
                .styleElementName(".original")
                .stylePropsName("color")
                .build());

        // === 2단계: 부분 업데이트 테스트 ===
        // ComponentType 부분 업데이트 (일부 필드만 변경)
        ComponentType typeUpdate = ComponentType.builder()
                .explain("수정된 컴포넌트 설명")
                .sizeX(150)  // 크기만 변경
                // 다른 필드들은 null -> 기존 값 유지
                .build();

        ComponentType updatedType = componentTypeService.updateComponentType(originalType.getComponentTypeId(), typeUpdate);
        
        assertThat(updatedType.getExplain()).isEqualTo("수정된 컴포넌트 설명");
        assertThat(updatedType.getSizeX()).isEqualTo(150);
        assertThat(updatedType.getSizeY()).isEqualTo(50);  // 기존 값 유지
        assertThat(updatedType.getComponentPath()).isEqualTo("/ios/update-test");  // 기존 값 유지
        assertThat(updatedType.getComponentName()).isEqualTo("UpdateTestComponent");  // 기존 값 유지

        // DesignComponent 부분 업데이트
        DesignComponent designUpdate = DesignComponent.builder()
                .isPublic(true)  // 비공개 → 공개
                .imageUrl("https://example.com/updated-design.png")
                // userEmail, componentType은 변경하지 않음
                .build();

        DesignComponent updatedDesign = designComponentService.updateDesignComponent(
                originalDesign.getDesignComponentId(), designUpdate);
        
        assertThat(updatedDesign.getIsPublic()).isTrue();
        assertThat(updatedDesign.getImageUrl()).isEqualTo("https://example.com/updated-design.png");
        assertThat(updatedDesign.getUserEmail()).isEqualTo("update-test@example.com");  // 기존 값 유지
        assertThat(updatedDesign.getComponentType().getComponentTypeId())
                .isEqualTo(originalType.getComponentTypeId());  // 연관관계 유지

        // ColorStyle 부분 업데이트
        ColorStyle styleUpdate = ColorStyle.builder()
                .explain("수정된 색상 설명")
                .platform(Platform.ANDROID)
                .styleSheetPath("android/colors.xml")
                .styleElementName("View")
                .stylePropsName("updated_android_color")
                .build();

        ColorStyle updatedStyle = colorStyleService.updateColorStyle(originalStyle.getColorTypeId(), styleUpdate);
        
        assertThat(updatedStyle.getExplain()).isEqualTo("수정된 색상 설명");
        assertThat(updatedStyle.getStylePropsName()).isEqualTo("updated_android_color");

        // === 3단계: 업데이트 후 연관관계 재검증 ===
        DesignComponent finalDesign = designComponentService.getDesignComponentById(originalDesign.getDesignComponentId());
        assertThat(finalDesign.getComponentType().getExplain()).isEqualTo("수정된 컴포넌트 설명");
        assertThat(finalDesign.getComponentType().getSizeX()).isEqualTo(150);
    }

    @Test
    void multipleDesignComponentsWithSharedComponentType() {
        // === 1단계: 공유될 ComponentType 생성 ===
        ComponentType sharedType = componentTypeService.createComponentType(ComponentType.builder()
                .explain("공유 컴포넌트 타입")
                .platform(Platform.IOS)
                .componentPath("/ios/shared")
                .componentName("SharedComponent")
                .sizeX(200)
                .sizeY(100)
                .build());

        // === 2단계: 동일한 ComponentType을 사용하는 여러 DesignComponent 생성 ===
        CreateDesignComponentRequest request1 = new CreateDesignComponentRequest();
        request1.setUserEmail("user1@shared-test.com");
        request1.setImageUrl("https://example.com/design1.png");
        request1.setComponentTypeId(sharedType.getComponentTypeId());
        request1.setIsPublic(true);

        CreateDesignComponentRequest request2 = new CreateDesignComponentRequest();
        request2.setUserEmail("user2@shared-test.com");
        request2.setImageUrl("https://example.com/design2.png");
        request2.setComponentTypeId(sharedType.getComponentTypeId());
        request2.setIsPublic(false);

        CreateDesignComponentRequest request3 = new CreateDesignComponentRequest();
        request3.setUserEmail("user3@shared-test.com");
        request3.setImageUrl("https://example.com/design3.png");
        request3.setComponentTypeId(sharedType.getComponentTypeId());
        request3.setIsPublic(true);

        DesignComponent design1 = designComponentService.createDesignComponent(request1);
        DesignComponent design2 = designComponentService.createDesignComponent(request2);
        DesignComponent design3 = designComponentService.createDesignComponent(request3);

        // === 3단계: 연관관계 검증 ===
        assertThat(design1.getComponentType().getComponentTypeId()).isEqualTo(sharedType.getComponentTypeId());
        assertThat(design2.getComponentType().getComponentTypeId()).isEqualTo(sharedType.getComponentTypeId());
        assertThat(design3.getComponentType().getComponentTypeId()).isEqualTo(sharedType.getComponentTypeId());

        // 각각 고유한 ID를 가지는지 확인
        assertThat(design1.getDesignComponentId()).isNotEqualTo(design2.getDesignComponentId());
        assertThat(design2.getDesignComponentId()).isNotEqualTo(design3.getDesignComponentId());
        assertThat(design1.getDesignComponentId()).isNotEqualTo(design3.getDesignComponentId());

        // === 4단계: ComponentType에서 역방향 연관관계 조회 ===
        // Repository 직접 조회로 연관관계 확인 (지연 로딩 문제 회피)
        List<DesignComponent> relatedDesigns = designComponentRepository.findAll().stream()
                .filter(d -> d.getComponentType().getComponentTypeId().equals(sharedType.getComponentTypeId()))
                .toList();
        assertThat(relatedDesigns).hasSize(3);

        // 사용자별로 제대로 생성되었는지 확인
        assertThat(relatedDesigns)
                .extracting(DesignComponent::getUserEmail)
                .containsExactlyInAnyOrder("user1@shared-test.com", "user2@shared-test.com", "user3@shared-test.com");

        // 공개/비공개 설정 확인
        long publicCount = relatedDesigns.stream()
                .mapToLong(d -> d.getIsPublic() ? 1 : 0)
                .sum();
        assertThat(publicCount).isEqualTo(2);  // design1, design3가 공개
    }

    @Test
    void deleteWorkflow_CascadeAndDependencyHandling() {
        // === 1단계: 연관관계 데이터 생성 ===
        ComponentType componentType = componentTypeService.createComponentType(ComponentType.builder()
                .explain("삭제 테스트 컴포넌트")
                .platform(Platform.IOS)
                .componentPath("/ios/delete-test")
                .componentName("DeleteTestComponent")
                .build());

        CreateDesignComponentRequest designRequest = new CreateDesignComponentRequest();
        designRequest.setUserEmail("delete-test@example.com");
        designRequest.setImageUrl("https://example.com/delete-test.png");
        designRequest.setComponentTypeId(componentType.getComponentTypeId());
        designRequest.setIsPublic(true);

        DesignComponent designComponent = designComponentService.createDesignComponent(designRequest);

        ColorStyle colorStyle = colorStyleService.createColorStyle(ColorStyle.builder()
                .explain("삭제 테스트 색상")
                .platform(Platform.IOS)
                .styleSheetPath("styles/ios.css")
                .styleElementName(".delete-test")
                .stylePropsName("color")
                .build());

        // === 2단계: 데이터 존재 확인 ===
        assertThat(componentTypeRepository.existsById(componentType.getComponentTypeId())).isTrue();
        assertThat(designComponentRepository.existsById(designComponent.getDesignComponentId())).isTrue();
        assertThat(colorStyleRepository.existsById(colorStyle.getColorTypeId())).isTrue();

        // === 3단계: 개별 삭제 및 예외 처리 테스트 ===
        // ColorStyle 삭제 (독립적)
        colorStyleService.deleteColorStyle(colorStyle.getColorTypeId());
        assertThatThrownBy(() -> colorStyleService.getColorStyleById(colorStyle.getColorTypeId()))
                .isInstanceOf(ResourceNotFoundException.class);

        // DesignComponent 삭제
        designComponentService.deleteDesignComponent(designComponent.getDesignComponentId());
        assertThatThrownBy(() -> designComponentService.getDesignComponentById(designComponent.getDesignComponentId()))
                .isInstanceOf(ResourceNotFoundException.class);

        // ComponentType 삭제 (연관된 DesignComponent가 제거되었으므로 안전)
        componentTypeService.deleteComponentType(componentType.getComponentTypeId());
        assertThatThrownBy(() -> componentTypeService.getComponentTypeById(componentType.getComponentTypeId()))
                .isInstanceOf(ResourceNotFoundException.class);

        // === 4단계: Repository 레벨에서 삭제 확인 ===
        assertThat(componentTypeRepository.existsById(componentType.getComponentTypeId())).isFalse();
        assertThat(designComponentRepository.existsById(designComponent.getDesignComponentId())).isFalse();
        assertThat(colorStyleRepository.existsById(colorStyle.getColorTypeId())).isFalse();
    }

    @Test
    void invalidReferences_ShouldFailWithProperExceptions() {
        // === 존재하지 않는 ComponentType 참조 테스트 ===
        CreateDesignComponentRequest invalidRequest = new CreateDesignComponentRequest();
        invalidRequest.setUserEmail("invalid@test.com");
        invalidRequest.setImageUrl("https://test.com/invalid.png");
        invalidRequest.setComponentTypeId(99999);
        invalidRequest.setIsPublic(true);

        assertThatThrownBy(() -> designComponentService.createDesignComponent(invalidRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ComponentType not found with id: 99999");

        // === 존재하지 않는 엔티티 조회 테스트 ===
        assertThatThrownBy(() -> componentTypeService.getComponentTypeById(88888))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ComponentType not found with id: 88888");

        assertThatThrownBy(() -> designComponentService.getDesignComponentById(77777))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DesignComponent not found with id: 77777");

        assertThatThrownBy(() -> colorStyleService.getColorStyleById(66666))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ColorStyle not found with id: 66666");
    }

    @Test
    void bulkOperationsWithRelationships() {
        // === 1단계: 대량 ComponentType 생성 ===
        ComponentType[] types = new ComponentType[3];
        for (int i = 0; i < 3; i++) {
            types[i] = componentTypeService.createComponentType(ComponentType.builder()
                    .explain("벌크 테스트 타입 " + i)
                    .platform(Platform.IOS)
                    .componentPath("/ios/bulk-" + i)
                    .componentName("BulkComponent" + i)
                    .sizeX(100 + i * 20)
                    .sizeY(50 + i * 10)
                    .build());
        }

        // === 2단계: 각 ComponentType에 여러 DesignComponent 생성 ===
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {  // 각 타입당 2개씩
                CreateDesignComponentRequest request = new CreateDesignComponentRequest();
                request.setUserEmail("bulk-user-" + i + "-" + j + "@test.com");
                request.setImageUrl("https://test.com/bulk-" + i + "-" + j + ".png");
                request.setComponentTypeId(types[i].getComponentTypeId());
                request.setIsPublic(j == 0);  // 첫 번째만 공개

                designComponentService.createDesignComponent(request);
            }
        }

        // === 3단계: 대량 ColorStyle 생성 ===
        for (int i = 0; i < 5; i++) {
            colorStyleService.createColorStyle(ColorStyle.builder()
                    .explain("벌크 색상 " + i)
                    .platform(Platform.IOS)
                    .styleSheetPath("styles/ios.css")
                    .styleElementName(".bulk-" + i)
                    .stylePropsName("color")
                    .build());
        }

        // === 4단계: 데이터 개수 및 관계 검증 ===
        assertThat(componentTypeRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(designComponentRepository.count()).isGreaterThanOrEqualTo(6);  // 3 타입 × 2 컴포넌트
        assertThat(colorStyleRepository.count()).isGreaterThanOrEqualTo(5);

        // 각 ComponentType이 정확히 2개의 DesignComponent를 가지는지 확인
        for (ComponentType type : types) {
            // Repository 직접 조회로 연관관계 확인 (지연 로딩 문제 회피)
            List<DesignComponent> typeRelatedDesigns = designComponentRepository.findAll().stream()
                    .filter(d -> d.getComponentType().getComponentTypeId().equals(type.getComponentTypeId()))
                    .toList();
            assertThat(typeRelatedDesigns).hasSize(2);
            
            // 공개/비공개 조합 확인
            long publicCount = typeRelatedDesigns.stream()
                    .mapToLong(d -> d.getIsPublic() ? 1 : 0)
                    .sum();
            assertThat(publicCount).isEqualTo(1);  // 각 타입당 1개씩 공개
        }
    }

    @Test
    void updateWithRelationshipChange() {
        // === 1단계: 두 개의 ComponentType 생성 ===
        ComponentType type1 = componentTypeService.createComponentType(ComponentType.builder()
                .explain("첫 번째 타입")
                .platform(Platform.IOS)
                .componentPath("/ios/type1")
                .componentName("Type1Component")
                .build());

        ComponentType type2 = componentTypeService.createComponentType(ComponentType.builder()
                .explain("두 번째 타입")
                .platform(Platform.IOS)
                .componentPath("/ios/type2")
                .componentName("Type2Component")
                .build());

        // === 2단계: type1을 참조하는 DesignComponent 생성 ===
        CreateDesignComponentRequest request = new CreateDesignComponentRequest();
        request.setUserEmail("relationship-test@example.com");
        request.setImageUrl("https://test.com/relationship.png");
        request.setComponentTypeId(type1.getComponentTypeId());
        request.setIsPublic(true);

        DesignComponent design = designComponentService.createDesignComponent(request);
        assertThat(design.getComponentType().getComponentTypeId()).isEqualTo(type1.getComponentTypeId());

        // === 3단계: DesignComponent의 ComponentType을 type2로 변경 ===
        DesignComponent updateRequest = DesignComponent.builder()
                .componentType(type2)  // 연관관계 변경
                .build();

        DesignComponent updatedDesign = designComponentService.updateDesignComponent(
                design.getDesignComponentId(), updateRequest);

        // === 4단계: 연관관계 변경 검증 ===
        assertThat(updatedDesign.getComponentType().getComponentTypeId()).isEqualTo(type2.getComponentTypeId());
        assertThat(updatedDesign.getComponentType().getExplain()).isEqualTo("두 번째 타입");

        // type1에는 더 이상 DesignComponent가 없어야 함
        List<DesignComponent> type1RelatedDesigns = designComponentRepository.findAll().stream()
                .filter(d -> d.getComponentType().getComponentTypeId().equals(type1.getComponentTypeId()))
                .toList();
        assertThat(type1RelatedDesigns).isEmpty();

        // type2에는 이제 DesignComponent가 있어야 함
        List<DesignComponent> type2RelatedDesigns = designComponentRepository.findAll().stream()
                .filter(d -> d.getComponentType().getComponentTypeId().equals(type2.getComponentTypeId()))
                .toList();
        assertThat(type2RelatedDesigns).hasSize(1);
        assertThat(type2RelatedDesigns.get(0).getUserEmail())
                .isEqualTo("relationship-test@example.com");
    }

    @Test
    void componentTypeWithoutDesignComponents_EmptyRelationshipHandling() {
        // === 연관관계 없는 ComponentType 처리 테스트 ===
        ComponentType lonelyType = componentTypeService.createComponentType(ComponentType.builder()
                .explain("독립적인 컴포넌트")
                .platform(Platform.IOS)
                .componentPath("/ios/lonely")
                .componentName("LonelyComponent")
                .sizeX(75)
                .sizeY(25)
                .build());

        // === 빈 연관관계 컬렉션 처리 ===
        List<DesignComponent> lonelyRelatedDesigns = designComponentRepository.findAll().stream()
                .filter(d -> d.getComponentType().getComponentTypeId().equals(lonelyType.getComponentTypeId()))
                .toList();
        assertThat(lonelyRelatedDesigns).isEmpty();

        // === 업데이트 시에도 빈 컬렉션 유지 ===
        ComponentType update = ComponentType.builder()
                .explain("업데이트된 독립 컴포넌트")
                .sizeX(100)
                .build();

        ComponentType updated = componentTypeService.updateComponentType(lonelyType.getComponentTypeId(), update);
        assertThat(updated.getExplain()).isEqualTo("업데이트된 독립 컴포넌트");
        assertThat(updated.getSizeX()).isEqualTo(100);
        assertThat(updated.getSizeY()).isEqualTo(25);  // 기존 값 유지
        
        // 업데이트 후에도 연관관계 없음 확인
        List<DesignComponent> updatedLonelyRelatedDesigns = designComponentRepository.findAll().stream()
                .filter(d -> d.getComponentType().getComponentTypeId().equals(lonelyType.getComponentTypeId()))
                .toList();
        assertThat(updatedLonelyRelatedDesigns).isEmpty();

        // === 연관관계 없이 안전하게 삭제 ===
        componentTypeService.deleteComponentType(lonelyType.getComponentTypeId());
        assertThatThrownBy(() -> componentTypeService.getComponentTypeById(lonelyType.getComponentTypeId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}