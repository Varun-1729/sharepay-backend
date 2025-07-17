package com.splitwise.util;

import com.splitwise.entity.User;
import com.splitwise.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
public class SecurityUtil {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get the current logged-in user from session
     * @return the current user or null if not logged in
     */
    public User getCurrentUser() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(false);
            
            if (session != null) {
                User currentUser = (User) session.getAttribute("currentUser");
                if (currentUser != null) {
                    // Refresh user data from database to get latest info
                    Optional<User> userOpt = userRepository.findById(currentUser.getId());
                    return userOpt.orElse(null);
                }
            }
        } catch (Exception e) {
            // Session not available or other error
        }
        return null;
    }

    /**
     * Get the current user ID
     * @return the current user ID or null if not logged in
     */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Check if a user is logged in
     * @return true if user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }
}
