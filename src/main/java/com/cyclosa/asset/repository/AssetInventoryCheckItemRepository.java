package com.cyclosa.asset.repository;

import com.cyclosa.asset.entity.AssetInventoryCheckItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssetInventoryCheckItemRepository extends JpaRepository<AssetInventoryCheckItem, UUID> {

    List<AssetInventoryCheckItem> findByInventoryCheckId(UUID inventoryCheckId);
}
