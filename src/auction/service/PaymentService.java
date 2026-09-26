package auction.service;

import auction.enums.AuctionStatus;
import auction.enums.PaymentStatus;
import auction.exceptions.InvalidDataException;
import auction.model.Auction;

/**
 * Service for managing payment status on closed auctions.
 */
public class PaymentService {

    private final AuctionService auctionService;

    public PaymentService(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    /**
     * Marks payment as {@link PaymentStatus#PAID} for a closed auction.
     *
     * @throws InvalidDataException if the auction is not found, not CLOSED,
     *                              or has no winner
     */
    public void markAsPaid(String auctionId) throws InvalidDataException {
        Auction auction = getClosedAuctionWithWinner(auctionId);

        if (auction.getPaymentStatus() == PaymentStatus.PAID) {
            throw new InvalidDataException(
                    "Auction '%s' is already marked as PAID.".formatted(auctionId));
        }

        auction.setPaymentStatus(PaymentStatus.PAID);
    }

    /**
     * Returns the current {@link PaymentStatus} for an auction,
     * or {@code null} if the auction has no winner / payment tracking.
     *
     * @throws InvalidDataException if the auction is not found
     */
    public PaymentStatus getPaymentStatus(String auctionId) throws InvalidDataException {
        Auction auction = auctionService.findById(auctionId);
        if (auction == null) {
            throw new InvalidDataException(
                    "Auction with ID '%s' not found.".formatted(auctionId));
        }
        return auction.getPaymentStatus();
    }

    // ── Helpers ──────────────────────────────────────────────

    private Auction getClosedAuctionWithWinner(String auctionId)
            throws InvalidDataException {
        Auction auction = auctionService.findById(auctionId);
        if (auction == null) {
            throw new InvalidDataException(
                    "Auction with ID '%s' not found.".formatted(auctionId));
        }
        if (auction.getStatus() != AuctionStatus.CLOSED) {
            throw new InvalidDataException(
                    "Auction '%s' is %s — payment can only be processed for CLOSED auctions."
                            .formatted(auctionId, auction.getStatus()));
        }
        if (auction.getWinnerId() == null) {
            throw new InvalidDataException(
                    "Auction '%s' has no winner — cannot process payment."
                            .formatted(auctionId));
        }
        return auction;
    }
}
