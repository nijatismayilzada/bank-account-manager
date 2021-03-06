package com.thepot.bankaccountmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepot.bankaccountmanager.model.account.Account;
import com.thepot.bankaccountmanager.model.exception.TransactionRuntimeException;
import com.thepot.bankaccountmanager.model.transaction.Transaction;
import com.thepot.bankaccountmanager.model.transaction.TransactionEvent;
import com.thepot.bankaccountmanager.model.transaction.TransactionType;
import com.thepot.bankaccountmanager.repository.AccountRepository;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Service
public class AccountService implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    private final MessageConsumer messageConsumer;
    private final TransactionManagerClient transactionManagerClient;
    private final MessageProducer messageProducer;
    private final ValidationService validationService;

    @Inject
    public AccountService(AccountRepository accountRepository, MessageConsumer messageConsumer, TransactionManagerClient transactionManagerClient, MessageProducer messageProducer, ValidationService validationService) throws JMSException {
        this.accountRepository = accountRepository;
        this.messageConsumer = messageConsumer;
        messageConsumer.getConsumer().setMessageListener(this);
        this.transactionManagerClient = transactionManagerClient;
        this.messageProducer = messageProducer;
        this.validationService = validationService;
    }

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            TransactionEvent transactionEvent = new ObjectMapper().readValue(textMessage.getText(), TransactionEvent.class);

            Transaction transaction = transactionManagerClient.getTransactionById(transactionEvent.getTransactionId());


            processTransaction(transaction);

            messageProducer.getTransactionCompletedProducer().send(message);

        } catch (Exception e) {
            handleProcessFailure(message, e);
        }

    }

    private void handleProcessFailure(Message message, Exception e) {
        try {
            LOG.error("Failed to process message: {}", message, e);
            messageProducer.getTransactionFailedProducer().send(message);
        } catch (JMSException ex) {
            LOG.error("JMS error while handling failed message", ex);
            throw new TransactionRuntimeException(ex);
        }
    }

    private void processTransaction(Transaction transaction) {
        Account account = accountRepository.getAccount(transaction.getAccountId());
        validationService.validateTransaction(account, transaction);
        if (transaction.getTransactionType() == TransactionType.SIMPLE_INCREASE) {
            accountRepository.increaseAccountBalance(account.getAccountId(), transaction.getAmount());
        } else if (transaction.getTransactionType() == TransactionType.SIMPLE_DECREASE) {
            accountRepository.decreaseAccountBalance(account.getAccountId(), transaction.getAmount());
        } else if (transaction.getTransactionType() == TransactionType.TRANSFER) {
            accountRepository.decreaseAccountBalance(account.getAccountId(), transaction.getAmount());
            transactionManagerClient.createSimpleIncreaseTransactionRequest(transaction);
        } else {
            throw new TransactionRuntimeException(String.format("Does not support this transaction type. transaction %s", transaction));
        }
    }

}
