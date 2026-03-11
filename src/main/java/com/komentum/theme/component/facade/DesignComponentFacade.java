package com.komentum.theme.component.facade;

import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.service.DesignComponentService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignComponentFacade {

  private final UserRetrieveService userRetrieveService;
  private final DesignComponentService designComponentService;

  // CREATE
  public DesignComponentDto createDesignComponent(CreateDesignComponentRequest request,
      String publicUserId) {
    User user = userRetrieveService.findUserEntity(publicUserId);
    return designComponentService.createDesignComponent(request, user);
  }

  // READ
  @Transactional(readOnly = true)
  public DesignComponentDto getDesignComponentById(Integer id) {
    return designComponentService.getDesignComponentById(id);
  }

  @Transactional(readOnly = true)
  public Page<DesignComponentDto> getAllDesignComponents(Pageable pageable) {
    return designComponentService.getAllDesignComponents(pageable);
  }

  // UPDATE
  public DesignComponentDto updateDesignComponent(Integer designComponentId,
      UpdateDesignComponentRequest request) {
    return designComponentService.updateDesignComponent(designComponentId, request);

  }

  // DELETE
  public void deleteComponent(Integer designComponentId) {

    designComponentService.deleteComponent(designComponentId);
  }
}
