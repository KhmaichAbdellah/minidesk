package com.minidesk.config;

import com.minidesk.entity.User;
import com.minidesk.enums.Role;
import com.minidesk.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DataInitializerTest {

    @Test
    void doesNotCreateWhenAdminExists() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repo.existsByRole(Role.ADMIN)).thenReturn(true);
        DataInitializer init = new DataInitializer(repo, encoder);
        // set properties
        setField(init,"adminName","Admin");
        setField(init,"adminEmail","admin@example.com");
        setField(init,"adminPassword","secret");
        init.run();
        verify(repo, never()).save(any());
    }

    @Test
    void createsAdminWhenMissing() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repo.existsByRole(Role.ADMIN)).thenReturn(false);
        when(encoder.encode("secret")).thenReturn("encodedSecret");
        DataInitializer init = new DataInitializer(repo, encoder);
        setField(init,"adminName","Admin");
        setField(init,"adminEmail","admin@example.com");
        setField(init,"adminPassword","secret");
        init.run();
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(repo).save(cap.capture());
        User saved = cap.getValue();
        assertEquals("Admin", saved.getName());
        assertEquals("admin@example.com", saved.getEmail());
        assertEquals("encodedSecret", saved.getPassword());
        assertEquals(Role.ADMIN, saved.getRole());
        assertTrue(saved.isEnabled());
    }

    private void setField(Object target, String name, String value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
