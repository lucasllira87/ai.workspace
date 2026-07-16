package com.aiworkspace.billing.application.service;

import com.aiworkspace.billing.application.dto.InvoiceDto;
import com.aiworkspace.billing.application.port.in.GetInvoiceUseCase;
import com.aiworkspace.billing.application.port.out.InvoiceRepository;
import com.aiworkspace.billing.application.port.out.PaymentGatewayPort;
import com.aiworkspace.billing.application.port.out.PlanRepository;
import com.aiworkspace.billing.application.port.out.SubscriptionRepository;
import com.aiworkspace.billing.domain.exception.InvoiceNotFoundException;
import com.aiworkspace.billing.domain.exception.PlanNotFoundException;
import com.aiworkspace.billing.domain.exception.SubscriptionNotFoundException;
import com.aiworkspace.billing.domain.model.Invoice;
import com.aiworkspace.billing.domain.model.InvoiceLineItem;
import com.aiworkspace.billing.domain.model.Plan;
import com.aiworkspace.billing.domain.model.Subscription;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService implements GetInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PaymentGatewayPort paymentGateway;
    private final ApplicationEventPublisher eventPublisher;

    public InvoiceService(InvoiceRepository invoiceRepository,
                           SubscriptionRepository subscriptionRepository,
                           PlanRepository planRepository,
                           PaymentGatewayPort paymentGateway,
                           ApplicationEventPublisher eventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.paymentGateway = paymentGateway;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Invoice generateForSubscription(UUID subscriptionId, UUID userId) {
        Subscription subscription = subscriptionRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new SubscriptionNotFoundException(userId));

        Plan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("id:" + subscription.getPlanId()));

        List<InvoiceLineItem> lineItems = List.of(
                InvoiceLineItem.of(plan.getName() + " — " +
                        subscription.getCurrentPeriod().startAt() + " to " +
                        subscription.getCurrentPeriod().endAt(),
                        plan.getPrice())
        );

        Invoice invoice = Invoice.generate(UUID.randomUUID(), userId, subscription.getId(),
                subscription.getCurrentPeriod(), lineItems, plan.getPrice().currency());

        if (!plan.getPrice().isZero() && subscription.getExternalId() != null) {
            PaymentGatewayPort.GatewayInvoice gateway = paymentGateway.createInvoice(
                    userId, subscription.getExternalId(), plan.getPrice(),
                    "Subscription fee — " + plan.getName());
            invoice.issue(gateway.externalId());
        }

        Invoice saved = invoiceRepository.save(invoice);
        invoice.getDomainEvents().forEach(eventPublisher::publishEvent);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto getById(UUID invoiceId, UUID userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
        if (!invoice.getUserId().equals(userId)) {
            throw new InvoiceNotFoundException(invoiceId);
        }
        return InvoiceDto.from(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDto> listByUser(UUID userId) {
        return invoiceRepository.findAllByUserId(userId).stream().map(InvoiceDto::from).toList();
    }
}
