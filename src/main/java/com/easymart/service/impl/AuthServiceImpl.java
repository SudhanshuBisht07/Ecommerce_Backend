package com.easymart.service.impl;

import com.easymart.domain.AccountStatus;
import com.easymart.service.AuthService;
import com.easymart.config.JwtProvider;
import com.easymart.domain.USER_ROLE;
import com.easymart.model.Cart;
import com.easymart.model.Seller;
import com.easymart.model.User;
import com.easymart.model.VerificationCode;
import com.easymart.repository.CartRepository;
import com.easymart.repository.SellerRepository;
import com.easymart.repository.UserRepository;
import com.easymart.repository.VerificationCodeRepository;
import com.easymart.request.LoginRequest;
import com.easymart.response.AuthResponse;
import com.easymart.request.SignupRequest;
import com.easymart.service.EmailService;
import com.easymart.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final CustomUserServiceImpl customUserService;
    private final SellerRepository sellerRepository;

    @Override
    public void sentLoginOtp(String email, USER_ROLE role) throws Exception {
        if (role.equals(USER_ROLE.ROLE_SELLER)) {
            Seller seller = sellerRepository.findByEmail(email);
            if (seller == null) {
                throw new Exception("seller not found..");
            }
        }
        VerificationCode exists=verificationCodeRepository.findByEmail(email);
        if(exists!=null){
            verificationCodeRepository.delete(exists);
        }
        String otp= OtpUtil.generateOtp();
        VerificationCode verificationCode=new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verificationCodeRepository.save(verificationCode);

        String subject="EasyMart login/signup otp";
        String text="Login/Signup to EasyMart using this otp : "+otp;
        emailService.sendVerificationOtpEmail(email, otp, subject, text);
    }

    @Override
    public String createUser(SignupRequest req) throws Exception {

        VerificationCode verificationCode=verificationCodeRepository.findByEmail(req.getEmail());
        if(verificationCode==null|| !verificationCode.getOtp().equals(req.getOtp())){
            throw new Exception("wrong otp...");
        }
        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new Exception("OTP has expired");
        }
        verificationCodeRepository.delete(verificationCode);
        User user=userRepository.findByEmail(req.getEmail());
        if(user==null){
            User createdUser=new User();
            createdUser.setEmail(req.getEmail());
            createdUser.setFullName(req.getFullName());
            createdUser.setRole(USER_ROLE.ROLE_CUSTOMER);
            createdUser.setMobile(req.getMobile());
            createdUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            user=userRepository.save(createdUser);

            Cart cart=new Cart();
            cart.setUser(user);
            cartRepository.save(cart);

        }
        List<GrantedAuthority> authorities= new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().toString()));
        Authentication authentication=new UsernamePasswordAuthenticationToken(req.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtProvider.generateToken(authentication);
    }

    @Override
    public AuthResponse signing(LoginRequest req) {
        String username=req.getEmail();
        String otp=req.getOtp();
        Authentication authentication=authenticate(username,otp);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token=jwtProvider.generateToken(authentication);
        AuthResponse authResponse=new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("login successful");

        Collection<? extends GrantedAuthority> authorities=authentication.getAuthorities();
        String roleName = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();
        authResponse.setRole(roleName != null ? USER_ROLE.valueOf(roleName) : USER_ROLE.ROLE_CUSTOMER);
        return authResponse;
    }
    private Authentication authenticate(String username, String otp){
        String SELLER_PREFIX="seller_";
        String emailForOtpLookup = username.startsWith(SELLER_PREFIX)
                ? username.substring(SELLER_PREFIX.length())
                : username;
        UserDetails userDetails=customUserService.loadUserByUsername(username);
        if(userDetails==null){
            throw new BadCredentialsException("invalid username or password");
        }
        if (username.startsWith(SELLER_PREFIX)) {
            Seller seller = sellerRepository.findByEmail(emailForOtpLookup);
            if (seller != null && seller.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new BadCredentialsException("Seller account is not active");
            }
        }
        VerificationCode verificationCode=verificationCodeRepository.findByEmail(emailForOtpLookup);
        if(verificationCode==null|| !verificationCode.getOtp().equals(otp)){
            throw new BadCredentialsException("wrong otp");
        }
        if(verificationCode.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new BadCredentialsException("otp has expired");
        }
        verificationCodeRepository.delete(verificationCode);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
