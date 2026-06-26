package com.flowforge.repository;

import com.flowforge.domain.entity.AppUser;
import com.flowforge.domain.model.RoleName;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<AppUser> findById(Long id);

    @EntityGraph(attributePaths = "roles")
    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "roles")
    List<AppUser> findAllByOrderByFullNameAsc();

    @EntityGraph(attributePaths = "roles")
    List<AppUser> findByRoles_NameAndActiveTrueOrderByFullNameAsc(RoleName roleName);
}
