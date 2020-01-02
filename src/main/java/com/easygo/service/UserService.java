package com.easygo.service;

import com.easygo.config.Constants;
import com.easygo.domain.Authority;
import com.easygo.domain.Document;
import com.easygo.domain.User;
import com.easygo.repository.AuthorityRepository;
import com.easygo.repository.DocumentRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.AuthoritiesConstants;
import com.easygo.security.SecurityUtils;
import com.easygo.service.dto.UserDTO;
import com.easygo.service.util.RandomUtil;
import com.easygo.web.rest.errors.*;
import com.easygo.web.rest.vm.ManagedUserVM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    DocumentRepository documentRepository;
    
    @Autowired
    OtpService otpService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                userRepository.save(user);
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository.findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                userRepository.save(user);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmailIgnoreCase(mail)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                userRepository.save(user);
                return user;
            });
    }

    public User registerUser(ManagedUserVM managedUserVM) throws UnsupportedEncodingException {
    	UserDTO userDTO = managedUserVM;
    	 User newUser = new User();
    	
        userRepository.findOneByMobile(userDTO.getMobile()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
            	if(managedUserVM.getType().equalsIgnoreCase("vendor")&&existingUser.getAuthorities().contains(authorityRepository.findById(AuthoritiesConstants.VENDOR).get()))
                throw new LoginAlreadyUsedException();
            	else if(managedUserVM.getType().equalsIgnoreCase("driver")&&existingUser.getAuthorities().contains(authorityRepository.findById(AuthoritiesConstants.DELIVERER).get()))
                    throw new LoginAlreadyUsedException();
            	else {
            		newUser.setId(existingUser.getId());
            		newUser.setAuthorities(existingUser.getAuthorities());
            	}
            }
        });
        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
            	if(managedUserVM.getType().equalsIgnoreCase("vendor")&&existingUser.getAuthorities().contains(authorityRepository.findById(AuthoritiesConstants.VENDOR).get()))
            		throw new EmailAlreadyUsedException();
                	else if(managedUserVM.getType().equalsIgnoreCase("driver")&&existingUser.getAuthorities().contains(authorityRepository.findById(AuthoritiesConstants.DELIVERER).get()))
                		throw new EmailAlreadyUsedException();
                	else {
            			newUser.setId(existingUser.getId());
            			newUser.setAuthorities(existingUser.getAuthorities());
                	}
                
            }
        });
       
//        String encryptedPassword = passwordEncoder.encode(password);
        if(!userDTO.getMobile().isEmpty())
        	newUser.setLogin(userDTO.getMobile());
        else if(!userDTO.getEmail().isEmpty())
        	newUser.setLogin(userDTO.getEmail());
        
        newUser.setMobile(userDTO.getMobile());
        // new user gets initially a generated password
//        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
//        newUser.setLastName(userDTO.getLastName());
        newUser.setEmail(userDTO.getEmail().toLowerCase());
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is not active
        if(null==newUser.getId())
        newUser.setActivated(false);
        newUser.setAddress(userDTO.getAddress());
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        Set<Authority> authorities = newUser.getAuthorities();
        if(managedUserVM.getType().equalsIgnoreCase("vendor")) {
        authorityRepository.findById(AuthoritiesConstants.VENDOR).ifPresent(authorities::add);
        otpService.sendOtp(userDTO.getMobile(), 0);
        }
        else if(managedUserVM.getType().equalsIgnoreCase("driver")) {
            authorityRepository.findById(AuthoritiesConstants.DELIVERER).ifPresent(authorities::add);
//            otpService.sendOtp(userDTO.getMobile(), 0);
        }
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);

      
        Document doc=new Document();
        doc.setAadhar(managedUserVM.getAadhar());
        doc.setBank(managedUserVM.getBank());
        doc.setGstNo(managedUserVM.getGstNo());
        doc.setPan(managedUserVM.getPan());
        doc.setDrivingLic(managedUserVM.getDrivingLic());
        doc.setUserId(newUser.getId());
        documentRepository.save(doc);
      

        
        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser){
        if (existingUser.getActivated()) {
             return false;
        }
        userRepository.delete(existingUser);
        return true;
    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
//        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail().toLowerCase());
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO.getAuthorities().stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
//                user.setLastName(lastName);
                user.setEmail(email.toLowerCase());
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                userRepository.save(user);
                log.debug("Changed Information for User: {}", user);
            });
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        return Optional.of(userRepository
            .findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
//                user.setLastName(userDTO.getLastName());
                user.setEmail(userDTO.getEmail().toLowerCase());
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                user.setAddress(userDTO.getAddress());
                user.setOtp(userDTO.getOtp());
//                user.setFcmTokens(userDTO.getFCM);
//                Set<Authority> managedAuthorities = user.getAuthorities();
//                managedAuthorities.clear();
//                userDTO.getAuthorities().stream()
//                    .map(authorityRepository::findById)
//                    .filter(Optional::isPresent)
//                    .map(Optional::get)
//                    .forEach(managedAuthorities::add);
                userRepository.save(user);
                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(UserDTO::new);
    }
    
   public UserDTO getUserDTO(User user) {
	   
	   UserDTO userDTO=new UserDTO();
	   userDTO.setId(user.getId());
	   userDTO.setLogin(user.getLogin());
	   userDTO.setFirstName(user.getFirstName());
	   userDTO.setMobile(user.getMobile());
	   userDTO.setAddress(user.getAddress());
	   userDTO.setEmail(user.getEmail());
	   userDTO.setImageUrl(user.getImageUrl());
	   userDTO.setActivated(user.getActivated());
	   userDTO.setLangKey(user.getLangKey());
	   userDTO.setOtp(user.getOtp());
	   Set<String>authorities=new HashSet<>();
	   for(Authority auth:user.getAuthorities())
		   authorities.add(auth.getName());
	   userDTO.setAuthorities(authorities);
	   return userDTO;
   }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
        	documentRepository.findOneByUserId(user.getId()).ifPresent(doc ->{documentRepository.delete(doc);});
            userRepository.delete(user);
            log.debug("Deleted User: {}", user);
        });
    }

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                userRepository.save(user);
                log.debug("Changed password for User: {}", user);
            });
    }

    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneByLogin(login);
    }

    public Optional<User> getUserWithAuthorities(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                log.debug("Deleting not activated user {}", user.getLogin());
                documentRepository.delete(documentRepository.findOneByUserId(user.getId()).get());
                userRepository.delete(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     * @return a list of all the authorities.
     */
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }
}
