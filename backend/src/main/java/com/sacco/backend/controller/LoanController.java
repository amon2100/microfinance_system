package com.sacco.backend.controller;

import com.sacco.backend.dto.LoanActionRequest;
import com.sacco.backend.dto.LoanCreateRequest;
import com.sacco.backend.model.Loan;
import com.sacco.backend.security.AuthUser;
import com.sacco.backend.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public Loan create(@Valid @RequestBody LoanCreateRequest request, Authentication authentication) {
        AuthUser actor = (AuthUser) authentication.getPrincipal();
        return loanService.createLoan(request, actor);
    }

    @PostMapping("/approve")
    public void approve(@Valid @RequestBody LoanActionRequest request, Authentication authentication) {
        AuthUser actor = (AuthUser) authentication.getPrincipal();
        loanService.approveLoan(request.getLoanId(), actor);
    }

    @PostMapping("/disburse")
    public void disburse(@Valid @RequestBody LoanActionRequest request, Authentication authentication) {
        AuthUser actor = (AuthUser) authentication.getPrincipal();
        loanService.disburseLoan(request.getLoanId(), actor);
    }
}
