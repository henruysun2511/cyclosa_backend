package com.cyclosa.auth.entity;

import com.cyclosa.auth.enums.OAuthProvider;
import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "oauth_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_oauth_provider_user", columnNames = {"provider", "provider_user_id"})
}, indexes = {
        @Index(name = "idx_oauth_user_id", columnList = "user_id"),
        @Index(name = "idx_oauth_provider_user", columnList = "provider, provider_user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE oauth_accounts SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class OAuthAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;
}

