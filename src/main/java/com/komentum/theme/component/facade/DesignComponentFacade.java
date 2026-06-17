package com.komentum.theme.component.facade;

import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.mapper.DesignComponentMapper;
import com.komentum.theme.component.service.DesignComponentService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import com.komentum.user.service.UserService;
import java.util.List;
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
  private final UserEntityFinder userEntityFinder;
  private final DesignComponentMapper designComponentMapper;

  // CREATE
  public DesignComponentDto createDesignComponent(CreateDesignComponentRequest request,
      MultipartFile image,
      String publicUserId) {
    User user = userService.findUserEntity(publicUserId);
    return designComponentService.createDesignComponent(request, image, user);
  }

  public List<DesignComponentDto> createDesignComponents(CreateDesignComponentRequest request,
      List<MultipartFile> files, String publicUserId) {
    User user = userService.findUserEntity(publicUserId);
    return designComponentService.createDesignComponents(request, files, user);
  }

  // READ
  @Transactional(readOnly = true)
  public DesignComponentDto getDesignComponentById(Integer id) {
    return designComponentService.getDesignComponentById(id);
  }

  @Transactional(readOnly = true)
  public List<DesignComponentDto> findBookmarkedDesignComponents(String userIdentifier) {
    User client = userEntityFinder.findUserEntity(userIdentifier);
    return designComponentService.findBookmarkedDesignComponents(client)
        .stream().map(designComponentMapper::toDto)
        .toList();
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
