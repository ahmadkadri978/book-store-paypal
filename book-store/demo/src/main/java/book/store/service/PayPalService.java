package book.store.service;


import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class PayPalService {

    private static final Logger logger = Logger.getLogger(PayPalService.class.getName());

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    public Payment createPayment(Double total, String currency, String method,
                                 String intent, String description, String cancelUrl, String successUrl)
            throws PayPalRESTException {
        // Format the amount to two decimal places
        String formattedTotal = String.format("%.2f", total).replace(",", ".");

        logger.info("Formatted Amount: " + formattedTotal);
        logger.info("Currency: " + currency);
        logger.info("Method: " + method);
        logger.info("Intent: " + intent);
        logger.info("Description: " + description);
        logger.info("Cancel URL: " + cancelUrl);
        logger.info("Success URL: " + successUrl);

        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(formattedTotal);

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        APIContext apiContext = new APIContext(clientId, clientSecret, mode);
        return payment.create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        APIContext apiContext = new APIContext(clientId, clientSecret, mode);
        return payment.execute(apiContext, paymentExecute);
    }
}


