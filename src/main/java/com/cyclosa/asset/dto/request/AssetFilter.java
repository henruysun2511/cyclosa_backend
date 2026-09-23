package com.cyclosa.asset.dto.request;

import com.cyclosa.asset.enums.AssetCategory;
import com.cyclosa.asset.enums.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetFilter {

    private UUID companyId;
    private AssetCategory category;
    private AssetStatus status;
    private String keyword;
}
