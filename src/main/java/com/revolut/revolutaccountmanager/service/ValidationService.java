package com.revolut.revolutaccountmanager.service;

import com.revolut.revolutaccountmanager.model.account.Account;
import com.revolut.revolutaccountmanager.model.exception.TransactionRuntimeException;
import com.revolut.revolutaccountmanager.model.transaction.Transaction;
import com.revolut.revolutaccountmanager.model.transaction.TransactionAction;
import org.jvnet.hk2.annotations.Service;

@Service
public class ValidationService {
    public void validateTransaction(Account account, Transaction transaction) {
        if (account.getAccountId() == 0) {
            throw new TransactionRuntimeException(String.format("Could not find account. account: %s, transaction %s", account, transaction));
        }

        if (!account.getCurrency().equals(transaction.getCurrency())) {
            throw new TransactionRuntimeException(String.format("Different currency transactions are not supported yet. account: %s, transaction %s", account, transaction));
        }

        if (transaction.getTransactionAction() == TransactionAction.DECREASE) {
            if (transaction.getAmount().compareTo(account.getBalance()) > 0) {
                throw new TransactionRuntimeException(String.format("Not enough funds. account: %s, transaction %s", account, transaction));
            }
        }
    }
}