package io.github.nct682000.authservice.service;

import io.github.nct682000.authservice.dto.response.UserProfileResponseDTO;
import io.github.nct682000.authservice.entity.AuthUserDetails;
import io.github.nct682000.authservice.exception.AuthException;
import io.github.nct682000.authservice.exception.UserNotFoundException;
import io.github.nct682000.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getMe(AuthUserDetails currentUser) throws AuthException {
        return userRepository.findById(currentUser.getUserId())
                .map(UserProfileResponseDTO::from)
                .orElseThrow(UserNotFoundException::new);
    }
}
