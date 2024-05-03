package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)
    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and

        // Check if User with given userId exists
        Optional<User> userOptional = this.userRepository.findById(payload.getUserId());
        if(userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(payload.getUserId());
        }
        User user = userOptional.get();

        // Create a new CreditCard entity from the payload
        CreditCard creditCardToCreate = new CreditCard();
        creditCardToCreate.setIssuanceBank(payload.getCardIssuanceBank());
        creditCardToCreate.setNumber(payload.getCardNumber());

        BalanceHistory initBalanceHistory = new BalanceHistory();
        initBalanceHistory.setBalance(0);
        initBalanceHistory.setDate(LocalDate.now());
        creditCardToCreate.setBalanceHistory(new TreeMap<LocalDate, BalanceHistory>(Map.<LocalDate, BalanceHistory>of(LocalDate.now(), initBalanceHistory)));

        // Associate card with the user
        creditCardToCreate.setOwner(user);

        CreditCard createdCreditCard = this.creditCardRepository.save(creditCardToCreate);

        return ResponseEntity.ok(createdCreditCard.getId());
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null

        // Get all creditCards associated with given userId
        List<CreditCardView> creditCardViews = new ArrayList<>();
        Optional<User> userOptional = this.userRepository.findById(userId);

        if(userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(creditCardViews);
        }

        User user = userOptional.get();
        List<CreditCard> creditCards = user.getCreditCards();

        creditCardViews = creditCards.stream()
                .map(creditCard ->
                    CreditCardView.builder()
                        .issuanceBank(creditCard.getIssuanceBank())
                        .number(creditCard.getNumber())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(creditCardViews);
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        // Find creditCard from creditCardNumber
        Optional<CreditCard> creditCardOptional = creditCardRepository.findByNumber(creditCardNumber);

        if (creditCardOptional.isPresent()) {
            CreditCard creditCard = creditCardOptional.get();

            // Check if a user is associated with the credit card
            Integer userId = creditCard.getOwner().getId();
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                return ResponseEntity.ok(userId);
            }
        }

        return ResponseEntity.badRequest().body(null);

    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<String> updateBalance(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      1. For the balance history in the credit card
        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
        //      4. If the different is not 0, update all the following budget with the difference
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
        //      This is because
        //      1. You would first populate 4/11 with previous day's balance (4/10), so {date: 4/11, amount: 100}
        //      2. And then you observe there is a +10 difference
        //      3. You propagate that +10 difference until today
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.

//        // Sort payload by date
//        Arrays.sort(payload, (a, b) -> a.getBalanceDate().compareTo(b.getBalanceDate())); // Uses Inline Comparator
//
//        for (UpdateBalancePayload transaction: payload) {
//            // Find CreditCard associated with card number in transaction
//            Optional<CreditCard> creditCardOptional = creditCardRepository.findByNumber(transaction.getCreditCardNumber());
//            if (creditCardOptional.isPresent()) {
//                CreditCard creditCard = creditCardOptional.get();
//
//                SortedMap<LocalDate, Double> balanceHistory = creditCard.getBalanceHistory();
//                if (balanceHistory == null) {
//                    balanceHistory = new TreeMap<>();
//                }
//
//
//                LocalDate currentDate = LocalDate.now();
//                Double previousBalance = null;
//                LocalDate previousBalanceDate = null; // Track previous balance date
//
//                for (LocalDate date : new TreeMap<>(balanceHistory).keySet()) {
//                    // Fill gaps and propagate differences
//
//                    Double balance = balanceHistory.get(date);
//
//                    // Fill gaps between dates with the previous balance
//                    if (previousBalance != null && !date.minusDays(1).equals(previousBalanceDate)) {
//                        LocalDate gapDate = date.minusDays(1);
//                        while (!gapDate.equals(previousBalanceDate)) {
//                            balanceHistory.put(gapDate, previousBalance);
//                            gapDate = gapDate.minusDays(1);
//                        }
//                    }
//
//                    // Update the previous balance and balance date for the next iteration
//                    previousBalance = balance;
//                    previousBalanceDate = date;
//                }
//
//                // Save the updated balance history
//                creditCardRepository.save(creditCard);
//            }
//        }


        return ResponseEntity.ok("Balance history updated successfully");
    }
    
}
