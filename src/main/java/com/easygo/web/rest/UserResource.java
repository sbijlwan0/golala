package com.easygo.web.rest;

import com.easygo.config.Constants;
import com.easygo.domain.Organisation;
import com.easygo.domain.User;
import com.easygo.repository.AuthorityRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.AuthoritiesConstants;
import com.easygo.service.MailService;
import com.easygo.service.UserService;
import com.easygo.service.dto.ResultStatus;
import com.easygo.service.dto.UserDTO;
import com.easygo.web.rest.errors.BadRequestAlertException;
import com.easygo.web.rest.errors.EmailAlreadyUsedException;
import com.easygo.web.rest.errors.LoginAlreadyUsedException;
import com.easygo.security.SecurityUtils;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import io.undertow.util.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the {@link User} entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api")
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserService userService;

    private final UserRepository userRepository;

    private final MailService mailService;
    
    @Autowired
    AuthorityRepository authRepo;

    public UserResource(UserService userService, UserRepository userRepository, MailService mailService) {

        this.userService = userService;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    /**
     * {@code POST  /users}  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user, or with status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) throws URISyntaxException {
        log.debug("REST request to save User : {}", userDTO);

        if (userDTO.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else if (userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).isPresent()) {
            throw new LoginAlreadyUsedException();
        } else if (userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException();
        } else {
            User newUser = userService.createUser(userDTO);
            mailService.sendCreationEmail(newUser);
            return ResponseEntity.created(new URI("/api/users/" + newUser.getLogin()))
                .headers(HeaderUtil.createAlert(applicationName,  "A user is created with identifier " + newUser.getLogin(), newUser.getLogin()))
                .body(newUser);
        }
    }

    /**
     * {@code PUT /users} : Updates an existing User.
     *
     * @param userDTO the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already in use.
     */
    @PutMapping("/users")
//    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO userDTO) {
        log.debug("REST request to update User : {}", userDTO);
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            throw new EmailAlreadyUsedException();
        }
        existingUser = userRepository.findOneByLogin(userDTO.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            throw new LoginAlreadyUsedException();
        }
        Optional<UserDTO> updatedUser = userService.updateUser(userDTO);
        
        updatedUser=userService.getUserDTO(userRepository.findById(userDTO.getId()).get());
        
        return ResponseUtil.wrapOrNotFound(updatedUser,
            HeaderUtil.createAlert(applicationName, "A user is updated with identifier " + userDTO.getLogin(), userDTO.getLogin()));
    }
    
    
    @GetMapping("/currentUser")
    public ResponseEntity<User> getUser() {
        log.debug("REST request to get User : {}");
        System.out.println(SecurityUtils.getCurrentUserLogin().get());
        
        return new ResponseEntity<>(userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get(),HttpStatus.OK);
       
    }
    
    
    @GetMapping("/updateToken/{token}")
    public ResponseEntity<?> updateToken(@PathVariable("token")String token) {
        log.debug("REST request to update fcm token : {}");
        User user=new User();
        try {
         user=userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
        }catch(Exception a) {
        	return new ResponseEntity<>(new ResultStatus("Error","User Not Found"),HttpStatus.OK);
        }
        if(user.getFcmTokens().isEmpty())
        	user.getFcmTokens().add(token);
        else
        	if(!user.getFcmTokens().contains(token))
        		user.getFcmTokens().add(token);
        
        userRepository.save(user);
        return new ResponseEntity<>(new ResultStatus("Success","Token Updated",userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get()),HttpStatus.OK);
       
    }
    
    
    @GetMapping("/removeToken/{token}")
    public ResponseEntity<?> removeToken(@PathVariable("token")String token) {
        log.debug("REST request to remove fcm token : {}");
        User user=new User();
        try {
         user=userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get();
        }catch(Exception a) {
        	return new ResponseEntity<>(new ResultStatus("Error","User Not Found"),HttpStatus.OK);
        }
        user.getFcmTokens().remove(token);
        userRepository.save(user);
        return new ResponseEntity<>(new ResultStatus("Success","Token Updated",userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get()),HttpStatus.OK);
       
    }

    /**
     * {@code GET /users} : get all users.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam MultiValueMap<String, String> queryParams, UriComponentsBuilder uriBuilder, Pageable pageable) {
        final Page<UserDTO> page = userService.getAllManagedUsers(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("getAuthUser/{auth}/{page}")
    public ResponseEntity<?> getAllAuthUsers(@PathVariable("auth") String auth,@PathVariable("page") int page) throws BadRequestException{
    	
    	log.debug("get all user by auth ",auth);
    	if(auth.equalsIgnoreCase("ROLE_ADMIN"))
    		throw new BadRequestException("Forbidden");
    	Page<User>user=userRepository.findAllByAuthoritiesContains(authRepo.findById(auth).get(), PageRequest.of(page, 10));
    	
    	return new ResponseEntity<>(user,HttpStatus.OK);
    }
    
    
    @GetMapping("pendingDrivers/{page}")
    public ResponseEntity<?> getPendingDrivers(@PathVariable("page") int page) throws BadRequestException{
    	
    	
    	Page<User>user=userRepository.findAllByActivatedIsFalseAndAuthoritiesContains(authRepo.findById(AuthoritiesConstants.DELIVERER).get(), PageRequest.of(page, 10));
    	
    	return new ResponseEntity<>(user,HttpStatus.OK);
    }
    
    
    /**
     * Gets a list of all roles.
     * @return a string list of all roles.
     */
    @GetMapping("/users/authorities")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<String> getAuthorities() {
        return userService.getAuthorities();
    }
    
    
    @GetMapping("/pendingUsers/{page}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<?> getPendingUsers(@PathVariable("page") int page) {
        
    	log.debug("getting all pending users");
    	
    	return new ResponseEntity<>(new ResultStatus("Success","Users Fetched",userRepository.findAllByActivatedIsFalse(PageRequest.of(page, 10))),HttpStatus.OK);
    	
    }

    /**
     * {@code GET /users/:login} : get the "login" user.
     *
     * @param login the login of the user to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" user, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/users/{login}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String login) {
        log.debug("REST request to get User : {}", login);
        return ResponseUtil.wrapOrNotFound(
            userService.getUserWithAuthoritiesByLogin(login)
                .map(UserDTO::new));
    }
    
    
    @GetMapping("/getUserById/{id}") 
    public ResponseEntity<?> getUserById(@PathVariable("id")String id) {
    	
    	log.debug("get user by id");
    	
    	return new ResponseEntity<>(new ResultStatus("Success","User Fetched", userRepository.findById(id).get()),HttpStatus.OK);
    }

    /**
     * {@code DELETE /users/:login} : delete the "login" User.
     *
     * @param login the login of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteUser(@PathVariable String login) {
        log.debug("REST request to delete User: {}", login);
        userService.deleteUser(login);
        return ResponseEntity.noContent().headers(HeaderUtil.createAlert(applicationName,  "A user is deleted with identifier " + login, login)).build();
    }
}
