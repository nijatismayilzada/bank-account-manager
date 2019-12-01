package com.revolut.revolutaccountmanager.repository.service;

import com.revolut.revolutaccountmanager.model.account.Account;
import com.revolut.revolutaccountmanager.model.exception.TransactionRuntimeException;
import com.revolut.revolutaccountmanager.model.transaction.Transaction;
import com.revolut.revolutaccountmanager.model.transaction.TransactionAction;
import com.revolut.revolutaccountmanager.service.ValidationService;
import com.revolut.revolutaccountmanager.util.TestHelper;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;

public class ValidationServiceTest {
    public static final int TRANSACTION_ID = 111;
    public static final int ACCOUNT_ID = 222;
    public static final String GBP = "GBP";
    private ValidationService validationService;

    @Before
    public void setUp() throws Exception {
        validationService = new ValidationService();
    }

    @Test(expected = TransactionRuntimeException.class)
    public void validateTransaction_givenEmptyInvalidAccount_failsValidation() {
        Account account = TestHelper.getAccount(0);
        Transaction transaction = TestHelper.getTransaction(TRANSACTION_ID, 0, BigDecimal.TEN, Currency.getInstance(GBP), TransactionAction.INCREASE);

        validationService.validateTransaction(account, transaction);
    }

    @Test(expected = TransactionRuntimeException.class)
    public void validateTransaction_givenValidAccountButDifferentCurrencyTransaction_failsValidation() {
        Account account = TestHelper.getAccount(ACCOUNT_ID, BigDecimal.TEN, Currency.getInstance(GBP));
        Transaction transaction = TestHelper.getTransaction(TRANSACTION_ID, 0, BigDecimal.TEN, Currency.getInstance("AZN"), TransactionAction.INCREASE);

        validationService.validateTransaction(account, transaction);
    }

    @Test(expected = TransactionRuntimeException.class)
    public void validateTransaction_givenNotEnoughFunds_failsValidation() {
        Account account = TestHelper.getAccount(ACCOUNT_ID, BigDecimal.ONE, Currency.getInstance(GBP));
        Transaction transaction = TestHelper.getTransaction(TRANSACTION_ID, 0, BigDecimal.TEN, Currency.getInstance(GBP), TransactionAction.DECREASE);

        validationService.validateTransaction(account, transaction);
    }

    @Test(expected = TransactionRuntimeException.class)
    public void validateTransaction_givenInvalidTransaction_failsValidation() {
        Account account = TestHelper.getAccount(ACCOUNT_ID, BigDecimal.ONE, Currency.getInstance(GBP));
        Transaction transaction = TestHelper.getTransaction(TRANSACTION_ID, 0, BigDecimal.ZERO, Currency.getInstance(GBP), TransactionAction.DECREASE);

        validationService.validateTransaction(account, transaction);
    }

    @Test
    public void validateTransaction_givenValidTransaction_doesNotFailValidation() {
        Account account = TestHelper.getAccount(ACCOUNT_ID, BigDecimal.ONE, Currency.getInstance(GBP));
        Transaction transaction = TestHelper.getTransaction(TRANSACTION_ID, 0, BigDecimal.ONE, Currency.getInstance(GBP), TransactionAction.DECREASE);

        validationService.validateTransaction(account, transaction);
    }
}