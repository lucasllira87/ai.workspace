# ADR-026: Stripe como único gateway de pagamento na v1 via PaymentGatewayPort

**Status:** Accepted  
**Data:** 2026-07-15  
**Módulo:** billing

## Contexto

O módulo billing precisa processar pagamentos. Stripe é o gateway mais adotado em SaaS B2C/B2B. A questão é: acoplar diretamente ao SDK do Stripe ou abstrair via port?

## Decisão

`PaymentGatewayPort` é definido no application layer. `StripePaymentGatewayAdapter` (infrastructure) implementa a porta. O application layer nunca importa classes do SDK Stripe.

## Justificativa

- Testabilidade: `PaymentGatewayPort` pode ser mockado em testes unitários de `SubscriptionService` sem iniciar o Stripe
- Portabilidade: trocar para Paddle, Braintree ou PIX nativo requer apenas novo adapter
- Desenvolvimento: adapter retorna stubs durante desenvolvimento local (sem chave Stripe real)

## Consequências

- Webhook parsing (`parseAndVerify`) está na porta — o adapter implementa verificação de assinatura HMAC do Stripe
- Objetos Stripe (`Customer`, `Subscription`, `Invoice`) nunca vazam para o domain ou application layer
- O endpoint `/api/v1/billing/webhooks/stripe` é público (sem JWT) — autenticado pela assinatura Stripe no payload
