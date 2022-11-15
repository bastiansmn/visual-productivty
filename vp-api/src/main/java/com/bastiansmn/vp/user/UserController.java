package com.bastiansmn.vp.user;

import com.bastiansmn.vp.exception.FunctionalException;
import com.bastiansmn.vp.exception.TechnicalException;
import com.bastiansmn.vp.user.dto.UserCreationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(summary = "Créé un nouvel utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé"),
            @ApiResponse(responseCode = "400", description = "Utilisateur déjà connecté via provider externe (Google)"),
            @ApiResponse(responseCode = "400", description = "L'utilisateur existe déjà"),
            @ApiResponse(responseCode = "400", description = "Email invalide"),
            @ApiResponse(responseCode = "400", description = "Mot de passe invalide"),
    })
    @PostMapping("/register")
    public ResponseEntity<UserDAO> create(@RequestBody UserCreationDTO userDTO)
            throws FunctionalException, TechnicalException {
        URI uri = URI.create(
                ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/user/register")
                        .toUriString()
        );
        return ResponseEntity.created(uri).body(this.userService.create(userDTO));
    }

    @Operation(summary = "Récupère un utilisateur via son email (doit être admin ou l'utilisateur lui-même)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable"),
    })
    @GetMapping("/fetchByEmail")
    @PreAuthorize("#email.equals(authentication.principal) or hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDAO> fetchByEmail(@Param("email") String email) throws FunctionalException {
        return ResponseEntity.ok(this.userService.fetchByEmail(email));
    }

    @GetMapping("/fetchAll")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserDAO>> fetchAll() {
        return ResponseEntity.ok(this.userService.fetchAll());
    }

    @Operation(summary = "Supprime un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UUtilisateur supprimé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur inconnu"),
    })
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam Long user_id) throws FunctionalException {
        this.userService.delete(user_id);
        return ResponseEntity.ok().build();
    }

}
