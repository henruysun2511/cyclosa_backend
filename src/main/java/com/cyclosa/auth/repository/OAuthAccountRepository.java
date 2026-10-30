package com.cyclosa.auth.repository;

import com.cyclosa.auth.entity.OAuthAccount;
import com.cyclosa.auth.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, UUID> {

    Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    boolean existsByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
}

