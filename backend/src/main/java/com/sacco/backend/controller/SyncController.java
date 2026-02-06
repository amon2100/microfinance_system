package com.sacco.backend.controller;

import com.sacco.backend.dto.MemberSyncRequest;
import com.sacco.backend.dto.UserSyncRequest;
import com.sacco.backend.dto.SavingsSyncRequest;
import com.sacco.backend.dto.LoanSyncRequest;
import com.sacco.backend.dto.TransactionSyncRequest;
import com.sacco.backend.model.Member;
import com.sacco.backend.model.User;
import com.sacco.backend.model.SavingsAccount;
import com.sacco.backend.model.Loan;
import com.sacco.backend.model.Transaction;
import com.sacco.backend.model.LoanStatus;
import com.sacco.backend.model.Role;
import com.sacco.backend.repository.MemberRepository;
import com.sacco.backend.repository.UserRepository;
import com.sacco.backend.repository.SavingsAccountRepository;
import com.sacco.backend.repository.LoanRepository;
import com.sacco.backend.repository.TransactionRepository;
import com.sacco.backend.service.AuditService;
import com.sacco.backend.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/sync")
public class SyncController {
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final String syncKey;

    public SyncController(MemberRepository memberRepository,
                          UserRepository userRepository,
                          SavingsAccountRepository savingsAccountRepository,
                          LoanRepository loanRepository,
                          TransactionRepository transactionRepository,
                          FileStorageService fileStorageService,
                          AuditService auditService,
                          @Value("${app.syncKey:CHANGE_ME}") String syncKey) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.loanRepository = loanRepository;
        this.transactionRepository = transactionRepository;
        this.fileStorageService = fileStorageService;
        this.auditService = auditService;
        this.syncKey = syncKey;
    }

    @PostMapping("/members")
    public Member syncMember(@RequestHeader("X-Sync-Key") String key,
                             @Valid @RequestBody MemberSyncRequest request) {
        requireKey(key);
        Member member = memberRepository.findByExternalId(request.getExternalId()).orElseGet(Member::new);
        member.setExternalId(request.getExternalId());
        member.setFullName(request.getFullName());
        member.setNationalId(request.getNationalId());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());
        Member saved = memberRepository.save(member);
        auditService.log(null, "SYNC_MEMBER", "MEMBER", saved.getId(), "ExternalId=" + request.getExternalId());
        return saved;
    }

    @PostMapping("/members/{externalId}/photo")
    public void uploadMemberPhoto(@RequestHeader("X-Sync-Key") String key,
                                  @PathVariable String externalId,
                                  @RequestParam("file") MultipartFile file) {
        requireKey(key);
        Member member = memberRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        String path = fileStorageService.saveMemberPhoto(externalId, file);
        member.setPhotoUrl(path);
        memberRepository.save(member);
        auditService.log(null, "SYNC_MEMBER_PHOTO", "MEMBER", member.getId(), "ExternalId=" + externalId);
    }

    @PostMapping("/users")
    public User syncUser(@RequestHeader("X-Sync-Key") String key,
                         @Valid @RequestBody UserSyncRequest request) {
        requireKey(key);
        User user = userRepository.findByExternalId(request.getExternalId()).orElseGet(User::new);
        user.setExternalId(request.getExternalId());
        user.setUsername(request.getUsername());
        user.setRole(Role.valueOf(request.getRole()));
        User saved = userRepository.save(user);
        auditService.log(null, "SYNC_USER", "USER", saved.getId(), "ExternalId=" + request.getExternalId());
        return saved;
    }

    @PostMapping("/savings")
    public SavingsAccount syncSavings(@RequestHeader("X-Sync-Key") String key,
                                      @Valid @RequestBody SavingsSyncRequest request) {
        requireKey(key);
        Member member = memberRepository.findByExternalId(request.getMemberExternalId())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        SavingsAccount account = savingsAccountRepository.findByExternalId(request.getExternalId())
                .orElseGet(SavingsAccount::new);
        account.setExternalId(request.getExternalId());
        account.setMemberId(member.getId());
        account.setAccountNo(request.getAccountNo());
        SavingsAccount saved = savingsAccountRepository.save(account);
        auditService.log(null, "SYNC_SAVINGS", "SAVINGS_ACCOUNT", saved.getId(), "ExternalId=" + request.getExternalId());
        return saved;
    }

    @PostMapping("/loans")
    public Loan syncLoan(@RequestHeader("X-Sync-Key") String key,
                         @Valid @RequestBody LoanSyncRequest request) {
        requireKey(key);
        Member member = memberRepository.findByExternalId(request.getMemberExternalId())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Loan loan = loanRepository.findByExternalId(request.getExternalId()).orElseGet(Loan::new);
        loan.setExternalId(request.getExternalId());
        loan.setMemberId(member.getId());
        loan.setPrincipal(request.getPrincipal());
        loan.setInterestRate(request.getInterestRate());
        loan.setTermMonths(request.getTermMonths());
        if (loan.getStatus() == null) {
            loan.setStatus(LoanStatus.PENDING);
        }
        if (loan.getOutstandingBalance() == null) {
            loan.setOutstandingBalance(request.getPrincipal());
        }
        Loan saved = loanRepository.save(loan);
        auditService.log(null, "SYNC_LOAN", "LOAN", saved.getId(), "ExternalId=" + request.getExternalId());
        return saved;
    }

    @PostMapping("/transactions")
    public Transaction syncTransaction(@RequestHeader("X-Sync-Key") String key,
                                        @Valid @RequestBody TransactionSyncRequest request) {
        requireKey(key);
        Member member = memberRepository.findByExternalId(request.getMemberExternalId())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Transaction tx = transactionRepository.findByExternalId(request.getExternalId()).orElseGet(Transaction::new);
        tx.setExternalId(request.getExternalId());
        tx.setMemberId(member.getId());
        tx.setType(request.getType());
        tx.setAmount(request.getAmount());
        Transaction saved = transactionRepository.save(tx);
        auditService.log(null, "SYNC_TRANSACTION", "TRANSACTION", saved.getId(), "ExternalId=" + request.getExternalId());
        return saved;
    }

    private void requireKey(String key) {
        if (!syncKey.equals(key)) {
            throw new RuntimeException("Invalid sync key");
        }
    }
}
