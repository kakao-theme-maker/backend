package com.komentum.designcomponent.facade;

import com.komentum.designcomponent.dto.CreateDesignComponentRequest;
import com.komentum.designcomponent.dto.DesignComponentDto;
import com.komentum.designcomponent.dto.UpdateDesignComponentRequest;
import com.komentum.designcomponent.service.DesignComponentService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignComponentFacade {

  private final UserService userService;
  private final DesignComponentService designComponentService;

  // CREATE
  public DesignComponentDto createDesignComponent(CreateDesignComponentRequest request,
      MultipartFile image,
      String publicUserId) {
    User user = userService.findUserEntity(publicUserId);
    return designComponentService.createDesignComponent(request, image, user);
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
      UpdateDesignComponentRequest request, MultipartFile image) {
    return designComponentService.updateDesignComponent(designComponentId, request, image);

  }

  // DELETE
  public void deleteComponent(Integer designComponentId) {

    designComponentService.deleteComponent(designComponentId);
  }
}
