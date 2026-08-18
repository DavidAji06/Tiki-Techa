package com.tikitecha.backend.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tikitecha.backend.dto.BuyPlayerRequest;
import com.tikitecha.backend.model.Transaction;
import com.tikitecha.backend.model.TransactionType;
import com.tikitecha.backend.model.User;
import com.tikitecha.backend.repository.TransactionRepository;
import com.tikitecha.backend.repository.UserRepository;
import com.tikitecha.backend.service.TransferService;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransferService transferService;

    public TransferController(TransactionRepository transactionRepository, UserRepository userRepository, TransferService transferService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.transferService = transferService;
    }

    @GetMapping
    public List<Transaction> getTransfers(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount
    ) {
        ArrayList<Specification<Transaction>> filters = new ArrayList<>();

        if (userId != null) {
            filters.add((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId));
        }
        if (type != null) {
            filters.add((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type));
        }
        if (minAmount != null) {
            filters.add((root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minAmount));
        }
        if (maxAmount != null) {
            filters.add((root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxAmount));
        }

        if (filters.isEmpty()) {
            return transactionRepository.findAll();
        }

        Specification<Transaction> spec = filters.get(0);
        for (int i = 1; i < filters.size(); i++) {
            spec = spec.and(filters.get(i));
        }
        return transactionRepository.findAll(spec);
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyPlayer(@RequestBody BuyPlayerRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        try {
            transferService.buyPlayer(user.getId(), request.getPlayerId(), request.isStarting());
            return ResponseEntity.ok("Player purchased successfully");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellPlayer(@RequestBody BuyPlayerRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        try {
            transferService.sellPlayer(user.getId(), request.getPlayerId());
            return ResponseEntity.ok("Player sold successfully");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}