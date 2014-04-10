package stripe

class StripeNotification {

    String eventId
    String created
    String type
    String customerId
    String planId
    String subscriptionId
    String cardId
    String amount
    String jsonResult

    static constraints = {
        eventId(nullable: true)
        created(nullable: true)
        type(nullable: true)
        customerId(nullable: true)
        planId(nullable: true)
        subscriptionId(nullable: true)
        cardId(nullable: true)
        amount(nullable: true)
        jsonResult(nullable: true)
    }

    static mapping = {
        jsonResult type: "text"
    }
}
