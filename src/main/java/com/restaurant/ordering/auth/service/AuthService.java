package com.restaurant.ordering.auth.service;

import com.restaurant.ordering.auth.dto.LoginRequest;
import com.restaurant.ordering.auth.dto.LoginResponse;
import com.restaurant.ordering.auth.dto.RegisterRequest;
import com.restaurant.ordering.auth.dto.VerifyRegistrationRequest;
import com.restaurant.ordering.model.Role;
import com.restaurant.ordering.model.User;
import com.restaurant.ordering.repository.UserRepository;
import com.restaurant.ordering.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RestClient restClient = RestClient.create();
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    @Value("${app.phone-verification.provider:console}")
    private String verificationProvider;

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.verify-service-sid:}")
    private String twilioVerifyServiceSid;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        return new LoginResponse(generateToken(user));
    }

    public void requestRegistration(RegisterRequest request) {
        validateRegistration(request);

        String emailKey = normalizeEmail(request.email());
        String phone = request.phone().trim();
        String localCode = usesTwilio() ? null : String.format("%06d", secureRandom.nextInt(1_000_000));

        if (usesTwilio()) {
            sendTwilioVerification(phone);
        } else {
            System.out.println("========================================");
            System.out.println("PHONE VERIFICATION (development mode)");
            System.out.println("Phone: " + phone);
            System.out.println("Code : " + localCode);
            System.out.println("========================================");
        }

        pendingRegistrations.put(
                emailKey,
                new PendingRegistration(
                        request,
                        localCode,
                        Instant.now().plus(10, ChronoUnit.MINUTES)
                )
        );
    }

    public LoginResponse verifyRegistration(VerifyRegistrationRequest request) {
        String emailKey = normalizeEmail(request.email());
        PendingRegistration pending = pendingRegistrations.get(emailKey);

        if (pending == null || pending.expiresAt().isBefore(Instant.now())) {
            pendingRegistrations.remove(emailKey);
            throw new IllegalArgumentException("Verification request expired. Please register again.");
        }

        boolean approved = usesTwilio()
                ? checkTwilioVerification(pending.request().phone(), request.code())
                : pending.localCode().equals(request.code());

        if (!approved) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        RegisterRequest registration = pending.request();
        validateRegistration(registration);

        User user = new User();
        user.setUsername(registration.username().trim());
        user.setPassword(passwordEncoder.encode(registration.password()));
        user.setFirstName(registration.firstName().trim());
        user.setLastName(registration.lastName().trim());
        user.setEmail(registration.email().trim().toLowerCase(Locale.ROOT));
        user.setPhone(registration.phone().trim());
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);

        User saved = userRepository.save(user);
        pendingRegistrations.remove(emailKey);

        return new LoginResponse(generateToken(saved));
    }

    private void validateRegistration(RegisterRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password()) ||
                isBlank(request.firstName()) || isBlank(request.lastName()) ||
                isBlank(request.email()) || isBlank(request.phone())) {
            throw new IllegalArgumentException("All registration fields are required");
        }

        if (request.password().length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters");
        }

        String email = request.email().trim();
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        String phone = request.phone().trim();
        if (!phone.matches("^\\+[1-9]\\d{7,14}$")) {
            throw new IllegalArgumentException("Phone number must use international E.164 format, for example +972501234567");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username().trim())) {
            throw new IllegalArgumentException("Username is already registered");
        }
    }

    private boolean usesTwilio() {
        return "twilio".equalsIgnoreCase(verificationProvider)
                && !isBlank(twilioAccountSid)
                && !isBlank(twilioAuthToken)
                && !isBlank(twilioVerifyServiceSid);
    }

    private void sendTwilioVerification(String phone) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", phone);
        form.add("Channel", "sms");

        restClient.post()
                .uri("https://verify.twilio.com/v2/Services/{serviceSid}/Verifications", twilioVerifyServiceSid)
                .headers(headers -> headers.setBasicAuth(twilioAccountSid, twilioAuthToken))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    private boolean checkTwilioVerification(String phone, String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", phone);
        form.add("Code", code);

        Map<String, Object> response = restClient.post()
                .uri("https://verify.twilio.com/v2/Services/{serviceSid}/VerificationCheck", twilioVerifyServiceSid)
                .headers(headers -> headers.setBasicAuth(twilioAccountSid, twilioAuthToken))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        return response != null && "approved".equalsIgnoreCase(String.valueOf(response.get("status")));
    }

    private String generateToken(User user) {
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record PendingRegistration(
            RegisterRequest request,
            String localCode,
            Instant expiresAt
    ) {
    }
}
