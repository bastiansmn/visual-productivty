package com.bastiansmn.vp.role.impl;

import com.bastiansmn.vp.authorities.AuthoritiesDAO;
import com.bastiansmn.vp.authorities.AuthoritiesRepository;
import com.bastiansmn.vp.authorities.AuthoritiesService;
import com.bastiansmn.vp.exception.FunctionalException;
import com.bastiansmn.vp.exception.FunctionalRule;
import com.bastiansmn.vp.role.DefaultRoles;
import com.bastiansmn.vp.role.RoleDAO;
import com.bastiansmn.vp.role.RoleRepository;
import com.bastiansmn.vp.role.RoleService;
import com.bastiansmn.vp.role.dto.RoleCreationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final AuthoritiesService authoritiesService;

    public RoleDAO create(RoleCreationDTO roleCreationDTO) {
        RoleDAO role = RoleDAO.builder()
                .name(roleCreationDTO.getName())
                .build();

        log.info("Creating role {}", role);
        return this.roleRepository.save(role);
    }

    public RoleDAO fetchByID(Long id) throws FunctionalException {
        Optional<RoleDAO> role = this.roleRepository.findById(id);

        if (role.isEmpty())
            throw new FunctionalException(FunctionalRule.ROLE_0001, HttpStatus.NOT_FOUND);

        log.info("Fetching role with id {}", id);
        return role.get();
    }

    public RoleDAO fetchByName(String name) throws FunctionalException {
        Optional<RoleDAO> role = this.roleRepository.findByName(name);

        if (role.isEmpty())
            throw new FunctionalException(FunctionalRule.ROLE_0001, HttpStatus.NOT_FOUND);

        log.info("Fetching role with name {}", name);
        return role.get();
    }

    public RoleDAO addAuthorityToRole(Long roleId, Long authId) throws FunctionalException {
        RoleDAO role = this.fetchByID(roleId);
        AuthoritiesDAO authority = this.authoritiesService.fetchByID(authId);

        role.getAuthorities().add(authority);

        return this.roleRepository.save(role);
    }

    public RoleDAO addAuthorityToRole(String roleName, String authName) throws FunctionalException {
        RoleDAO role = this.fetchByName(roleName);
        AuthoritiesDAO authority = this.authoritiesService.fetchByName(authName);

        role.getAuthorities().add(authority);

        return this.roleRepository.save(role);
    }

    public RoleDAO addAuthorityToRole(String roleName, String... authName) throws FunctionalException {
        RoleDAO role = this.fetchByName(roleName);
        Arrays.stream(authName).forEach(auth -> {
            try {
                AuthoritiesDAO authority = this.authoritiesService.fetchByName(auth);
                role.getAuthorities().add(authority);
            } catch (FunctionalException e) {
                throw new RuntimeException(e);
            }
        });
        return this.roleRepository.save(role);
    }

    public void delete(Long id) throws FunctionalException {
        Optional<RoleDAO> role = this.roleRepository.findById(id);
        if (role.isEmpty())
            throw new FunctionalException(FunctionalRule.ROLE_0001, HttpStatus.NOT_FOUND);

        log.info("Deleting role with id {}", id);
        this.roleRepository.deleteById(id);
    }

    public void deleteAll() {
        log.info("Deleting all roles");
        this.roleRepository.deleteAll();
    }

    public Set<RoleDAO> fetchAll() {
        return new HashSet<>(this.roleRepository.findAll());
    }

    public Set<RoleDAO> getDefaultRoles() {
        return DefaultRoles.DEFAULT_ROLES.stream()
                .map(role -> {
                    Optional<RoleDAO> optRole = this.roleRepository.findByName(role);
                    if (optRole.isEmpty()) return null;
                    return optRole.get();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public boolean existsByID(Long id) {
        return this.roleRepository.existsById(id);
    }
}