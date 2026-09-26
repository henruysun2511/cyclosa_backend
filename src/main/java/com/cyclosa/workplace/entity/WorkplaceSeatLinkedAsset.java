package com.cyclosa.workplace.entity;

import com.cyclosa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "workplace_seat_linked_assets")
@SQLDelete(sql = "UPDATE workplace_seat_linked_assets SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkplaceSeatLinkedAsset extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_seat_id", nullable = false)
    private WorkplaceSeat seat;

    @Column(name = "asset_id", nullable = false)
    private UUID assetId; // FK tới Asset (Module 13)
}
