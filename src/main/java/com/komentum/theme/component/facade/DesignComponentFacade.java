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

@Service
@RequiredArgsConstructor
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
  public DesignComponentDto getDesignComponentById(Integer id) {
    return designComponentService.getDesignComponentById(id);
  }

  public Page<DesignComponentDto> getAllDesignComponents(Pageable pageable) {
    return designComponentService.getAllDesignComponents(pageable);
  }

  // UPDATE
  public DesignComponentDto updateDesignComponent(Integer designComponentId,
      UpdateDesignComponentRequest request, String publicUserId) {
    User owner = userRetrieveService.findUserEntity(publicUserId);

    return designComponentService.updateDesignComponent(designComponentId, request, owner);

  }

  // DELETE
  public void deleteComponent(Integer designComponentId, String publicUserId) {
    User owner = userRetrieveService.findUserEntity(publicUserId);

    designComponentService.deleteComponent(designComponentId, owner);
  }
}
