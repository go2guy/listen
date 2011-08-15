<html>
  <head>
    <meta name="layout" content="public"/>
    <title>Checkout</title>
    <script type="text/javascript" src="${resource(dir: 'resources/jquery', file: 'jquery-1.4.2.min.js')}"></script>
    <style type="text/css">
fieldset,
#cart-wrapper {
    display: inline-block;
    vertical-align: text-top;
}

fieldset {
    margin-right: 20px;
    width: 380px;
}

#cart-wrapper {
    width: 540px;
}

table {
    border-collapse: collapse;
}

td,
th {
    border: 1px solid #CCC;
    padding: 5px;
}

tfoot tr {
    background-color: #E4F0FB;
    font-weight: bold;
}

.col-item {
    border-right-style: dashed;
    width: 70%;
}

.col-buttons {
    border-left-style: dashed;
    border-right-width: 2px;
    text-align: center;
    width: 15%;
}

.col-cost {
    background-color: #F6F6F6;
    border-left-width: 2px;
    text-align: right;
    font-family: Lucida Console, Courier New, monospace;
    font-weight: normal;
    vertical-align: middle;
    width: 15%;
}

tfoot .col-cost {
    background-color: #E4F0FB;
}

.col-buttons button {
    font-size: 11px;
}

fieldset.aligned label,
fieldset.aligned input,
fieldset.aligned select {
    display: block;
    float: left;
    margin-bottom: 10px;
    margin-top: 10px;
}

fieldset.aligned label {
    clear: left;
    font-size: 14px;
    margin-right: 20px;
    margin-top: 13px;
    text-align: right;
    width: 120px;
}

fieldset.aligned input[type=text],
fieldset.aligned input[type=password],
fieldset.aligned select {
    width: 220px;
}

fieldset.aligned ul.buttons {
    background-color: #FFFFFF;
    border-color: #CCCCCC #E4F0FB #80B5ED #E4F0FB;
    border-style: solid;
    border-width: 1px 0 1px 0;
    clear: both;
    padding: 5px 0 5px 138px;
    margin-top: 15px;
}

fieldset.aligned ul.buttons li {
    display: inline-block;
    margin-right: 15px;
}

fieldset.aligned ul.buttons .primary-button {
    border-color: #090 #060 #060 #090;
    border-width: 2px;
    color: #437A3A;
    font-weight: bold;
    padding: 5px 20px;
}

fieldset.aligned ul.buttons .primary-button:hover {
    background-color: #D1FFC9;
    color: #060;
}

fieldset.aligned ul.buttons .cancel-button {
    border-width: 0;
    text-decoration: underline;
}

fieldset.aligned ul.buttons .cancel-button:hover {
    background-color: #A81818;
    color: #FFFFFF;
}

fieldset #accepted-cards {
    display: block;
    text-align: right;
}

fieldset #accepted-cards img {
    height: 28px;
    width: 120px;
}

#cardExpirationMonth {
    margin-right: 10px;
    width: 50px;
}

#cardExpirationYear {
    width: 80px;
}

#cardVerification {
    width: 80px;
}
    </style>
  </head>
  <body>

    <g:if test="${flash.successMessage}">
      <ul class="messages success"><li>${flash.successMessage}</li></ul>
    </g:if>
    <g:if test="${flash.errorMessage}">
      <ul class="messages error"><li>${flash.errorMessage}</li></ul>
    </g:if>
    <g:else>
      <g:hasErrors>
        <ul class="messages error">
          <g:eachError><li><g:message error="${it}"/></li></g:eachError>
        </ul>
      </g:hasErrors>
    </g:else>

    <fieldset class="aligned">
      <g:form controller="cart" method="post" class="payment-form">
        <h3>Payment Information</h3>

        <div id="accepted-cards">
          <img src="${resource(dir: 'resources/app/images', file: 'supported-cards.jpg')}" alt="We accept Visa, Mastercard, and American Express"/>
        </div>

        <label for="cardNumber">Card number</label>
        <g:textField name="cardNumber" value="${fieldValue(bean: payment, field: 'cardNumber')}" class="${listen.validationClass(bean: payment, field: 'cardNumber')}"/>

        <label for="cardholderName">Name on card</label>
        <g:textField name="cardholderName" value="${fieldValue(bean: payment, field: 'cardholderName')}" class="${listen.validationClass(bean: payment, field: 'cardholderName')}"/>

        <label for="cardExpirationMonth">Expires</label>
        <g:select name="cardExpirationMonth" from="${1..12}" value="${fieldValue(bean: payment, field: 'cardExpirationMonth')}" class="${listen.validationClass(bean: payment, field: 'cardExpirationMonth')}"/>
        <g:select name="cardExpirationYear" from="${2011..2020}" value="${fieldValue(bean: payemtn, field: 'cardExpirationYear')}" class="${listen.validationClass(bean: payment, field: 'cardExpirationYear')}"/>

        <label for="cardVerification">CVV</label>
        <g:textField name="cardVerification" value="${fieldValue(bean: payment, field: 'cardVerification')}" class="${listen.validationClass(bean: payment, field: 'cardVerification')}"/>

        <div style="clear: both;"></div>

        <ul class="buttons">
          <li><g:actionSubmit action="authorize" value="Authorize" class="primary-button"/></li>
          <li><g:actionSubmit action="cancelCheckout" value="Cancel" class="cancel-button"/></li>
        </ul>
      </g:form>
    </fieldset>

    <div id="cart-wrapper">
      <table>
        <caption>Shopping Cart</caption>
        <thead>
          <tr>
            <th class="col-item">Item</th>
            <th class="col-buttons"></th>
            <th class="col-cost"></th>
          </tr>
        </thead>
        <tfoot>
          <tr>
            <th class="col-item">Total</th>
            <th class="col-buttons"></th>
            <th class="col-cost">67.52</th>
          </tr>
        </tfoot>
        <tbody>
          <tr>
            <td class="col-item">Conference (20 callers, 60 minutes)</td>
            <td class="col-buttons">
              <button type="button">Remove</button>
            </td>
            <td class="col-cost">40.00</td>
          </tr>

          <tr>
            <td class="col-item">Invigor8! Rating Engine</td>
            <td class="col-buttons">
              <button type="button">Remove</button>
            </td>
            <td class="col-cost">17.52</td>
          </tr>
        </tbody>
      </table>
    </div>
    <script type="text/javascript">
$(document).ready(function() {
    $('.payment-form').submit(function() {
        $('.payment-form input[type=submit]').attr('readonly', 'readonly').addClass('disabled');
    });
});
    </script>
  </body>
</html>