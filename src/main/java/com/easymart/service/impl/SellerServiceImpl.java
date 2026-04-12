package com.easymart.service.impl;

import com.easymart.model.VerificationCode;
import com.easymart.repository.VerificationCodeRepository;
import com.easymart.service.SellerService;
import com.easymart.config.JwtProvider;
import com.easymart.domain.AccountStatus;
import com.easymart.domain.USER_ROLE;
import com.easymart.exceptions.SellerException;
import com.easymart.model.Address;
import com.easymart.model.Seller;
import com.easymart.repository.AddressRepository;
import com.easymart.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;
    private final VerificationCodeRepository verificationCodeRepository;


    @Override
    public Seller getSellerProfile(String jwt) throws SellerException {
        String email = jwtProvider.getEmailFromJwtToken(jwt);
        if(email.startsWith("seller_")) {
            email = email.substring("seller_".length());
        }
        return this.getSellerByEmail(email);
    }

    @Override
    public Seller createSeller(Seller seller) throws Exception {
        Seller sellerExist=sellerRepository.findByEmail(seller.getEmail());
        if(sellerExist!=null){
            throw new Exception("seller already exist, use different email.");
        }
        Address savedAddress=addressRepository.save(seller.getPickupAddress());
        Seller newSeller=new Seller();
        newSeller.setEmail(seller.getEmail());
        newSeller.setPassword(passwordEncoder.encode(seller.getPassword()));
        newSeller.setSellerName(seller.getSellerName());
        newSeller.setPickupAddress(savedAddress);
        newSeller.setGSTIN(seller.getGSTIN());
        newSeller.setRole(USER_ROLE.ROLE_SELLER);
        newSeller.setMobile(seller.getMobile());
        newSeller.setBankDetails(seller.getBankDetails());
        newSeller.setBusinessDetails(seller.getBusinessDetails());
         return sellerRepository.save(newSeller);

    }

    @Override
    public Seller getSellerById(Long id) throws SellerException {
        return sellerRepository.findById(id).orElseThrow(()->new SellerException("seller not found with id: "+id));
    }

    @Override
    public Seller getSellerByEmail(String email) throws SellerException {
        Seller seller=sellerRepository.findByEmail(email);
        if(seller==null){
            throw new SellerException("seller not found..");
        }
        return seller;
    }

    @Override
    public List<Seller> getAllSellers(AccountStatus status) {
        if (status != null) {
            return sellerRepository.findByAccountStatus(status);
        }
        return sellerRepository.findAll();
    }

    @Override
    public Seller updateSeller(Long id, Seller seller) throws SellerException {
        Seller existingSeller=this.getSellerById(id);
        if(seller.getSellerName()!=null) {
            existingSeller.setSellerName(seller.getSellerName());
        }
        if(seller.getMobile()!=null) {
            existingSeller.setMobile(seller.getMobile());
        }
        if(seller.getEmail() != null) {
            Seller emailExists = sellerRepository.findByEmail(seller.getEmail());
            if(emailExists != null && !emailExists.getId().equals(id)){
                throw new SellerException("Email already in use by another seller");
            }
            existingSeller.setEmail(seller.getEmail());
        }
        if(seller.getBusinessDetails()!=null
                && seller.getBusinessDetails().getBusinessName() != null)
        {
            existingSeller.getBusinessDetails().setBusinessName(seller.getBusinessDetails().getBusinessName());
        }
        if(seller.getBankDetails() != null
                && seller.getBankDetails().getAccountHolderName() !=null
                && seller.getBankDetails().getIfscCode() !=null
                && seller.getBankDetails().getAccountNumber() !=null
        ){
            existingSeller.getBankDetails().setAccountHolderName(seller.getBankDetails().getAccountHolderName());
            existingSeller.getBankDetails().setIfscCode(seller.getBankDetails().getIfscCode());
            existingSeller.getBankDetails().setAccountNumber(seller.getBankDetails().getAccountNumber());
        }
        if(seller.getPickupAddress() != null
                && seller.getPickupAddress().getAddress() != null
                && seller.getPickupAddress().getMobile() != null
                && seller.getPickupAddress().getCity() != null
                && seller.getPickupAddress().getState() != null
        ){
            existingSeller.getPickupAddress().setAddress(seller.getPickupAddress().getAddress());
            existingSeller.getPickupAddress().setCity(seller.getPickupAddress().getCity());
            existingSeller.getPickupAddress().setState(seller.getPickupAddress().getState());
            existingSeller.getPickupAddress().setMobile(seller.getPickupAddress().getMobile());
            existingSeller.getPickupAddress().setPincode(seller.getPickupAddress().getPincode());
        }
        if(seller.getGSTIN() != null){
            existingSeller.setGSTIN(seller.getGSTIN());
        }
        return sellerRepository.save(existingSeller);
    }

    @Override
    public void deleteSeller(Long id) throws SellerException {
        Seller seller=getSellerById(id);
        sellerRepository.delete(seller);
    }

    @Override
    public Seller verifyEmail(String email, String otp) throws SellerException {
        Seller seller=getSellerByEmail(email);
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email);
        if(verificationCode == null || !verificationCode.getOtp().equals(otp)){
            throw new SellerException("Invalid OTP");
        }
        if(verificationCode.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new SellerException("OTP has expired");
        }
        seller.setEmailVerified(true);
        verificationCodeRepository.delete(verificationCode); // clean up after use
        return sellerRepository.save(seller);
    }

    @Override
    public Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws SellerException {

        Seller seller=getSellerById(sellerId);
        seller.setAccountStatus(status);
        return sellerRepository.save(seller);
    }
}
