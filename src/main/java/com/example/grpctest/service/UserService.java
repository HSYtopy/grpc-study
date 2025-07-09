package com.example.grpctest.service;

import com.example.grpctest.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 */
public interface UserService {

    UserDTO createUser(UserDTO userDTO);

    Optional<UserDTO> findById(Long id);

    Optional<UserDTO> findByEmail(String email);

    List<UserDTO> findAllActiveUsers();

    Page<UserDTO> findUsers(Pageable pageable);

    UserDTO updateUser(Long id, UserDTO userDTO);

    boolean deleteUser(Long id);

    boolean emailExists(String email, Long excludeId);

    long countActiveUsers();

    List<UserDTO> findRecentUsers(int limit);

    List<UserDTO> searchUsersByName(String name);
}