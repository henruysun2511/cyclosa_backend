package com.cyclosa.employee.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FamilyRelationship {
    SPOUSE("Vợ / Chồng"),
    CHILD("Con ruột / Con nuôi"),
    PARENT("Bố / Mẹ"),
    SIBLING("Anh / Chị / Em"),
    OTHER("Khác");

    private final String description;
}
