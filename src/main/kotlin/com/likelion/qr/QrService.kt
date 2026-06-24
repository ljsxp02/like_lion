package com.likelion.qr

import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.common.auth.CurrentUserProvider
import com.likelion.domain.benefitusage.BenefitUsageEntity
import com.likelion.domain.benefitusage.BenefitUsageRepository
import com.likelion.domain.qr.QrTokenRepository
import com.likelion.domain.store.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class QrService(
    private val currentUserProvider: CurrentUserProvider,
    private val qrTokenRepository: QrTokenRepository,
    private val storeRepository: StoreRepository,
    private val benefitUsageRepository: BenefitUsageRepository,
) {
    @Transactional
    fun verify(request: QrVerifyRequest): QrVerifyResponse {
        val token = request.qrToken.trim()
        if (token.isEmpty()) {
            throw ApiException(ErrorCode.QR_400)
        }
        val qrToken = qrTokenRepository.findByTokenAndIsActiveTrue(token)
            ?: throw ApiException(ErrorCode.QR_404)
        val storeId = qrToken.storeId
        val store = storeRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(storeId)
            ?: throw ApiException(ErrorCode.QR_404)
        val userId = currentUserProvider.currentUserId()
        val qrTokenId = requireNotNull(qrToken.id)
        if (benefitUsageRepository.existsByUserIdAndQrTokenId(userId, qrTokenId)) {
            throw ApiException(ErrorCode.QR_409)
        }

        val verifiedAt = LocalDateTime.now()
        val usage = benefitUsageRepository.save(
            BenefitUsageEntity(
                userId = userId,
                storeId = storeId,
                qrTokenId = qrTokenId,
                usedAt = verifiedAt,
            ),
        )

        return QrVerifyResponse(
            usageId = requireNotNull(usage.id),
            storeId = storeId,
            storeName = store.name,
            verifiedAt = verifiedAt,
        )
    }
}
