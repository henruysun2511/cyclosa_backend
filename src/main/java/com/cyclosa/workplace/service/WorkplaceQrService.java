package com.cyclosa.workplace.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.workplace.dto.request.QrScanRequest;
import com.cyclosa.workplace.dto.response.QrScanResultResponse;
import com.cyclosa.workplace.dto.response.WorkplaceNonDeskObjectResponse;
import com.cyclosa.workplace.dto.response.WorkplaceSeatDetailResponse;
import com.cyclosa.workplace.entity.WorkplaceNonDeskObject;
import com.cyclosa.workplace.entity.WorkplaceSeat;
import com.cyclosa.workplace.enums.NonDeskObjectType;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.mapper.WorkplaceMapper;
import com.cyclosa.workplace.repository.WorkplaceNonDeskObjectRepository;
import com.cyclosa.workplace.repository.WorkplaceSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkplaceQrService {

    private static final String SEAT_URI_PREFIX = "cyclosa://workplace/seat/";
    private static final String ROOM_URI_PREFIX = "cyclosa://workplace/room/";

    private static final Pattern UUID_PATTERN = Pattern.compile("([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");

    private final WorkplaceSeatRepository seatRepository;
    private final WorkplaceNonDeskObjectRepository nonDeskObjectRepository;
    private final WorkplaceSeatService seatService;
    private final WorkplaceMapper workplaceMapper;
    private final WorkplaceSecurityContext securityContext;

    @Transactional
    public String generateSeatQrCode(UUID seatId) {
        WorkplaceSeat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SEAT_NOT_FOUND));

        String qrString = SEAT_URI_PREFIX + seat.getId() + "?code=" + seat.getSeatCode();
        seat.setQrCodeData(qrString);
        seatRepository.save(seat);

        log.info("Generated QR code for seat {}: {}", seat.getSeatCode(), qrString);
        return qrString;
    }

    @Transactional(readOnly = true)
    public String generateRoomQrCode(UUID roomId) {
        WorkplaceNonDeskObject room = nonDeskObjectRepository.findById(roomId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.NON_DESK_OBJECT_NOT_FOUND));

        return ROOM_URI_PREFIX + room.getId() + "?code=" + room.getName();
    }

    @Transactional(readOnly = true)
    public QrScanResultResponse processQrScan(QrScanRequest request) {
        UUID companyId = securityContext.getCurrentCompanyId();
        String qrData = request.getQrCodeData().trim();

        // 1. Kiểm tra nếu là QR Ghế ngồi
        if (qrData.startsWith(SEAT_URI_PREFIX)) {
            UUID seatId = extractUuid(qrData);
            return handleSeatQr(companyId, seatId);
        }

        // 2. Kiểm tra nếu là QR Phòng họp
        if (qrData.startsWith(ROOM_URI_PREFIX)) {
            UUID roomId = extractUuid(qrData);
            return handleRoomQr(companyId, roomId);
        }

        // 3. Fallback: Thử tìm theo seatCode hoặc qrCodeData
        if (companyId != null) {
            WorkplaceSeat seatByCode = seatRepository.findByCompanyIdAndSeatCode(companyId, qrData).orElse(null);
            if (seatByCode != null) {
                return handleSeatQr(companyId, seatByCode.getId());
            }

            WorkplaceNonDeskObject objByCode = nonDeskObjectRepository.findByCompanyIdAndQrCodeData(companyId, qrData).orElse(null);
            if (objByCode != null) {
                return handleRoomQr(companyId, objByCode.getId());
            }
        }

        // Fallback: nếu chuỗi quét chứa UUID
        UUID extracted = extractUuid(qrData);
        if (extracted != null) {
            if (seatRepository.existsById(extracted)) {
                return handleSeatQr(companyId, extracted);
            }
            if (nonDeskObjectRepository.existsById(extracted)) {
                return handleRoomQr(companyId, extracted);
            }
        }

        throw new AppException(WorkplaceErrorCode.QR_CODE_INVALID);
    }

    private QrScanResultResponse handleSeatQr(UUID companyId, UUID seatId) {
        WorkplaceSeatDetailResponse seatDetail = seatService.getSeatDetail(seatId);

        return QrScanResultResponse.builder()
                .targetType("SEAT")
                .targetId(seatDetail.getId())
                .targetCode(seatDetail.getSeatCode())
                .targetName("Bàn làm việc " + seatDetail.getSeatCode())
                .floorNumber(seatDetail.getFloorNumber())
                .zoneName(seatDetail.getZoneName())
                .actionSuggestion("CHECK_IN_SEAT")
                .details(seatDetail)
                .build();
    }

    private QrScanResultResponse handleRoomQr(UUID companyId, UUID roomId) {
        WorkplaceNonDeskObject room = nonDeskObjectRepository.findById(roomId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.NON_DESK_OBJECT_NOT_FOUND));

        WorkplaceNonDeskObjectResponse roomResp = workplaceMapper.toNonDeskObjectResponse(room);

        String suggestion = (room.getObjectType() == NonDeskObjectType.MEETING_ROOM)
                ? "BOOK_ROOM_QUICK"
                : "WAYFINDING_ORIGIN";

        return QrScanResultResponse.builder()
                .targetType(room.getObjectType() == NonDeskObjectType.MEETING_ROOM ? "MEETING_ROOM" : "FACILITY")
                .targetId(room.getId())
                .targetCode(room.getName())
                .targetName(room.getName())
                .floorNumber(room.getZone() != null && room.getZone().getFloor() != null ? room.getZone().getFloor().getFloorNumber() : null)
                .zoneName(room.getZone() != null ? room.getZone().getName() : null)
                .actionSuggestion(suggestion)
                .details(roomResp)
                .build();
    }

    private UUID extractUuid(String text) {
        Matcher matcher = UUID_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return UUID.fromString(matcher.group(1));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
