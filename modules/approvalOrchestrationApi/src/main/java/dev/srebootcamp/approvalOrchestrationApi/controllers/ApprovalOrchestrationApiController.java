package dev.srebootcamp.approvalOrchestrationApi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.srebootcamp.approvalOrchestrationApi.clients.AuditClient;
import dev.srebootcamp.approvalOrchestrationApi.clients.CustomerClient;
import dev.srebootcamp.approvalOrchestrationApi.clients.FraudClient;
import dev.srebootcamp.approvalOrchestrationApi.clients.MandateClient;
import dev.srebootcamp.approvalOrchestrationApi.domain.DetectFraudData;
import dev.srebootcamp.approvalOrchestrationApi.domain.Mandate;
import dev.srebootcamp.approvalOrchestrationApi.domain.payments.*;
import dev.srebootcamp.approvalOrchestrationApi.service.IBasket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.Callable;

@RestController
public class ApprovalOrchestrationApiController {

  @Autowired
  CustomerClient customerClient;
  @Autowired
  MandateClient mandateClient;
  @Autowired
  IBasket basket;

  @Autowired
  AuditClient audit;

  @Autowired
  FraudClient fraudClient;

  @Autowired
  ObjectMapper mapper;

  <P extends IPayment> ResponseEntity<P> result(Optional<P> result) {
    return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @PostMapping(value = "/payment", produces = "application/json")
  public NewPayment createPaymentEndpoint(@RequestBody NewPaymentRequest payment) throws Exception {
    NewPayment result = basket.newPayment(payment);
    audit.audit("Payment request: " + result.id() + " for " + result.amount() + " from " + result.payer().customerId() + " to " + result.payee().customerId());
    return result;
  }

  <T> T audit(String msg, Callable<T> action) throws Exception {
    try {
      T result = action.call();
      audit.audit(msg + " succeeded");
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      audit.audit(msg + " failed");
      throw e;
    }
  }

  @PostMapping(value = "/payment/{id}/mandate", produces = "application/json")
  public ResponseEntity<IPayment> addMandateEndpoint(@PathVariable String id, @RequestBody Mandate mandate) throws Exception {
    return result(audit("Add mandate to " + id, () -> basket.map(id, PaymentMapper.onlyNewPayment("Can only add a mandate if there isn't one already", p -> p.asPayment(mandate)))));
  }


  @PostMapping(value = "/payment/{id}/approve", produces = "application/json")
  public ResponseEntity<IPayment> approvePaymentEndpoint(@PathVariable String id) throws Exception {
    return result(audit("approve " + id, () -> basket.map(id, PaymentMapper.onlyPayment("Can only approve a payment that has been given a mandate", payment -> {
      var mandates = mandateClient.getMandateForCustomer(payment.payer().customerId());
      var validMandates = mandates.stream().filter(m -> m.matches(payment)).findFirst();
      if (validMandates.isEmpty()) throw new RuntimeException("No valid mandate found");
      var mandate = validMandates.get();
      var customer = customerClient.getCustomer(payment.payer().customerId());
      var fraud = fraudClient.detectFraud(new DetectFraudData(payment.payer().customerId(), payment.payee().customerId(), Double.toString(payment.amount().amount())));
      if (fraud == null) throw new RuntimeException("Fraud detection failed");
      if (fraud) throw new RuntimeException("Fraud detected");
      if (customer == null || customer.id() == null) throw new RuntimeException("Customer does not exist");
      if (customer.id().equals(payment.payer().customerId())) //really just checking the customer exists...
        if (mandate.mandateId().equals(payment.payer().mandateId()))
          if (mandate.accountId().equals(payment.payer().accountId()))
            return payment.approve();
      throw new RuntimeException("Mandate does not match payment");
    }))));
  }

  @PostMapping(value = "/payment/{id}/reject", produces = "application/json")
  public ResponseEntity<IPayment> rejectPaymentEndpoint(@PathVariable String id) throws Exception {
    return result(audit("reject" + "id", () -> basket.map(id, new PaymentMapper(Payment::reject, NewPayment::reject))));
  }

}
