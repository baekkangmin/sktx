package com.skala.sktx.controller;

import com.skala.sktx.common.ApiResponse;
import com.skala.sktx.consumer.PaymentConsumer;
import com.skala.sktx.dto.api.payment.PaymentConfirmRequest;
import com.skala.sktx.entity.Ticket;
import com.skala.sktx.service.PaymentService;
import com.skala.sktx.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

// 결제 확정 API: POST /api/payments/confirm
// 성공하면 Ticket 발급하고 SSE로 결제 완료 이벤트 push

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final TicketService ticketService;
    private final PaymentConsumer paymentConsumer;

    public PaymentController(PaymentService paymentService, TicketService ticketService, PaymentConsumer paymentConsumer) {
        this.paymentService = paymentService;
        this.ticketService = ticketService;
        this.paymentConsumer = paymentConsumer;
    }

    @PostMapping("/confirm")
    public ApiResponse<Object> confirm(@Valid @RequestBody PaymentConfirmRequest req) {
        paymentService.confirmPayment(req.reservationId());
        Ticket ticket = ticketService.issueIfPaid(req.reservationId());

        // SSE + Kafka 알림
        paymentConsumer.onPaymentConfirmed(req.reservationId(), ticket.getId());

        return ApiResponse.ok(
                java.util.Map.of("reservationId", req.reservationId(), "ticketId", ticket.getId(), "status", "CONFIRMED")
        );
    }
}
