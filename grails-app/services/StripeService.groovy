import com.stripe.Stripe
import com.stripe.model.Customer
import com.stripe.model.Plan
import com.stripe.model.Subscription
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONElement
import stripe.StripeNotification

class StripeService {
    def grailsApplication

    Plan getOrCreatePlan(String planId, String planInterval, Long amountPaidMonthly, int installment, String currencyType) {
        // planId is string that related to your plan or something like userid or loanid incase you have (It is necessary field)

        Plan plan = null
        planInterval = planInterval ?: "monthly"
        currencyType = currencyType ?: "gbp"
        try {
            Stripe.apiKey = stripeSecretKey
            Map<String, Object> planParams = new HashMap<String, Object>();
            planParams.put("amount", amountPaidMonthly);
            planParams.put("interval", planInterval);
            planParams.put("interval_count", installment)
            planParams.put("name", "Plan_${planId}");
            planParams.put("currency", currencyType);
            planParams.put("id", planId);

            plan = Plan.create(planParams);

        } catch (Exception e) {
        }
        return plan

    }

    Customer getOrCreateCustomer(String email, String stripeToken, String description) {
        Stripe.apiKey = stripeSecretKey
        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("description", description);
        customerParams.put("card", "${stripeToken}");
        customerParams.put("email", email);
        Customer stripeCustomer = Customer.create(customerParams);
        return stripeCustomer

    }

    Subscription createSubscription(Plan plan, Customer stripeCustomer) {
        Stripe.apiKey = stripeSecretKey

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("plan", "${plan?.id?.toString()}");
        Subscription subscription = stripeCustomer.updateSubscription(params)
        return subscription
    }

    String getStripeSecretKey() {
        String secretKey = "${grailsApplication.config.grails.plugins.stripe.secretKey}"
        return secretKey
    }

    def stripeNotification() {
        try {
            String key = "${grailsApplication.config.grails.plugins.stripe.secretKey}"

            URL url = new URL("https://api.stripe.com/v1/events?count=100")
            HttpURLConnection conn = (HttpURLConnection) url.openConnection()
            conn.setRequestMethod("GET")
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Authorization", "Bearer " + key)
            conn.setDoInput(true)
            conn.setDoOutput(true)
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.inputStream))
            String output = ""
            String inputLine;
            while ((inputLine = br.readLine()) != null)
                output += inputLine + "\n"
//            println("------------output-----------" + output)

            br.close();
            JSONElement json = JSON.parse(output)
            json.data.eachWithIndex { def dataJson, int i ->
                if (!StripeNotification.findByEventId(dataJson?.id)) {
                    StripeNotification notification = new StripeNotification()
                    notification.eventId = dataJson?.id
                    notification.created = dataJson?.created
                    notification.type = dataJson?.type
                    notification.customerId = getCustomerId(dataJson)
                    notification.planId = getPlanId(dataJson)
                    notification.subscriptionId = getSubscriptionId(dataJson)
                    notification.cardId = getCardId(dataJson)
                    notification.amount = dataJson.data?.object?.amount ?: ""
                    notification.jsonResult = dataJson
                    notification.save(flush: true)
                }
            }
        } catch (Exception e) {

        }
    }

    String getCustomerId(def dataJson) {
        String customerId = dataJson?.data?.object?.customer
        if (customerId && !customerId.startsWith("cus_")) {
            customerId = null
        }
        if (!customerId) {
            String result = dataJson?.data?.object?.id
            if (result && result?.startsWith("cus_")) {
                customerId = result
            }
        }
        return customerId
    }

    String getPlanId(def dataJson) {
        String planId = dataJson?.data?.object?.plan?.id
        if (planId && !planId.startsWith("Plan_")) {
            planId = null
        }
        if (!planId) {
            planId = dataJson?.data?.object?.lines?.data?.plan?.id?.join(" ")
            if (planId && !planId.startsWith("Plan_")) {
                planId = null
            }
        }
        if (!planId) {
            String out = dataJson?.data?.object?.id
            if (out && out.startsWith("Plan_")) {
                planId = out
            }
        }
        return planId
    }

    String getSubscriptionId(def dataJson) {
        String subscriptionId = dataJson?.data?.object?.lines?.data?.first()?.id
        if (!subscriptionId?.startsWith("sub_")) {
            subscriptionId = null
        }
        if (!subscriptionId) {
            subscriptionId = dataJson?.data?.object?.id
            if (!subscriptionId?.startsWith("sub_")) {
                subscriptionId = null
            }
        }
        return subscriptionId
    }

    String getCardId(def dataJson) {
        String cardId = dataJson?.data?.object?.card?.id
        if (cardId && !cardId.startsWith("card_")) {
            cardId = null
        }
        if (!cardId) {
            cardId = dataJson?.data?.object?.cards?.data?.first()?.id
            if (cardId && !cardId.startsWith("card_")) {
                cardId = null
            }
        }
        if (!cardId) {
            String out = dataJson?.data?.object?.id
            if (out && out?.startsWith("card_")) {
                cardId = out
            }
        }
        return cardId
    }

}
