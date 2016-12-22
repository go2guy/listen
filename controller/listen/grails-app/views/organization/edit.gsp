<html>
  <head>
    <title><g:message code="page.organization.edit.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="organization"/>
    <meta name="button" content="edit"/>
    <meta name="page-header" content="${g.message(code: 'page.organization.edit.header')}"/>

    <style type="text/css">
      .apikey-box { width: 275px; }
    </style>
  </head>
  <body>
    <g:form controller="organization" action="update" method="post">
      <fieldset class="vertical">
        <h3><g:message code="page.organization.edit.details.header"/></h3>
        <g:hiddenField name="id" value="${organization.id}"/>

        <label for="name"><g:message code="organization.name.label"/></label>
        <g:textField name="name" value="${fieldValue(bean: organization, field: 'name')}" maxlength="100" class="${listen.validationClass(bean: organization, field: 'name')}"/>

        <label for="contextPath"><g:message code="organization.contextPath.label"/></label>
        <g:textField name="contextPath" value="${fieldValue(bean: organization, field: 'contextPath')}" maxlength="50" class="${listen.validationClass(bean: organization, field: 'contextPath')}"/>

        <label for="outboundCallid"><g:message code="organization.outboundCallid.label"/></label>
        <g:textField name="outboundCallid" value="${fieldValue(bean: organization, field: 'outboundCallid')}" maxlength="10" class="${listen.validationClass(bean: organization, field: 'outboundCallid')}"/>
        
        <input type="checkbox" id="outboundCallidByDid" name="outboundCallidByDid" value="${true}" ${organization?.outboundCallidByDid ? "checked=checked" : ""}/>
        <g:message code="organization.outboundCallidByDid.label"/>

        <label for="extLength"><g:message code="organizationConfiguration.extLength.label"/></label>
        <g:textField name="extLength" value="${fieldValue(bean: organization, field: 'extLength')}" class="${listen.validationClass(bean: organization, field: 'extLength')}"/>

          <label for="route"><g:message code="organization.route.label"/></label>
          <g:textField name="route" value="${fieldValue(bean: organization, field: 'route')}" maxlength="100" class="${listen.validationClass(bean: organization, field: 'route')}"/>

          <label for="adServer">AD Server</label>
          <g:textField name="adServer" value="${fieldValue(bean: organization, field: 'adServer')}" class="${listen.validationClass(bean: organization, field: 'adServer')}"/>

          <label for="adDomain">AD Domain</label>
          <g:textField name="adDomain" value="${fieldValue(bean: organization, field: 'adDomain')}" class="${listen.validationClass(bean: organization, field: 'adDomain')}"/>

          <label for="ldapBasedn">LDAP Base Dn</label>
          <g:textField name="ldapBasedn" value="${fieldValue(bean: organization, field: 'ldapBasedn')}" class="${listen.validationClass(bean: organization, field: 'ldapBasedn')}"/>

          <label for="ldapPort">Ldap Port</label>
          <g:textField name="ldapPort" value="${fieldValue(bean: organization, field: 'ldapPort')}" class="${listen.validationClass(bean: organization, field: 'ldapPort')}"/>

          <label for="ldapDc">Ldap DC</label>
          <g:textField name="ldapDc" value="${fieldValue(bean: organization, field: 'ldapDc')}" class="${listen.validationClass(bean: organization, field: 'ldapDc')}"/>

          <h3><g:message code="page.organization.edit.features.header"/></h3>

        <g:each in="${enableableFeatures}" var="feature">
          <label for="enabledFeature-${feature}">
            <input type="checkbox" id="enabledFeature-${feature}" name="enabledFeature-${feature}" value="${feature}" ${organization?.enabledFeatures?.contains(feature) ? "checked=checked" : ""}/>
            ${feature.displayName}
          </label>
        </g:each>

          <h3><g:message code="organizationConfiguration.apiKey.label"/></h3>
          <g:textField name="apiKey" value="${fieldValue(bean: organization, field: 'apiKey')}" style="width: 300px;" disabled="disabled"/>

        <ul class="form-buttons">
          <li><g:submitButton name="submit" value="${g.message(code: 'default.button.save.label')}"/></li>
        </ul>
      </fieldset>
    </g:form>
    <script type="text/javascript">
$(document).ready(function() {
    $('#name').focus();
});
    </script>
  </body>
</html>