<!doctype html>
<html>
<head>
    <script type="text/javascript" src="https://js.stripe.com/v2/"></script>
</head>

<body>
<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">
            <div class="span4">

                <h3>Checkout</h3>

                <stripe:script formName="payment-form"/>

                <g:form action="calculationForStripeSubscription"
                        method="POST"
                        controller="payment"
                        name="payment-form">
                    <span class="payment-errors alert-error"></span>
                    <g:hiddenField name="id" value="${id}"/>
                    <div class="form-row">
                        <label>Amount (GBP)</label>
                        <input type="text" size="20" autocomplete="off" id="amount" name="amount"
                               value="${amount}"/>
                    </div>

                    <div class="form-row">
                        <label>Card Number</label>
                        <input type="text" size="20" value="${cardNumber}" autocomplete="off"
                               data-stripe="number"/>
                    </div>

                    <div class="form-row">
                        <label>CVC</label>
                        <input type="text" size="4" value="${securityCode}" autocomplete="off"
                               data-stripe="cvc"/>
                    </div>

                    <div class="form-row">
                        <label>Expiration (MM/YYYY)</label>
                        <input type="text" size="2" data-stripe="exp-month"
                               value="${expiryMM}"
                               style="width: 5%"/>
                        <span>/</span>
                        <input type="text" size="4" data-stripe="exp-year"
                               value="${expiryYYYY}"
                               style="width: 10%"/>
                    </div>

                %{--<stripe:creditCardInputs cssClass="form-row"/>--}%

                    <button type="submit" class="submit-button btn btn-primary">Submit Payment</button>
                </g:form>

            </div>

        </div>
    </div>
</div>

<script type="text/javascript">
    Stripe.setPublishableKey('${grailsApplication.config.grails.plugins.stripe.publishableKey}');

    jQuery(function ($) {
        $('#payment-form').submit(function (event) {
            var $form = $(this);

            // Disable the submit button to prevent repeated clicks
            $form.find('button').prop('disabled', true);

            Stripe.card.createToken($form, stripeResponseHandler);

            // Prevent the form from submitting with the default action
            return false;
        });
    });

    var stripeResponseHandler = function (status, response) {
        var $form = $('#payment-form');

        if (response.error) {
            // Show the errors on the form
            $form.find('.payment-errors').text(response.error.message);
            $form.find('button').prop('disabled', false);
        } else {
            // token contains id, last4, and card type
            var token = response.id;
            // Insert the token into the form so it gets submitted to the server
            $form.append($('<input type="hidden" name="stripeToken" />').val(token));
            // and submit
            $form.get(0).submit();
        }
    };

</script>
</body>
</html>