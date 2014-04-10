package stripe


import com.stripe.model.Customer
import com.stripe.model.Plan
import com.stripe.model.Subscription

//import org.hibernate.validator.util.privilegedactions.GetMethod;


class PaymentController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def grailsApplication
    def stripeService

    def installments() {
        String uniqueId = params.uniqueId
        Long amountUserPaying = 300
        Long id = 123 //this can be id related to payment
        String cardNumber = "424242424242" // for testing
        String securityCode = "222"
        Calendar calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 4)
        calendar.get(Calendar.MONTH)
        String expiryMM = calendar.get(Calendar.MONTH)
        String expiryYYYY = calendar.get(Calendar.YEAR)
        render(view: '/payment/stripePaymentButton', model: [expiryYYYY:expiryYYYY,expiryMM:expiryMM,securityCode: securityCode, id: id, amount: amountUserPaying, cardNumber: cardNumber])
    }

    def calculationForStripeSubscription() {
        Long id = params.id as Long // you can get the id that you pass from installments action

        String stripeToken = params?.stripeToken
        Long amount = 300
        Long amountPaidMonthly = 100
        Long monthlyInstallmentStripe = amountPaidMonthly * 100
        int installments = 3
        String planInterval = "monthly"
        String currencyType = "gbp"
        Plan plan = stripeService.getOrCreatePlan("1213", planInterval, amountPaidMonthly, installments, currencyType)

        String userEmail = "prabhat.kashyap.28@gmail.com"
        String customerDescription = "My name is prabhat kashyap"
        Customer stripeCustomer = stripeService.getOrCreateCustomer(userEmail, stripeToken, customerDescription)

        Subscription subscription = stripeService.createSubscription(plan, stripeCustomer)

        redirect(action: 'paymentSuccessful')
    }

    def paymentSuccessful() {


    }


    def stripeCallback = {

        render "${params}"

    }

}
