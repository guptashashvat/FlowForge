package com.flowforge.repository;

import com.flowforge.domain.entity.Role;
import com.flowforge.domain.model.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
