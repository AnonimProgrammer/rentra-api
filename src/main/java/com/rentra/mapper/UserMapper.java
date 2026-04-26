package com.rentra.mapper;

import com.rentra.domain.auth.RoleEntity;
import com.rentra.domain.auth.RoleName;
import com.rentra.domain.user.UserEntity;
import com.rentra.dto.user.MeResponse;
import com.rentra.dto.user.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    MeResponse toMeResponse(UserEntity entity);

    UserResponse toUserResponse(UserEntity entity);

    default RoleName toRoleName(RoleEntity entity) {
        return entity.getName();
    }
}
