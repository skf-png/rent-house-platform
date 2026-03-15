package framework.portal.user.service;

import framework.admin.api.appuser.domain.DTO.UserEditReqDTO;
import framework.portal.user.domain.DTO.LoginDTO;
import framework.portal.user.domain.DTO.UserDTO;
import framework.security.domain.DTO.TokenDTO;

public interface UserService {

    TokenDTO login(LoginDTO wechatLoginDTO);

    String sendCode(String phone);

    String edit(UserEditReqDTO userEditReqDTO);

    UserDTO getLoginUser();

    void logout();
}
