package com.easygo.web.rest;

import java.io.UnsupportedEncodingException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easygo.domain.User;
import com.easygo.repository.UserRepository;
import com.easygo.security.jwt.JWTFilter;
import com.easygo.security.jwt.TokenProvider;
import com.easygo.service.OtpService;
import com.easygo.service.UserService;
import com.easygo.service.dto.ResultStatus;
import com.easygo.service.dto.UserDTO;
import com.easygo.web.rest.vm.LoginVM;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class UserJWTController {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserService userService;

	@Autowired
	OtpService otpService;

	@Autowired
	PasswordEncoder passwordEncoder;

	private final TokenProvider tokenProvider;

	private final AuthenticationManagerBuilder authenticationManagerBuilder;

	public UserJWTController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
		this.tokenProvider = tokenProvider;
		this.authenticationManagerBuilder = authenticationManagerBuilder;
	}

//    @PostMapping("/authenticate")
//    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM) {
//
//    	User existingUser=new User();
//    	
//    	if(userRepository.findOneByMobile(loginVM.getUsername()).isPresent()){
//    		existingUser=userRepository.findOneByMobile(loginVM.getUsername()).get();
//        }
//        else if(userRepository.findOneByEmailIgnoreCase(loginVM.getUsername()).isPresent()) {
//        	existingUser=userRepository.findOneByEmailIgnoreCase(loginVM.getUsername()).get();
//        }
//        else {
//        	existingUser=userRepository.findOneByLogin(loginVM.getUsername()).get();
//        }
//        
//        UsernamePasswordAuthenticationToken authenticationToken =
//            new UsernamePasswordAuthenticationToken(existingUser.getLogin(), loginVM.getPassword());
//
//        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        boolean rememberMe = (loginVM.isRememberMe() == null) ? false : loginVM.isRememberMe();
//        String jwt = tokenProvider.createToken(authentication, rememberMe);
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
//        
//        return new ResponseEntity<>(new JWTToken(jwt,existingUser), httpHeaders, HttpStatus.OK);
//    }

	@PostMapping("/authenticate")
	public ResponseEntity<?> authorize(@Valid @RequestBody LoginVM loginVM) throws UnsupportedEncodingException {

		User existingUser = new User();

		if (userRepository.findOneByMobile(loginVM.getUsername()).isPresent()) {
			String code = otpService.verifyOtp(loginVM.getUsername(), loginVM.getPassword());

			if (code.equalsIgnoreCase("{\"message\":\"otp_verified\",\"type\":\"success\"}")
					|| code.equalsIgnoreCase("{\"message\":\"already_verified\",\"type\":\"error\"}")) {
				User user = userRepository.findOneByMobile(loginVM.getUsername()).get();
				user.setPassword(passwordEncoder.encode(loginVM.getPassword()));
				user.setOtp(loginVM.getPassword());
				user.setActivated(true);
				existingUser = userRepository.save(user);
			}
			else if (code.equalsIgnoreCase("{\"message\":\"otp_not_verified\",\"type\":\"error\"}")) {
				return new ResponseEntity<>(new ResultStatus("Error", "OTP Mismatch"), HttpStatus.BAD_REQUEST);
			}
			else if (passwordEncoder.matches(loginVM.getPassword(),
					userRepository.findOneByMobile(loginVM.getUsername()).get().getPassword())) {
				existingUser=userRepository.findOneByMobile(loginVM.getUsername()).get();
			}
			else {

				return new ResponseEntity<>(new ResultStatus("Error", "OTP Mismatch"), HttpStatus.BAD_REQUEST);

			}

		}

		else if (userRepository.findOneByEmailIgnoreCase(loginVM.getUsername()).isPresent()) {
			existingUser = userRepository.findOneByEmailIgnoreCase(loginVM.getUsername()).get();
		}

		else {
			existingUser = userRepository.findOneByLogin(loginVM.getUsername()).get();
		}

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				existingUser.getLogin(), loginVM.getPassword());

		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		boolean rememberMe = (loginVM.isRememberMe() == null) ? false : loginVM.isRememberMe();
		String jwt = tokenProvider.createToken(authentication, rememberMe);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

		UserDTO user=userService.getUserDTO(existingUser);	
		
		return new ResponseEntity<>(new JWTToken(jwt, user), httpHeaders, HttpStatus.OK);
	}

	/**
	 * Object to return as body in JWT Authentication.
	 */
	static class JWTToken {

		private String idToken;

		private UserDTO user;

		JWTToken(String idToken, UserDTO user) {
			this.idToken = idToken;
			this.user = user;
		}

		@JsonProperty("id_token")
		String getIdToken() {
			return idToken;
		}

		void setIdToken(String idToken) {
			this.idToken = idToken;
		}

		public UserDTO getUser() {
			return user;
		}

		public void setUser(UserDTO user) {
			this.user = user;
		}
	}
}
