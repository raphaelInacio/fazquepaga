package com.fazquepaga.taskandpay.whatsapp;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioWhatsAppClient implements WhatsAppClient {

    private final String accountSid;
    private final String authToken;
    private final String fromPhoneNumber;

    public TwilioWhatsAppClient(
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.from-phone-number}") String fromPhoneNumber) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromPhoneNumber = fromPhoneNumber;
        Twilio.init(accountSid, authToken);
    }

    @Override
    public void sendMessage(String to, String message) {
        Message.creator(
                        new PhoneNumber("whatsapp:" + to),
                        new PhoneNumber("whatsapp:" + fromPhoneNumber),
                        message)
                .create();
    }
}
