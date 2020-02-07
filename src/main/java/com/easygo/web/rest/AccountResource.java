package com.easygo.web.rest;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.Authority;
import com.easygo.domain.User;
import com.easygo.repository.AuthorityRepository;
import com.easygo.repository.UserRepository;
import com.easygo.security.AuthoritiesConstants;
import com.easygo.security.SecurityUtils;
import com.easygo.service.MailService;
import com.easygo.service.OtpService;
import com.easygo.service.UserService;
import com.easygo.service.dto.PasswordChangeDTO;
import com.easygo.service.dto.ResultStatus;
import com.easygo.service.dto.UserDTO;
import com.easygo.service.util.RandomUtil;
import com.easygo.web.rest.errors.EmailAlreadyUsedException;
import com.easygo.web.rest.errors.EmailNotFoundException;
import com.easygo.web.rest.errors.InvalidPasswordException;
import com.easygo.web.rest.errors.LoginAlreadyUsedException;
import com.easygo.web.rest.vm.KeyAndPasswordVM;
import com.easygo.web.rest.vm.ManagedUserVM;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

	private static class AccountResourceException extends RuntimeException {
		private AccountResourceException(String message) {
			super(message);
		}
	}

	private final Logger log = LoggerFactory.getLogger(AccountResource.class);

	private final UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	private final UserService userService;

	private final MailService mailService;

	@Autowired
	OtpService otpService;

	@Autowired
	AuthorityRepository authorityRepository;

	public AccountResource(UserRepository userRepository, UserService userService, MailService mailService) {

		this.userRepository = userRepository;
		this.userService = userService;
		this.mailService = mailService;
	}

	/**
	 * {@code POST  /register} : register the user.
	 *
	 * @param managedUserVM
	 *            the managed user View Model.
	 * @throws UnsupportedEncodingException
	 * @throws InvalidPasswordException
	 *             {@code 400 (Bad Request)} if the password is incorrect.
	 * @throws EmailAlreadyUsedException
	 *             {@code 400 (Bad Request)} if the email is already used.
	 * @throws LoginAlreadyUsedException
	 *             {@code 400 (Bad Request)} if the login is already used.
	 */
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM)
			throws UnsupportedEncodingException {
		// if (!checkPasswordLength(managedUserVM.getPassword())) {
		// throw new InvalidPasswordException();
		// }
		User user = userService.registerUser(managedUserVM);
		// mailService.sendActivationEmail(user);

		return new ResponseEntity<>(new ResultStatus("Success", managedUserVM.getType() + " registered", user),
				HttpStatus.CREATED);
	}

	/**
	 * {@code GET  /activate} : activate the registered user.
	 *
	 * @param key
	 *            the activation key.
	 * @throws UnsupportedEncodingException
	 * @throws RuntimeException
	 *             {@code 500 (Internal Server Error)} if the user couldn't be
	 *             activated.
	 */

	@GetMapping("/activate/{mobile}/{otp}")
	public ResponseEntity<?> activateAccountViaMobile(@PathVariable("mobile") String mobile,
			@PathVariable("otp") String otp) throws UnsupportedEncodingException {

		String code = otpService.verifyOtp(mobile, otp);

		User use = new User();

		if (code.equalsIgnoreCase("{\"message\":\"otp_verified\",\"type\":\"success\"}")
				|| code.equalsIgnoreCase("{\"message\":\"already_verified\",\"type\":\"error\"}")) {
			Optional<User> user = userRepository.findOneByMobile(mobile);
			if (!user.isPresent()) {
				throw new AccountResourceException("No user was found for this activation key");
			} else
				use = user.get();

		} else
			return new ResponseEntity<>(new ResultStatus("Error", "Otp MisMatch"), HttpStatus.BAD_REQUEST);

		if(use.getActivated())
			return new ResponseEntity<>(new ResultStatus("Success", "User Already Activated", use), HttpStatus.OK);
		
		Optional<User> result = userService.activateRegistration(use.getActivationKey());

		return new ResponseEntity<>(new ResultStatus("Success", "User Activated", result.get()), HttpStatus.OK);
	}

	@GetMapping("/activate")
	public ResponseEntity<?> activateAccount(@RequestParam(value = "key") String key) {
		User use=userRepository.findById(key).get();
		Optional<User> user = userService.activateRegistration(use.getActivationKey());
		if (!user.isPresent()) {
			throw new AccountResourceException("No user was found for this activation key");
		}
		return new ResponseEntity<>(new ResultStatus("Success", "User Activated", user), HttpStatus.OK);	
	}

	/**
	 * {@code GET  /authenticate} : check if the user is authenticated, and return
	 * its login.
	 *
	 * @param request
	 *            the HTTP request.
	 * @return the login if the user is authenticated.
	 */
	@GetMapping("/authenticate")
	public String isAuthenticated(HttpServletRequest request) {
		log.debug("REST request to check if the current user is authenticated");
		return request.getRemoteUser();
	}



	@GetMapping("/sendOtp/{mobile}/{flag}")
	public ResponseEntity<?> sendOtp(@PathVariable("mobile") String mobile, @PathVariable("flag") String flag)
			throws UnsupportedEncodingException {

		log.debug("sending otp to ", mobile);

		if (!mobile.contains("@")) {
			if (flag.equalsIgnoreCase("customer")) {
				if (userRepository.findOneByMobile(mobile).isPresent()) {
					if (userRepository.findOneByMobile(mobile).get().getAuthorities().iterator().next()
							.equals(authorityRepository.findById(AuthoritiesConstants.CUSTOMER).get()))
						otpService.sendOtp(mobile, 0);
					else {
						User user = userRepository.findOneByMobile(mobile).get();
						user.getAuthorities().add(authorityRepository.findById(AuthoritiesConstants.CUSTOMER).get());
						userRepository.save(user);
						otpService.sendOtp(mobile, 0);
					}
				} else {
					Set<Authority> authorities = new HashSet<>();
					User user = new User();
					user.setMobile(mobile);
					user.setActivated(true);
					user.setLogin(mobile);
					authorityRepository.findById(AuthoritiesConstants.CUSTOMER).ifPresent(authorities::add);
					user.setAuthorities(authorities);
					userRepository.save(user);
					otpService.sendOtp(mobile, 0);
				}
			} else if (flag.equalsIgnoreCase("vendor")) {
				if (userRepository.findOneByMobile(mobile).isPresent())
					if (userRepository.findOneByMobile(mobile).get().getAuthorities()
							.contains(authorityRepository.findById(AuthoritiesConstants.VENDOR).get())
							|| userRepository.findOneByMobile(mobile).get().getAuthorities()
									.contains(authorityRepository.findById(AuthoritiesConstants.DELIVERER).get())) {
						otpService.sendOtp(mobile, 0);
					} else
						return new ResponseEntity<>(new ResultStatus("error", "Register First"),
								HttpStatus.BAD_REQUEST);
			} else
				return new ResponseEntity<>(new ResultStatus("error", "Register First"), HttpStatus.BAD_REQUEST);

		} else if (mobile.contains("@")) {
			if (flag.equalsIgnoreCase("customer")) {
				if (userRepository.findOneByEmailIgnoreCase(mobile).isPresent()) {
					if (userRepository.findOneByEmailIgnoreCase(mobile).get().getAuthorities()
							.contains(authorityRepository.findById(AuthoritiesConstants.CUSTOMER).get())) {
						User user = userRepository.findOneByEmailIgnoreCase(mobile).get();
						String otp = RandomUtil.generateOTP();
						user.setPassword(passwordEncoder.encode(otp));
						userRepository.save(user);
						mailService.userOTPMail(user, otp);
					} else {
						User user = userRepository.findOneByEmailIgnoreCase(mobile).get();
						String otp = RandomUtil.generateOTP();
						user.setPassword(passwordEncoder.encode(otp));
						user.getAuthorities().add(authorityRepository.findById(AuthoritiesConstants.CUSTOMER).get());
						userRepository.save(user);
						mailService.userOTPMail(user, otp);

					}
				}

				else {
					Set<Authority> authorities = new HashSet<>();
					User user = new User();
					user.setEmail(mobile);
					user.setActivated(true);
					user.setLogin(mobile);
					authorityRepository.findById(AuthoritiesConstants.CUSTOMER).ifPresent(authorities::add);
					user.setAuthorities(authorities);
					String otp = RandomUtil.generateOTP();
					user.setPassword(passwordEncoder.encode(otp));
					userRepository.save(user);
					mailService.userOTPMail(user, otp);
				}
			} else if (flag.equalsIgnoreCase("vendor")) {
				if (userRepository.findOneByEmailIgnoreCase(mobile).isPresent()) {
					if (userRepository.findOneByEmailIgnoreCase(mobile).get().getAuthorities()
							.contains(authorityRepository.findById(AuthoritiesConstants.VENDOR).get())
							|| userRepository.findOneByEmailIgnoreCase(mobile).get().getAuthorities()
									.contains(authorityRepository.findById(AuthoritiesConstants.DELIVERER).get())) {
						User user = userRepository.findOneByEmailIgnoreCase(mobile).get();
						String otp = RandomUtil.generateOTP();
						user.setPassword(passwordEncoder.encode(otp));
						userRepository.save(user);
						mailService.userOTPMail(user, otp);
					}
				} else
					return new ResponseEntity<>(new ResultStatus("error", "Register First"), HttpStatus.BAD_REQUEST);
			} else
				return new ResponseEntity<>(new ResultStatus("error", "Register First"), HttpStatus.BAD_REQUEST);

		} else
			return new ResponseEntity<>(new ResultStatus("Error", "Invalid UserName"), HttpStatus.BAD_REQUEST);

		return new ResponseEntity<>(new ResultStatus("Success", "OTP Sent", mobile), HttpStatus.OK);
	}

	@GetMapping("/resendOtp/{mobile}")
	public ResponseEntity<?> resendOtp(@PathVariable("mobile") String mobile) throws UnsupportedEncodingException {

		log.debug("sending otp to ", mobile);

		if (!mobile.contains("@")) {
			if (userRepository.findOneByMobile(mobile).isPresent())
				otpService.resendOtp(mobile);
		} else if (mobile.contains("@")) {
			if (userRepository.findOneByEmailIgnoreCase(mobile).isPresent()) {
				User user = userRepository.findOneByEmailIgnoreCase(mobile).get();
				String otp = RandomUtil.generateOTP();
				user.setPassword(passwordEncoder.encode(otp));
				userRepository.save(user);
				mailService.userOTPMail(user, otp);
			}
		} else
			return new ResponseEntity<>(new ResultStatus("Error", "Invalid UserName"), HttpStatus.BAD_REQUEST);

		return new ResponseEntity<>(new ResultStatus("Success", "OTP Sent", mobile), HttpStatus.OK);
	}

	/**
	 * {@code GET  /account} : get the current user.
	 *
	 * @return the current user.
	 * @throws RuntimeException
	 *             {@code 500 (Internal Server Error)} if the user couldn't be
	 *             returned.
	 */
	@GetMapping("/account")
	public UserDTO getAccount() {
		return userService.getUserWithAuthorities().map(UserDTO::new)
				.orElseThrow(() -> new AccountResourceException("User could not be found"));
	}

	/**
	 * {@code POST  /account} : update the current user information.
	 *
	 * @param userDTO
	 *            the current user information.
	 * @throws EmailAlreadyUsedException
	 *             {@code 400 (Bad Request)} if the email is already used.
	 * @throws RuntimeException
	 *             {@code 500 (Internal Server Error)} if the user login wasn't
	 *             found.
	 */
	@PostMapping("/account")
	public void saveAccount(@Valid @RequestBody UserDTO userDTO) {
		String userLogin = SecurityUtils.getCurrentUserLogin()
				.orElseThrow(() -> new AccountResourceException("Current user login not found"));
		Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
		if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
			throw new EmailAlreadyUsedException();
		}
		Optional<User> user = userRepository.findOneByLogin(userLogin);
		if (!user.isPresent()) {
			throw new AccountResourceException("User could not be found");
		}
		userService.updateUser(userDTO.getFirstName(), userDTO.getEmail(), userDTO.getLangKey(), userDTO.getImageUrl());
	}

	/**
	 * {@code POST  /account/change-password} : changes the current user's password.
	 *
	 * @param passwordChangeDto
	 *            current and new password.
	 * @throws InvalidPasswordException
	 *             {@code 400 (Bad Request)} if the new password is incorrect.
	 */
	@PostMapping(path = "/account/change-password")
	public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
		if (!checkPasswordLength(passwordChangeDto.getNewPassword())) {
			throw new InvalidPasswordException();
		}
		userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
	}

	/**
	 * {@code POST   /account/reset-password/init} : Send an email to reset the
	 * password of the user.
	 *
	 * @param mail
	 *            the mail of the user.
	 * @throws EmailNotFoundException
	 *             {@code 400 (Bad Request)} if the email address is not registered.
	 */
	@PostMapping(path = "/account/reset-password/init")
	public void requestPasswordReset(@RequestBody String mail) {
		mailService
				.sendPasswordResetMail(userService.requestPasswordReset(mail).orElseThrow(EmailNotFoundException::new));
	}

	/**
	 * {@code POST   /account/reset-password/finish} : Finish to reset the password
	 * of the user.
	 *
	 * @param keyAndPassword
	 *            the generated key and the new password.
	 * @throws InvalidPasswordException
	 *             {@code 400 (Bad Request)} if the password is incorrect.
	 * @throws RuntimeException
	 *             {@code 500 (Internal Server Error)} if the password could not be
	 *             reset.
	 */
	@PostMapping(path = "/account/reset-password/finish")
	public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
		if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
			throw new InvalidPasswordException();
		}
		Optional<User> user = userService.completePasswordReset(keyAndPassword.getNewPassword(),
				keyAndPassword.getKey());

		if (!user.isPresent()) {
			throw new AccountResourceException("No user was found for this reset key");
		}
	}

	private static boolean checkPasswordLength(String password) {
		return !StringUtils.isEmpty(password) && password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH
				&& password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH;
	}
}
